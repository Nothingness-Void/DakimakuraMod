# Dakimakura Mod (Nothingness-Void Fork)

## 中文说明

这是 Dakimakura Mod 的维护分支，由 **Nothingness-Void** 接手维护。原上游仓库已经约两年没有继续维护，因此这个仓库现在用于继续修复问题、发布构建，并维护新版本移植。

这个 Mod 允许玩家把抱枕放在床上或世界中，并保留了玩家自定义抱枕图片包的功能。

### 当前维护内容

- Minecraft Forge 1.20.1 维护与修复
- Dedicated Server 服务端兼容性修复
- Forge 1.21.1 移植，位于 `codex/port-1.21` 分支
- NeoForge 1.21.1 移植，位于 `codex/neoforge-1.21.1` 分支
- 使用 GitHub Actions 同时构建 Forge 1.20.1、Forge 1.21.1 和 NeoForge 1.21.1 版本

### 下载

发布页：

https://github.com/Nothingness-Void/DakimakuraMod/releases

发布资产命名格式：

- `dakimakuramod-<mod_version>+mc1.20.1-forge.jar`
- `dakimakuramod-<mod_version>+mc1.21.1-forge.jar`
- `dakimakuramod-<mod_version>+mc1.21.1-neoforge.jar`

正式发布时，`<mod_version>` 会与 release tag 去掉 `v` 后的版本号一致，例如 `v1.1.0` 会构建 `dakimakuramod-1.1.0+...jar`。

请按你的服务端 loader 下载对应 jar。Forge jar 与 NeoForge jar 不能混用；客户端与服务端也应使用同一 loader 与同一 Minecraft 版本的 jar。

### 分支

- `master`：Minecraft Forge 1.20.1
- `codex/port-1.21`：Minecraft Forge 1.21.1
- `codex/neoforge-1.21.1`：Minecraft NeoForge 1.21.1

### 构建

使用 Gradle Wrapper：

```powershell
./gradlew.bat build
```

构建产物会输出到 `build/libs/`。

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

This is a maintained fork of Dakimakura Mod, now maintained by **Nothingness-Void**. The original upstream repository has not been actively maintained for roughly two years, so this repository is used to continue maintenance, publish builds, and carry forward newer-version ports.

The mod allows players to place Dakimakuras on beds and around the world, while keeping support for custom Dakimakura image packs.

### Current Maintenance

- Minecraft Forge 1.20.1 maintenance and fixes
- Dedicated server compatibility fixes
- Forge 1.21.1 port on the `codex/port-1.21` branch
- NeoForge 1.21.1 port on the `codex/neoforge-1.21.1` branch
- GitHub Actions builds for Forge 1.20.1, Forge 1.21.1, and NeoForge 1.21.1

### Downloads

Releases are published here:

https://github.com/Nothingness-Void/DakimakuraMod/releases

Release asset naming format:

- `dakimakuramod-<mod_version>+mc1.20.1-forge.jar`
- `dakimakuramod-<mod_version>+mc1.21.1-forge.jar`
- `dakimakuramod-<mod_version>+mc1.21.1-neoforge.jar`

For official releases, `<mod_version>` matches the release tag without the leading `v`; for example, `v1.1.0` builds `dakimakuramod-1.1.0+...jar`.

Download the jar that matches your server loader. Forge jars and NeoForge jars are not interchangeable; clients and servers should use the same loader and the same Minecraft-version jar.

### Branches

- `master`: Minecraft Forge 1.20.1
- `codex/port-1.21`: Minecraft Forge 1.21.1
- `codex/neoforge-1.21.1`: Minecraft NeoForge 1.21.1

### Building

Use the Gradle wrapper:

```powershell
./gradlew.bat build
```

Build outputs are written to `build/libs/`.

### Credits

This fork is maintained by Nothingness-Void.

Original modern Dakimakura Mod development by andrew0030. Former mod author and owner: RiskyKen. Vanilla Mobs art and new Dakimakura models by Apprentice Necromancer. Old Dakimakura models by ORCS004. Localisers include MikaPikaaa, [//972], Ethan, TROU2004, TartaricAcid, and _Hoppang_.

### License And Upstream Notice

This repository retains and follows the upstream project license notices:

- Current-version code is licensed under the [GNU Lesser General Public License v3.0](https://www.gnu.org/licenses/lgpl-3.0.en.html).
- Dakimakura Mod 1.12.2 and older, originally by RiskyKen, and related legacy content are licensed under the [Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License](https://creativecommons.org/licenses/by-nc-sa/3.0/).
- When using or redistributing this fork, keep the original author and contributor attribution and follow the applicable license terms.

[![CC BY-NC-SA 3.0](https://i.creativecommons.org/l/by-nc-sa/3.0/88x31.png)](https://creativecommons.org/licenses/by-nc-sa/3.0/)
