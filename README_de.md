<div align="right">
  <a href="https://github.com/knutolof06/hypixel-skyblock-npc-and-items-translate/blob/main/README.md">🇺🇸 English</a> | <a href="https://github.com/knutolof06/hypixel-skyblock-npc-and-items-translate/blob/main/README_tr.md">🇹🇷 Türkçe</a> | <a href="https://github.com/knutolof06/hypixel-skyblock-npc-and-items-translate/blob/main/README_ru.md">🇷🇺 Русский</a> | <a href="https://github.com/knutolof06/hypixel-skyblock-npc-and-items-translate/blob/main/README_de.md">🇩🇪 Deutsch</a> | <a href="https://github.com/knutolof06/hypixel-skyblock-npc-and-items-translate/blob/main/README_zh.md">🇨🇳 中文</a>
</div>

---

# 🌍 NPC & Items Translator für Hypixel Skyblock & Mehr

Die Sprachbarriere sollte kein Problem mehr sein, während Sie Ihre Lieblingsserver genießen. **NPC & Items Translator** ist ein vollständig anpassbarer, intelligenter clientseitiger Fabric-Mod, der NPC-Chatnachrichten, Item-Beschreibungen und GUI-Tooltips nahtlos in Ihre bevorzugte Sprache übersetzt!

Dieser Mod wurde speziell für anspruchsvolle Spielmodi wie **Hypixel SkyBlock** entwickelt und verarbeitet komplexe Farbcodes, dynamische Texte (wie Live-Bazaar-Preise) und interaktive Chatnachrichten perfekt.

---

## ✨ Hauptfunktionen

### 💬 Interaktive Chat-Übersetzung
Haben Sie es satt, Texte in Ihren Browser zu kopieren? Eine kleine Schaltfläche **[Translate]** (Übersetzen) erscheint automatisch neben den Chatnachrichten.  
Klicken Sie darauf, um den Text sofort zu übersetzen!

- **Automatische Übersetzung:** Sie können die automatische Übersetzung für den Chat aktivieren.
- **Nur NPCs übersetzen:** Wenn aktiviert, werden nur NPC-Nachrichten automatisch übersetzt. Normale Spielernachrichten bleiben unangetastet (die manuelle Übersetzungstaste bleibt für sie jedoch erhalten).
- **Vollständig reversibel & sicher:** Klicken Sie jederzeit auf das **[TR]**-Tag, um den Text sofort in seinen ursprünglichen Zustand zurückzuversetzen. ALLE ursprünglichen interaktiven Chat-Klick-Ereignisse (wie *"Klicken Sie hier, um das Auktionshaus zu öffnen"*) bleiben vollständig erhalten!

### 📦 On-Demand Item-Tooltip-Übersetzung
Fahren Sie mit der Maus über ein beliebiges Item und drücken Sie die anpassbaren Hotkeys, um die Item-Beschreibung mit Ihrer bevorzugten Übersetzungsmaschine zu übersetzen:
- **`G`** — Übersetzen via **Google Translate** (Kostenlos, Unbegrenzt)
- **`X`** — Übersetzen via **Gemini API** (Intelligent, Schnell)
- **`C`** — Übersetzen via **Groq API** (Ultraschnelles LLM)
- **`[Nicht zugewiesen]`** — Übersetzen via **Mistral API** *(Sie müssen in der Steuerung eine Taste zuweisen)*
- **`[Nicht zugewiesen]`** — Übersetzen via **OpenRouter API** *(Sie müssen in der Steuerung eine Taste zuweisen)*
- **`V`** — Übersetzung rückgängig machen

*Hinweis: Wenn Sie dieselbe Übersetzungstaste zweimal drücken, fungiert sie als Schalter und setzt die Übersetzung zurück.*

### 🎨 Makellose Erhaltung der Farbcodes
Im Gegensatz zu einfachen Übersetzern, die die Minecraft-Formatierung zerstören, extrahiert dieser Mod die `§`-Farbcodes, übersetzt die rohe Bedeutung und fügt die Originalfarben präzise wieder ein.  
Ihre **Epic** (Epischen) Items bleiben lila und Ihre **Legendary** (Legendären) Items bleiben orange!

