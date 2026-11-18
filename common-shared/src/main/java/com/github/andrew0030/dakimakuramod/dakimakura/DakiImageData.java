package com.github.andrew0030.dakimakuramod.dakimakura;

import com.github.andrew0030.dakimakuramod.DakimakuraModCommon;
import com.github.andrew0030.dakimakuramod.dakimakura.pack.IDakiPack;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

/** Used to handle loading the textures of a given {@link Daki} and storing them as byte arrays */
public class DakiImageData
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String[] VALID_FILE_EXT = {"png", "jpg", "jpeg"};
    private static final String DEFAULT_NAME_FRONT = "front";
    private static final String DEFAULT_NAME_BACK = "back";
    private final Daki daki;
    private byte[] textureFront;
    private byte[] textureBack;

    public DakiImageData(Daki daki)
    {
        this.daki = daki;
        this.load();
    }

    public DakiImageData(Daki daki, byte[] textureFront, byte[] textureBack)
    {
        this.daki = daki;
        this.textureFront = textureFront;
        this.textureBack = textureBack;
    }

    public Daki getDaki()
    {
        return this.daki;
    }

    /** @return The Front Texture byte array */
    public byte[] getTextureFront()
    {
        return this.textureFront;
    }

    /** @return The Back Texture byte array */
    public byte[] getTextureBack()
    {
        return this.textureBack;
    }

    public void clearTextureData()
    {
        this.textureFront = null;
        this.textureBack = null;
    }

    /** Sets the texture byte arrays based on the Images of the {@link Daki}, which was passed to the {@link DakiImageData} object during initialization. */
    public void load()
    {
        byte[][] cached = DakiImageServerCache.load(this.daki);
        if (cached != null)
        {
            this.textureFront = cached[0];
            this.textureBack = cached[1];
            return;
        }

        IDakiPack dakiPack = DakimakuraModCommon.getDakimakuraManager().getDakiPack(this.daki.getPackDirectoryName());
        if (dakiPack == null)
            return;

        String pathFront = this.findImagePath(dakiPack, this.daki.getDakiDirectoryName(), this.daki.getImageFront(), DEFAULT_NAME_FRONT);
        String pathBack = this.findImagePath(dakiPack, this.daki.getDakiDirectoryName(), this.daki.getImageBack(), DEFAULT_NAME_BACK);
        this.textureFront = this.loadOptimizedResource(dakiPack, pathFront);
        this.textureBack = pathFront != null && pathFront.equals(pathBack)
                ? this.textureFront
                : this.loadOptimizedResource(dakiPack, pathBack);

        DakiImageServerCache.save(this.daki, this.textureFront, this.textureBack);
    }

    private byte[] loadOptimizedResource(IDakiPack dakiPack, String path)
    {
        if (!DakiImageOptimizer.isSourceSizeAllowed(dakiPack.getResourceSize(path), path))
            return null;
        byte[] imageBytes = dakiPack.getResource(path);
        return DakiImageOptimizer.optimizeForTransfer(this.daki, imageBytes, path);
    }

    private String findImagePath(IDakiPack dakiPack, String dakiDirectoryName, String dakiImage, String defaultImageName)
    {
        if (dakiImage != null)
            return dakiDirectoryName + "/" + dakiImage;
        for (String ext : VALID_FILE_EXT)
        {
            String imagePath = dakiDirectoryName + "/" + defaultImageName + "." + ext;
            if (dakiPack.resourceExists(imagePath))
                return imagePath;
        }
        LOGGER.error(String.format("Failed finding any valid image for Daki: %s/%s", dakiDirectoryName, defaultImageName));
        return null;
    }
}
