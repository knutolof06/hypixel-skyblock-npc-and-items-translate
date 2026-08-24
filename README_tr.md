[🇺🇸 English](https://github.com/knutolof06/hypixel-skyblock-npc-and-items-translate/blob/main/README.md) | [🇹🇷 Türkçe](https://github.com/knutolof06/hypixel-skyblock-npc-and-items-translate/blob/main/README_tr.md) | [🇷🇺 Русский](https://github.com/knutolof06/hypixel-skyblock-npc-and-items-translate/blob/main/README_ru.md) | [🇩🇪 Deutsch](https://github.com/knutolof06/hypixel-skyblock-npc-and-items-translate/blob/main/README_de.md) | [🇨🇳 中文](https://github.com/knutolof06/hypixel-skyblock-npc-and-items-translate/blob/main/README_zh.md)

---

# 🌍 Hypixel Skyblock ve Daha Fazlası İçin NPC & Eşya Çevirici

Favori sunucularınızda oynarken dil engeli artık bir sorun olmamalı. **NPC & Eşya Çevirici (NPC & Items Translator)**; NPC sohbet mesajlarını, eşya açıklamalarını ve GUI ipuçlarını anında kendi dilinize çeviren, akıllı yapay zeka desteği ve sesli okuma (TTS) özelliklerine sahip, tamamen özelleştirilebilir bir istemci tabanlı (client-side) Fabric modudur!

Özellikle **Hypixel SkyBlock** gibi yoğun RPG/MMO oyun modları için tasarlanmış olup, karmaşık renk kodlarını, dinamik verileri (Bazaar fiyatları vb.), etkileşimli sohbet mesajlarını ve 17 farklı dil arayüzünü mükemmel şekilde işler.

---

## ✨ Temel Özellikler

### 💬 Etkileşimli Sohbet Çevirisi
Sürekli tarayıcıya metin kopyalamaktan sıkıldınız mı? Sohbet mesajlarının yanında küçük bir **[Çevir]** butonu otomatik olarak belirecektir.  
Mesajı anında çevirmek için butona tıklamanız yeterli!

- **Otomatik Çeviri Seçeneği:** Sohbet mesajlarını tıklama gerektirmeden otomatik olarak çevirir.
- **Sadece NPC'leri Çevir:** Sadece NPC diyaloglarını otomatik çevirir, normal oyuncu sohbetini bozmaz.
- **Tamamen Geri Döndürülebilir & Güvenli:** Metni orijinal haline döndürmek için **[TR]** veya **[Çevrildi]** etiketine tıklamanız yeterlidir. Orijinal etkileşimli tıklama özellikleri (*"Auction House açmak için tıklayın"* vb.) tamamen korunur!

### 🗣️ Entegre Sesli Okuma (TTS - Metin Okuma)
NPC diyaloglarını ve sohbeti kendi dilinizde sesli olarak dinleyin!
- **Doğal Ses Çıkışı:** Çevrilen NPC mesajlarını yüksek kaliteli Google TTS motoru ile otomatik olarak seslendirir.
- **Detaylı Ayarlar:** **Konuşma Hızı** (0.25x - 2.00x) ve **Ses Tonu / Kalınlık** (0.60x - 1.40x) ayarlarını menüden anlık olarak ayarlayabilirsiniz.
- **Kuyruk Sistemi:** Seri gelen diyaloglar takılmadan sırayla seslendirilir.

### 📦 İsteğe Bağlı Eşya Açıklaması Çevirisi
Herhangi bir eşyanın üzerine gelin ve onu tercih ettiğiniz motorla çevirmek için kısayol tuşuna basın:
- **`G`** — **Google Translate** ile Çevir (Ücretsiz, Anında)
- **`X`** — **Gemini API** ile Çevir (Akıllı, Yüksek Kalite)
- **`C`** — **Groq API** ile Çevir (Aşırı Hızlı Yapay Zeka)
- **`[Tuş Atanabilir]`** — **Mistral API** ile Çevir *(Ayarlar > Kontroller kısmından atanabilir)*
- **`[Tuş Atanabilir]`** — **OpenRouter API** ile Çevir *(Ayarlar > Kontroller kısmından atanabilir)*
- **`V`** — Çeviriyi geri al (Orijinale dön)

*Not: Çeviri yaptığınız tuşa tekrar basmak, çeviriyi kapatır ve orijinaline döndürür.*

### 🎨 Kusursuz Renk Kodu Koruması
Minecraft formatlarını bozan basit çevirmenlerin aksine, bu mod `§` renk kodlarını çıkarır, ham anlamı çevirir ve orijinal renkleri harika bir şekilde metne geri yerleştirir.  
**Destansı (Epic)** eşyalarınız mor, **Efsanevi (Legendary)** eşyalarınız turuncu kalmaya devam eder!

### 🛡️ Akıllı Hata Yakalama & Limitte Otomatik Model Değişimi
- API anahtarı girilmediğinde veya hatalı olduğunda doğrudan eşya açıklamasında net bir uyarı görünür.
- **Limitte Otomatik Geçiş:** Eğer bir model kota aşımına (429) takılırsa, mod çeviriyi yarıda bırakmaz; arka planda aynı sağlayıcının **bir sonraki uygun modeline** otomatik geçer!

---

## 🌐 17 Dil Desteği (Arayüz & Çeviri)

Mod, hem arayüz hem de hedef çeviri dili olarak **17 farklı dili** tam olarak destekler:

| Dil | Kod | Orijinal İsim |
| :--- | :--- | :--- |
| 🇬🇧 İngilizce | `en_us` | English |
| 🇹🇷 Türkçe | `tr_tr` | Türkçe |
| 🇨🇳 Çince (Basitleştirilmiş) | `zh_cn` | 简体中文 |
| 🇪🇸 İspanyolca | `es_es` | Español |
| 🇮🇳 Hintçe | `hi_in` | हिन्दी |
| 🇸🇦 Arapça | `ar_sa` | العربية |
| 🇫🇷 Fransızca | `fr_fr` | Français |
| 🇷🇺 Rusça | `ru_ru` | Русский |
| 🇧🇷 Portekizce (Brezilya) | `pt_br` | Português (Brasil) |
| 🇮🇩 Endonezce | `id_id` | Bahasa Indonesia |
| 🇩🇪 Almanca | `de_de` | Deutsch |
| 🇯🇵 Japonca | `ja_jp` | 日本語 |
| 🇰🇷 Korece | `ko_kr` | 한국어 |
| 🇻🇳 Vietnamca | `vi_vn` | Tiếng Việt |
| 🇮🇹 İtalyanca | `it_it` | Italiano |
| 🇵🇱 Lehçe | `pl_pl` | Polski |
| 🇺🇦 Ukraynaca | `uk_ua` | Українська |

---

## ⚙️ Çeviri Motorları

Ayarlar menüsü (`/translate`, ModMenu veya **Z** tuşu) üzerinden çeviri motorunu dilediğiniz gibi seçebilirsiniz.

| Motor | Açıklama |
|---|---|
| 🤖 **Gemini API** | Google'ın en yeni Gemini modelleri (Gemini 2.5 Flash, Flash-Lite vb.). [Google AI Studio](https://aistudio.google.com) üzerinden ücretsiz anahtar alınabilir. |
| ⚡ **Groq API** | Minecraft terimlerini bağlam içinde anlamak için aşırı hızlı dil modelleri (Llama 3.3 70B, Qwen3). [console.groq.com](https://console.groq.com) üzerinden ücretsiz anahtar alınabilir. |
| 🌪️ **Mistral API** | Mistral'in yüksek performanslı modelleri (Mistral Large vb.). [console.mistral.ai](https://console.mistral.ai) üzerinden ücretsiz anahtar alınabilir. |
| 🌍 **OpenRouter API** | Claude, Llama, Qwen, DeepSeek gibi yüzlerce modele tek API ile erişim. [openrouter.ai](https://openrouter.ai) üzerinden anahtar alınabilir. |
| 🌐 **Google Translate** | API anahtarı gerekmez! Sınırsız, hızlı ve tamamen ücretsiz. |

---

## 🔧 Yapılandırma & Komutlar

- **Oyun İçi Menü:** `/translate` komutu, **`Z`** kısayolu veya `ESC → Seçenekler → Mods → NPC & Items Translator`
- **Dinamik Dil:** İster mevcut Minecraft dilinize otomatik çevirir, isterseniz sabit bir Hedef Dil seçebilirsiniz.
- **Kısayollar:** `Seçenekler → Kontroller → Tuş Atamaları → NPC & Items Translator`
- **Sözlük Sıfırlama:** `/translate DeleteDict` veya oyun içi menüden sözlüğü temizleyebilirsiniz.

---

## 📦 Uyumluluk & Gereksinimler

- **Yükleyici:** Fabric
- **Desteklenen Minecraft Sürümleri:** `1.21.11`, `26.1`, `26.2`
- **Gerekli Modlar:** 
  - [Fabric API](https://modrinth.com/mod/fabric-api)
  - [YetAnotherConfigLib (YACL)](https://modrinth.com/mod/yacl)
  - [ModMenu](https://modrinth.com/mod/modmenu)
- **Taraf:** YALNIZCA İstemci Taraflı (Client-side) — Hypixel dahil hiçbir sunucuda ban riski taşımaz!

---

## 📄 Lisans

MIT Lisansı
