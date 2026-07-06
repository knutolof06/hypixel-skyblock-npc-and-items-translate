<div align="right">
  <a href="https://github.com/knutolof06/hypixel-skyblock-npc-and-items-translate/blob/main/README.md">🇺🇸 English</a> | <a href="https://github.com/knutolof06/hypixel-skyblock-npc-and-items-translate/blob/main/README_tr.md">🇹🇷 Türkçe</a> | <a href="https://github.com/knutolof06/hypixel-skyblock-npc-and-items-translate/blob/main/README_ru.md">🇷🇺 Русский</a> | <a href="https://github.com/knutolof06/hypixel-skyblock-npc-and-items-translate/blob/main/README_de.md">🇩🇪 Deutsch</a> | <a href="https://github.com/knutolof06/hypixel-skyblock-npc-and-items-translate/blob/main/README_zh.md">🇨🇳 中文</a>
</div>

---

# 🌍 Hypixel Skyblock 及更多服务器的 NPC 与物品翻译模组

语言障碍不应该成为您享受最喜爱服务器的阻碍。**NPC 与物品翻译模组 (NPC & Items Translator)** 是一款完全可自定义的、智能的客户端 Fabric 模组，能够无缝地将 NPC 聊天消息、物品描述和 GUI 提示翻译成您首选的语言！

该模组特别针对像 **Hypixel SkyBlock** 这样的大型游戏模式而设计，完美处理复杂的颜色代码、动态描述（如实时 Bazaar 价格）和交互式聊天消息。

---

## ✨ 核心功能

### 💬 交互式聊天翻译
厌倦了将文本复制到浏览器中？一个小的 **[翻译]** (Translate) 按钮将自动出现在聊天消息旁边。  
点击它即可立即翻译文本！

- **自动翻译选项：** 您可以启用聊天的自动翻译。
- **仅翻译 NPC：** 启用后，它将只自动翻译 NPC 的消息，保留普通玩家的聊天内容（但仍为它们保留手动翻译按钮）。
- **完全可逆且安全：** 随时点击 **[TR]** 标签即可立即将文本恢复为原始状态。所有原始的交互式聊天点击事件（如 *“点击此处打开拍卖行”*）都会被完全保留！

### 📦 按需物品提示翻译
将鼠标悬停在任何物品上，按下自定义的快捷键即可使用您首选的翻译引擎翻译物品描述：
- **`G`** — 通过 **Google 翻译** 进行翻译（免费，无限制）
- **`X`** — 通过 **Gemini API** 进行翻译（智能，快速）
- **`C`** — 通过 **Groq API** 进行翻译（超快大语言模型）
- **`[未分配]`** — 通过 **Mistral API** 进行翻译 *(您必须在“控制”中分配按键)*
- **`[未分配]`** — 通过 **OpenRouter API** 进行翻译 *(您必须在“控制”中分配按键)*
- **`V`** — 恢复翻译为原始内容

*注意：连续按两次相同的翻译键相当于切换开关，可恢复翻译。*

### 🎨 完美保留颜色代码
与破坏 Minecraft 格式的基础翻译工具不同，此模组提取 `§` 颜色代码，翻译原始内容，并准确地将原始颜色放回原处。  
您的 **Epic** (史诗) 物品将保持紫色，您的 **Legendary** (传奇) 物品将保持橙色！

### 🛡️ 智能错误处理与 API 备用方案
- 如果您忘记输入 API 密钥或输入错误，物品提示底部将出现清晰的警告。
- **达到限制时自动切换 (Auto-Fallback)：** 如果 AI 模型达到其速率限制 (Rate Limit 429)，模组将立即且无声地使用同一提供商的下一个可用模型重试翻译！

---

## ⚙️ 翻译引擎

您可以通过配置菜单（`/translate` 命令、通过 ModMenu 或按 **Z** 键）完全控制翻译后端。该模组支持两种 AI 引擎的多种模型。在游戏中将鼠标悬停在设置上，可查看详细的模型比较（RPM 限制，最佳用途）！

| 引擎 | 描述 |
|--------|-------------|
| 🤖 **Gemini API** | 使用 Google 最新的 Gemini 模型（如 Gemini 2.5 Flash, Gemini Flash-Lite）。出色的上下文理解和格式保留能力。需要从 [Google AI Studio](https://aistudio.google.com) 获取免费的 API 密钥。 |
| ⚡ **Groq API** | 使用超快的 LLM（如 Llama-3.3-70B, Qwen3）在上下文中理解 Minecraft 术语。需要从 [console.groq.com](https://console.groq.com) 获取免费的 API 密钥。 |
| 🌪️ **Mistral API** | 使用 Mistral 强大的模型（如 Mistral Large）。可靠且高质量。需要从 [console.mistral.ai](https://console.mistral.ai) 获取免费的 API 密钥。 |
| 🌍 **OpenRouter API** | 通过统一的 API 访问各种 AI 模型（Claude, Llama, Qwen 等）。需要从 [openrouter.ai](https://openrouter.ai) 获取免费的 API 密钥。 |
| 🌐 **Google 翻译** | 不想使用 API 密钥？享受无限制、快速且完全免费的翻译，无需任何设置。 |

---

## 🔧 配置与设置

- **游戏内 GUI：** `/translate` 命令、**Z** 快捷键或 ESC → Mods → NPC & Items Translator
- **动态语言：** 自动翻译为您 Minecraft 客户端的语言，或选择特定的目标语言。
- **快捷键：** `控制` → `按键绑定` → **NPC & Eşya Çevirici**

---

## 📦 兼容性与要求

- **加载器：** Fabric
- **Minecraft:** 1.21.11, 1.21.4 (26.1, 26.2)
- **前置模组：** Fabric API ([Modrinth](https://modrinth.com/mod/fabric-api) / [CurseForge](https://www.curseforge.com/minecraft/mc-mods/fabric-api)), YetAnotherConfigLib ([Modrinth](https://modrinth.com/mod/yacl) / [CurseForge](https://www.curseforge.com/minecraft/mc-mods/yacl)), ModMenu ([Modrinth](https://modrinth.com/mod/modmenu) / [CurseForge](https://www.curseforge.com/minecraft/mc-mods/modmenu))
- **端：** 仅限客户端 (Client-side ONLY) — 可以在包括 Hypixel 在内的任何多人服务器上安全使用！

---

## 📄 许可证

MIT 许可证
