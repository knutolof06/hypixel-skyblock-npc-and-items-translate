# NPC Translator v1.1.6 — Changelog

## 🐛 Bug Fixes

### Chat Translation
- **Fixed scroll position reset**: Translating an older message while scrolled up no longer jumps you to the bottom of chat.
- **Fixed duplicate message targeting**: When multiple identical messages exist in chat (e.g. repeated NPC dialogue), clicking [Translate] on a specific one now correctly translates only that exact message — not the bottom-most copy.
- **Fixed auto-translate not working**: Enabling "Auto-Translate Chat" now properly translates all incoming messages in real-time.
- **Fixed username/NPC name translation**: The AI no longer translates speaker names, player names, or prefixes (e.g. `[NPC] Farmhand:`, `<PlayerName>`). Only the actual message content is translated.

### Item Tooltip Translation
- **Fixed unreliable key detection**: Pressing the translate key (default: X) now reliably triggers on the first press in inventories and chests. Previously required multiple presses due to tooltip render frequency conflicts.
- **Improved RER/RRV compatibility**: Added per-tick toggle locking to prevent rapid state flickering caused by high-FPS tooltip rendering in Recipe Viewer mods.

## ✨ New Features
- **Groq API setup guide**: Hovering over the "Groq API Key" field in settings now shows a step-by-step guide on how to get your free API key (localized in English and Turkish).

## ⚙️ Internal Improvements
- Unique message ID system for precise chat message targeting
- Scroll position preservation using `scrolledLines` accessor
- `TOGGLED_THIS_TICK` debounce mechanism for tooltip key inputs
- Proper JAR naming: output is now `npc_translator-{version}.jar`

---

**Full Compatibility:** Minecraft 1.21.11 (Fabric)  
**Dependencies:** Fabric API, Cloth Config, Mod Menu  
**License:** MIT
