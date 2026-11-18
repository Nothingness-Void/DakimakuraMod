# Custom Packs Guide

[Back to Wiki Home](./Home.md) | _[中文版本](./Custom-Packs-zh.md)_

Custom dakimakura packs live in `<gameDir>/dakimakura-mod/`. You can open this folder anytime in-game with `/dakimakura openPackFolder`.

On first launch, if `<gameDir>/dakimakura-mod/` does not exist, the mod automatically creates it and extracts the built-in **Vanilla Mobs** pack.

---

## Pack Formats: Folder vs. `.zip`

Both **unpacked folders** and **`.zip` archives** are supported side-by-side. The pack's display name is taken directly from the folder name or `.zip` filename.

> [!NOTE]
> Legacy `pack-info.json` files from 1.12 versions are ignored in modern versions.

### Option A: Folder Pack

```text
<gameDir>/dakimakura-mod/
└── MyAwesomePack/              <-- Pack name
    ├── CharacterOne/           <-- Daki folder name
    │   ├── front.png
    │   ├── back.png
    │   └── daki-info.json      (optional)
    └── CharacterTwo/
        ├── front.jpg
        ├── back.jpg
        └── daki-info.json      (optional)
```

### Option B: `.zip` Pack

`DakiPackZip` automatically detects folder depth, so **both** flat and nested `.zip` layouts work:

```text
MyAwesomePack.zip               <-- Pack name
├── CharacterOne/               (flat layout at zip root)
│   ├── front.png
│   ├── back.png
│   └── daki-info.json
└── ...

MyAwesomePack.zip               <-- Pack name
└── MyAwesomePack/              (nested wrapper folder also works)
    ├── CharacterOne/
    │   ├── front.png
    │   ├── back.png
    │   └── daki-info.json
    └── ...
```

---

## Image Requirements & Optimization

- **Auto-detected filenames**: `front.png` / `front.jpg` / `front.jpeg` and `back.png` / `back.jpg` / `back.jpeg` (or any custom filename specified in `daki-info.json`).
- **Same-image deduplication**: If front and back point to the same image file, the server optimizes it once and deduplicates it.
- **Aspect ratio**: **1 : 3** (`50 cm × 150 cm` in-game proportions, e.g. `512 × 1536`, `1024 × 3072`).
- **Supported limits & automatic downscaling**: Source images up to `maxSourceMegabytes` (default `32 MB`), `32768` max dimension, and `256M` pixels are accepted. The server automatically downscales images to `maxTransferHeight` (default `1536`, i.e. `512 × 1536`) and compresses them before sending to players.

---

## `daki-info.json` Reference

Place an optional UTF-8 `daki-info.json` file inside each dakimakura's folder. **All fields are optional.**

| Field | Type | Default | Description |
|---|---|---|---|
| `"name"` | `string` | Daki folder name | Display name shown in tooltips and Creative tab. |
| `"author"` | `string` | *(none)* | Artist / pack author credit shown in tooltip. |
| `"image-front"` | `string` | Auto-detected `front.*` | Custom filename for the front texture. |
| `"image-back"` | `string` | Auto-detected `back.*` | Custom filename for the back texture. |
| `"flavour-text"` | `string` | *(none)* | Quote or description shown at the bottom of the item tooltip. |
| `"smooth"` | `boolean` | `true` | `true` = bicubic scaling + JPEG compression for opaque images `> 1 MB` or resized (best for anime/illustrations). `false` = nearest-neighbor crisp pixels + lossless PNG (best for pixel art like `Vanilla Mobs`). |

### Example 1: Smooth Illustration / Anime Art (`"smooth": true`)

```json
{
  "name": "Example Character",
  "author": "ArtistName",
  "image-front": "cover_a.png",
  "image-back": "cover_b.png",
  "flavour-text": "Soft and comfy!",
  "smooth": true
}
```

### Example 2: Crisp Pixel Art (`"smooth": false`)

```json
{
  "name": "Creeper",
  "author": "Mojang / Vanilla Mobs",
  "flavour-text": "Tsssss...",
  "smooth": false
}
```

---

## Hot Reloading

After adding, editing, or removing packs in `<gameDir>/dakimakura-mod/`, you do **not** need to restart the game or server:

1. Run `/dakimakura reload` (requires OP / cheats enabled, permission level `2`).
2. The server rescans `<gameDir>/dakimakura-mod/`, reloads configs, and pushes the updated pack list to all connected players in 256 KiB batches.
3. Content-hashed caches (`dakimakura-mod-server-cache` and `dakimakura-mod-cache`) automatically invalidate only modified entries.
