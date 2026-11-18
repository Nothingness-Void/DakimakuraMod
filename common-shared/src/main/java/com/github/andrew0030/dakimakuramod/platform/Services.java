package com.github.andrew0030.dakimakuramod.platform;

import java.util.ServiceLoader;

public final class Services
{
    private static volatile IPlatformHelper platform;

    private Services() {}

    public static void register(IPlatformHelper helper)
    {
        if (helper != null)
            platform = helper;
    }

    public static IPlatformHelper getPlatform()
    {
        IPlatformHelper current = platform;
        if (current == null)
        {
            synchronized (Services.class)
            {
                current = platform;
                if (current == null)
                {
                    current = ServiceLoader.load(IPlatformHelper.class)
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException("Failed to load IPlatformHelper service"));
                    platform = current;
                }
            }
        }
        return current;
    }
}
