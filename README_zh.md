[🇺🇸 English](https://github.com/knutolof06/hypixel-skyblock-npc-and-items-translate/blob/main/README.md) | [🇹🇷 Türkçe](https://github.com/knutolof06/hypixel-skyblock-npc-and-items-translate/blob/main/README_tr.md) | [🇷🇺 Русский](https://github.com/knutolof06/hypixel-skyblock-npc-and-items-translate/blob/main/README_ru.md) | [🇩🇪 Deutsch](https://github.com/knutolof06/hypixel-skyblock-npc-and-items-translate/blob/main/README_de.md) | [🇨🇳 中文](https://github.com/knutolof06/hypixel-skyblock-npc-and-items-translate/blob/main/README_zh.md)

---

# 🌍 Hypixel Skyblock 及更多服务器的 NPC 与物品翻译模组

语言障碍不应该成为您畅玩最喜爱的 Minecraft 服务器的阻碍。**NPC & Items Translator (NPC 与物品翻译器)** 是一款完全可自定义的、智能的客户端 Fabric 模组，能够通过 AI 强大的准确度以及逼真的语音朗读 (TTS)，将 NPC 对话、物品描述和 GUI 提示无缝翻译成您的首选语言！

该模组专为像 **Hypixel SkyBlock** 这样的大型 RPG/MMO 游戏模式而设计，完美支持复杂的颜色代码、动态数据（如 Bazaar 实时价格）、交互式聊天事件以及 17 种语言的完整 UI 本地化。

---

## ✨ 核心功能

### 💬 交互式聊天翻译
告别繁琐的手动复制到浏览器！聊天消息旁会自动出现精致的 **[翻译]** (Translate) 按钮。  
只需点击即可立即将对话翻译为您所需的语言！

- **自动翻译选项：** 无需手动点击，自动翻译所有接收到的聊天消息。
- **仅翻译 NPC：** 仅自动翻译 NPC 对话，保持普通玩家聊天内容不变。
- **安全且可还原：** 随时点击语言标签（如 `[ZH]`、`[TR]` 等）即可立即将文本还原为原始内容。所有交互式点击事件（例如 *“点击此处打开拍卖行”* 等）均完美保留！

### 🗣️ 内置文本转语音 (TTS) 语音朗读
以您的母语实时聆听 NPC 的对话朗读！
- **自然语音输出：** 使用高质量 Google TTS 引擎自动朗读已翻译的 NPC 对话或聊天消息。
- **完全自定义：** 可在设置菜单中轻松调节**语速** (0.25x - 2.00x) 和**音调 / 声音粗细** (0.60x - 1.40x)。
- **智能队列：** 连续对话流畅有序地播放，绝不卡顿。

### 📦 按需物品提示翻译
将鼠标悬停在任何物品上，按下自定义快捷键即可通过首选引擎翻译物品 Lore：
- **`G`** — 通过 **Google 翻译** 翻译（免费、即时）
- **`X`** — 通过 **Gemini AI** 翻译（智能、高质量）
- **`C`** — 通过 **Groq AI** 翻译（超快速大语言模型）
- **`[可自定义]`** — 通过 **Mistral API** 翻译 *(在“控制”设置中绑定)*
- **`[可自定义]`** — 通过 **OpenRouter API** 翻译 *(在“控制”设置中绑定)*
- **`V`** — 将翻译还原为原始物品描述

*注意：连续按两次相同的翻译键可充当开关并还原翻译。*

### 🎨 完美保留颜色与排版代码
与破坏 Minecraft 格式的普通翻译器不同，该模组会智能提取 `§` 颜色代码，翻译文本内容，并准确放回原处的颜色。  
您的 **Epic (史诗)** 物品依然保持紫色，**Legendary (传奇)** 物品依然保持橙色！

### 🛡️ 智能错误处理与模型自动回退
- 若未配置或输入了错误的 API 密钥，物品下方或聊天中将显示清晰的提示。
- **超限自动回退 (Auto-Fallback)：** 当某个 AI 模型触发速率限制 (429) 时，模组会自动且静默地切换到同一服务商的**下一个可用模型**继续翻译！

---

## 🌐 支持 17 种语言（界面与翻译）

模组内置了完整的 UI 界面本地化以及对 **17 种主要语言** 的双向翻译支持：

| 语言 | 语言代码 | 原生名称 |
| :--- | :--- | :--- |
| 🇬🇧 英语 | `en_us` | English |
| 🇹🇷 土耳其语 | `tr_tr` | Türkçe |
| 🇨🇳 中文（简体） | `zh_cn` | 简体中文 |
| 🇪🇸 西班牙语 | `es_es` | Español |
| 🇮🇳 印地语 | `hi_in` | हिन्दी |
| 🇸🇦 阿拉伯语 | `ar_sa` | العربية |
| 🇫🇷 法语 | `fr_fr` | Français |
| 🇷🇺 俄语 | `ru_ru` | Русский |
| 🇧🇷 葡萄牙语（巴西） | `pt_br` | Português (Brasil) |
| 🇮🇩 印度尼西亚语 | `id_id` | Bahasa Indonesia |
| 🇩🇪 德语 | `de_de` | Deutsch |
| 🇯🇵 日语 | `ja_jp` | 日本語 |
| 🇰🇷 韩语 | `ko_kr` | 한국어 |
| 🇻🇳 越南语 | `vi_vn` | Tiếng Việt |
| 🇮🇹 意大利语 | `it_it` | Italiano |
| 🇵🇱 波兰语 | `pl_pl` | Polski |
| 🇺🇦 乌克兰语 | `uk_ua` | Українська |

---

## ⚙️ 翻译引擎

您可以在配置菜单（`/translate`、ModMenu 或按 **Z** 键）中随时自由选择翻译引擎。

| 引擎 | 说明 |
|---|---|
| 🤖 **Gemini AI** | Google 最新的 Gemini 模型（Gemini 2.5 Flash, Flash-Lite 等）。在 [Google AI Studio](https://aistudio.google.com) 获取免费密钥。 |
| ⚡ **Groq AI** | 超快速 LLM（Llama 3.3 70B, Qwen3），深入理解 Minecraft 及 MMO 术语。在 [console.groq.com](https://console.groq.com) 获取免费密钥。 |
| 🌪️ **Mistral AI** | Mistral 高性能模型（Mistral Large 等）。稳定且质量优异。在 [console.mistral.ai](https://console.mistral.ai) 获取免费密钥。 |
| 🌍 **OpenRouter AI** | 通过统一接口调用数百种顶级模型（Claude, Llama, Qwen, DeepSeek）。在 [openrouter.ai](https://openrouter.ai) 获取密钥。 |
| 🌐 **Google 翻译** | 无需任何 API 密钥！开箱即用，无限次快速免费翻译。 |

---

## 🔧 配置与常用命令

- **游戏内配置菜单：** `/translate` 命令、**`Z`** 快捷键或 `ESC → 选项 → Mods → NPC & Items Translator`
- **动态语言：** 自动翻译为您当前游戏客户端语言，亦可手动指定固定目标语言。
- **按键绑定：** `选项 → 控制 → 按键绑定 → NPC & Items Translator`
- **重置翻译词典：** `/translate DeleteDict` 或通过游戏内的 Hub 菜单清除。

---

## 📦 兼容性与运行需求

- **模组加载器：** Fabric
- **支持的 Minecraft 版本：** `1.21.11`, `26.1`, `26.2`
- **前置依赖：** 
  - [Fabric API](https://modrinth.com/mod/fabric-api)
  - [YetAnotherConfigLib (YACL)](https://modrinth.com/mod/yacl)
  - [ModMenu](https://modrinth.com/mod/modmenu)
- **运行端：** 仅限客户端 (Client-side ONLY) — 在包括 Hypixel 在内的所有多人服务器上 100% 安全可用！

---

## 📄 许可证

MIT 许可证