### 🛡️ Intelligente Fehlerbehandlung & API-Rückfall
- Wenn Sie vergessen, Ihren API-Schlüssel einzugeben, oder einen falschen eingeben, erscheint unten im Item-Tooltip eine deutliche Warnung.
- **Automatischer Rückfall bei Limit (Auto-Fallback):** Wenn ein KI-Modell sein Ratenlimit (Rate Limit 429) erreicht, wiederholt der Mod die Übersetzung sofort und im Hintergrund mit dem nächsten verfügbaren Modell desselben Anbieters!

---

## ⚙️ Übersetzungsmaschinen

Sie haben die volle Kontrolle über das Übersetzungs-Backend über das Konfigurationsmenü (Befehl `/translate`, über ModMenu oder Taste **Z**). Der Mod unterstützt mehrere Modelle für beide KI-Engines. Fahren Sie im Spiel mit der Maus über die Einstellung, um detaillierte Modellvergleiche (RPM-Limits, beste Anwendungsfälle) zu sehen!

| Maschine | Beschreibung |
|--------|-------------|
| 🤖 **Gemini API** | Verwendet die neuesten Gemini-Modelle von Google (z. B. Gemini 2.5 Flash, Gemini Flash-Lite). Hervorragendes kontextuelles Verständnis und Beibehaltung der Formatierung. Erfordert einen kostenlosen API-Schlüssel von [Google AI Studio](https://aistudio.google.com). |
| ⚡ **Groq API** | Verwendet ultraschnelle LLMs (wie Llama-3.3-70B, Qwen3), um Minecraft-Begriffe im Kontext zu verstehen. Erfordert einen kostenlosen API-Schlüssel von [console.groq.com](https://console.groq.com). |
| 🌪️ **Mistral API** | Verwendet leistungsstarke Mistral-Modelle (z.B. Mistral Large). Zuverlässig und hochwertig. Erfordert einen kostenlosen API-Schlüssel von [console.mistral.ai](https://console.mistral.ai). |
| 🌍 **OpenRouter API** | Zugriff auf eine Vielzahl von KI-Modellen (Claude, Llama, Qwen usw.) über eine einzige API. Erfordert einen kostenlosen API-Schlüssel von [openrouter.ai](https://openrouter.ai). |
| 🌐 **Google Translate** | Möchten Sie keinen API-Schlüssel verwenden? Genießen Sie unbegrenzte, schnelle und völlig kostenlose Übersetzungen ohne Einrichtung. |

---

## 🔧 Konfiguration & Einstellungen

- **In-Game-GUI:** Befehl `/translate`, Tastenkürzel **Z** oder ESC → Mods → NPC & Items Translator
- **Dynamische Sprache:** Automatische Übersetzung in Ihre Minecraft-Client-Sprache oder Auswahl einer spezifischen Zielsprache.
- **Tastenkürzel:** `Steuerung` → `Tastenbelegung` → **NPC & Eşya Çevirici**

---

## 📦 Kompatibilität & Anforderungen

- **Loader:** Fabric
- **Minecraft:** 1.21.11, 1.21.4 (26.1, 26.2)
- **Erforderlich:** Fabric API ([Modrinth](https://modrinth.com/mod/fabric-api) / [CurseForge](https://www.curseforge.com/minecraft/mc-mods/fabric-api)), YetAnotherConfigLib ([Modrinth](https://modrinth.com/mod/yacl) / [CurseForge](https://www.curseforge.com/minecraft/mc-mods/yacl)), ModMenu ([Modrinth](https://modrinth.com/mod/modmenu) / [CurseForge](https://www.curseforge.com/minecraft/mc-mods/modmenu))
- **Seite:** NUR Client-Seite (Client-side) — sicher auf jedem Multiplayer-Server, einschließlich Hypixel, zu verwenden!

---

## 📄 Lizenz

MIT-Lizenz
