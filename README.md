# AE Tetra Plus

> 作者 / Author: **YingOvO**  
> 源码 / Source: https://github.com/yingovo/ae2-tetra-plus  
> 反饋 / Issues: https://github.com/yingovo/ae2-tetra-plus/issues  
> 下载 / Download: https://github.com/yingovo/ae2-tetra-plus/releases

[English](#english) | [简体中文](#简体中文)

---

## 简体中文

AE Tetra 的扩展模组：给 **模块化存储磁盘** 增加一个「存储类型」槽，让它能在多种存储类型之间切换。

### 功能

- **存储类型槽**：在 Tetra 加工台上可以给磁盘换装不同的核心
  - 物品核心（默认）
  - 流体核心
  - 魔力核心（需要 AppliedBotanics）
  - 能量核心（需要 AppliedFlux）
  - 空气核心（需要 AppliedPneumatics）
  - 全类型核心（物品 + 流体 + 上述所有类型共用一张盘）
- **未安装对应扩展时，对应的核心选项自动隐藏**（物品/流体是 AE2 原生能力，永远保留）
- **切换规则**
  - 任意类型 → 全类型：**不清空**，已有内容全部保留
  - 全类型 → 物品核心：只保留物品，非物品数据被清除
  - 全类型 → 某个具体类型：只保留匹配类型，其它被清除
- **零材料切换**：核心本身不需要材料，直接在改造界面点选即可
- **AE2 风格提示**：内容网格与「占用 / 类型」两行都使用 AE2 自己的组件与文本
- **保留 Shift 查看部件**：无论哪种核心，Tetra 的部件列表都会正常显示
- **像素图标**：六种核心各有自己的像素图标，并随整合包语言显示中文名

### 依赖

| 模组 | 必需 |
|---|---|
| Minecraft 1.20.1 + Forge 47.x | ✅ |
| Applied Energistics 2 (15.4.10+) | ✅ |
| Tetra (6.9.0+) | ✅ |
| AE Tetra (6.9.0+) | ✅ |
| AppliedBotanics | ⭕ 魔力核心 |
| AppliedFlux | ⭕ 能量核心 |
| AppliedPneumatics | ⭕ 空气核心 |

### 安装

1. 把 jar 放进 `mods/`（客户端与服务端都需要）；
2. 启动游戏，在 Tetra 加工台上对 AE Tetra 的模块化磁盘进行改造；
3. 会出现新的「存储类型」槽，点开选择核心即可。

### 兼容性说明

- 本模组通过 Mixin 在运行时挂接 AE Tetra / Tetra 的内部方法，**不会修改它们的任何文件**；
- 若今后 AE Tetra / Tetra 更新导致方法名变化，可能出现加载失败——删除本模组 jar 即可完全恢复原状；
- 全类型核心的「物品部分」沿用 AE Tetra 的存储实现，「非物品部分」由本模组实现，容量按**共享字节池**近似计算。

### 构建

```bash
./gradlew build
```

产物在 `build/libs/`。

### 许可

MIT

---

## English

An add-on for **AE Tetra** that adds a **Storage Type** slot to modular storage cells, allowing one cell to switch between storage types.

### Features

- **Storage Type slot** on the Tetra workbench:
  - Item core (default)
  - Fluid core
  - Mana core (requires AppliedBotanics)
  - FE core (requires AppliedFlux)
  - Air core (requires AppliedPneumatics)
  - Universal core (items + fluids + all supported addon types in one cell)
- Cores whose dependency mod is missing are **hidden automatically**
- **Switch rules**
  - anything → universal: nothing is wiped
  - universal → item core: only items kept
  - universal → specific type: only that type kept
- **No materials needed** to switch cores
- **AE2-native tooltip** (content grid + byte/type lines rendered with AE2's own components)
- **Shift part list preserved** for every core type
- **Pixel icons** per core, localized names

### Requirements

| Mod | Required |
|---|---|
| Minecraft 1.20.1 + Forge 47.x | ✅ |
| Applied Energistics 2 (15.4.10+) | ✅ |
| Tetra (6.9.0+) | ✅ |
| AE Tetra (6.9.0+) | ✅ |
| AppliedBotanics / AppliedFlux / AppliedPneumatics | ⭕ optional |

### License

MIT
