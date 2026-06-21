# 🌍 NPC & Items Translator for Hypixel Skyblock & More

Language barrier should never be a problem while enjoying your favorite servers. **NPC & Items Translator** is a fully customizable, intelligent client-side Fabric mod that seamlessly translates NPC chat messages, item descriptions, and GUI tooltips into your preferred language!

Built especially with heavy game modes like **Hypixel SkyBlock** in mind, it perfectly handles complex color codes, dynamic lore (like live Bazaar prices), and interactive chat messages.

---

## ✨ Core Features

### 💬 Interactive Chat Translation
Tired of copying texts to your browser? A small **[Çevir]** (Translate) button will automatically appear next to chat messages.  
Click it to instantly translate the text!

- **Auto-Translate Option:** You can enable auto-translation for chat. 
- **Only Translate NPCs:** If enabled, it will only auto-translate NPC messages, leaving regular player chat alone (but keeping the manual translate button for them).
- **Fully Reversible & Safe:** Click the **[TR]** tag anytime to instantly revert the text back to its original state. ALL original interactive chat click events (like *"Click here to open Auction House"*) are completely preserved!

### 📦 On-Demand Item Tooltip Translation
Hover over any item and press the customizable hotkeys to translate the item lore with your preferred translation engine:
- **`G`** — Translate via **Google Translate** (Free, Unlimited)
- **`X`** — Translate via **Gemini API** (Smart, Fast)
- **`C`** — Translate via **Groq API** (Ultra-fast LLM)
- **`V`** — Revert translation to original

*Note: Pressing the same translation key twice acts as a toggle and reverts the translation.*

### 🎨 Flawless Color Code Preservation
Unlike basic translators that break Minecraft formatting, this mod extracts the `§` color codes, translates the raw meaning, and accurately puts the original colors back.  
Your **Epic** items will stay purple, and your **Legendary** items will remain orange!

### 🛡️ Smart Error Handling & API Fallback
- If you forget to enter your API key or enter a wrong one, a clear warning will appear at the bottom of the item tooltip.
- **Auto-Fallback on Limit:** If an AI model hits its Rate Limit (429), the mod will instantly and silently retry the translation using the next available model from the same provider!

---

## ⚙️ Translation Engines

You have full control over the translation backend through the configuration menu (`/translate` or via ModMenu, or press **Z**). The mod supports multiple models for both AI engines. Hover over the setting in-game to see detailed model comparisons (RPM limits, best uses)!

| Engine | Description |
|--------|-------------|
| 🤖 **Gemini API** | Uses Google's latest Gemini models (e.g. Gemini 2.5 Flash, Gemini Flash-Lite). Excellent contextual understanding and formatting retention. Requires a free API key from [Google AI Studio](https://aistudio.google.com). |
| ⚡ **Groq API** | Uses ultra-fast LLMs (like Llama-3.3-70B, Qwen3) to contextually understand Minecraft terms. Requires a free API key from [console.groq.com](https://console.groq.com). |
| 🌐 **Google Translate** | Don't want an API key? Enjoy unlimited, fast, and completely free translations with no setup required. |

---

## 🔧 Configuration & Settings

- **In-game GUI:** `/translate` command, **Z** keybind, or ESC → Mods → NPC & Items Translator
- **Dynamic Language:** Translate to your Minecraft Client Language automatically, or choose a specific Target Language.
- **Keybinds:** `Controls` → `Key Binds` → **NPC & Eşya Çevirici**

---

## 📦 Compatibility & Requirements

- **Loader:** Fabric
- **Minecraft:** 1.21.1+
- **Required:** [Fabric API](https://modrinth.com/mod/fabric-api), [Cloth Config API](https://modrinth.com/mod/cloth-config), [ModMenu](https://modrinth.com/mod/modmenu)
- **Side:** Client-side ONLY — safe to use on any multiplayer server including Hypixel!

---

## 📄 License

MIT License
