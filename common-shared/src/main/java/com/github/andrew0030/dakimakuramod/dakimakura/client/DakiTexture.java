package com.github.andrew0030.dakimakuramod.dakimakura.client;

import com.github.andrew0030.dakimakuramod.DakimakuraModClientCommon;
import com.github.andrew0030.dakimakuramod.dakimakura.Daki;
import com.github.andrew0030.dakimakuramod.dakimakura.DakiImageData;
import org.lwjgl.opengl.GL11;
import org.lwjgl.stb.STBImage;
import org.lwjgl.stb.STBImageResize;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class DakiTexture implements AutoCloseable
{
    private static final long UPLOAD_WINDOW_MS = 10L;
    private static final int MAX_UPLOADS_PER_WINDOW = 3;
    private static long uploadWindowStart = 0L;
    private static int uploadsInWindow = 0;

    private final Daki daki;
    private int id = 0;
    private volatile boolean requested = false;
    private boolean released = false;
    private int textureSize = 0;
    private ByteBuffer imageBuffer;

    public DakiTexture(Daki daki)
    {
        this.daki = daki;
    }

    /**
     * Decodes {@code imageData} into a combined off-heap RGBA buffer on a background worker.
     *
     * @return {@code true} if a new buffer was stored on this texture (occupying a ready-upload slot),
     *         {@code false} if decoding failed or the texture was already released.
     */
    public boolean decodeImageBuffer(DakiImageData imageData)
    {
        if (this.isReleased())
            return false;

        boolean isFrontMissing = imageData == null || imageData.getTextureFront() == null;
        boolean isBackMissing = imageData == null || imageData.getTextureBack() == null;
        byte[] imageBytesFront = isFrontMissing ? this.getMissingTextureBytes() : imageData.getTextureFront();
        byte[] imageBytesBack = isBackMissing ? this.getMissingTextureBytes() : imageData.getTextureBack();

        if (imageBytesFront == null || imageBytesBack == null)
            return false;

        int[] frontWidth = new int[1];
        int[] frontHeight = new int[1];
        int[] frontComp = new int[1];
        int[] backWidth = new int[1];
        int[] backHeight = new int[1];
        int[] backComp = new int[1];
        ByteBuffer encodedBufferFront = this.byteArrayToByteBuffer(imageBytesFront);
        ByteBuffer encodedBufferBack = this.byteArrayToByteBuffer(imageBytesBack);
        ByteBuffer imageBufferFront = null;
        ByteBuffer imageBufferBack = null;
        ByteBuffer combinedBuffer = null;
        try
        {
            try
            {
                imageBufferFront = STBImage.stbi_load_from_memory(encodedBufferFront, frontWidth, frontHeight, frontComp, 4);
                imageBufferBack = STBImage.stbi_load_from_memory(encodedBufferBack, backWidth, backHeight, backComp, 4);
            }
            finally
            {
                MemoryUtil.memFree(encodedBufferFront);
                MemoryUtil.memFree(encodedBufferBack);
            }
            if (imageBufferFront == null || imageBufferBack == null || this.isReleased())
                return false;

            int biggerTexture = Math.max(frontHeight[0], backHeight[0]);
            int maxTextureSize = this.getMaxTextureSize();
            int resolvedSize = Math.min(biggerTexture, maxTextureSize / 2);
            if (resolvedSize < 3) resolvedSize = 3;

            ByteBuffer resizedFront = this.resize(imageBufferFront, frontWidth[0], frontHeight[0], resolvedSize / 3, resolvedSize, !isFrontMissing && this.daki.isSmooth());
            imageBufferFront = resizedFront;
            ByteBuffer resizedBack = this.resize(imageBufferBack, backWidth[0], backHeight[0], resolvedSize / 3, resolvedSize, !isBackMissing && this.daki.isSmooth());
            imageBufferBack = resizedBack;

            combinedBuffer = this.combineImages(imageBufferFront, imageBufferBack, resolvedSize / 3, resolvedSize);
            imageBufferFront = null;
            imageBufferBack = null;

            boolean replacedExisting = false;
            boolean accepted = false;
            synchronized (this)
            {
                if (!this.released)
                {
                    if (this.imageBuffer != null)
                    {
                        MemoryUtil.memFree(this.imageBuffer);
                        replacedExisting = true;
                    }
                    this.textureSize = resolvedSize;
                    this.imageBuffer = combinedBuffer;
                    combinedBuffer = null;
                    accepted = true;
                }
            }
            if (replacedExisting)
            {
                DakiTextureManagerClient textureManager = DakimakuraModClientCommon.getDakiTextureManager();
                if (textureManager != null)
                    textureManager.onReadyBufferFreed();
            }
            return accepted;
        }
        finally
        {
            if (imageBufferFront != null)
                MemoryUtil.memFree(imageBufferFront);
            if (imageBufferBack != null)
                MemoryUtil.memFree(imageBufferBack);
            if (combinedBuffer != null)
                MemoryUtil.memFree(combinedBuffer);
        }
    }

    public boolean isLoaded()
    {
        if (this.id != 0) return true;
        if (this.isReleased()) return false;

        long now = System.currentTimeMillis();
        if (now - DakiTexture.uploadWindowStart >= UPLOAD_WINDOW_MS)
        {
            DakiTexture.uploadWindowStart = now;
            DakiTexture.uploadsInWindow = 0;
        }

        if (DakiTexture.uploadsInWindow < MAX_UPLOADS_PER_WINDOW && this.load())
        {
            DakiTexture.uploadsInWindow++;
            return true;
        }

        if (!this.requested)
        {
            this.requested = true;
            DakiTextureManagerClient textureManager = DakimakuraModClientCommon.getDakiTextureManager();
            if (textureManager != null && this.daki != null)
                textureManager.requestTexture(this.daki, this);
        }
        return false;
    }

    private boolean load()
    {
        ByteBuffer bufferToUpload;
        int uploadSize;
        synchronized (this)
        {
            if (this.released || this.imageBuffer == null)
                return false;
            bufferToUpload = this.imageBuffer;
            uploadSize = this.textureSize;
            this.imageBuffer = null;
        }

        if (this.id != 0)
        {
            GL11.glDeleteTextures(this.id);
            this.id = 0;
        }
        try
        {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.getId());
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, uploadSize / 3, uploadSize * 2, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, bufferToUpload);
        }
        finally
        {
            MemoryUtil.memFree(bufferToUpload);
            DakiTextureManagerClient textureManager = DakimakuraModClientCommon.getDakiTextureManager();
            if (textureManager != null)
                textureManager.onReadyBufferFreed();
        }
        return true;
    }

    public synchronized boolean isReleased()
    {
        return this.released;
    }

    public void resetRequest()
    {
        this.requested = false;
    }

    private ByteBuffer byteArrayToByteBuffer(byte[] imageData)
    {
        ByteBuffer buffer = MemoryUtil.memAlloc(imageData.length);
        buffer.put(imageData);
        buffer.flip();
        return buffer;
    }

    private ByteBuffer resize(ByteBuffer imgBuffer, int oldWidth, int oldHeight, int newWidth, int newHeight, boolean isSmooth)
    {
        if (oldWidth == newWidth && oldHeight == newHeight) return imgBuffer;

        long resizedBytes = (long) newWidth * (long) newHeight * 4L;
        if (resizedBytes <= 0 || resizedBytes > Integer.MAX_VALUE)
            throw new IllegalStateException("Dakimakura resize target too large: " + resizedBytes + " bytes");
        ByteBuffer resizedBuffer = MemoryUtil.memAlloc((int) resizedBytes);
        if (isSmooth) {
            STBImageResize.stbir_resize_uint8(imgBuffer, oldWidth, oldHeight, 0, resizedBuffer, newWidth, newHeight, 0, 4);
        } else {
            this.scaleNearestNeighborRGBA(imgBuffer, oldWidth, oldHeight, resizedBuffer, newWidth, newHeight);
        }
        MemoryUtil.memFree(imgBuffer);
        return resizedBuffer;
    }

    private void scaleNearestNeighborRGBA(ByteBuffer srcBuffer, int srcWidth, int srcHeight, ByteBuffer destBuffer, int destWidth, int destHeight)
    {
        float scaleX = (float) srcWidth / destWidth;
        float scaleY = (float) srcHeight / destHeight;

        for (int y = 0; y < destHeight; y++)
        {
            int srcY = (int) (y * scaleY);
            int srcRowOffset = srcY * srcWidth;
            int destRowOffset = y * destWidth;

            for (int x = 0; x < destWidth; x++)
            {
                int srcX = (int) (x * scaleX);
                int srcPosition = (srcRowOffset + srcX) * 4;
                int destPosition = (destRowOffset + x) * 4;

                destBuffer.putInt(destPosition, srcBuffer.getInt(srcPosition));
            }
        }
    }

    private ByteBuffer combineImages(ByteBuffer imageBufferFront, ByteBuffer imageBufferBack, int imagesWidth, int imagesHeight)
    {
        long combinedBytes = (long) imagesWidth * (long) imagesHeight * 2L * 4L;
        if (combinedBytes <= 0 || combinedBytes > Integer.MAX_VALUE)
            throw new IllegalStateException("Dakimakura combined texture too large: " + combinedBytes + " bytes");
        ByteBuffer combinedBuffer = MemoryUtil.memAlloc((int) combinedBytes);

        imageBufferFront.rewind();
        combinedBuffer.put(imageBufferFront);
        imageBufferBack.rewind();
        combinedBuffer.position(imageBufferFront.capacity());
        combinedBuffer.put(imageBufferBack);
        combinedBuffer.rewind();

        MemoryUtil.memFree(imageBufferFront);
        MemoryUtil.memFree(imageBufferBack);

        return combinedBuffer;
    }

    private int getMaxTextureSize()
    {
        return DakimakuraModClientCommon.getMaxGpuTextureSize();
    }

    private byte[] getMissingTextureBytes()
    {
        try (InputStream inputStream = DakiTexture.class.getClassLoader().getResourceAsStream("assets/dakimakuramod/textures/obj/missing.png")) {
            return inputStream != null ? inputStream.readAllBytes() : null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getId()
    {
        if (this.id == 0)
            this.id = GL11.glGenTextures();
        return this.id;
    }

    public void releaseId()
    {
        if (this.id != 0)
        {
            GL11.glDeleteTextures(this.id);
            this.id = 0;
        }
        ByteBuffer bufferToFree;
        synchronized (this)
        {
            this.released = true;
            bufferToFree = this.imageBuffer;
            this.imageBuffer = null;
        }
        if (bufferToFree != null)
        {
            MemoryUtil.memFree(bufferToFree);
            DakiTextureManagerClient textureManager = DakimakuraModClientCommon.getDakiTextureManager();
            if (textureManager != null)
                textureManager.onReadyBufferFreed();
        }
    }

    @Override
    public void close()
    {
        this.releaseId();
    }
}
