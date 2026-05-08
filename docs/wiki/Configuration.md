# Configuration Reference

_[中文版本 / Chinese version](./Configuration-zh.md)_

Dakimakura Mod (Nothingness-Void Fork) exposes a number of tunables through two Forge config files. Both are created automatically on first launch and reload on-the-fly when edited in-game from the **Mods → Dakimakura Mod → Config** screen.

| File | Side | Path |
|---|---|---|
| `dakimakuramod-common.toml` | Server & single-player host | `<gameDir>/config/dakimakuramod-common.toml` |
| `dakimakuramod-client.toml` | Client only | `<gameDir>/config/dakimakuramod-client.toml` |

The server file is the source of truth on multiplayer servers. Connecting clients use the server's values for anything related to texture transfer.

Texture files on disk are keyed by a content hash that includes the relevant optimization parameters, so **changing any of the server options below will transparently invalidate old cache entries**. You will not get a broken texture rendered from a mismatched cache.

---

## Common (server) options

Section `[server]` in `dakimakuramod-common.toml`.

### `maxTransferHeight`

- **Type**: integer, 256 - 8192
- **Default**: `1536`

The maximum pixel height used when the server downscales a Dakimakura texture before sending it. The width is derived automatically from a 1 : 3 aspect ratio (so height 1536 pairs with width 512).

**Why it matters.** Source images are often 4096 × 12288 or larger. Sending that unmodified eats bandwidth and VRAM for no visible benefit in-game because dakimakura blocks are physically small. A height of 1536 is visually indistinguishable from the original at normal viewing distance but produces transfers roughly an order of magnitude smaller.

**When to change.**

- Lower to `1024` or `768` on constrained servers or if players complain about long first-join waits.
- Raise to `2048` or `3072` only if you have plenty of bandwidth and your players regularly look at dakimakuras from very close up.

### `jpegQuality`

- **Type**: decimal, 0.10 - 1.0
- **Default**: `0.80`

JPEG compression quality used when re-encoding an opaque, `smooth = true` texture before transfer. Images that have transparency are always sent as PNG and ignore this setting.

**Why it matters.** JPEG at 0.80 typically shrinks a re-encoded texture to 15 - 30 % of its PNG size with no visible artifacts on photo / illustration style art. Lowering the quality further trades picture quality for bandwidth.

**When to change.**

- `0.70` is a good aggressive setting if you still care about image quality.
- `0.60` is visibly softer on complex art but halves the transfer again.
- `0.90 - 1.0` all but disables the JPEG savings — only useful for art with very fine details you want to preserve.

### `maxSourceMegabytes`

- **Type**: integer, 1 - 1024
- **Default**: `32`

Hard ceiling on the file size of a single source image. Anything larger is logged and skipped (the dakimakura will render as a missing texture).

**Why it matters.** This is a safety valve. Without it, a 500 MB bogus PNG would still be read entirely into memory before any size check. The limit is applied before `ImageIO` ever opens the file.

**When to change.**

- Raise only if you are deliberately importing art stored as very large PNGs and you are confident the server has the RAM to load them.
- There is rarely a reason to lower this.

### `memoryCacheMinutes`

- **Type**: integer, 1 - 1440
- **Default**: `10`

How long the server keeps an optimized texture in its in-memory cache after the last access. Entries are evicted automatically after this many minutes of inactivity.

**Why it matters.** The memory cache is the fastest path: if five players request the same dakimakura within this window, only the first triggers the expensive image pipeline. The remaining four get the bytes straight from RAM.

**When to change.**

- Raise (30 - 60) on servers with many concurrent players or many dakimakuras, and plenty of heap headroom.
- Lower (3 - 5) on tight-memory servers or if you rarely have overlapping texture requests.

### `diskCacheEnabled`

- **Type**: boolean
- **Default**: `true`

If enabled, the server additionally keeps a persistent copy of every optimized texture on disk.

