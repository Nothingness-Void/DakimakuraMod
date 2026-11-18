# Dakimakura Mod (Nothingness-Void Fork)

## 中文说明

这是 Dakimakura Mod 的维护分支，由 **Nothingness-Void** 接手维护。原上游仓库已经约两年没有继续维护，因此这个仓库现在用于继续修复问题、发布构建，并维护新版本与多 Loader 移植。

这个 Mod 允许玩家把抱枕放在床上或世界中，并保留了玩家自定义抱枕图片包的功能。

### 当前维护内容

- 统一的 MultiLoader 仓库结构（`common-shared/` 共享核心逻辑与资源）
- `mc1.20.1/`：Minecraft 1.20.1（Forge + Fabric）
- `mc1.21.1/`：Minecraft 1.21.1（NeoForge + Fabric + Forge）
- Dedicated Server 服务端兼容性修复与纹理传输 / 磁盘缓存优化
- 使用 GitHub Actions 并行构建全部 5 个目标 JAR

### 使用指南 / Wiki

完整的玩法、合成、自定义抱枕包制作与开服优化教程请查看中文 Wiki：

- **[Wiki 首页（中文）](docs/wiki/Home-zh.md)** — 5 分钟快速上手与全站导航
- [玩法与操作指南](docs/wiki/Gameplay-zh.md) — 安装依赖、合成与洗白配方、世界/床铺摆放与翻面控制、指令
- [自定义抱枕包教程](docs/wiki/Custom-Packs-zh.md) — 文件夹与 `.zip` 打包结构、1:3 图片规格、`daki-info.json` 完整说明与热重载
- [联机部署与常见问题](docs/wiki/Multiplayer-And-FAQ-zh.md) — 独立服务端部署、压缩/缓存/分块传输架构、调优预设与 FAQ
- [配置项参考](docs/wiki/Configuration-zh.md) — `dakimakuramod-common.toml` 与 `dakimakuramod-client.toml` 逐项说明

### 下载

发布页：

https://github.com/Nothingness-Void/DakimakuraMod/releases

发布资产命名格式（共 5 个目标）：

- `dakimakuramod-<mod_version>+mc1.20.1-forge.jar`
- `dakimakuramod-<mod_version>+mc1.20.1-fabric.jar`
- `dakimakuramod-<mod_version>+mc1.21.1-neoforge.jar`
- `dakimakuramod-<mod_version>+mc1.21.1-fabric.jar`
- `dakimakuramod-<mod_version>+mc1.21.1-forge.jar`

正式发布时，`<mod_version>` 会与 release tag 去掉 `v` 后的版本号一致，例如 `v1.3.0` 会构建 `dakimakuramod-1.3.0+...jar`。

请按你的服务端 loader 下载对应 jar。Forge、Fabric 与 NeoForge jar 不能混用；客户端与服务端也应使用同一 loader 与同一 Minecraft 版本的 jar。

### 仓库结构

- `common-shared/`：跨版本、跨 Loader 共享的代码与资源
- `mc1.20.1/`：Minecraft 1.20.1 多 Loader 工程（`common`、`forge`、`fabric`）
- `mc1.21.1/`：Minecraft 1.21.1 多 Loader 工程（`common`、`neoforge`、`fabric`、`forge`）

### 配置

首次启动后会在 `<gameDir>/config/` 下生成两个配置文件。在 Forge / NeoForge 上可通过游戏内 **Mods → Dakimakura Mod → Config** 直接编辑并即时生效；在 Fabric 上可直接编辑配置文件后执行 `/dakimakura reload` 重载。

- `dakimakuramod-common.toml`：服务端下发图片的尺寸上限、JPEG 质量、服务端内存 / 磁盘缓存开关与上限
- `dakimakuramod-client.toml`：客户端显存缓存时长、本地磁盘缓存上限

默认值对一般服务器都合适，不改也能用。带宽紧张可以把 `maxTransferHeight` 调到 `1024` 或把 `jpegQuality` 调到 `0.70`；显存有限可以把客户端 `memoryCacheMinutes` 降到 `10` 以内。

