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
    public boolean enableMistralItem = true;
    public boolean enableOpenRouterItem = true;
    public boolean autoFallbackOnLimit = true;
    public String groqApiKey = "";
    public String geminiApiKey = "";
    
    public String mistralApiKey = "";
    public MistralModel mistralModel = MistralModel.MISTRAL_LARGE_2512;
    
    public String openRouterApiKey = "";
    public OpenRouterModel openRouterModel = OpenRouterModel.OPENROUTER_FREE;
    public GroqModel groqModel = GroqModel.LLAMA_3_3_70B;
    public GeminiModel geminiModel = GeminiModel.GEMINI_2_5_FLASH;
    public boolean useGameLanguage = true;
    public TranslationLanguage targetLanguage = TranslationLanguage.TURKISH;
    public boolean autoTranslateChat = false;
    public boolean onlyTranslateNpcChat = true;
    public boolean autoTranslateItems = false;


    public enum TranslationProvider {
        GOOGLE("Google Translate"),
        GEMINI("Gemini AI"),
        GROQ("Groq AI"),
        MISTRAL("Mistral AI"),
        OPENROUTER("OpenRouter AI");

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
        COMPOUND_MINI("groq/compound-mini", "Groq Compound Mini", "30 RPM | 250/gün | 70K TPM | Token limiti yok | Hızlı"),
        ALLAM_2_7B("allam-2-7b", "Allam 2 7B", "30 RPM | 7K/gün | Arapça odaklı çok dilli model");

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
        GEMINI_2_0_PRO_EXP("gemini-2.0-pro-exp-02-05", "Gemini 2.0 Pro Exp", "Deneysel Pro Sürümü | Sınırları zorlar"),
        GEMINI_2_0_FLASH_THINKING("gemini-2.0-flash-thinking-exp-01-21", "Gemini 2.0 Flash Thinking", "Deneysel Flash Düşünen Sürüm");

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

    public enum MistralModel {
        MISTRAL_LARGE_2512("mistral-large-2512", "Mistral Large 2512", "En akıllı model | Ücretsiz deneme (düşük TPS)"),
        MISTRAL_MEDIUM_LATEST("mistral-medium-latest", "Mistral Medium Latest", "Dengeli model | Ücretsiz deneme"),
        MISTRAL_SMALL_2603("mistral-small-2603", "Mistral Small 2603", "Hızlı ve etkili model | Ücretsiz deneme"),
        OPEN_MISTRAL_NEMO("open-mistral-nemo", "Open Mistral Nemo", "Açık kaynak model | Ücretsiz deneme"),
        MINISTRAL_3B_2512("ministral-3b-2512", "Ministral 3B 2512", "Çok hızlı model | Yüksek TPS limiti"),
        MINISTRAL_8B_2512("ministral-8b-2512", "Ministral 8B 2512", "Hızlı küçük model | Yüksek TPS limiti"),
        MINISTRAL_14B_2512("ministral-14b-2512", "Ministral 14B 2512", "Orta boy model"),
        CODESTRAL_2508("codestral-2508", "Codestral 2508", "Kod odaklı model"),
        MAGISTRAL_MEDIUM_2509("magistral-medium-2509", "Magistral Medium 2509", "Magistral model"),
        MAGISTRAL_SMALL_2509("magistral-small-2509", "Magistral Small 2509", "Magistral küçük model"),
        DEVSTRAL_2512("devstral-2512", "Devstral 2512", "Geliştirici modeli"),
        LABS_LEANSTRAL_1_5_1("labs-leanstral-1-5-1", "Labs Leanstral", "Deneysel model");

        public final String modelId;
        public final String displayName;
        public final String description;

        MistralModel(String modelId, String displayName, String description) {
            this.modelId = modelId;
            this.displayName = displayName;
            this.description = description;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public enum OpenRouterModel {
        OPENROUTER_FREE("openrouter/free", "OpenRouter Auto (Free)", "Ücretsiz | Müsait olan modeli otomatik seçer"),
        NEMOTRON_3_ULTRA("nvidia/nemotron-3-ultra-550b-a55b:free", "Nemotron 3 Ultra 550B", "Ücretsiz | NVIDIA devasa model"),
        NEMOTRON_3_SUPER("nvidia/nemotron-3-super-120b-a12b:free", "Nemotron 3 Super 120B", "Ücretsiz | NVIDIA güçlü model"),
        NEMOTRON_3_NANO("nvidia/nemotron-3-nano-30b-a3b:free", "Nemotron 3 Nano 30B", "Ücretsiz | NVIDIA hızlı model"),
        NEMOTRON_3_NANO_OMNI("nvidia/nemotron-3-nano-omni-30b-a3b-reasoning:free", "Nemotron 3 Nano Omni", "Ücretsiz | Akıl yürütme modeli"),
        NEMOTRON_NANO_12B_VL("nvidia/nemotron-nano-12b-v2-vl:free", "Nemotron Nano 12B VL", "Ücretsiz"),
        NEMOTRON_NANO_9B("nvidia/nemotron-nano-9b-v2:free", "Nemotron Nano 9B", "Ücretsiz"),
        NEMOTRON_3_5_SAFETY("nvidia/nemotron-3.5-content-safety:free", "Nemotron 3.5 Safety", "Ücretsiz"),
        LAGUNA_M1("poolside/laguna-m1:free", "Laguna M1", "Ücretsiz | Poolside modeli"),
        LAGUNA_XS_2("poolside/laguna-xs-2:free", "Laguna XS 2", "Ücretsiz"),
        LAGUNA_XS_2_1("poolside/laguna-xs-2.1:free", "Laguna XS 2.1", "Ücretsiz"),
        NORTH_MINI("cohere/north-mini:free", "Cohere North Mini", "Ücretsiz | Cohere hızlı model"),
        GPT_OSS_120B("openai/gpt-oss-120b:free", "GPT OSS 120B", "Ücretsiz"),
        GPT_OSS_20B("openai/gpt-oss-20b:free", "GPT OSS 20B", "Ücretsiz"),
        GEMMA_4_31B("google/gemma-4-31b-it:free", "Gemma 4 31B IT", "Ücretsiz | Google Gemma"),
        GEMMA_4_26B("google/gemma-4-26b-a4b-it:free", "Gemma 4 26B", "Ücretsiz"),
        LFM_2_5_THINKING("liquid/lfm-2.5-1.2b-thinking:free", "Liquid LFM 2.5 Thinking", "Ücretsiz"),
        LFM_2_5_1_2B("liquid/lfm-2.5-1.2b:free", "Liquid LFM 2.5 1.2B", "Ücretsiz"),
        QWEN3_NEXT_80B("qwen/qwen3-next-80b-a3b-instruct:free", "Qwen3 Next 80B", "Ücretsiz"),
        QWEN3_CODER_480B("qwen/qwen3-coder-480b-a35b-instruct:free", "Qwen3 Coder 480B", "Ücretsiz | Dev Qwen modeli"),
        LLAMA_3_3_70B("meta-llama/llama-3.3-70b-instruct:free", "Llama 3.3 70B", "Ücretsiz | Güçlü Meta modeli"),
        LLAMA_3_2_3B("meta-llama/llama-3.2-3b-instruct:free", "Llama 3.2 3B", "Ücretsiz"),
        HERMES_3_405B("nousresearch/hermes-3-llama-3.1-405b:free", "Hermes 3 405B", "Ücretsiz | Çok büyük Hermes"),
        DOLPHIN_MISTRAL("cognitivecomputations/dolphin-mistral-24b-venice-edition:free", "Dolphin Mistral 24B", "Ücretsiz");

        public final String modelId;
        public final String displayName;
        public final String description;

        OpenRouterModel(String modelId, String displayName, String description) {
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
