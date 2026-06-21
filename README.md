# 🌍 NPC & Items Translator for Hypixel Skyblock & More

Language barrier should never be a problem while enjoying your favorite servers. **NPC & Items Translator** is a fully customizable, intelligent client-side Fabric mod that seamlessly translates NPC chat messages, item descriptions, and GUI tooltips into your preferred language!

Built especially with heavy game modes like **Hypixel SkyBlock** in mind, it perfectly handles complex color codes, dynamic lore (like live Bazaar prices), and interactive chat messages.

---

## ✨ Core Features

### 💬 Interactive Chat Translation
Tired of copying texts to your browser? A small **[Translate]** button will automatically appear next to NPC chat messages.  
Click it to instantly translate the text!

- **Fully Reversible & Safe:** Click the **[TR]** tag anytime to instantly revert the text back to its original state. ALL original interactive chat click events (like *"Click here to open Auction House"*) are completely preserved!

### 📦 On-Demand Item Tooltip Translation
Hover over any item and press the customizable hotkey (default: **X**) to toggle translation.  
Works flawlessly in custom mod inventories (like NEU Search, SkyblockAddons, etc.).

### 🎨 Flawless Color Code Preservation
Unlike basic translators that break Minecraft formatting, this mod extracts the `§` color codes, translates the raw meaning, and accurately puts the original colors back.  
Your **Epic** items will stay purple, and your **Legendary** items will remain orange!

### 🧠 Intelligent Caching & Anti-Spam
- Translations are cached based on the item's exact fingerprint.
- If an item's lore changes (e.g., dynamic *"Time Left"* or live Bazaar prices), the mod instantly detects it as a new item and reverts to original to prevent outdated information.
- Need a fresh translation? Use the **Force Re-Translate** keybind (default: **M**) to clear the cache for that specific item.

---

## ⚙️ Engine Options (Free vs. AI)

You have full control over the translation backend through the configuration menu (`/translate` or via ModMenu, or press **Z**):

| Engine | Description |
|--------|-------------|
| 🤖 **Groq API** | Uses ultra-fast LLMs (like Llama-3) to contextually understand Minecraft terms. Requires a free API key from [console.groq.com](https://console.groq.com). Best color retention & contextual accuracy. |
| 🌐 **Google Translate** | Don't want an API key? Enjoy unlimited, fast, and completely free translations with no setup required. |

---

## 🔧 Configuration & Settings

- **In-game GUI:** `/translate` command, **Z** keybind, or ESC → Mods → NPC & Items Translator
- **Dynamic Language:** Translate to your Minecraft Client Language automatically, or choose a specific Target Language.
- **Keybinds:** `Controls` → `Key Binds` → **NPC & Eşya Çevirici**
  - `X` — Translate / toggle item tooltip
  - `M` — Force re-translate item
  - `Z` — Open translator menu

---

## 📦 Compatibility & Requirements

- **Loader:** Fabric
- **Minecraft:** 1.21.1+
- **Required:** [Fabric API](https://modrinth.com/mod/fabric-api), [Cloth Config API](https://modrinth.com/mod/cloth-config), [ModMenu](https://modrinth.com/mod/modmenu)
- **Side:** Client-side ONLY — safe to use on any multiplayer server including Hypixel!

---

## 📄 License

MIT License
