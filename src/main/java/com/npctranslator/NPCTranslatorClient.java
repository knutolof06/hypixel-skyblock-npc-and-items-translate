package com.npctranslator;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.npctranslator.config.ModConfig;
import com.npctranslator.mixin.ChatHudAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.Window;
import net.minecraft.item.ItemStack;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class NPCTranslatorClient implements ClientModInitializer {

    private static ModConfig config;
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final Gson GSON = new Gson();
    public static KeyBinding keyTranslateGoogle;
    public static KeyBinding keyTranslateGemini;
    public static KeyBinding keyTranslateGroq;
    public static KeyBinding keyTranslateMistral;
    public static KeyBinding keyTranslateOpenRouter;
    public static KeyBinding keyRevertTranslation;
    public static KeyBinding menuKey;

    private static final Map<String, ModConfig.TranslationProvider> TRANSLATED_PROVIDER_CACHE = new ConcurrentHashMap<>();
    
    public static final Map<String, List<Text>> ITEM_CACHE = new ConcurrentHashMap<>();
    private static final Set<String> PENDING_ITEMS = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static final Map<String, Text> ORIGINAL_MESSAGES = new ConcurrentHashMap<>();
    private static final Set<String> REVERTED_ITEMS = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static final java.util.concurrent.atomic.AtomicInteger messageCounter = new java.util.concurrent.atomic.AtomicInteger(0);
    private static final Map<String, String> MESSAGE_ID_TO_BASE64 = new ConcurrentHashMap<>();
    private static final Set<String> TOGGLED_THIS_TICK = Collections.newSetFromMap(new ConcurrentHashMap<>());
    
    private static final Set<String> ACTIVE_TRANSLATED_ITEMS = Collections.newSetFromMap(new ConcurrentHashMap<>());
    public static boolean googleJustPressed = false;
    public static boolean geminiJustPressed = false;
    public static boolean groqJustPressed = false;
    public static boolean mistralJustPressed = false;
    public static boolean openRouterJustPressed = false;
    public static boolean revertJustPressed = false;
    private static boolean lastGoogleState = false;
    private static boolean lastGeminiState = false;
    private static boolean lastGroqState = false;
    private static boolean lastMistralState = false;
    private static boolean lastOpenRouterState = false;
    private static boolean lastRevertState = false;

    private static boolean isKeyCurrentlyDown(KeyBinding keyBinding) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getWindow() == null) return false;
        InputUtil.Key boundKey = KeyBindingHelper.getBoundKeyOf(keyBinding);
        if (boundKey.getCategory() == InputUtil.Type.KEYSYM && boundKey.getCode() != -1) {
            return InputUtil.isKeyPressed(client.getWindow(), boundKey.getCode());
        } else if (boundKey.getCategory() == InputUtil.Type.MOUSE && boundKey.getCode() != -1) {
            return GLFW.glfwGetMouseButton(client.getWindow().getHandle(), boundKey.getCode()) == GLFW.GLFW_PRESS;
        }
        return false;
    }

    @Override
    public void onInitializeClient() {
        ModConfig.load();
        config = ModConfig.INSTANCE;

        KeyBinding.Category modCategory = KeyBinding.Category.create(Identifier.of("npctranslator", "keys"));

        keyTranslateGoogle = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.npctranslator.translate.google",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                modCategory
        ));

        keyTranslateGemini = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.npctranslator.translate.gemini",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_X,
                modCategory
        ));

        keyTranslateGroq = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.npctranslator.translate.groq",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_C,
                modCategory
        ));

        keyTranslateMistral = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.npctranslator.translate.mistral",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                modCategory
        ));

        keyTranslateOpenRouter = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.npctranslator.translate.openrouter",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                modCategory
        ));

        keyRevertTranslation = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.npctranslator.translate.revert",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                modCategory
        ));
        
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.npctranslator.menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Z,
                modCategory
        ));

        ClientReceiveMessageEvents.MODIFY_GAME.register((text, overlay) -> {
            if (!config.enabled) return text;
            if (overlay) return text;
            String rawText = text.getString();
            if (rawText.trim().length() < 2) return text;
            if (rawText.contains("[Çevir]") || rawText.contains("[TR]") || rawText.contains("[Translate]") || rawText.contains("[EN]")) {
                return text;
            }
            
            String msgId = String.valueOf(messageCounter.incrementAndGet());
            String encodedText = Base64.getEncoder().encodeToString(rawText.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            ORIGINAL_MESSAGES.put(msgId, text.copy());
            MESSAGE_ID_TO_BASE64.put(msgId, encodedText);

            boolean shouldAutoTranslate = config.autoTranslateChat && (!config.onlyTranslateNpcChat || rawText.contains("[NPC] "));

            if (shouldAutoTranslate) {
                handleTranslation(msgId);
                return Text.empty().append(text.copy()).append(Text.literal("\u200B").styled(s -> s.withClickEvent(new ClickEvent.RunCommand("/translate_npc " + msgId))));
            }

            MutableText originalMessage = text.copy();
            MutableText translateButton = Text.translatable("npctranslator.button")
                    .append(" ")
                    .styled(style -> style.withColor(Formatting.AQUA)
                            .withClickEvent(new ClickEvent.RunCommand("/translate_npc " + msgId))
                            .withHoverEvent(new HoverEvent.ShowText(Text.translatable("npctranslator.hover"))));
            return Text.empty().append(translateButton).append(originalMessage);
        });

        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(client -> {
            boolean pGoogle = isKeyCurrentlyDown(keyTranslateGoogle);
            googleJustPressed = pGoogle && !lastGoogleState;
            lastGoogleState = pGoogle;

            boolean pGemini = isKeyCurrentlyDown(keyTranslateGemini);
            geminiJustPressed = pGemini && !lastGeminiState;
            boolean isGoogleDown = isKeyCurrentlyDown(keyTranslateGoogle);
            boolean isGeminiDown = isKeyCurrentlyDown(keyTranslateGemini);
            boolean isGroqDown = isKeyCurrentlyDown(keyTranslateGroq);
            boolean isMistralDown = isKeyCurrentlyDown(keyTranslateMistral);
            boolean isOpenRouterDown = isKeyCurrentlyDown(keyTranslateOpenRouter);
            boolean isRevertDown = isKeyCurrentlyDown(keyRevertTranslation);

            googleJustPressed = isGoogleDown && !lastGoogleState;
            geminiJustPressed = isGeminiDown && !lastGeminiState;
            groqJustPressed = isGroqDown && !lastGroqState;
            mistralJustPressed = isMistralDown && !lastMistralState;
            openRouterJustPressed = isOpenRouterDown && !lastOpenRouterState;
            revertJustPressed = isRevertDown && !lastRevertState;

            lastGoogleState = isGoogleDown;
            lastGeminiState = isGeminiDown;
            lastGroqState = isGroqDown;
            lastMistralState = isMistralDown;
            lastOpenRouterState = isOpenRouterDown;
            lastRevertState = isRevertDown;

            TOGGLED_THIS_TICK.clear();
            
            while (menuKey.wasPressed()) {
                openSettings();
            }
        });

        net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (!config.hasSeenWelcomeScreen) {
                client.execute(() -> {
                    client.setScreen(new com.npctranslator.gui.WelcomeScreen());
                });
            }
        });

        net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("translate")
                .executes(context -> {
                    openSettings();
                    return 1;
                })
            );
        });
    }



    public static boolean isItemTranslationEnabled() {
        return config != null && config.enabled;
    }

    public static void setApiKey(String apiKey) {
        MinecraftClient client = MinecraftClient.getInstance();
        config.groqApiKey = apiKey;
        ModConfig.save();
        client.execute(() -> {
            client.inGameHud.getChatHud().addMessage(
                    Text.translatable("npctranslator.key_saved").formatted(Formatting.GREEN));
        });
    }

    public static void openSettings() {
        MinecraftClient client = MinecraftClient.getInstance();
        client.execute(() -> {
            Screen screen = new com.npctranslator.gui.HubScreen();
            client.setScreen(screen);
        });
    }

    public static void reloadConfig() {
        config = ModConfig.INSTANCE;
    }

    private static void replaceMessageInChat(MinecraftClient client, String searchMarker, Text newMessage) {
        ChatHud chatHud = client.inGameHud.getChatHud();
        ChatHudAccessor accessor = (ChatHudAccessor) chatHud;
        List<ChatHudLine> messages = accessor.getMessages();
        // First try exact marker match (for unique ID commands embedded in click events)
        for (int i = 0; i < messages.size(); i++) {
            ChatHudLine line = messages.get(i);
            String fullStyled = extractClickCommands(line.content());
            if (fullStyled.contains(searchMarker)) {
                ChatHudLine newLine = new ChatHudLine(line.creationTick(), newMessage, line.signature(), line.indicator());
                messages.set(i, newLine);
                int scroll = accessor.getScrolledLines();
                accessor.invokeRefresh();
                accessor.setScrolledLines(scroll);
                return;
            }
        }
        // Fallback: text content match (for auto-translate)
        String searchText = searchMarker.trim();
        for (int i = 0; i < messages.size(); i++) {
            ChatHudLine line = messages.get(i);
            String lineText = line.content().getString();
            if (lineText.contains(searchText) || searchText.contains(lineText.trim())) {
                ChatHudLine newLine = new ChatHudLine(line.creationTick(), newMessage, line.signature(), line.indicator());
                messages.set(i, newLine);
                int scroll = accessor.getScrolledLines();
                accessor.invokeRefresh();
                accessor.setScrolledLines(scroll);
                return;
            }
        }
    }

    private static String extractClickCommands(Text text) {
        StringBuilder sb = new StringBuilder();
        if (text.getStyle() != null && text.getStyle().getClickEvent() instanceof ClickEvent.RunCommand runCmd) {
            sb.append(runCmd.command());
        }
        for (Text sibling : text.getSiblings()) {
            sb.append(extractClickCommands(sibling));
        }
        return sb.toString();
    }

    private static final java.util.Map<Formatting, Character> FORMAT_TO_CODE = new java.util.HashMap<>();
    static {
        for (Formatting f : Formatting.values()) {
            if (f.getCode() != 0) FORMAT_TO_CODE.put(f, f.getCode());
        }
    }

    private static String textToFormattedString(Text text) {
        StringBuilder sb = new StringBuilder();
        text.visit((style, string) -> {
            // Apply color
            if (style != null && style.getColor() != null) {
                for (Formatting f : Formatting.values()) {
                    if (f.isColor() && style.getColor().equals(net.minecraft.text.TextColor.fromFormatting(f))) {
                        sb.append('\u00A7').append(f.getCode());
                        break;
                    }
                }
            }
            // Apply decorations
            if (style != null) {
                if (style.isBold()) sb.append("\u00A7l");
                if (style.isItalic()) sb.append("\u00A7o");
                if (style.isUnderlined()) sb.append("\u00A7n");
                if (style.isStrikethrough()) sb.append("\u00A7m");
                if (style.isObfuscated()) sb.append("\u00A7k");
            }
            sb.append(string);
            sb.append("\u00A7r"); // Reset to prevent bleeding
            return java.util.Optional.empty();
        }, net.minecraft.text.Style.EMPTY);
        return sb.toString();
    }

    private static MutableText formattedStringToText(String formatted) {
        MutableText result = Text.empty();
        StringBuilder current = new StringBuilder();
        Formatting currentColor = null;
        boolean bold = false, italic = false, underline = false, strikethrough = false, obfuscated = false;

        for (int i = 0; i < formatted.length(); i++) {
            if (formatted.charAt(i) == '\u00A7' && i + 1 < formatted.length()) {
                // Flush current segment
                if (current.length() > 0) {
                    MutableText segment = Text.literal(current.toString());
                    applyStyle(segment, currentColor, bold, italic, underline, strikethrough, obfuscated);
                    result.append(segment);
                    current.setLength(0);
                }
                char code = formatted.charAt(i + 1);
                Formatting fmt = Formatting.byCode(code);
                if (fmt != null) {
                    if (fmt.isColor()) {
                        currentColor = fmt;
                        bold = false; italic = false; underline = false; strikethrough = false; obfuscated = false;
                    } else if (fmt == Formatting.RESET) {
                        currentColor = null;
                        bold = false; italic = false; underline = false; strikethrough = false; obfuscated = false;
                    } else if (fmt == Formatting.BOLD) bold = true;
                    else if (fmt == Formatting.ITALIC) italic = true;
                    else if (fmt == Formatting.UNDERLINE) underline = true;
                    else if (fmt == Formatting.STRIKETHROUGH) strikethrough = true;
                    else if (fmt == Formatting.OBFUSCATED) obfuscated = true;
                }
                i++; // skip code char
            } else {
                current.append(formatted.charAt(i));
            }
        }
        // Flush remaining
        if (current.length() > 0) {
            MutableText segment = Text.literal(current.toString());
            applyStyle(segment, currentColor, bold, italic, underline, strikethrough, obfuscated);
            result.append(segment);
        }
        return result;
    }

    private static void applyStyle(MutableText text, Formatting color, boolean bold, boolean italic, boolean underline, boolean strikethrough, boolean obfuscated) {
        if (color != null) text.formatted(color);
        if (bold) text.formatted(Formatting.BOLD);
        if (italic) text.formatted(Formatting.ITALIC);
        if (underline) text.formatted(Formatting.UNDERLINE);
        if (strikethrough) text.formatted(Formatting.STRIKETHROUGH);
        if (obfuscated) text.formatted(Formatting.OBFUSCATED);
    }

    public static void handleTranslation(String msgId) {
        reloadConfig();
        MinecraftClient client = MinecraftClient.getInstance();

        String base64Message = MESSAGE_ID_TO_BASE64.get(msgId);
        if (base64Message == null) {
            // Fallback: treat msgId as raw base64 (for backwards compat)
            base64Message = msgId;
        }
        final String finalBase64 = base64Message;

        String decodedMessage;
        try {
            decodedMessage = new String(Base64.getDecoder().decode(finalBase64), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) { return; }

        if (config.chatTranslationProvider == ModConfig.TranslationProvider.GROQ && (config.groqApiKey == null || config.groqApiKey.isEmpty())) {
            client.execute(() -> client.inGameHud.getChatHud().addMessage(Text.translatable("npctranslator.error.config").formatted(Formatting.RED)));
            return;
        }
        if (config.chatTranslationProvider == ModConfig.TranslationProvider.GEMINI && (config.geminiApiKey == null || config.geminiApiKey.isEmpty())) {
            client.execute(() -> client.inGameHud.getChatHud().addMessage(Text.translatable("npctranslator.error.config").formatted(Formatting.RED)));
            return;
        }

        String targetLangName;
        String displayLanguageCode;
        if (config.useGameLanguage) {
            String userLanguage = client.options.language;
            targetLangName = "the language for locale: " + userLanguage;
            displayLanguageCode = userLanguage.contains("_") ? userLanguage.split("_")[0] : "tr";
        } else {
            targetLangName = config.targetLanguage.englishName;
            displayLanguageCode = config.targetLanguage.code;
        }

        final String searchMarker = "translate_npc " + msgId;
        CompletableFuture.runAsync(() -> {
            String tempFormattedInput = decodedMessage;
            ClickEvent tempClickEvent = null;
            try {
                // Convert original message to §-coded string to preserve colors
                Text originalMsg = ORIGINAL_MESSAGES.get(msgId);
                if (originalMsg != null) {
                    tempFormattedInput = textToFormattedString(originalMsg);
                    // Extract click event
                    if (originalMsg.getStyle() != null && originalMsg.getStyle().getClickEvent() != null) {
                        tempClickEvent = originalMsg.getStyle().getClickEvent();
                    } else {
                        for (Text sibling : originalMsg.getSiblings()) {
                            if (sibling.getStyle() != null && sibling.getStyle().getClickEvent() != null) {
                                tempClickEvent = sibling.getStyle().getClickEvent();
                                break;
                            }
                        }
                    }
                }
                final String formattedInput = tempFormattedInput;
                final ClickEvent finalClickEvent = tempClickEvent;

                String translatedText = getTranslatedText(formattedInput, displayLanguageCode, targetLangName, false, config.chatTranslationProvider);

                MutableText prefix = Text.translatable("npctranslator.translated", displayLanguageCode.toUpperCase())
                    .styled(style -> style.withColor(Formatting.GREEN)
                        .withClickEvent(new ClickEvent.RunCommand("/revert_npc " + msgId))
                        .withHoverEvent(new HoverEvent.ShowText(Text.translatable("npctranslator.hover_revert"))));

                // Parse §-coded translated text back into colored Text
                MutableText translationContent = formattedStringToText(translatedText);
                if (finalClickEvent != null) {
                    translationContent.styled(s -> s.withClickEvent(finalClickEvent));
                }

                MutableText translatedMessage = Text.empty().append(prefix).append(translationContent);
                client.execute(() -> replaceMessageInChat(client, searchMarker, translatedMessage));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static String getTranslatedText(String content, String langCode, String langName, boolean isTooltip, ModConfig.TranslationProvider provider) throws Exception {
        if (provider == ModConfig.TranslationProvider.GOOGLE) {
            String url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=" + langCode + "&dt=t&q=" + java.net.URLEncoder.encode(content, "UTF-8");
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonArray jsonArray = GSON.fromJson(response.body(), JsonArray.class);
                JsonArray sentences = jsonArray.get(0).getAsJsonArray();
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < sentences.size(); i++) {
                    sb.append(sentences.get(i).getAsJsonArray().get(0).getAsString());
                }
                return sb.toString();
            }
            throw new Exception("Google Translate failed. Status: " + response.statusCode());
        } else if (provider == ModConfig.TranslationProvider.GROQ) {
            ModConfig.GroqModel[] allModels = ModConfig.GroqModel.values();
            int startIndex = config.groqModel.ordinal();
            int attempts = config.autoFallbackOnLimit ? allModels.length : 1;
            
            for (int i = 0; i < attempts; i++) {
                ModConfig.GroqModel currentModel = allModels[(startIndex + i) % allModels.length];
                JsonObject jsonBody = new JsonObject();
                jsonBody.addProperty("model", currentModel.modelId);
                JsonArray messages = new JsonArray();
                JsonObject systemMessage = new JsonObject();
                systemMessage.addProperty("role", "system");
                if (isTooltip) {
                    systemMessage.addProperty("content", "Translate Minecraft item tooltip to " + langName + ". CRITICAL: Keep all Minecraft § color codes. Maintain exact line order and spacing. Output ONLY translation.");
                } else {
                    systemMessage.addProperty("content", "Translate Minecraft NPC chat to " + langName + ". CRITICAL: Keep ALL Minecraft § color/formatting codes (like §e, §f, §l, §o etc) exactly where they are. DO NOT translate speaker names, player names, or prefixes. ONLY translate the actual message content. Output ONLY the final translated line with § codes preserved.");
                }
                messages.add(systemMessage);
                JsonObject userMessage = new JsonObject();
                userMessage.addProperty("role", "user");
                userMessage.addProperty("content", content);
                messages.add(userMessage);
                jsonBody.add("messages", messages);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                        .header("Authorization", "Bearer " + config.groqApiKey)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(jsonBody)))
                        .build();

                HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    JsonObject responseJson = GSON.fromJson(response.body(), JsonObject.class);
                    return responseJson.getAsJsonArray("choices").get(0).getAsJsonObject().getAsJsonObject("message").get("content").getAsString();
                } else if (response.statusCode() == 429 && i < attempts - 1) {
                    continue; // Limit aşıldı, sıradaki modele geç
                }
                
                if (i == attempts - 1) {
                    throw new Exception("Groq API failed. Status: " + response.statusCode());
                }
            }
        } else if (provider == ModConfig.TranslationProvider.GEMINI) {
            JsonObject jsonBody = new JsonObject();
            
            JsonObject systemInstruction = new JsonObject();
            JsonObject partsObjSys = new JsonObject();
            if (isTooltip) {
                partsObjSys.addProperty("text", "Translate Minecraft item tooltip to " + langName + ". CRITICAL: Keep all Minecraft § color codes. Maintain exact line order and spacing. Output ONLY translation.");
            } else {
                partsObjSys.addProperty("text", "Translate Minecraft NPC chat to " + langName + ". CRITICAL: Keep ALL Minecraft § color/formatting codes (like §e, §f, §l, §o etc) exactly where they are. DO NOT translate speaker names, player names, or prefixes. ONLY translate the actual message content. Output ONLY the final translated line with § codes preserved.");
            }
            JsonArray sysParts = new JsonArray();
            sysParts.add(partsObjSys);
            systemInstruction.add("parts", sysParts);
            jsonBody.add("systemInstruction", systemInstruction);

            JsonArray contents = new JsonArray();
            JsonObject contentObj = new JsonObject();
            contentObj.addProperty("role", "user");
            JsonArray parts = new JsonArray();
            JsonObject textPart = new JsonObject();
            textPart.addProperty("text", content);
            parts.add(textPart);
            contentObj.add("parts", parts);
            contents.add(contentObj);
            
            jsonBody.add("contents", contents);

            ModConfig.GeminiModel[] allModels = ModConfig.GeminiModel.values();
            int startIndex = config.geminiModel.ordinal();
            int attempts = config.autoFallbackOnLimit ? allModels.length : 1;

            for (int i = 0; i < attempts; i++) {
                ModConfig.GeminiModel currentModel = allModels[(startIndex + i) % allModels.length];
                String url = "https://generativelanguage.googleapis.com/v1beta/models/" + currentModel.modelId + ":generateContent?key=" + config.geminiApiKey;
                
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(jsonBody)))
                        .build();

                HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    JsonObject responseJson = GSON.fromJson(response.body(), JsonObject.class);
                    return responseJson.getAsJsonArray("candidates").get(0).getAsJsonObject().getAsJsonObject("content").getAsJsonArray("parts").get(0).getAsJsonObject().get("text").getAsString();
                } else if (response.statusCode() == 429 && i < attempts - 1) {
                    continue; // Limit aşıldı, sıradaki modele geç
                }

                if (i == attempts - 1) {
                    throw new Exception("Gemini API failed. Status: " + response.statusCode());
                }
            }
        } else if (provider == ModConfig.TranslationProvider.MISTRAL) {
            ModConfig.MistralModel[] allModels = ModConfig.MistralModel.values();
            int startIndex = config.mistralModel.ordinal();
            int attempts = config.autoFallbackOnLimit ? allModels.length : 1;

            for (int i = 0; i < attempts; i++) {
                ModConfig.MistralModel currentModel = allModels[(startIndex + i) % allModels.length];
                JsonObject jsonBody = new JsonObject();
                jsonBody.addProperty("model", currentModel.modelId);
                JsonArray messages = new JsonArray();
                JsonObject systemMessage = new JsonObject();
                systemMessage.addProperty("role", "system");
                if (isTooltip) {
                    systemMessage.addProperty("content", "Translate Minecraft item tooltip to " + langName + ". CRITICAL: Keep all Minecraft § color codes. Maintain exact line order and spacing. Output ONLY translation.");
                } else {
                    systemMessage.addProperty("content", "Translate Minecraft NPC chat to " + langName + ". CRITICAL: Keep ALL Minecraft § color/formatting codes (like §e, §f, §l, §o etc) exactly where they are. DO NOT translate speaker names, player names, or prefixes. ONLY translate the actual message content. Output ONLY the final translated line with § codes preserved.");
                }
                messages.add(systemMessage);
                JsonObject userMessage = new JsonObject();
                userMessage.addProperty("role", "user");
                userMessage.addProperty("content", content);
                messages.add(userMessage);
                jsonBody.add("messages", messages);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.mistral.ai/v1/chat/completions"))
                        .header("Authorization", "Bearer " + config.mistralApiKey)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(jsonBody)))
                        .build();

                HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    JsonObject responseJson = GSON.fromJson(response.body(), JsonObject.class);
                    return responseJson.getAsJsonArray("choices").get(0).getAsJsonObject().getAsJsonObject("message").get("content").getAsString();
                } else if (response.statusCode() == 429 && i < attempts - 1) {
                    continue; // Limit aşıldı, sıradaki modele geç
                }
                
                if (i == attempts - 1) {
                    throw new Exception("Mistral API failed. Status: " + response.statusCode());
                }
            }
        } else if (provider == ModConfig.TranslationProvider.OPENROUTER) {
            ModConfig.OpenRouterModel[] allModels = ModConfig.OpenRouterModel.values();
            int startIndex = config.openRouterModel.ordinal();
            int attempts = config.autoFallbackOnLimit ? allModels.length : 1;

            for (int i = 0; i < attempts; i++) {
                ModConfig.OpenRouterModel currentModel = allModels[(startIndex + i) % allModels.length];
                JsonObject jsonBody = new JsonObject();
                jsonBody.addProperty("model", currentModel.modelId);
                
                // Extra setting recommended by OpenRouter documentation for routing
                JsonObject httpReferer = new JsonObject();
                httpReferer.addProperty("HTTP-Referer", "https://github.com/knutolof06/hypixel-skyblock-npc-and-items-translate");
                httpReferer.addProperty("X-Title", "Minecraft NPC Translator Mod");
                
                JsonArray messages = new JsonArray();
                JsonObject systemMessage = new JsonObject();
                systemMessage.addProperty("role", "system");
                if (isTooltip) {
                    systemMessage.addProperty("content", "Translate Minecraft item tooltip to " + langName + ". CRITICAL: Keep all Minecraft § color codes. Maintain exact line order and spacing. Output ONLY translation.");
                } else {
                    systemMessage.addProperty("content", "Translate Minecraft NPC chat to " + langName + ". CRITICAL: Keep ALL Minecraft § color/formatting codes (like §e, §f, §l, §o etc) exactly where they are. DO NOT translate speaker names, player names, or prefixes. ONLY translate the actual message content. Output ONLY the final translated line with § codes preserved.");
                }
                messages.add(systemMessage);
                JsonObject userMessage = new JsonObject();
                userMessage.addProperty("role", "user");
                userMessage.addProperty("content", content);
                messages.add(userMessage);
                jsonBody.add("messages", messages);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://openrouter.ai/api/v1/chat/completions"))
                        .header("Authorization", "Bearer " + config.openRouterApiKey)
                        .header("Content-Type", "application/json")
                        .header("HTTP-Referer", "https://github.com/knutolof06/hypixel-skyblock-npc-and-items-translate")
                        .header("X-Title", "Minecraft NPC Translator Mod")
                        .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(jsonBody)))
                        .build();

                HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    JsonObject responseJson = GSON.fromJson(response.body(), JsonObject.class);
                    return responseJson.getAsJsonArray("choices").get(0).getAsJsonObject().getAsJsonObject("message").get("content").getAsString();
                } else if (response.statusCode() == 429 && i < attempts - 1) {
                    continue; // Limit aşıldı, sıradaki modele geç
                }
                
                if (i == attempts - 1) {
                    throw new Exception("OpenRouter API failed. Status: " + response.statusCode());
                }
            }
        }
        return content;
    }

    public static void handleRevert(String msgId, String unused) {
        MinecraftClient client = MinecraftClient.getInstance();
        try {
            Text originalMessage = ORIGINAL_MESSAGES.get(msgId);
            if (originalMessage == null) {
                // Fallback
                String base64 = MESSAGE_ID_TO_BASE64.get(msgId);
                if (base64 != null) {
                    String originalText = new String(Base64.getDecoder().decode(base64), java.nio.charset.StandardCharsets.UTF_8);
                    originalMessage = Text.literal(originalText);
                } else {
                    return;
                }
            }

            MutableText translateButton = Text.translatable("npctranslator.button")
                    .append(" ")
                    .styled(style -> style.withColor(Formatting.AQUA)
                            .withClickEvent(new ClickEvent.RunCommand("/translate_npc " + msgId))
                            .withHoverEvent(new HoverEvent.ShowText(Text.translatable("npctranslator.hover"))));

            Text finalMessage = originalMessage;
            MutableText fullOriginal = Text.empty().append(translateButton).append(finalMessage.copy());
            String searchMarker = "revert_npc " + msgId;
            client.execute(() -> replaceMessageInChat(client, searchMarker, fullOriginal));

        } catch (Exception e) {}
    }

    public static void handleItemTooltip(ItemStack stack, List<Text> tooltip) {
        if (!config.enabled) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;
        
        String itemKey = stack.getItem().toString() + "_" + (stack.getComponents() != null ? stack.getComponents().hashCode() : 0);
        String lang = config.useGameLanguage ? clientLanguage() : config.targetLanguage.code;
        String cacheKey = itemKey + "_" + lang;

        handleTooltipStateAndRender(tooltip, cacheKey);
    }

    public static void handleGeneralTooltip(List<Text> tooltip) {
        if (!config.enabled) return;
        if (tooltip.isEmpty()) return;

        // Skip if already handled by ItemStack tooltip mapping
        for (Text t : tooltip) {
            String str = t.getString();
            if (str.contains("✅") || str.contains("evirmek") || str.contains("Google ile") || str.contains("Yapay zeka") || str.contains("Translated by")) return;
        }

        // Create a cache key from the tooltip content itself since we don't have an ItemStack
        StringBuilder content = new StringBuilder();
        for (Text t : tooltip) content.append(t.getString());
        if (content.length() < 10) return; // Too short to be an item lore usually

        String lang = config.useGameLanguage ? clientLanguage() : config.targetLanguage.code;
        String cacheKey = "gen_" + Integer.toHexString(content.toString().hashCode()) + "_" + lang;

        handleTooltipStateAndRender(tooltip, cacheKey);
    }

    private static void handleTooltipStateAndRender(List<Text> tooltip, String cacheKey) {
        ModConfig.TranslationProvider currentProvider = TRANSLATED_PROVIDER_CACHE.get(cacheKey);
        boolean isAlreadyTranslated = ACTIVE_TRANSLATED_ITEMS.contains(cacheKey) && ITEM_CACHE.containsKey(cacheKey);

        if (googleJustPressed && config.enableGoogleItem && !TOGGLED_THIS_TICK.contains(cacheKey + "_G")) {
            if (isAlreadyTranslated && currentProvider == ModConfig.TranslationProvider.GOOGLE) {
                ACTIVE_TRANSLATED_ITEMS.remove(cacheKey); REVERTED_ITEMS.add(cacheKey);
                TRANSLATED_PROVIDER_CACHE.remove(cacheKey); ITEM_CACHE.remove(cacheKey);
            } else {
                ITEM_CACHE.remove(cacheKey); PENDING_ITEMS.remove(cacheKey); REVERTED_ITEMS.remove(cacheKey);
                ACTIVE_TRANSLATED_ITEMS.add(cacheKey); TRANSLATED_PROVIDER_CACHE.put(cacheKey, ModConfig.TranslationProvider.GOOGLE);
                triggerTranslation(tooltip, cacheKey, ModConfig.TranslationProvider.GOOGLE);
            }
            TOGGLED_THIS_TICK.add(cacheKey + "_G");
        } else if (geminiJustPressed && config.enableGeminiItem && !TOGGLED_THIS_TICK.contains(cacheKey + "_X")) {
            if (isAlreadyTranslated && currentProvider == ModConfig.TranslationProvider.GEMINI) {
                ACTIVE_TRANSLATED_ITEMS.remove(cacheKey); REVERTED_ITEMS.add(cacheKey);
                TRANSLATED_PROVIDER_CACHE.remove(cacheKey); ITEM_CACHE.remove(cacheKey);
            } else {
                ITEM_CACHE.remove(cacheKey); PENDING_ITEMS.remove(cacheKey); REVERTED_ITEMS.remove(cacheKey);
                ACTIVE_TRANSLATED_ITEMS.add(cacheKey); TRANSLATED_PROVIDER_CACHE.put(cacheKey, ModConfig.TranslationProvider.GEMINI);
                triggerTranslation(tooltip, cacheKey, ModConfig.TranslationProvider.GEMINI);
            }
        }
        if (groqJustPressed && config.enableGroqItem && !TOGGLED_THIS_TICK.contains(cacheKey + "_C")) {
            TOGGLED_THIS_TICK.add(cacheKey + "_C");
            if (isAlreadyTranslated && currentProvider == ModConfig.TranslationProvider.GROQ) {
                ACTIVE_TRANSLATED_ITEMS.remove(cacheKey); REVERTED_ITEMS.add(cacheKey);
                TRANSLATED_PROVIDER_CACHE.remove(cacheKey); ITEM_CACHE.remove(cacheKey);
            } else {
                ITEM_CACHE.remove(cacheKey); PENDING_ITEMS.remove(cacheKey); REVERTED_ITEMS.remove(cacheKey);
                ACTIVE_TRANSLATED_ITEMS.add(cacheKey); TRANSLATED_PROVIDER_CACHE.put(cacheKey, ModConfig.TranslationProvider.GROQ);
                triggerTranslation(tooltip, cacheKey, ModConfig.TranslationProvider.GROQ);
            }
        }
        if (mistralJustPressed && config.enableMistralItem && !TOGGLED_THIS_TICK.contains(cacheKey + "_M")) {
            TOGGLED_THIS_TICK.add(cacheKey + "_M");
            if (isAlreadyTranslated && currentProvider == ModConfig.TranslationProvider.MISTRAL) {
                ACTIVE_TRANSLATED_ITEMS.remove(cacheKey); REVERTED_ITEMS.add(cacheKey);
                TRANSLATED_PROVIDER_CACHE.remove(cacheKey); ITEM_CACHE.remove(cacheKey);
            } else {
                ITEM_CACHE.remove(cacheKey); PENDING_ITEMS.remove(cacheKey); REVERTED_ITEMS.remove(cacheKey);
                ACTIVE_TRANSLATED_ITEMS.add(cacheKey); TRANSLATED_PROVIDER_CACHE.put(cacheKey, ModConfig.TranslationProvider.MISTRAL);
                triggerTranslation(tooltip, cacheKey, ModConfig.TranslationProvider.MISTRAL);
            }
        }
        if (openRouterJustPressed && config.enableOpenRouterItem && !TOGGLED_THIS_TICK.contains(cacheKey + "_O")) {
            TOGGLED_THIS_TICK.add(cacheKey + "_O");
            if (isAlreadyTranslated && currentProvider == ModConfig.TranslationProvider.OPENROUTER) {
                ACTIVE_TRANSLATED_ITEMS.remove(cacheKey); REVERTED_ITEMS.add(cacheKey);
                TRANSLATED_PROVIDER_CACHE.remove(cacheKey); ITEM_CACHE.remove(cacheKey);
            } else {
                ITEM_CACHE.remove(cacheKey); PENDING_ITEMS.remove(cacheKey); REVERTED_ITEMS.remove(cacheKey);
                ACTIVE_TRANSLATED_ITEMS.add(cacheKey); TRANSLATED_PROVIDER_CACHE.put(cacheKey, ModConfig.TranslationProvider.OPENROUTER);
                triggerTranslation(tooltip, cacheKey, ModConfig.TranslationProvider.OPENROUTER);
            }
        }
        if (revertJustPressed && !TOGGLED_THIS_TICK.contains(cacheKey + "_R")) {
            TOGGLED_THIS_TICK.add(cacheKey + "_R");
            ACTIVE_TRANSLATED_ITEMS.remove(cacheKey); REVERTED_ITEMS.add(cacheKey);
            TRANSLATED_PROVIDER_CACHE.remove(cacheKey); ITEM_CACHE.remove(cacheKey);
        }

        boolean isTranslated = ACTIVE_TRANSLATED_ITEMS.contains(cacheKey) && ITEM_CACHE.containsKey(cacheKey);
        // Check if there's an API error message stored
        boolean hasError = !ACTIVE_TRANSLATED_ITEMS.contains(cacheKey) && ITEM_CACHE.containsKey(cacheKey) 
                           && ITEM_CACHE.get(cacheKey).stream().anyMatch(t -> t.getString().startsWith("⚠"));
        
        if (isTranslated) {
            ModConfig.TranslationProvider provider = TRANSLATED_PROVIDER_CACHE.getOrDefault(cacheKey, ModConfig.TranslationProvider.GOOGLE);
            
            // "tikli olmayan item altında yazısı çıkmasın google tranlate dahil"
            boolean providerEnabled = false;
            switch(provider) {
                case GOOGLE: providerEnabled = config.enableGoogleItem; break;
                case GEMINI: providerEnabled = config.enableGeminiItem; break;
                case GROQ: providerEnabled = config.enableGroqItem; break;
                case MISTRAL: providerEnabled = config.enableMistralItem; break;
                case OPENROUTER: providerEnabled = config.enableOpenRouterItem; break;
            }

            if (providerEnabled) {
                List<Text> translated = ITEM_CACHE.get(cacheKey);
                try {
                    tooltip.clear();
                    tooltip.addAll(translated);
                    tooltip.add(Text.empty());
                    tooltip.add(Text.translatable(
                        provider == ModConfig.TranslationProvider.GOOGLE ? "npctranslator.tooltip.translated.google" :
                        provider == ModConfig.TranslationProvider.GEMINI ? "npctranslator.tooltip.translated.gemini" :
                        provider == ModConfig.TranslationProvider.MISTRAL ? "npctranslator.tooltip.translated.mistral" :
                        provider == ModConfig.TranslationProvider.OPENROUTER ? "npctranslator.tooltip.translated.openrouter" :
                        "npctranslator.tooltip.translated.groq"
                    ).formatted(Formatting.GRAY, Formatting.ITALIC));
                } catch (Exception ignored) {}
            }
        } else if (hasError) {
            // Show the error message stored in cache
            try {
                tooltip.add(Text.empty());
                ITEM_CACHE.get(cacheKey).forEach(tooltip::add);
            } catch (Exception ignored) {}
        } else if (config.autoTranslateItems && !REVERTED_ITEMS.contains(cacheKey) && !ACTIVE_TRANSLATED_ITEMS.contains(cacheKey)) {
            // Auto translate items logic
            ACTIVE_TRANSLATED_ITEMS.add(cacheKey);
            TRANSLATED_PROVIDER_CACHE.put(cacheKey, ModConfig.TranslationProvider.GOOGLE);
            triggerTranslation(tooltip, cacheKey, ModConfig.TranslationProvider.GOOGLE);
        }

        renderShortcutsFooter(tooltip, isTranslated, cacheKey);
    }

    private static void renderShortcutsFooter(List<Text> tooltip, boolean isTranslated, String cacheKey) {
        try {
            if (!isTranslated) tooltip.add(Text.empty());
            
            ModConfig.TranslationProvider currentProvider = TRANSLATED_PROVIDER_CACHE.get(cacheKey);
            
            if (config.enableGoogleItem && currentProvider != ModConfig.TranslationProvider.GOOGLE) {
                String k = getKeyName(keyTranslateGoogle, "G");
                tooltip.add(Text.translatable("npctranslator.tooltip.translate.google", k).formatted(Formatting.DARK_GRAY));
            }
            if (config.enableGeminiItem && currentProvider != ModConfig.TranslationProvider.GEMINI) {
                String k = getKeyName(keyTranslateGemini, "X");
                tooltip.add(Text.translatable("npctranslator.tooltip.translate.gemini", k).formatted(Formatting.DARK_GRAY));
            }
            if (config.enableGroqItem && currentProvider != ModConfig.TranslationProvider.GROQ) {
                String k = getKeyName(keyTranslateGroq, "C");
                tooltip.add(Text.translatable("npctranslator.tooltip.translate.groq", k).formatted(Formatting.DARK_GRAY));
            }
            if (config.enableMistralItem && currentProvider != ModConfig.TranslationProvider.MISTRAL) {
                String k = getKeyName(keyTranslateMistral, "Yok");
                tooltip.add(Text.translatable("npctranslator.tooltip.translate.mistral", k).formatted(Formatting.DARK_GRAY));
            }
            if (config.enableOpenRouterItem && currentProvider != ModConfig.TranslationProvider.OPENROUTER) {
                String k = getKeyName(keyTranslateOpenRouter, "Yok");
                tooltip.add(Text.translatable("npctranslator.tooltip.translate.openrouter", k).formatted(Formatting.DARK_GRAY));
            }
            
            if (isTranslated) {
                String k = getKeyName(keyRevertTranslation, "V");
                tooltip.add(Text.translatable("npctranslator.tooltip.revert", k).formatted(Formatting.DARK_GRAY));
            }
        } catch (Exception ignored) {}
    }

    private static String getKeyName(KeyBinding bind, String def) {
        String n = bind.getBoundKeyLocalizedText().getString().toUpperCase();
        if (n.isEmpty() || n.equals("-")) return def;
        return n;
    }

    private static void triggerTranslation(List<Text> tooltip, String cacheKey, ModConfig.TranslationProvider provider) {
        if (PENDING_ITEMS.contains(cacheKey)) return;
        if (provider == ModConfig.TranslationProvider.GROQ && (config.groqApiKey == null || config.groqApiKey.isEmpty())) {
            ACTIVE_TRANSLATED_ITEMS.remove(cacheKey);
            ITEM_CACHE.put(cacheKey + "_API_ERROR", List.of(Text.literal("⚠ API KONTROL EDİN").formatted(Formatting.RED, Formatting.BOLD)));
            ITEM_CACHE.put(cacheKey, List.of(Text.literal("⚠ Groq API Anahtarı Girilmemiş!").formatted(Formatting.RED, Formatting.BOLD)));
            return;
        }
        if (provider == ModConfig.TranslationProvider.GEMINI && (config.geminiApiKey == null || config.geminiApiKey.isEmpty())) {
            ACTIVE_TRANSLATED_ITEMS.remove(cacheKey);
            ITEM_CACHE.put(cacheKey, List.of(Text.literal("⚠ Gemini API Anahtarı Girilmemiş!").formatted(Formatting.RED, Formatting.BOLD)));
            return;
        }

        PENDING_ITEMS.add(cacheKey);
        
        StringBuilder fullTooltip = new StringBuilder();
        for (Text line : tooltip) {
            fullTooltip.append(toFormattedString(line)).append("\n");
        }

        String targetLang = config.useGameLanguage ? clientLanguage() : config.targetLanguage.englishName;
        String langCode = config.useGameLanguage ? (clientLanguage().contains("_") ? clientLanguage().split("_")[0] : "tr") : config.targetLanguage.code;

        CompletableFuture.runAsync(() -> {
            try {
                String translatedContent = getTranslatedText(fullTooltip.toString(), langCode, targetLang, true, provider);
                String[] lines = translatedContent.split("\n");
                List<Text> translatedTooltip = new ArrayList<>();
                for (String line : lines) {
                    if (!line.trim().isEmpty()) {
                        translatedTooltip.add(parseColorCodes(line.trim()));
                    }
                }
                if (!translatedTooltip.isEmpty()) {
                    ITEM_CACHE.put(cacheKey, translatedTooltip);
                }
            } catch (Exception e) {
                e.printStackTrace();
                String providerName = provider == ModConfig.TranslationProvider.GEMINI ? "Gemini" : provider == ModConfig.TranslationProvider.GROQ ? "Groq" : "Google";
                boolean isRateLimit = e.getMessage() != null && e.getMessage().contains("429");
                String errorMsgKey = e.getMessage() != null && e.getMessage().contains("401") ? "npctranslator.error.api_key_invalid" :
                                  isRateLimit ? "npctranslator.error.api_limit_exceeded" :
                                  "npctranslator.error.api_general";
                ITEM_CACHE.put(cacheKey, List.of(Text.translatable(errorMsgKey, providerName).formatted(Formatting.RED, Formatting.BOLD)));
                ACTIVE_TRANSLATED_ITEMS.remove(cacheKey);
            } finally {
                PENDING_ITEMS.remove(cacheKey);
            }
        });
    }

    private static String toFormattedString(Text text) {
        StringBuilder sb = new StringBuilder();
        text.visit((style, string) -> {
            TextColor color = style.getColor();
            if (color != null) {
                Formatting format = Formatting.byName(color.getName());
                if (format != null) {
                    sb.append("§").append(format.getCode());
                }
            }
            if (style.isBold()) sb.append("§l");
            if (style.isItalic()) sb.append("§o");
            if (style.isUnderlined()) sb.append("§n");
            if (style.isStrikethrough()) sb.append("§m");
            if (style.isObfuscated()) sb.append("§k");
            sb.append(string);
            return Optional.empty();
        }, Style.EMPTY);
        return sb.toString();
    }

    private static Text parseColorCodes(String text) {
        MutableText root = Text.empty();
        String[] parts = text.split("§");
        if (parts.length == 0) return Text.literal(text);
        root.append(Text.literal(parts[0]));
        List<Formatting> activeFormats = new ArrayList<>();
        for (int i = 1; i < parts.length; i++) {
            String part = parts[i];
            if (part.isEmpty()) continue;
            char code = part.charAt(0);
            Formatting format = Formatting.byCode(code);
            if (format != null) {
                if (format.isColor() || format == Formatting.RESET) activeFormats.clear();
                if (format != Formatting.RESET) activeFormats.add(format);
                if (part.length() > 1) {
                    MutableText t = Text.literal(part.substring(1));
                    for (Formatting f : activeFormats) t.formatted(f);
                    root.append(t);
                }
            } else {
                root.append(Text.literal("§" + part));
            }
        }
        return root;
    }
    
    private static String clientLanguage() {
        MinecraftClient client = MinecraftClient.getInstance();
        return (client != null && client.options != null) ? client.options.language : "en_us";
    }
}