详细说明、每一项的作用、推荐调整方向、缓存目录结构与哈希保证：[详细配置参考（中文）](docs/wiki/Configuration-zh.md)。

### 构建

在仓库根目录使用 Gradle Wrapper 一键构建全部 5 个目标 JAR（需本机安装 JDK 17 与 JDK 21）：

```powershell
./gradlew.bat build
```

5 个最终构建产物会统一收集到根目录 `build/libs/`。

也可以进入对应版本目录单独构建：

```powershell
# Minecraft 1.20.1 (JDK 17)
cd mc1.20.1
./gradlew.bat build          # 同时构建 Forge 与 Fabric
./gradlew.bat :forge:build   # 仅构建 Forge 1.20.1
./gradlew.bat :fabric:build  # 仅构建 Fabric 1.20.1

# Minecraft 1.21.1 (JDK 21)
cd mc1.21.1
./gradlew.bat build            # 同时构建 NeoForge、Fabric 与 Forge
./gradlew.bat :neoforge:build  # 仅构建 NeoForge 1.21.1
./gradlew.bat :fabric:build    # 仅构建 Fabric 1.21.1
./gradlew.bat :forge:build     # 仅构建 Forge 1.21.1
```

### 致谢

本 fork 由 Nothingness-Void 维护。

现代版本原作者为 andrew0030。更早期的原作者与所有者为 RiskyKen。Vanilla Mobs 美术与新抱枕模型由 Apprentice Necromancer 制作。旧抱枕模型由 ORCS004 制作。翻译贡献者包括 MikaPikaaa、[//972]、Ethan、TROU2004、TartaricAcid 和 _Hoppang_。

### 许可证与上游声明

本仓库保留并遵守上游项目的许可声明：

- 当前版本代码基于 [GNU Lesser General Public License v3.0](https://www.gnu.org/licenses/lgpl-3.0.en.html) 许可发布。
- RiskyKen 制作的 1.12.2 及更早版本，以及相关旧内容，基于 [Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License](https://creativecommons.org/licenses/by-nc-sa/3.0/) 许可发布。
- 使用或再分发本 fork 时，请保留原作者与贡献者署名，并遵守对应许可证条款。

[![CC BY-NC-SA 3.0](https://i.creativecommons.org/l/by-nc-sa/3.0/88x31.png)](https://creativecommons.org/licenses/by-nc-sa/3.0/)

---

## English

This is a maintained fork of Dakimakura Mod, now maintained by **Nothingness-Void**. The original upstream repository has not been actively maintained for roughly two years, so this repository is used to continue maintenance, publish builds, and carry forward newer-version and multi-loader ports.

The mod allows players to place Dakimakuras on beds and around the world, while keeping support for custom Dakimakura image packs.

### Current Maintenance

- Unified MultiLoader repository layout (`common-shared/` for shared core logic and assets)
- `mc1.20.1/`: Minecraft 1.20.1 (Forge + Fabric)
- `mc1.21.1/`: Minecraft 1.21.1 (NeoForge + Fabric + Forge)
- Dedicated server compatibility fixes, texture transfer optimization, and disk caches
- Parallel GitHub Actions builds for all 5 target JARs

### Wiki & Guides

For complete instructions on gameplay, crafting, custom pack creation, and server hosting, see the English Wiki:

- **[Wiki Home (English)](docs/wiki/Home.md)** — 5-minute quickstart and full guide index
- [Gameplay & Controls](docs/wiki/Gameplay.md) — Installation & Fabric API requirement, crafting/washing recipes, world & bed placement/flipping controls, commands
- [Custom Dakimakura Packs](docs/wiki/Custom-Packs.md) — Folder & `.zip` structures, 1:3 image specs, full `daki-info.json` reference, hot reload
- [Multiplayer & FAQ](docs/wiki/Multiplayer-And-FAQ.md) — Dedicated server setup, compression/cache/chunked transfer pipeline, tuning presets, troubleshooting FAQ
- [Configuration Reference](docs/wiki/Configuration.md) — Detailed reference for `dakimakuramod-common.toml` and `dakimakuramod-client.toml`

### Downloads

Releases are published here:

https://github.com/Nothingness-Void/DakimakuraMod/releases

Release asset naming format (5 targets):

- `dakimakuramod-<mod_version>+mc1.20.1-forge.jar`
- `dakimakuramod-<mod_version>+mc1.20.1-fabric.jar`
- `dakimakuramod-<mod_version>+mc1.21.1-neoforge.jar`
- `dakimakuramod-<mod_version>+mc1.21.1-fabric.jar`
- `dakimakuramod-<mod_version>+mc1.21.1-forge.jar`

For official releases, `<mod_version>` matches the release tag without the leading `v`; for example, `v1.3.0` builds `dakimakuramod-1.3.0+...jar`.

Download the jar that matches your server loader. Forge, Fabric, and NeoForge jars are not interchangeable; clients and servers should use the same loader and the same Minecraft-version jar.

### Repository Layout

- `common-shared/`: Core logic and assets shared across Minecraft versions and loaders
- `mc1.20.1/`: Minecraft 1.20.1 MultiLoader project (`common`, `forge`, `fabric`)
- `mc1.21.1/`: Minecraft 1.21.1 MultiLoader project (`common`, `neoforge`, `fabric`, `forge`)

### Configuration

On first launch the mod generates two config files under `<gameDir>/config/`. On Forge / NeoForge they can be edited live via **Mods → Dakimakura Mod → Config** in-game; on Fabric, edit the TOML files directly and run `/dakimakura reload`.

- `dakimakuramod-common.toml`: server-side texture transfer height, JPEG quality, memory / disk cache toggles and size caps
- `dakimakuramod-client.toml`: client-side VRAM cache lifetime and local disk cache cap

Defaults work for most servers. If bandwidth is tight, lower `maxTransferHeight` to `1024` or drop `jpegQuality` to `0.70`. If VRAM is tight, drop the client's `memoryCacheMinutes` to 10 or below.

For every option's behaviour, recommended tweaks, cache layout and hashing guarantees, see the [full configuration reference (English)](docs/wiki/Configuration.md).

### Building

Use the root Gradle wrapper to build all 5 target JARs at once (requires JDK 17 and JDK 21 installed):

```powershell
./gradlew.bat build
```

All 5 release JARs are collected into the root `build/libs/` directory.

You can also build an individual Minecraft version or loader from its subproject directory:

```powershell
# Minecraft 1.20.1 (JDK 17)
cd mc1.20.1
./gradlew.bat build          # Build both Forge and Fabric
./gradlew.bat :forge:build   # Build Forge 1.20.1 only
./gradlew.bat :fabric:build  # Build Fabric 1.20.1 only

# Minecraft 1.21.1 (JDK 21)
cd mc1.21.1
./gradlew.bat build            # Build NeoForge, Fabric, and Forge
./gradlew.bat :neoforge:build  # Build NeoForge 1.21.1 only
./gradlew.bat :fabric:build    # Build Fabric 1.21.1 only
./gradlew.bat :forge:build     # Build Forge 1.21.1 only
```

### Credits

This fork is maintained by Nothingness-Void.

Original modern Dakimakura Mod development by andrew0030. Former mod author and owner: RiskyKen. Vanilla Mobs art and new Dakimakura models by Apprentice Necromancer. Old Dakimakura models by ORCS004. Localisers include MikaPikaaa, [//972], Ethan, TROU2004, TartaricAcid, and _Hoppang_.

### License And Upstream Notice

This repository retains and follows the upstream project license notices:

- Current-version code is licensed under the [GNU Lesser General Public License v3.0](https://www.gnu.org/licenses/lgpl-3.0.en.html).
- Dakimakura Mod 1.12.2 and older, originally by RiskyKen, and related legacy content are licensed under the [Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License](https://creativecommons.org/licenses/by-nc-sa/3.0/).
- When using or redistributing this fork, keep the original author and contributor attribution and follow the applicable license terms.

[![CC BY-NC-SA 3.0](https://i.creativecommons.org/l/by-nc-sa/3.0/88x31.png)](https://creativecommons.org/licenses/by-nc-sa/3.0/)
