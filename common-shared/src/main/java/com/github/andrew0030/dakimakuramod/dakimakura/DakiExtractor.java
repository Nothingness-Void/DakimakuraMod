package com.github.andrew0030.dakimakuramod.dakimakura;

import com.github.andrew0030.dakimakuramod.DakimakuraModCommon;
import com.mojang.logging.LogUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import java.io.*;

public final class DakiExtractor
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String DAKI_ASSETS_LOCATION = "assets/" + DakimakuraModCommon.MODID + "/dakis/";
    private static final String[] README_LIST = new String[] {"en_GB", "de_DE"};
    private static final String DEFAULT_DAKI_PACK = "Vanilla Mobs";
    private static final String[] DEFAULT_DAKI_LIST = new String[] {"Allay", "Axolotl", "Camel", "Horse"};

    private DakiExtractor() {}

    /** Creates the daki folder in the game directory, and then extracts the default dakis and README's into it */
    public static void extractDakis()
    {
        File packFolder = DakimakuraModCommon.getDakimakuraManager().getPackFolder();

        if (!packFolder.exists())
            packFolder.mkdirs();

        File oldReadme = new File(packFolder, "readme.txt");
        if (oldReadme.exists()) { try { oldReadme.delete(); } catch (Exception ignored) {} }

        DakiExtractor.extractReadmeFiles(packFolder);
        DakiExtractor.extractDakiPack(packFolder, DEFAULT_DAKI_PACK, DEFAULT_DAKI_LIST);
    }

    private static void extractReadmeFiles(File packFolder)
    {
        LOGGER.info("Attempting READ_ME Extraction");
        for (int i = 0; i < README_LIST.length; i++)
        {
            String readmeName = "readme_" + README_LIST[i] + ".txt";
            DakiExtractor.extractResource(readmeName, new File(packFolder, readmeName), true, i == README_LIST.length - 1);
        }
    }

    private static void extractDakiPack(File packFolder, String packName, String[] dakiNames)
    {
        packFolder = new File(packFolder, packName);
        if (!packFolder.exists() && !packFolder.mkdir()) {
            LOGGER.error("Failed to make default pack folder for [" + packName + "]");
            return;
        }
        LOGGER.info("Attempting pack-info Extraction");
        DakiExtractor.extractResource(packName + "/pack-info.json", new File(packFolder, "pack-info.json"), true, true);
        for (String dakiName : dakiNames)
            DakiExtractor.extractDakiFiles(packFolder, packName, dakiName, true, "daki-info.json", "front.png", "back.png");
    }

    private static void extractDakiFiles(File packFolder, String packName, String name, boolean overwrite, String... files)
    {
        File dakiFolder = new File(packFolder, name);
        if (!dakiFolder.exists())
            dakiFolder.mkdir();
        LOGGER.info(String.format("Attempting File Extraction for: '%s'", name));
        for (int i = 0; i < files.length; i++)
            DakiExtractor.extractResource(packName + "/" + name + "/" + files[i], new File(dakiFolder, files[i]), overwrite, (i == files.length - 1));
    }

    private static void extractResource(String source, File target, boolean overwrite, boolean isLast)
    {
        String prefix = isLast ? " \\ " : " | ";
        if (!overwrite && target.exists())
            return;
        try (InputStream input = DakiExtractor.class.getClassLoader().getResourceAsStream(DAKI_ASSETS_LOCATION + source);
             OutputStream output = new BufferedOutputStream(new FileOutputStream(target))) {
            if (input != null)
            {
                LOGGER.info(String.format(prefix + "Extracting: '%s' -> '%s'", source, target.getAbsolutePath()));
                IOUtils.copy(input, output);
                output.flush();
            }
            else
            {
                LOGGER.error(String.format(prefix + "Error extracting '%s'", source));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
