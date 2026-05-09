package com.github.andrew0030.dakimakuramod.dakimakura;

import com.github.andrew0030.dakimakuramod.config.DMConfig;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

public final class DakiImageOptimizer
{
    private static final Logger LOGGER = LogUtils.getLogger();

    /** Hard ceiling used by {@link DakiTextureHasher} so cache keys stay stable across config edits. */
    public static final long MAX_SOURCE_BYTES_CEILING = 1024L * 1024L * 1024L;
    /** Hard ceiling used by {@link DakiTextureHasher} so cache keys stay stable across config edits. */
    public static final int MAX_TRANSFER_HEIGHT_CEILING = 8192;

    private static final long MAX_SOURCE_PIXELS = 256L * 1024L * 1024L;
    private static final int MAX_SOURCE_DIMENSION = 32768;
    private static final int REENCODE_THRESHOLD_BYTES = 1024 * 1024;

    /** Fallbacks if the config is not yet loaded (e.g. during early mod init). */
    private static final int FALLBACK_MAX_TRANSFER_HEIGHT = 1536;
    private static final long FALLBACK_MAX_SOURCE_BYTES = 32L * 1024L * 1024L;
    private static final float FALLBACK_JPEG_QUALITY = 0.80F;

    private DakiImageOptimizer() {}

    public static int getMaxTransferHeight()
    {
        if (DMConfig.COMMON_SPEC.isLoaded())
            return DMConfig.COMMON.maxTransferHeight.get();
        return FALLBACK_MAX_TRANSFER_HEIGHT;
    }

    public static long getMaxSourceBytes()
    {
        if (DMConfig.COMMON_SPEC.isLoaded())
            return (long) DMConfig.COMMON.maxSourceMegabytes.get() * 1024L * 1024L;
        return FALLBACK_MAX_SOURCE_BYTES;
    }

    public static float getJpegQuality()
    {
        if (DMConfig.COMMON_SPEC.isLoaded())
            return (float) (double) DMConfig.COMMON.jpegQuality.get();
        return FALLBACK_JPEG_QUALITY;
    }

    public static byte[] optimizeForTransfer(Daki daki, byte[] imageBytes, String imagePath)
    {
        if (imageBytes == null || imageBytes.length == 0)
            return imageBytes;
        if (!isSourceSizeAllowed(imageBytes.length, imagePath))
            return null;

        int maxTransferHeight = getMaxTransferHeight();

        try
        {
            BufferedImage source = readSafely(imageBytes, imagePath, maxTransferHeight);
            if (source == null)
                return imageBytes;

            int targetHeight = Math.min(source.getHeight(), maxTransferHeight);
            int targetWidth = Math.max(1, targetHeight / 3);
            boolean resize = source.getWidth() != targetWidth || source.getHeight() != targetHeight;
            boolean hasAlpha = source.getColorModel().hasAlpha();
            boolean useJpeg = !hasAlpha && daki.isSmooth() && (resize || imageBytes.length > REENCODE_THRESHOLD_BYTES);

            if (!resize && !useJpeg)
                return imageBytes;

            BufferedImage output = resize
                    ? resize(source, targetWidth, targetHeight, daki.isSmooth(), !useJpeg && hasAlpha)
                    : convertImage(source, !useJpeg && hasAlpha);
            byte[] optimizedBytes = encode(output, useJpeg);

            if (optimizedBytes.length >= imageBytes.length)
                return imageBytes;

            LOGGER.info(String.format("Optimized Dakimakura image '%s': %d bytes -> %d bytes", imagePath, imageBytes.length, optimizedBytes.length));
            return optimizedBytes;
        }
        catch (IOException e)
        {
            LOGGER.warn(String.format("Failed optimizing Dakimakura image '%s', using missing texture instead.", imagePath), e);
            return null;
        }
    }

    public static boolean isSourceSizeAllowed(long sourceSize, String imagePath)
    {
        if (sourceSize < 0L)
            return true;
        long maxSourceBytes = getMaxSourceBytes();
        if (sourceSize <= maxSourceBytes)
            return true;

        LOGGER.warn(String.format("Skipping Dakimakura image '%s': file is too large (%d bytes, max %d bytes).", imagePath, sourceSize, maxSourceBytes));
        return false;
    }

    private static BufferedImage readSafely(byte[] imageBytes, String imagePath, int maxTransferHeight) throws IOException
    {
        try (ImageInputStream inputStream = ImageIO.createImageInputStream(new ByteArrayInputStream(imageBytes)))
        {
            if (inputStream == null)
                return null;

            Iterator<ImageReader> readers = ImageIO.getImageReaders(inputStream);
            if (!readers.hasNext())
                return ImageIO.read(new ByteArrayInputStream(imageBytes));

            ImageReader reader = readers.next();
            try
            {
                reader.setInput(inputStream, true, true);
                int sourceWidth = reader.getWidth(0);
                int sourceHeight = reader.getHeight(0);
                validateDimensions(sourceWidth, sourceHeight, imagePath);

                ImageReadParam readParam = reader.getDefaultReadParam();
                int targetHeight = Math.min(sourceHeight, maxTransferHeight);
                int targetWidth = Math.max(1, targetHeight / 3);
                int subsampling = Math.max(1, Math.min(sourceWidth / targetWidth, sourceHeight / targetHeight));
                if (subsampling > 1)
                    readParam.setSourceSubsampling(subsampling, subsampling, 0, 0);
                return reader.read(0, readParam);
            }
            finally
            {
                reader.dispose();
            }
        }
    }

    private static void validateDimensions(int width, int height, String imagePath) throws IOException
    {
        long pixels = (long) width * (long) height;
        if (width <= 0 || height <= 0 || width > MAX_SOURCE_DIMENSION || height > MAX_SOURCE_DIMENSION || pixels > MAX_SOURCE_PIXELS)
            throw new IOException(String.format("Dakimakura image '%s' dimensions are too large: %dx%d", imagePath, width, height));
    }

    private static BufferedImage resize(BufferedImage source, int width, int height, boolean smooth, boolean preserveAlpha)
    {
        BufferedImage target = new BufferedImage(width, height, preserveAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = target.createGraphics();
        graphics.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                smooth ? RenderingHints.VALUE_INTERPOLATION_BICUBIC : RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        );
        graphics.drawImage(source, 0, 0, width, height, null);
        graphics.dispose();
        return target;
    }

    private static BufferedImage convertImage(BufferedImage source, boolean preserveAlpha)
    {
        int type = preserveAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
        if (source.getType() == type)
            return source;

        BufferedImage target = new BufferedImage(source.getWidth(), source.getHeight(), type);
        Graphics2D graphics = target.createGraphics();
        graphics.drawImage(source, 0, 0, null);
        graphics.dispose();
        return target;
    }

    private static byte[] encode(BufferedImage image, boolean useJpeg) throws IOException
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        if (!useJpeg)
        {
            ImageIO.write(image, "png", outputStream);
            return outputStream.toByteArray();
        }

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext())
        {
            ImageIO.write(image, "jpg", outputStream);
            return outputStream.toByteArray();
        }

        ImageWriter writer = writers.next();
        try (ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(outputStream))
        {
            ImageWriteParam writeParam = writer.getDefaultWriteParam();
            writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            writeParam.setCompressionQuality(getJpegQuality());
            writer.setOutput(imageOutputStream);
            writer.write(null, new IIOImage(image, null, null), writeParam);
        }
        finally
        {
            writer.dispose();
        }
        return outputStream.toByteArray();
    }
}
