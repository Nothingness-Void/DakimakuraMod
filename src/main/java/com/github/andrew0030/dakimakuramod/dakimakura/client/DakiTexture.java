package com.github.andrew0030.dakimakuramod.dakimakura.client;

import com.github.andrew0030.dakimakuramod.DakimakuraModClient;
import com.github.andrew0030.dakimakuramod.dakimakura.Daki;
import com.github.andrew0030.dakimakuramod.dakimakura.DakiImageData;
import com.github.andrew0030.dakimakuramod.netwok.NetworkUtil;
import org.lwjgl.opengl.GL11;
import org.lwjgl.stb.STBImage;
import org.lwjgl.stb.STBImageResize;
import org.lwjgl.system.MemoryUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DakiTexture implements AutoCloseable
{
    private final Daki daki;
    private int id = 0;
    private static long lastLoad;
    private boolean requested = false;
    private int textureSize = 0;
    private ByteBuffer imageBuffer;
    private ExecutorService executorService;

    public DakiTexture(Daki daki)
    {
        this.daki = daki;
    }

    public void createImageBuffer(DakiImageData imageData)
    {
        boolean isFrontMissing = imageData.getTextureFront() == null;
        boolean isBackMissing = imageData.getTextureBack() == null;
        byte[] imageBytesFront = isFrontMissing ? this.getMissingTextureBytes() : imageData.getTextureFront();
        byte[] imageBytesBack = isBackMissing ? this.getMissingTextureBytes() : imageData.getTextureBack();

        if (imageBytesFront == null || imageBytesBack == null)
            return;

        try {
            this.executorService = Executors.newSingleThreadExecutor();
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                // Load images using STBImage
                int[] frontWidth = new int[1];
                int[] frontHeight = new int[1];
                int[] frontComp = new int[1];
                int[] backWidth = new int[1];
                int[] backHeight = new int[1];
                int[] backComp = new int[1];
                ByteBuffer encodedBufferFront = this.byteArrayToByteBuffer(imageBytesFront);
                ByteBuffer encodedBufferBack = this.byteArrayToByteBuffer(imageBytesBack);
                ByteBuffer imageBufferFront;
                ByteBuffer imageBufferBack;
                try {
                    imageBufferFront = STBImage.stbi_load_from_memory(encodedBufferFront, frontWidth, frontHeight, frontComp, 4);
                    imageBufferBack = STBImage.stbi_load_from_memory(encodedBufferBack, backWidth, backHeight, backComp, 4);
                } finally {
                    MemoryUtil.memFree(encodedBufferFront);
                    MemoryUtil.memFree(encodedBufferBack);
                }
                if (imageBufferFront == null || imageBufferBack == null)
                {
                    if (imageBufferFront != null)
                        MemoryUtil.memFree(imageBufferFront);
                    if (imageBufferBack != null)
                        MemoryUtil.memFree(imageBufferBack);
                    return;
                }

                // We determine the bigger size to use, and make sure the size is within max size.
                // NOTE: front and back are stacked vertically during upload, so the uploaded
                // height is textureSize * 2. We must keep both the uploaded width (textureSize / 3)
                // and height (textureSize * 2) within GL_MAX_TEXTURE_SIZE, otherwise some GL
                // drivers (notably NVIDIA's nvoglv64.dll) crash inside glTexImage2D while
                // reading the user buffer (access violation).
                int biggerTexture = Math.max(frontHeight[0], backHeight[0]);
                int maxTextureSize = this.getMaxTextureSize();
                // Cap so uploaded dimensions never exceed GL_MAX_TEXTURE_SIZE.
                // Height constraint: textureSize * 2 <= maxTextureSize  -> textureSize <= maxTextureSize / 2
                int textureSize = Math.min(biggerTexture, maxTextureSize / 2);
                // Defensive floor: need at least 3 so textureSize / 3 >= 1.
                if (textureSize < 3) textureSize = 3;

                // Resize images if needed
                imageBufferFront = this.resize(imageBufferFront, frontWidth[0], frontHeight[0], textureSize / 3, textureSize, !isFrontMissing && this.daki.isSmooth());
                imageBufferBack = this.resize(imageBufferBack, backWidth[0], backHeight[0], textureSize / 3, textureSize, !isBackMissing && this.daki.isSmooth());

                // Stores the combines front and back images into one buffer
                ByteBuffer combinedBuffer = this.combineImages(imageBufferFront, imageBufferBack, textureSize / 3, textureSize);

                synchronized (this)
                {
                    this.textureSize = textureSize;
                    this.imageBuffer = combinedBuffer;
                }
                synchronized (imageData)
                {
                    imageData.clearTextureData();
                }
            }, this.executorService);

            // handles thread shutdown on completion
            future.thenRun(this.executorService::shutdown);
            // handles thread shutdown on exception
            future.exceptionally(ex -> {
                System.err.println("Exception occurred: " + ex.getMessage());
                this.executorService.shutdown();
                return null;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isLoaded()
    {
        // If id isn't 0 it means the texture has been loaded
        if (this.id != 0) return true;

        // Checks if enough time has passed since the last load attempt
        if (DakiTexture.lastLoad + 25 >= System.currentTimeMillis()) return false;

        // Attempts to load the texture
        if (this.load())
        {
            DakiTexture.lastLoad = System.currentTimeMillis();
            return false;
        }

        // If load failed because image buffer is null, and texture has not been requested, we request it
        if (!this.requested)
        {
            DakiImageData cachedImageData = DakiTextureDiskCache.load(this.daki);
            if (cachedImageData != null)
            {
                this.requested = true;
                this.createImageBuffer(cachedImageData);
                return false;
            }

            DakiTextureManagerClient textureManager = DakimakuraModClient.getDakiTextureManager();
            int requests = textureManager.getTextureRequests().get();

            // If there are less than 2 requests, we increment and request the texture
            if (requests < 2)
            {
                textureManager.getTextureRequests().incrementAndGet();
                if (this.daki != null)
                {
                    this.requested = true;
                    NetworkUtil.clientRequestTextures(daki);
                }
            }
        }
        return false;
    }

    private boolean load()
    {
        // If the ImageBuffer is null we can't load the image
        if (this.imageBuffer == null) return false;
        this.releaseId(); // We remove former textures if there are any and close any still existing threads
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.getId());
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        // TODO update the values bellow for width and height once images are no longer on top of each other
        // Note: both images are resized by this time, so its safe to assume they are the same size
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, this.textureSize / 3, this.textureSize * 2, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, this.imageBuffer);

        // Frees the memory associated with the Image Buffer
        MemoryUtil.memFree(this.imageBuffer);
        this.imageBuffer = null;
        return true;
    }

    /**
     * Converts the given byte array to a {@link ByteBuffer}
     * @param imageData The image bytes to be converted
     * @return A new {@link ByteBuffer} containing the image bytes
     */
    private ByteBuffer byteArrayToByteBuffer(byte[] imageData)
    {
        ByteBuffer buffer = MemoryUtil.memAlloc(imageData.length);
        buffer.put(imageData);
        buffer.flip();
        return buffer;
    }

    private ByteBuffer resize(ByteBuffer imgBuffer, int oldWidth, int oldHeight, int newWidth, int newHeight, boolean isSmooth)
    {
        // If no resizing is needed we return the given image buffer
        if (oldWidth == newWidth && oldHeight == newHeight) return imgBuffer;

        // Allocates memory for the resized image (guard against 32-bit overflow)
        long resizedBytes = (long) newWidth * (long) newHeight * 4L;
        if (resizedBytes <= 0 || resizedBytes > Integer.MAX_VALUE)
            throw new IllegalStateException("Dakimakura resize target too large: " + resizedBytes + " bytes");
        ByteBuffer resizedBuffer = MemoryUtil.memAlloc((int) resizedBytes); // 4 channels (RGBA)
        if (isSmooth) {
            STBImageResize.stbir_resize_uint8(imgBuffer, oldWidth, oldHeight, 0, resizedBuffer, newWidth, newHeight, 0, 4); // 4 channels (RGBA)
        } else {
            this.scaleNearestNeighborRGBA(imgBuffer, oldWidth, oldHeight, resizedBuffer, newWidth, newHeight);
        }
        // Frees the memory associated with the original input ByteBuffer
        MemoryUtil.memFree(imgBuffer);
        return resizedBuffer;
    }

    private void scaleNearestNeighborRGBA(ByteBuffer srcBuffer, int srcWidth, int srcHeight, ByteBuffer destBuffer, int destWidth, int destHeight)
    {
        float scaleX = (float) srcWidth / destWidth;
        float scaleY = (float) srcHeight / destHeight;
        byte[] pixel = new byte[4];

        for (int y = 0; y < destHeight; y++)
        {
            int srcY = (int) (y * scaleY);

            for (int x = 0; x < destWidth; x++)
            {
                int srcX = (int) (x * scaleX);
                int srcPosition = (srcY * srcWidth + srcX) * 4;
                int destPosition = (y * destWidth + x) * 4;

                srcBuffer.get(srcPosition, pixel, 0, 4);
                destBuffer.put(destPosition, pixel, 0, 4);
            }
        }
    }

    private ByteBuffer combineImages(ByteBuffer imageBufferFront, ByteBuffer imageBufferBack, int imagesWidth, int imagesHeight)
    {
        // Allocate memory for the combined image buffer.
        // Use long math to avoid 32-bit int overflow for very large textures
        // (e.g. 32768 * 32768 * 2 * 4 overflows int and would pass garbage to memAlloc).
        long combinedBytes = (long) imagesWidth * (long) imagesHeight * 2L * 4L;
        if (combinedBytes <= 0 || combinedBytes > Integer.MAX_VALUE)
            throw new IllegalStateException("Dakimakura combined texture too large: " + combinedBytes + " bytes");
        ByteBuffer combinedBuffer = MemoryUtil.memAlloc((int) combinedBytes); // 4 channels (RGBA)

        imageBufferFront.rewind(); // Resets position to start
        combinedBuffer.put(imageBufferFront);
        imageBufferBack.rewind(); // Resets position to start
        combinedBuffer.position(imageBufferFront.capacity()); // Start writing at the middle of the buffer
        combinedBuffer.put(imageBufferBack);
        combinedBuffer.rewind(); // Reset position to start of the combined buffer

        // Frees the memory associated with the original input ByteBuffers
        MemoryUtil.memFree(imageBufferFront);
        MemoryUtil.memFree(imageBufferBack);

        return combinedBuffer;
    }

    /** @return The max texture size an image can be in pixels */
    private int getMaxTextureSize()
    {
        int maxGpuSize = DakimakuraModClient.getMaxGpuTextureSize();
//        int maxConfigSize = ConfigHandler.textureMaxSize;
//        return Math.min(maxGpuSize, maxConfigSize);
        return maxGpuSize; // TODO: add config support for max image size
    }

    /** @return A byte array representing a missing texture */
    private byte[] getMissingTextureBytes()
    {
        try (InputStream inputStream = DakiTexture.class.getClassLoader().getResourceAsStream("assets/dakimakuramod/textures/obj/missing.png")) {
            return inputStream != null ? inputStream.readAllBytes() : null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Gets or creates a new id referencing to the texture location on the GPU. */
    public int getId()
    {
        if (this.id == 0)
            this.id = GL11.glGenTextures();
        return this.id;
    }

    /** Deletes textures associated with the id of this {@link DakiTexture} object from the GPU and resets the id. */
    public void releaseId()
    {
        if (this.id != 0)
        {
            GL11.glDeleteTextures(this.id);
            this.id = 0;
        }
        if (this.executorService != null)
            this.executorService.shutdownNow();
    }

    @Override
    public void close()
    {
        this.releaseId();
    }
}
