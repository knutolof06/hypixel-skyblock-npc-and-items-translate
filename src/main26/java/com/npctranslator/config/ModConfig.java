package com.npctranslator.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class ModConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "npc_translator.json");
    
    public static ModConfig INSTANCE = new ModConfig();

    public static void load() {
        if (FILE.exists()) {
            try (FileReader reader = new FileReader(FILE)) {
                INSTANCE = GSON.fromJson(reader, ModConfig.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(FILE)) {
            GSON.toJson(INSTANCE, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean hasSeenWelcomeScreen = false;

    public boolean enabled = true;
    public TranslationProvider chatTranslationProvider = TranslationProvider.GOOGLE;
    public boolean enableGoogleItem = true;
    public boolean enableGeminiItem = true;
    public boolean enableGroqItem = true;
    public boolean autoFallbackOnLimit = true;
    public String groqApiKey = "";
    public String geminiApiKey = "";
    public GroqModel groqModel = GroqModel.LLAMA_3_3_70B;
    public GeminiModel geminiModel = GeminiModel.GEMINI_2_5_FLASH;
    public boolean useGameLanguage = true;
    public TranslationLanguage targetLanguage = TranslationLanguage.TURKISH;
    public boolean autoTranslateChat = false;
    public boolean onlyTranslateNpcChat = true;
    public boolean autoTranslateItems = false;


    public enum TranslationProvider {
        GROQ("Groq API"),
        GEMINI("Gemini API"),
        GOOGLE("Google Translate");

        public final String displayName;

        TranslationProvider(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public enum GroqModel {
        LLAMA_3_3_70B("llama-3.3-70b-versatile", "Llama 3.3 70B", "30 RPM | 1K/gün | Güçlü ve dengeli | Önerilen"),
        LLAMA_3_1_8B("llama-3.1-8b-instant", "Llama 3.1 8B", "30 RPM | 14.4K/gün | En hızlı | Çok istek için"),
        LLAMA_4_SCOUT("meta-llama/llama-4-scout-17b-16e-instruct", "Llama 4 Scout 17B", "30 RPM | 1K/gün | Güncel Llama 4"),
        QWEN3_32B("qwen/qwen3-32b", "Qwen3 32B ⭐", "60 RPM | 1K/gün | En yüksek RPM | Çince/çok dilli için iyi"),
        QWEN3_6_27B("qwen/qwen3.6-27b", "Qwen3.6 27B", "30 RPM | 1K/gün | Qwen3 güncel sürüm"),
        GPT_OSS_120B("openai/gpt-oss-120b", "GPT OSS 120B", "30 RPM | 1K/gün | En büyük OSS modeli"),
        GPT_OSS_20B("openai/gpt-oss-20b", "GPT OSS 20B", "30 RPM | 1K/gün | OpenAI açık kaynak model"),
        COMPOUND("groq/compound", "Groq Compound", "30 RPM | 250/gün | 70K TPM | Token limiti yok"),
        COMPOUND_MINI("groq/compound-mini", "Groq Compound Mini", "30 RPM | 250/gün | 70K TPM | Token limiti yok | Hızlı");

        public final String modelId;
        public final String displayName;
        public final String description;

        GroqModel(String modelId, String displayName, String description) {
            this.modelId = modelId;
            this.displayName = displayName;
            this.description = description;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public enum GeminiModel {
        GEMINI_2_5_FLASH("gemini-2.5-flash", "Gemini 2.5 Flash", "Hızlı & Akıllı | Düşünme destekli | Önerilen"),
        GEMINI_2_5_PRO("gemini-2.5-pro", "Gemini 2.5 Pro", "En güçlü model | Düşünme destekli | Yavaş"),
        GEMINI_2_0_FLASH("gemini-2.0-flash", "Gemini 2.0 Flash", "Hızlı ve çok yönlü | Kararlı sürüm"),
        GEMINI_2_0_FLASH_001("gemini-2.0-flash-001", "Gemini 2.0 Flash 001", "Kararlı sürüm 001 | Üretim için güvenli"),
        GEMINI_2_0_FLASH_LITE("gemini-2.0-flash-lite", "Gemini 2.0 Flash-Lite ∞", "Sınırsız RPM | En hafif | Ücretsiz kullanım için ideal"),
        GEMINI_2_0_FLASH_LITE_001("gemini-2.0-flash-lite-001", "Gemini 2.0 Flash-Lite 001 ∞", "Sınırsız RPM | Kararlı lite sürüm"),
        GEMINI_FLASH_LATEST("gemini-flash-latest", "Gemini Flash Latest", "Her zaman en yeni Flash modeli"),
        GEMINI_FLASH_LITE_LATEST("gemini-flash-lite-latest", "Gemini Flash-Lite Latest ∞", "Sınırsız RPM | En yeni lite sürüm"),
        GEMINI_PRO_LATEST("gemini-pro-latest", "Gemini Pro Latest", "Her zaman en yeni Pro modeli");

        public final String modelId;
        public final String displayName;
        public final String description;

        GeminiModel(String modelId, String displayName, String description) {
            this.modelId = modelId;
            this.displayName = displayName;
            this.description = description;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public enum TranslationLanguage {
        TURKISH("tr", "Turkish"),
        ENGLISH("en", "English"),
        GERMAN("de", "German"),
        SPANISH("es", "Spanish"),
        FRENCH("fr", "French"),
        RUSSIAN("ru", "Russian");

        public final String code;
        public final String englishName;

        TranslationLanguage(String code, String englishName) {
            this.code = code;
            this.englishName = englishName;
        }

        @Override
        public String toString() {
            return englishName;
        }
    }
}
