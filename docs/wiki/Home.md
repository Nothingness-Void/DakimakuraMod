# Dakimakura Mod Wiki

_[中文版本](./Home-zh.md)_

Welcome to the **Dakimakura Mod (Nothingness-Void Fork)** Wiki! This mod brings customizable, two-sided dakimakura (body pillows) into Minecraft with support for custom image packs, bed placement, server-side image optimization, and automatic multiplayer pack synchronization.

---

## Supported Versions & Loaders

| Minecraft | Mod Loader | Minimum / Target | Required Dependencies |
|---|---|---|---|
| **1.20.1** | **Forge** | `47.1.47+` | None |
| **1.20.1** | **Fabric** | Fabric Loader | **Fabric API** (required) |
| **1.21.1** | **NeoForge** | `21.1.226+` | None |
| **1.21.1** | **Fabric** | Fabric Loader | **Fabric API** (required) |
| **1.21.1** | **Forge** | `52.1.14+` | None |

> [!IMPORTANT]
> Both client and server must install the matching Minecraft version and mod loader JAR (plus **Fabric API** on Fabric). Custom dakimakura packs only need to be installed on the **server** (or on your **client** in singleplayer).

---

## 5-Minute Quickstart

1. **Install the mod**: Drop the matching `dakimakuramod-*.jar` into your `mods/` folder (and Fabric API if using Fabric).
2. **Launch the game**: On first launch, the mod creates `<gameDir>/dakimakura-mod/` and extracts the built-in **Vanilla Mobs** pack.
3. **Craft & unlock**:
   - Craft a **Blank Dakimakura** using `2 × 3` White Wool (`WWW` / `WWW`).
   - Hunt hostile mobs for a **Dakimakura Design** (5% drop on player kill) and **Right-Click** in the air while holding it to unlock a random design—or grab any design directly from the **Dakimakura Mod** Creative tab.
   - Combine the **Blank Dakimakura** + **Unlocked Design** in any `2 × 2` or `3 × 3` crafting grid.
4. **Place & flip**:
   - Right-click the floor, a wall, a ceiling, or directly on a **Bed**.
   - **Sneak (Shift) + Right-Click** (in hand or placed) to flip between front and back artwork.
5. **Add custom art**: Run `/dakimakura openPackFolder`, drop in your own folder or `.zip` pack, and run `/dakimakura reload`!

---

## Wiki Index

- **[Gameplay & Controls](./Gameplay.md)** — Installation, crafting & washing recipes, world & bed placement controls, Creative tab, and commands.
- **[Custom Packs Guide](./Custom-Packs.md)** — Folder and `.zip` pack layouts, image requirements, complete `daki-info.json` reference, and hot reloading.
- **[Multiplayer & FAQ](./Multiplayer-And-FAQ.md)** — Dedicated server setup, optimization/cache/transfer pipeline diagram, tuning presets, and troubleshooting.
- **[Configuration Reference](./Configuration.md)** — Full reference for `dakimakuramod-common.toml` and `dakimakuramod-client.toml`.
