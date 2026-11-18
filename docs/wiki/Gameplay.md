# Gameplay & Controls

[Back to Wiki Home](./Home.md) | _[中文版本](./Gameplay-zh.md)_

---

## Installation

Install the JAR matching your Minecraft version and mod loader into `<gameDir>/mods/`:

| Minecraft | Loader | Notes |
|---|---|---|
| `1.20.1` | Forge (`47.1.47+`) | Standalone JAR |
| `1.20.1` | Fabric | Requires **Fabric API** |
| `1.21.1` | NeoForge (`21.1.226+`) | Standalone JAR |
| `1.21.1` | Fabric | Requires **Fabric API** |
| `1.21.1` | Forge (`52.1.14+`) | Standalone JAR |

In multiplayer, both the client and the server need the mod installed. Custom packs only need to be placed on the server—clients download textures automatically as needed.

---

## Obtaining & Crafting

### 1. Blank Dakimakura (`dakimakuramod:dakimakura`)

Crafted in a Crafting Table using **6 × White Wool** (`minecraft:white_wool`) arranged in a `2 rows × 3 columns` rectangle:

```text
[ White Wool ] [ White Wool ] [ White Wool ]
[ White Wool ] [ White Wool ] [ White Wool ]
[   Empty    ] [   Empty    ] [   Empty    ]
```

*(Any `2 × 3` horizontal placement in the `3 × 3` grid works.)*

### 2. Dakimakura Design (`dakimakuramod:dakimakura_design`)

- **How to get**: Has a **5% drop chance** whenever a **Player** kills any hostile mob (`Enemy`, such as Zombies, Skeletons, Creepers, etc.).
- **How to unlock**: Hold a **Blank Dakimakura Design** in your hand and **Right-Click** in the air. It will roll and unlock a random `Daki` from all currently loaded packs.

### 3. Applying a Design

**Shapeless recipe** (works in your `2 × 2` inventory crafting grid):

- `1 × Blank Dakimakura` + `1 × Unlocked Dakimakura Design` → **Designed Dakimakura**
- Preserves the pillow's current front/back flip state.

### 4. Clearing / Washing a Design

**Shapeless recipe** (works in your `2 × 2` inventory crafting grid):

- `1 × Designed Dakimakura` + `1 × White Dye` (`minecraft:white_dye`) → **Blank Dakimakura**

### 5. Creative Mode

The mod adds a dedicated **Dakimakura Mod** Creative tab (its icon cycles through currently cached dakimakuras). Inside you will find the Blank Design, Blank Dakimakura, and every loaded Dakimakura sorted alphabetically.

---

## Placement & Controls

| Context | Action | Result |
|---|---|---|
| **In Hand** | **Sneak (Shift) + Right-Click** in air | Flips the pillow between front and back sides. |
| **World (Floor)** | **Right-Click** top of a block | Places the pillow flat on the floor (occupies 2 blocks). |
| **World (Wall)** | **Right-Click** side of a block | Places the pillow upright against the wall (occupies 2 blocks vertically). |
| **World (Ceiling)** | **Right-Click** underside of a block | Hangs the pillow horizontally under the ceiling (occupies 2 blocks). |
| **World (Placed)** | **Sneak (Shift) + Right-Click** | Flips the placed pillow between front and back sides. |
| **World (Placed)** | Break either half | Drops the designed Dakimakura item. |
| **World (Placed)** | **Middle-Click** (Pick Block) | Copies the Dakimakura including its design and flip state. |
| **Bed** | **Right-Click** a Bed while holding a Dakimakura | Places the pillow directly onto the bed (no sneak needed; intercepts before sleeping). |
| **Bed (Placed)** | **Sneak (Shift) + Right-Click** | Flips the pillow on the bed between front and back sides. |
| **Bed (Placed)** | **Left-Click** | Pops/removes the pillow off the bed without breaking the bed (works even in Creative mode). |
| **Bed (Placed)** | Break the bed underneath | Drops the Dakimakura item. |

---

## Commands

| Command | Permission Level | Description |
|---|---|---|
| `/dakimakura openPackFolder` | `0` (Any player) | Opens your local `<gameDir>/dakimakura-mod/` folder in the OS file explorer. |
| `/dakimakura reload` | `2` (OP / Cheats enabled) | Reloads config files, rescans `<gameDir>/dakimakura-mod/`, and automatically syncs the updated pack list (in 256 KiB batches) to all online players without restarting. |
