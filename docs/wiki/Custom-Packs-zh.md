# 自定义抱枕包教程

_[English](./Custom-Packs.md) | [返回 Wiki 首页](./Home-zh.md)_

---

## 1. 存放位置与自带示例包

所有抱枕包都存放在游戏目录（或服务端根目录）下的：

```text
<gameDir>/dakimakura-mod/
```

- 在客户端或单人游戏中，可以运行 `/dakimakura openPackFolder` 直接打开该目录。
- 首次启动时，如果 `dakimakura-mod/` 目录不存在，Mod 会自动释放内置的 **`Vanilla Mobs`** 示例包（包含 Allay、Axolotl、Camel、Horse 四款像素风原版生物抱枕）。

---

## 2. 抱枕包目录结构（文件夹 vs `.zip`）

Mod 同时支持**文件夹包**与 **`.zip` 压缩包**两种形式，包名直接取自文件夹名称或 `.zip` 文件名。

> [!NOTE]
> 旧版 1.12.2 时代遗留的 `pack-info.json` 在现代版本中**已被忽略**，无需创建。

### 方式 A：文件夹包

```text
<gameDir>/dakimakura-mod/
└── MyAnimePack/
    ├── Rem/
    │   ├── daki-info.json
    │   ├── front.png
    │   └── back.png
    └── Megumin/
        ├── front.jpg
        └── back.jpg
```

### 方式 B：`.zip` 压缩包

`DakiPackZip` 会自动探测压缩包内的目录深度，以下两种打包层级**均受支持**：

```text
MyAnimePack.zip（抱枕目录直接位于 zip 根目录）
├── Rem/
│   ├── daki-info.json
│   ├── front.png
│   └── back.png
└── Megumin/
    ├── front.jpg
    └── back.jpg
```

```text
MyAnimePack.zip（多套了一层包名目录）
└── MyAnimePack/
    ├── Rem/
    │   ├── daki-info.json
    │   ├── front.png
    │   └── back.png
    └── Megumin/
        ├── front.jpg
        └── back.jpg
```

---

## 3. 图片格式与尺寸建议

- **标准长宽比**：**1 : 3**（对应游戏中 50cm × 150cm 的抱枕模型）。常见尺寸如 `512 × 1536`、`1024 × 3072`、`2048 × 6144`。
- **支持格式**：`.png`、`.jpg`、`.jpeg`。
- **默认文件名自动识别**：如果不写 `daki-info.json`，或未在其中指定文件名，Mod 会自动按顺序查找 `front.png` / `front.jpg` / `front.jpeg` 与 `back.png` / `back.jpg` / `back.jpeg`。
- **双面共用同图优化**：如果 `image-front` 与 `image-back` 指向同一张图片文件，服务端只优化处理一次并自动复用。
- **高分辨率原图无需手动压小**：服务端默认允许单张源图最大 `32 MB`（`maxSourceMegabytes`）、最大边长 `32768`、总像素 `256M` 以内，并会自动将其缩放到 `maxTransferHeight`（默认 `1536`，即 `512 × 1536`）后下发，同时结合 GPU `GL_MAX_TEXTURE_SIZE` 保护避免显存崩溃。

---

## 4. `daki-info.json` 完整字段参考

每个抱枕目录下的 `daki-info.json` 需使用 **UTF-8** 编码，**所有字段均为可选**（甚至可以完全省略该文件）：

| 字段 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `"name"` | `string` | 抱枕子目录名 | 物品与创造模式标签页中显示的抱枕名称 |
| `"author"` | `string` | 无 | 物品提示中显示的画师 / 作者名 |
| `"image-front"` | `string` | 自动查找 `front.*` | 自定义正面图片文件名（相对于当前抱枕目录） |
| `"image-back"` | `string` | 自动查找 `back.*` | 自定义背面图片文件名（相对于当前抱枕目录） |
| `"flavour-text"` | `string` | 无 | 物品提示底部的简介 / 趣味台词 |
| `"smooth"` | `boolean` | `true` | `true` 使用双三次插值平滑缩放，且对大于 1MB 或经过缩放的不透明图片启用 JPEG 压缩（适合插画/二次元立绘）；`false` 使用邻近插值保持锐利像素边缘并强制无损 PNG（适合像素画） |

### 示例 1：二次元插画 / 照片类抱枕（`smooth: true`）

```json
{
  "name": "Example Character",
  "author": "IllustratorName",
  "image-front": "cover_a.png",
  "image-back": "cover_b.png",
  "flavour-text": "A soft dakimakura illustration.",
  "smooth": true
}
```

### 示例 2：像素风抱枕（`smooth: false`，如内置 `Vanilla Mobs`）

```json
{
  "name": "Allay",
  "author": "Apprentice Necromancer",
  "flavour-text": "A cute floating helper!",
  "smooth": false
}
```

---

## 5. 热重载（无需重启）

把新制作或修改好的抱枕包放入 `<gameDir>/dakimakura-mod/` 后，在游戏内或服务端控制台执行：

```text
/dakimakura reload
```

服务端会重新读取配置、扫描包目录并计算纹理 SHA-256 指纹，随后将更新后的抱枕列表同步给所有在线玩家。旧纹理缓存会自动因哈希变化而失效，无需手动清理缓存目录。