**Why it matters.** With the disk cache on, the expensive optimization pipeline runs exactly once per (image, settings) combination, ever. Server restarts, new players joining, and memory cache evictions all skip straight to the saved bytes.

**When to change.**

- Turn off only if you specifically do not want any cached state on disk (tiny SSDs, read-only filesystems, etc.). In normal use leaving it on is strictly better.

### `diskCacheMegabytes`

- **Type**: integer, 0 - 65536
- **Default**: `1024`

Soft cap on the total size of the server disk cache. When the cache grows past this size, the oldest files (by last-accessed time) are evicted until it fits again. A value of `0` disables the cap entirely.

**Why it matters.** Optimized textures are small (hundreds of KB each), so 1 GB fits a very large number of dakimakuras. The cap mostly prevents runaway growth if someone adds thousands of packs.

**When to change.**

- Lower on small VPS instances where disk is scarce.
- Raise or set to `0` on dedicated machines with plenty of disk.

---

## Client options

Section `[client]` in `dakimakuramod-client.toml`.

### `memoryCacheMinutes`

- **Type**: integer, 1 - 1440
- **Default**: `20`

How long the client keeps a compiled GPU texture in its in-memory cache after the last access. Evicted textures are deleted from VRAM and reloaded on demand.

**Why it matters.** GPU textures are expensive to upload; keeping them around avoids hitches when you turn around and look at the same dakimakura again. Each cached dakimakura occupies VRAM roughly equal to `maxTransferHeight × (maxTransferHeight / 3) × 2 × 4` bytes (both sides, RGBA).

**When to change.**

- Lower (5 - 10) if you have limited VRAM or run many other heavy mods.
- Raise (60+) if you have plenty of VRAM and hate the micro-stutter of a dakimakura re-uploading after a trip back to base.

### `diskCacheMegabytes`

- **Type**: integer, 0 - 65536
- **Default**: `2048`

Soft cap on the total size of the client-side disk cache, kept under `<gameDir>/dakimakura-mod-cache/`. `0` disables the cap.

**Why it matters.** The client disk cache is what makes reconnects fast. When your client already has a dakimakura's texture bytes (indexed by hash), the server does not need to re-send them. This is the single biggest quality-of-life win on flaky connections or on servers with many custom dakimakuras.

**When to change.**

- Default 2 GB fits thousands of textures; for most people this is more than enough.
- Lower if you are on a small SSD and you mostly play on servers where textures change frequently.
- Raise or set to `0` if you hop between many heavily-customized servers and want them all kept locally.

---

## Where the caches live

| Path | Owner | Purpose |
|---|---|---|
| `<gameDir>/dakimakura-mod/` | Server | Source `daki-info.json` and image files. The pack folder. |
| `<gameDir>/dakimakura-mod-server-cache/` | Server | Optimized, ready-to-send texture bytes keyed by hash. |
| `<gameDir>/dakimakura-mod-cache/` | Client | Per-client copies of received textures, also keyed by hash. |

All three folders are safe to delete at any time. The pack folder will be re-extracted with the built-in Vanilla Mobs pack on next launch; the two cache folders will simply refill as needed.

---

## How texture hashing keeps caches honest

Every Dakimakura texture is fingerprinted with a SHA-256 that includes:

- the hash-version tag,
- the `smooth` flag from the Dakimakura's `daki-info.json`,
- the `maxTransferHeight`, `maxSourceMegabytes`, and `jpegQuality` server options,
- the front image path and bytes,
- the back image path and bytes.

This means:

- **Editing a config value invalidates old caches automatically.** You will not accidentally render a texture optimized under different settings.
- **Replacing an image in a pack invalidates only that dakimakura.** Everyone else still hits the cache.
- **Two dakimakuras that happen to share the same images are de-duplicated at the cache layer.** You only pay for storage and transfer once.

No user action is required when changing settings or updating packs. Stale entries are simply ignored and later evicted by the LRU size cap.
