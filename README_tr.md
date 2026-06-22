<div align="right">
  <a href="README.md">🇺🇸 English</a> | <a href="README_tr.md">🇹🇷 Türkçe</a> | <a href="README_ru.md">🇷🇺 Русский</a> | <a href="README_de.md">🇩🇪 Deutsch</a> | <a href="README_zh.md">🇨🇳 中文</a>
</div>

---

# 🌍 Hypixel Skyblock ve Daha Fazlası İçin NPC & Eşya Çevirici

Favori sunucularınızda oynarken dil engeli artık bir sorun olmamalı. **NPC & Eşya Çevirici (NPC & Items Translator)**; NPC sohbet mesajlarını, eşya açıklamalarını ve GUI ipuçlarını anında kendi dilinize çeviren, tamamen özelleştirilebilir, akıllı ve istemci tabanlı (client-side) bir Fabric modudur!

Özellikle **Hypixel SkyBlock** gibi yoğun oyun modları için tasarlanmış olup, karmaşık renk kodlarını, dinamik verileri (Bazaar fiyatları vb.) ve etkileşimli sohbet mesajlarını mükemmel bir şekilde işler.

---

## ✨ Temel Özellikler

### 💬 Etkileşimli Sohbet Çevirisi
Sürekli tarayıcıya metin kopyalamaktan sıkıldınız mı? Sohbet mesajlarının yanında küçük bir **[Çevir]** butonu otomatik olarak belirecektir.  
Mesajı anında çevirmek için butona tıklamanız yeterli!

- **Otomatik Çeviri Seçeneği:** Dilerseniz sohbet mesajlarını otomatik olarak çevirebilirsiniz.
- **Sadece NPC'leri Çevir:** Bu seçenek aktifken sadece NPC mesajları otomatik çevrilir. Normal oyuncu mesajları kendi haline bırakılır (ancak yanlarındaki manuel çeviri butonu durmaya devam eder).
- **Tamamen Geri Döndürülebilir & Güvenli:** Metni orijinal haline döndürmek için **[TR]** veya **[Çevrildi]** etiketine tıklamanız yeterlidir. Ayrıca orijinal etkileşimli tıklama özellikleri (örneğin *"Auction House açmak için tıklayın"*) tamamen korunur!

### 📦 İsteğe Bağlı Eşya Açıklaması Çevirisi
Herhangi bir eşyanın üzerine gelin ve onu tercih ettiğiniz motorla çevirmek için kısayol tuşuna basın:
- **`G`** — **Google Translate** ile Çevir (Ücretsiz, Sınırsız)
- **`X`** — **Gemini API** ile Çevir (Akıllı, Hızlı)
- **`C`** — **Groq API** ile Çevir (Aşırı hızlı yapay zeka)
- **`V`** — Çeviriyi geri al (Orijinale dön)

*Not: Çeviri yaptığınız tuşa tekrar basmak, çeviriyi kapatır ve orijinaline döndürür.*

### 🎨 Kusursuz Renk Kodu Koruması
Minecraft formatlarını bozan basit çevirmenlerin aksine, bu mod `§` renk kodlarını çıkarır, ham anlamı çevirir ve orijinal renkleri harika bir şekilde metne geri yerleştirir.  
**Destansı (Epic)** eşyalarınız mor, **Efsanevi (Legendary)** eşyalarınız turuncu kalmaya devam edecek!

### 🛡️ Akıllı Hata Yakalama & Limitte Geçiş
- Eğer API anahtarınızı girmeyi unutursanız veya hatalı girerseniz, eşyanın en altında net bir uyarı mesajı (örn. *API Hatası*) çıkar.
- **Limitte Otomatik Geçiş:** Eğer bir yapay zeka modeli Rate Limit'e (Kota aşımı - 429) takılırsa, mod çeviriyi iptal etmez; anında arka planda aynı sağlayıcının **bir sonraki uygun modeline** geçiş yaparak çeviriyi tamamlar!

---

## ⚙️ Çeviri Motorları

Çeviri altyapısı üzerinde, ayarlar menüsü üzerinden (`/translate`, ModMenu veya **Z** tuşu) tam kontrole sahipsiniz. Mod, her yapay zeka motoru için birden çok modeli destekler. Limitleri (RPM) ve modelleri görmek için oyun içindeki ayarların üzerine farenizle gelebilirsiniz!

| Motor | Açıklama |
|--------|-------------|
| 🤖 **Gemini API** | Google'ın en yeni Gemini modellerini (örn. Gemini 2.5 Flash, Flash-Lite) kullanır. Mükemmel bağlamsal anlama ve format korumasına sahiptir. [Google AI Studio](https://aistudio.google.com) üzerinden alınacak ücretsiz API anahtarı gerektirir. |
| ⚡ **Groq API** | Minecraft terimlerini bağlam içinde anlamak için aşırı hızlı dil modellerini (Llama-3.3-70B, Qwen3) kullanır. [console.groq.com](https://console.groq.com) üzerinden alınacak ücretsiz API anahtarı gerektirir. |
| 🌐 **Google Translate** | API anahtarıyla uğraşmak istemiyor musunuz? Hiçbir ayar gerektirmeden tamamen ücretsiz, sınırsız ve hızlı çevirinin tadını çıkarın. |

---

## 🔧 Yapılandırma & Ayarlar

- **Oyun İçi Menü:** `/translate` komutu, **Z** kısayolu veya ESC → Mods → NPC & Items Translator
- **Dinamik Dil:** İsterseniz doğrudan mevcut Minecraft oyununuzun diline otomatik çevirir, isterseniz sabit bir Hedef Dil seçebilirsiniz.
- **Kısayollar:** `Seçenekler` → `Kontroller` → `Tuş Atamaları` → **NPC & Eşya Çevirici**

---

## 📦 Uyumluluk & Gereksinimler

- **Yükleyici:** Fabric
- **Minecraft:** 1.21.11, 1.21.4 (26.1, 26.2)
- **Gerekli Modlar:** [Fabric API](https://modrinth.com/mod/fabric-api), [YetAnotherConfigLib (YACL)](https://modrinth.com/mod/yacl), [ModMenu](https://modrinth.com/mod/modmenu)
- **Taraf:** YALNIZCA İstemci Taraflı (Client-side) — Hypixel dahil hiçbir çok oyunculu sunucuda ban riski taşımaz!

---

## 📄 Lisans

MIT Lisansı
