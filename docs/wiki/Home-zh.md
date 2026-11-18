# Dakimakura Mod 中文 Wiki

_[English](./Home.md)_

欢迎来到 **Dakimakura Mod（Nothingness-Void 维护分支）** 的官方中文使用指南。本 Mod 允许你在 Minecraft 的地面、墙面、天花板以及床上摆放双面抱枕，并支持玩家自由导入自定义抱枕图片包（文件夹或 `.zip`），联机时由服务端自动压缩、去重并分块下发到客户端。

---

## 5 分钟快速上手

1. **下载安装**：前往 [Releases](https://github.com/Nothingness-Void/DakimakuraMod/releases) 下载与你客户端和服务端一致的 Minecraft 版本与 Loader 的 JAR：
   - **1.20.1**：Forge（`47.1.47+`）或 Fabric（需同时安装 **Fabric Loader** 与 **Fabric API**）
   - **1.21.1**：NeoForge（`21.1.226+`）、Fabric（需同时安装 **Fabric Loader** 与 **Fabric API**）或 Forge（`52.1.14+`）
2. **获取抱枕**：
   - 用 `2 × 3` 共 6 个白色羊毛合成**空白抱枕**。
   - 击杀敌对生物有 **5%** 概率掉落**空白设计图**，手持右键随机解锁一款抱枕设计。
   - 在任意合成格中将**空白抱枕 + 已解锁设计图**合成为成品抱枕。
3. **摆放与翻面**：
   - **直接对着床右键**：把抱枕放在床上（不触发睡觉）。
   - **对着地面 / 墙面 / 天花板右键**：在世界中摆放占用 2 格的抱枕。
   - **潜行（Shift）+ 右键**：切换抱枕正反面。
4. **添加自定义图片包**：运行 `/dakimakura openPackFolder` 打开 `<gameDir>/dakimakura-mod/`，放入你的抱枕包，再执行 `/dakimakura reload` 热重载（无需重启游戏或服务器）。

---

## Wiki 导航

| 页面 | 适用人群 | 内容概览 |
|---|---|---|
| [玩法与操作指南](./Gameplay-zh.md) | 所有玩家 | 版本与 Loader 要求、合成与洗白配方、世界/床铺摆放与控制表、指令列表 |
| [自定义抱枕包教程](./Custom-Packs-zh.md) | 抱枕包作者 | 文件夹与 `.zip` 结构、1:3 图片规格、`daki-info.json` 完整字段与示例、热重载 |
| [联机部署与常见问题](./Multiplayer-And-FAQ-zh.md) | 服主与联机玩家 | 独立服务端部署、服务端压缩/缓存/分块传输架构图、性能调优预设、排错 FAQ |
| [配置项参考](./Configuration-zh.md) | 服主与进阶玩家 | `dakimakuramod-common.toml` 与 `dakimakuramod-client.toml` 逐项说明、缓存目录与哈希机制 |
