package com.github.andrew0030.dakimakuramod.dakimakura.client;

import com.github.andrew0030.dakimakuramod.DakimakuraModClientCommon;
import com.github.andrew0030.dakimakuramod.DakimakuraModCommon;
import com.github.andrew0030.dakimakuramod.dakimakura.BatchPackListHelper;
import com.github.andrew0030.dakimakuramod.dakimakura.Daki;
import com.github.andrew0030.dakimakuramod.dakimakura.DakiManager;
import com.github.andrew0030.dakimakuramod.dakimakura.pack.DakiPackFolder;
import com.github.andrew0030.dakimakuramod.dakimakura.pack.IDakiPack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Client-only pack-list batch staging and merging handler.
 */
public final class ClientPackListReceiver
{
    private static final Map<String, IDakiPack> STAGING_PACKS = new LinkedHashMap<>();

    private ClientPackListReceiver() {}

    public static void handleClientBatch(boolean firstBatch, boolean lastBatch, List<IDakiPack> packs)
    {
        if (firstBatch)
            STAGING_PACKS.clear();

        if (packs != null)
        {
            for (IDakiPack pack : packs)
            {
                boolean isFolder = pack instanceof DakiPackFolder;
                String key = (isFolder ? "F:" : "Z:") + pack.getResourceName();
                IDakiPack merged = STAGING_PACKS.computeIfAbsent(key, k -> BatchPackListHelper.createEmptyPack(isFolder, pack.getResourceName()));
                for (Daki daki : pack.getDakisInPack())
                    merged.addDaki(daki);
            }
        }

        if (lastBatch)
        {
            ArrayList<IDakiPack> completePacks = new ArrayList<>(STAGING_PACKS.values());
            STAGING_PACKS.clear();
            DakiManager manager = DakimakuraModCommon.getDakimakuraManager();
            if (manager != null)
                manager.setDakiList(completePacks);
            DakiTextureManagerClient clientTextures = DakimakuraModClientCommon.getDakiTextureManager();
            if (clientTextures != null)
                clientTextures.reloadTextures();
        }
    }

    public static void clearStaging()
    {
        STAGING_PACKS.clear();
    }
}
