package com.npctranslator;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.npctranslator.config.ModConfig;
import com.npctranslator.mixin.ChatHudAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.ChatFormatting;
// Removed ResourceLocation import
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
    public static KeyMapping keyTranslateGoogle;
    public static KeyMapping keyTranslateGemini;
    public static KeyMapping keyTranslateGroq;
    public static KeyMapping keyTranslateMistral;
    public static KeyMapping keyTranslateOpenRouter;
    public static KeyMapping keyRevertTranslation;
    public static KeyMapping menuKey;

    private static final Map<String, ModConfig.TranslationProvider> TRANSLATED_PROVIDER_CACHE = new ConcurrentHashMap<>();
    
    public static final Map<String, List<Component>> ITEM_CACHE = new ConcurrentHashMap<>();
    private static final Set<String> PENDING_ITEMS = Collections.newSetFromMap(new ConcurrentHashMap<>());
    // WeakHashMap to track tooltips processed by ItemStackMixin without modifying them
    public static final java.util.Map<List<Component>, Boolean> PROCESSED_TOOLTIPS = new java.util.WeakHashMap<>();
    private static final Map<String, Component> ORIGINAL_MESSAGES = new ConcurrentHashMap<>();
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
    private static long animationTick = 0;

    private static ChatComponent getChatComponent(Minecraft client) {
        try {
            // Try 26.1
            java.lang.reflect.Method getChat = client.gui.getClass().getMethod("getChat");
            return (ChatComponent) getChat.invoke(client.gui);
        } catch (Exception e) {
            try {
                // Try 26.2
                java.lang.reflect.Field hudField = client.gui.getClass().getField("hud");
                Object hud = hudField.get(client.gui);
                java.lang.reflect.Method getChat = hud.getClass().getMethod("getChat");
                return (ChatComponent) getChat.invoke(hud);
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }
    }

    public static void setScreen(Minecraft client, Screen screen) {
        try {
            // Try 1.21.11 and 26.1
            java.lang.reflect.Method setScreenMethod = client.getClass().getMethod("setScreen", Screen.class);
            setScreenMethod.invoke(client, screen);
        } catch (Exception e) {
            try {
                // Try 26.2
                java.lang.reflect.Method setScreenMethod = client.gui.getClass().getMethod("setScreen", Screen.class);
                setScreenMethod.invoke(client.gui, screen);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static Screen getScreen(Minecraft client) {
        try {
            // Try 1.21.11 and 26.1
            java.lang.reflect.Field screenField = client.getClass().getField("screen");
            return (Screen) screenField.get(client);
        } catch (Exception e) {
            try {
                // Try 26.2
                java.lang.reflect.Method screenMethod = client.gui.getClass().getMethod("screen");
                return (Screen) screenMethod.invoke(client.gui);
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }
    }

    private static boolean isKeyCurrentlyDown(KeyMapping keyMapping) {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.getWindow() == null) return keyMapping.isDown();
        try {
            com.mojang.blaze3d.platform.InputConstants.Key key = com.mojang.blaze3d.platform.InputConstants.getKey(keyMapping.saveString());
            int keyCode = key.getValue();
            if (keyCode >= 0) {
                if (keyMapping.saveString().startsWith("key.mouse.")) {
                    return org.lwjgl.glfw.GLFW.glfwGetMouseButton(client.getWindow().handle(), keyCode) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
                } else {
                    return org.lwjgl.glfw.GLFW.glfwGetKey(client.getWindow().handle(), keyCode) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
                }
            }
        } catch (Exception e) {}
        return keyMapping.isDown();
    }

    @Override
    public void onInitializeClient() {
        TranslationDictionary.load();
        ModConfig.load();
        config = ModConfig.INSTANCE;

        net.minecraft.client.KeyMapping.Category customCategory = net.minecraft.client.KeyMapping.Category.MISC;
        try {
            // Try all possible names for ResourceLocation/Identifier depending on mapping (Fabric vs NeoForge/Mojmap)
            String[] classNames = { "net.minecraft.resources.Identifier", "net.minecraft.resources.ResourceLocation", "net.minecraft.util.Identifier" };
            Class<?> idClass = null;
            for (String name : classNames) {
                try {
                    idClass = Class.forName(name);
                    break;
                } catch (Exception ignored) {}
            }
            
            if (idClass != null) {
                // Try parse or fromNamespaceAndPath
                Object idObj = null;
                try {
                    idObj = idClass.getMethod("parse", String.class).invoke(null, "npctranslator:keys");
                } catch (Exception e1) {
                    try {
                        idObj = idClass.getMethod("fromNamespaceAndPath", String.class, String.class).invoke(null, "npctranslator", "keys");
                    } catch (Exception e2) {}
                }
                
                if (idObj != null) {
                    // Try to register (Fabric API)
                    try {
                        java.lang.reflect.Method regMethod = net.minecraft.client.KeyMapping.Category.class.getMethod("register", idClass);
                        customCategory = (net.minecraft.client.KeyMapping.Category) regMethod.invoke(null, idObj);
                    } catch (Exception e3) {
                        // Try NeoForge constructor
                        try {
                            java.lang.reflect.Constructor<?> constructor = net.minecraft.client.KeyMapping.Category.class.getConstructor(idClass);
                            customCategory = (net.minecraft.client.KeyMapping.Category) constructor.newInstance(idObj);
                        } catch (Exception e4) {
                            try {
                                java.lang.reflect.Constructor<?> constructor = net.minecraft.client.KeyMapping.Category.class.getConstructor(String.class);
                                customCategory = (net.minecraft.client.KeyMapping.Category) constructor.newInstance("key.category.npctranslator.keys");
                            } catch (Exception ignored) {}
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        final net.minecraft.client.KeyMapping.Category finalCategory = customCategory;

        keyTranslateGoogle = new KeyMapping(
                "key.npctranslator.translate.google",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                finalCategory
        );
        KeyMappingHelper.registerKeyMapping(keyTranslateGoogle);

        keyTranslateGemini = new KeyMapping(
                "key.npctranslator.translate.gemini",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_X,
                finalCategory
        );
        KeyMappingHelper.registerKeyMapping(keyTranslateGemini);

        keyTranslateGroq = new KeyMapping(
                "key.npctranslator.translate.groq",
                com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,
                org.lwjgl.glfw.GLFW.GLFW_KEY_C,
                finalCategory
        );
        KeyMappingHelper.registerKeyMapping(keyTranslateGroq);

        keyTranslateMistral = new KeyMapping(
                "key.npctranslator.translate.mistral",
                com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,
                org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN,
                finalCategory
        );
        KeyMappingHelper.registerKeyMapping(keyTranslateMistral);

        keyTranslateOpenRouter = new KeyMapping(
                "key.npctranslator.translate.openrouter",
                com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,
                org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN,
                finalCategory
        );
        KeyMappingHelper.registerKeyMapping(keyTranslateOpenRouter);

        keyRevertTranslation = new KeyMapping(
                "key.npctranslator.translate.revert",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                finalCategory
        );
        KeyMappingHelper.registerKeyMapping(keyRevertTranslation);
        
        menuKey = new KeyMapping(
                "key.npctranslator.menu",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_Z,
                finalCategory
        );
        KeyMappingHelper.registerKeyMapping(menuKey);

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
                return Component.empty().append(text.copy()).append(Component.literal("\u200B").withStyle(s -> s.withClickEvent(new ClickEvent.RunCommand("/translate_npc " + msgId))));
            }

            MutableComponent originalMessage = text.copy();
            MutableComponent translateButton = Component.translatable("npctranslator.button")
                    .append(" ")
                    .withStyle(style -> style.withColor(ChatFormatting.AQUA)
                            .withClickEvent(new ClickEvent.RunCommand("/translate_npc " + msgId))
                            .withHoverEvent(new HoverEvent.ShowText(Component.translatable("npctranslator.hover"))));
            return Component.empty().append(translateButton).append(originalMessage);
        });

        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(client -> {
            boolean pGoogle = isKeyCurrentlyDown(keyTranslateGoogle);
            googleJustPressed = pGoogle && !lastGoogleState;
            lastGoogleState = pGoogle;

            boolean pGemini = isKeyCurrentlyDown(keyTranslateGemini);
            geminiJustPressed = pGemini && !lastGeminiState;
            lastGeminiState = pGemini;

            boolean pGroq = isKeyCurrentlyDown(keyTranslateGroq);
            groqJustPressed = pGroq && !lastGroqState;
            lastGroqState = pGroq;

            boolean pMistral = isKeyCurrentlyDown(keyTranslateMistral);
            mistralJustPressed = pMistral && !lastMistralState;
            lastMistralState = pMistral;

            boolean pOpenRouter = isKeyCurrentlyDown(keyTranslateOpenRouter);
            openRouterJustPressed = pOpenRouter && !lastOpenRouterState;
            lastOpenRouterState = pOpenRouter;

            boolean pRevert = isKeyCurrentlyDown(keyRevertTranslation);
            revertJustPressed = pRevert && !lastRevertState;
            lastRevertState = pRevert;

            TOGGLED_THIS_TICK.clear();
            animationTick++;
            
            while (menuKey.consumeClick()) {
                openSettings();
            }
        });

        net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (!config.hasSeenWelcomeScreen) {
            client.execute(() -> {
                    setScreen(client, new com.npctranslator.gui.WelcomeScreen());
                });
            }
        });

        // Oyun kapanınca (sunucudan ayrılınca) sözlüğü sil
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            TranslationDictionary.clear();
            ITEM_CACHE.clear();
            PENDING_ITEMS.clear();
            ACTIVE_TRANSLATED_ITEMS.clear();
            REVERTED_ITEMS.clear();
        });

        net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register((com.mojang.brigadier.builder.LiteralArgumentBuilder) com.mojang.brigadier.builder.LiteralArgumentBuilder.literal("translate")
                .executes(context -> {
                    openSettings();
                    return 1;
                })
                .then(com.mojang.brigadier.builder.LiteralArgumentBuilder.literal("DeleteDict")
                    .executes(context -> {
                        int count = TranslationDictionary.size();
                        TranslationDictionary.clear();
                        ITEM_CACHE.clear();
                        Minecraft.getInstance().player.sendSystemMessage(Component.literal("§a✅ Sözlük silindi! (" + count + " kayıt temizlendi)"));
                        return 1;
                    })
                )
                .then(com.mojang.brigadier.builder.LiteralArgumentBuilder.literal("Delete")
                    .then(com.mojang.brigadier.builder.LiteralArgumentBuilder.literal("dictionary")
                        .executes(context -> {
                            int count = TranslationDictionary.size();
                            TranslationDictionary.clear();
                            ITEM_CACHE.clear();
                            Minecraft.getInstance().player.sendSystemMessage(Component.literal("§a✅ Sözlük silindi! (" + count + " kayıt temizlendi)"));
                            return 1;
                        })
                    )
                )
            );
        });
    }



    public static boolean isItemTranslationEnabled() {
        return config != null && config.enabled;
    }

    public static void setApiKey(String apiKey) {
        Minecraft client = Minecraft.getInstance();
        config.groqApiKey = apiKey;
        ModConfig.save();
        client.execute(() -> {
            ChatComponent chat = getChatComponent(client);
            if (chat != null) chat.addClientSystemMessage(
                    Component.translatable("npctranslator.key_saved").withStyle(ChatFormatting.GREEN));
        });
    }

    public static void openSettings() {
        Minecraft client = Minecraft.getInstance();
        if (client != null) {
            client.execute(() -> {
                Screen screen = new com.npctranslator.gui.HubScreen();
                setScreen(client, screen);
            });
        }
    }

    public static void reloadConfig() {
        config = ModConfig.INSTANCE;
    }

    private static void replaceMessageInChat(Minecraft client, String searchMarker, Component newMessage) {
        ChatComponent chatHud = getChatComponent(client);
        if (chatHud == null) return;
        ChatHudAccessor accessor = (ChatHudAccessor) chatHud;
        List<GuiMessage> messages = accessor.getMessages();
        // First try exact marker match (for unique ID commands embedded in click events)
        for (int i = 0; i < messages.size(); i++) {
            GuiMessage line = messages.get(i);
            String fullStyled = extractClickCommands(line.content());
            if (fullStyled.contains(searchMarker)) {
                GuiMessage newLine = new GuiMessage(line.addedTime(), newMessage, null, null, null);
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
            GuiMessage line = messages.get(i);
            String lineText = line.content().getString();
            if (lineText.contains(searchText) || searchText.contains(lineText.trim())) {
                GuiMessage newLine = new GuiMessage(line.addedTime(), newMessage, null, null, null);
                messages.set(i, newLine);
                int scroll = accessor.getScrolledLines();
                accessor.invokeRefresh();
                accessor.setScrolledLines(scroll);
                return;
            }
        }
    }

    private static String extractClickCommands(Component text) {
        StringBuilder sb = new StringBuilder();
        if (text.getStyle() != null && text.getStyle().getClickEvent() instanceof ClickEvent.RunCommand runCmd) {
            sb.append(runCmd.command());
        }
        for (Component sibling : text.getSiblings()) {
            sb.append(extractClickCommands(sibling));
        }
        return sb.toString();
    }

    // Hardcoded § code -> ChatFormatting map (avoids byCode() which may not exist in all MC versions)
    private static final java.util.Map<Character, ChatFormatting> CODE_TO_FORMAT = new java.util.HashMap<>();
    // Color RGB -> § code map for textToFormattedString
    private static final java.util.Map<Integer, Character> COLOR_TO_CODE = new java.util.HashMap<>();
    static {
        CODE_TO_FORMAT.put('0', ChatFormatting.BLACK);
        CODE_TO_FORMAT.put('1', ChatFormatting.DARK_BLUE);
        CODE_TO_FORMAT.put('2', ChatFormatting.DARK_GREEN);
        CODE_TO_FORMAT.put('3', ChatFormatting.DARK_AQUA);
        CODE_TO_FORMAT.put('4', ChatFormatting.DARK_RED);
        CODE_TO_FORMAT.put('5', ChatFormatting.DARK_PURPLE);
        CODE_TO_FORMAT.put('6', ChatFormatting.GOLD);
        CODE_TO_FORMAT.put('7', ChatFormatting.GRAY);
        CODE_TO_FORMAT.put('8', ChatFormatting.DARK_GRAY);
        CODE_TO_FORMAT.put('9', ChatFormatting.BLUE);
        CODE_TO_FORMAT.put('a', ChatFormatting.GREEN);
        CODE_TO_FORMAT.put('b', ChatFormatting.AQUA);
        CODE_TO_FORMAT.put('c', ChatFormatting.RED);
        CODE_TO_FORMAT.put('d', ChatFormatting.LIGHT_PURPLE);
        CODE_TO_FORMAT.put('e', ChatFormatting.YELLOW);
        CODE_TO_FORMAT.put('f', ChatFormatting.WHITE);
        CODE_TO_FORMAT.put('k', ChatFormatting.OBFUSCATED);
        CODE_TO_FORMAT.put('l', ChatFormatting.BOLD);
        CODE_TO_FORMAT.put('m', ChatFormatting.STRIKETHROUGH);
        CODE_TO_FORMAT.put('n', ChatFormatting.UNDERLINE);
        CODE_TO_FORMAT.put('o', ChatFormatting.ITALIC);
        CODE_TO_FORMAT.put('r', ChatFormatting.RESET);
        // Build color code lookup
        int[] colorCodes = {0x000000,0x0000AA,0x00AA00,0x00AAAA,0xAA0000,0xAA00AA,0xFFAA00,0xAAAAAA,0x555555,0x5555FF,0x55FF55,0x55FFFF,0xFF5555,0xFF55FF,0xFFFF55,0xFFFFFF};
        char[] chars =    {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
        for (int i = 0; i < colorCodes.length; i++) COLOR_TO_CODE.put(colorCodes[i], chars[i]);
    }
    // Color ChatFormatting set for quick lookup
    private static final java.util.Set<ChatFormatting> COLOR_FORMATS = new java.util.HashSet<>(java.util.Arrays.asList(
        ChatFormatting.BLACK, ChatFormatting.DARK_BLUE, ChatFormatting.DARK_GREEN, ChatFormatting.DARK_AQUA,
        ChatFormatting.DARK_RED, ChatFormatting.DARK_PURPLE, ChatFormatting.GOLD, ChatFormatting.GRAY,
        ChatFormatting.DARK_GRAY, ChatFormatting.BLUE, ChatFormatting.GREEN, ChatFormatting.AQUA,
        ChatFormatting.RED, ChatFormatting.LIGHT_PURPLE, ChatFormatting.YELLOW, ChatFormatting.WHITE
    ));

    private static String textToFormattedString(Component text) {
        StringBuilder sb = new StringBuilder();
        text.visit((style, string) -> {
            // Apply color
            if (style != null && style.getColor() != null) {
                int rgb = style.getColor().getValue() & 0xFFFFFF;
                Character c = COLOR_TO_CODE.get(rgb);
                if (c != null) sb.append('\u00A7').append(c);
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
        }, net.minecraft.network.chat.Style.EMPTY);
        return sb.toString();
    }

    private static MutableComponent formattedStringToText(String formatted) {
        MutableComponent result = Component.empty();
        StringBuilder current = new StringBuilder();
        ChatFormatting currentColor = null;
        boolean bold = false, italic = false, underline = false, strikethrough = false, obfuscated = false;

        for (int i = 0; i < formatted.length(); i++) {
            if (formatted.charAt(i) == '\u00A7' && i + 1 < formatted.length()) {
                // Flush current segment
                if (current.length() > 0) {
                    MutableComponent segment = Component.literal(current.toString());
                    applyStyle(segment, currentColor, bold, italic, underline, strikethrough, obfuscated);
                    result.append(segment);
                    current.setLength(0);
                }
                char code = Character.toLowerCase(formatted.charAt(i + 1));
                ChatFormatting fmt = CODE_TO_FORMAT.get(code);
                if (fmt != null) {
                    if (COLOR_FORMATS.contains(fmt)) {
                        currentColor = fmt;
                        bold = false; italic = false; underline = false; strikethrough = false; obfuscated = false;
                    } else if (fmt == ChatFormatting.RESET) {
                        currentColor = null;
                        bold = false; italic = false; underline = false; strikethrough = false; obfuscated = false;
                    } else if (fmt == ChatFormatting.BOLD) bold = true;
                    else if (fmt == ChatFormatting.ITALIC) italic = true;
                    else if (fmt == ChatFormatting.UNDERLINE) underline = true;
                    else if (fmt == ChatFormatting.STRIKETHROUGH) strikethrough = true;
                    else if (fmt == ChatFormatting.OBFUSCATED) obfuscated = true;
                }
                i++; // skip code char
            } else {
                current.append(formatted.charAt(i));
            }
        }
        // Flush remaining
        if (current.length() > 0) {
            MutableComponent segment = Component.literal(current.toString());
            applyStyle(segment, currentColor, bold, italic, underline, strikethrough, obfuscated);
            result.append(segment);
        }
        return result;
    }

    private static void applyStyle(MutableComponent text, ChatFormatting color, boolean bold, boolean italic, boolean underline, boolean strikethrough, boolean obfuscated) {
        Style s = Style.EMPTY;
        if (color != null) s = s.applyFormat(color);
        if (bold) s = s.applyFormat(ChatFormatting.BOLD);
        if (italic) s = s.applyFormat(ChatFormatting.ITALIC);
        if (underline) s = s.applyFormat(ChatFormatting.UNDERLINE);
        if (strikethrough) s = s.applyFormat(ChatFormatting.STRIKETHROUGH);
        if (obfuscated) s = s.applyFormat(ChatFormatting.OBFUSCATED);
        text.withStyle(s);
    }

    public static void handleTranslation(String msgId) {
        reloadConfig();
        Minecraft client = Minecraft.getInstance();

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
            client.execute(() -> { ChatComponent c = getChatComponent(client); if(c!=null) c.addClientSystemMessage(Component.translatable("npctranslator.error.config").withStyle(ChatFormatting.RED)); });
            return;
        }
        if (config.chatTranslationProvider == ModConfig.TranslationProvider.GEMINI && (config.geminiApiKey == null || config.geminiApiKey.isEmpty())) {
            client.execute(() -> { ChatComponent c = getChatComponent(client); if(c!=null) c.addClientSystemMessage(Component.translatable("npctranslator.error.config").withStyle(ChatFormatting.RED)); });
            return;
        }

        String targetLangName;
        String displayLanguageCode;
        if (config.useGameLanguage) {
            String userLanguage = client.options.languageCode;
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
                Component originalMsg = ORIGINAL_MESSAGES.get(msgId);
                if (originalMsg != null) {
                    tempFormattedInput = textToFormattedString(originalMsg);
                    // Extract click event
                    if (originalMsg.getStyle() != null && originalMsg.getStyle().getClickEvent() != null) {
                        tempClickEvent = originalMsg.getStyle().getClickEvent();
                    } else {
                        for (Component sibling : originalMsg.getSiblings()) {
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

                MutableComponent prefix = Component.translatable("npctranslator.translated", displayLanguageCode.toUpperCase())
                    .withStyle(style -> style.withColor(ChatFormatting.GREEN)
                        .withClickEvent(new ClickEvent.RunCommand("/revert_npc " + msgId))
                        .withHoverEvent(new HoverEvent.ShowText(Component.translatable("npctranslator.hover_revert"))));

                // Parse §-coded translated text back into colored Text
                MutableComponent translationContent = formattedStringToText(translatedText);
                if (finalClickEvent != null) {
                    translationContent.withStyle(s -> s.withClickEvent(finalClickEvent));
                }

                MutableComponent translatedMessage = Component.empty().append(prefix).append(translationContent);
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
                    systemMessage.addProperty("content", "Translate Minecraft item tooltip to " + langName + ". CRITICAL: Keep all Minecraft § color codes. You will receive lines prefixed with 'ID|'. You MUST return each translated line prefixed with the EXACT SAME 'ID|'. Output ONLY the translated lines.");
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
                partsObjSys.addProperty("text", "Translate Minecraft item tooltip to " + langName + ". CRITICAL: Keep all Minecraft § color codes. You will receive lines prefixed with 'ID|'. You MUST return each translated line prefixed with the EXACT SAME 'ID|'. Output ONLY the translated lines.");
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

            JsonArray safetySettings = new JsonArray();
            String[] categories = {"HARM_CATEGORY_HARASSMENT", "HARM_CATEGORY_HATE_SPEECH", "HARM_CATEGORY_SEXUALLY_EXPLICIT", "HARM_CATEGORY_DANGEROUS_CONTENT"};
            for (String cat : categories) {
                JsonObject setting = new JsonObject();
                setting.addProperty("category", cat);
                setting.addProperty("threshold", "BLOCK_NONE");
                safetySettings.add(setting);
            }
            jsonBody.add("safetySettings", safetySettings);

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
        Minecraft client = Minecraft.getInstance();
        try {
            Component originalMessage = ORIGINAL_MESSAGES.get(msgId);
            if (originalMessage == null) {
                // Fallback
                String base64 = MESSAGE_ID_TO_BASE64.get(msgId);
                if (base64 != null) {
                    String originalText = new String(Base64.getDecoder().decode(base64), java.nio.charset.StandardCharsets.UTF_8);
                    originalMessage = Component.literal(originalText);
                } else {
                    return;
                }
            }

            MutableComponent translateButton = Component.translatable("npctranslator.button")
                    .append(" ")
                    .withStyle(style -> style.withColor(ChatFormatting.AQUA)
                            .withClickEvent(new ClickEvent.RunCommand("/translate_npc " + msgId))
                            .withHoverEvent(new HoverEvent.ShowText(Component.translatable("npctranslator.hover"))));

            Component finalMessage = originalMessage;
            MutableComponent fullOriginal = Component.empty().append(translateButton).append(finalMessage.copy());
            String searchMarker = "revert_npc " + msgId;
            client.execute(() -> replaceMessageInChat(client, searchMarker, fullOriginal));

        } catch (Exception e) {}
    }

    public static void handleItemTooltip(ItemStack stack, List<Component> tooltip) {
        if (!config.enabled) return;
        Minecraft client = Minecraft.getInstance();
        if (client == null) return;
        
        String itemKey = stack.getItem().toString() + "_" + (stack.getComponents() != null ? stack.getComponents().hashCode() : 0);
        String lang = config.useGameLanguage ? clientLanguage() : config.targetLanguage.code;
        String cacheKey = itemKey + "_" + lang;

        handleTooltipStateAndRender(tooltip, cacheKey);
    }

    public static void handleGeneralTooltip(List<Component> tooltip) {
        if (!config.enabled) return;
        if (tooltip.isEmpty()) return;

        // Skip if already handled by ItemStack tooltip mapping (tracked in memory)
        if (PROCESSED_TOOLTIPS.containsKey(tooltip)) return;

        for (Component t : tooltip) {
            String str = t.getString();
            if (str.contains("✅") || str.contains("evirmek") || str.contains("Google ile") || str.contains("Yapay zeka")
                || str.contains("Translated by") || str.contains("to translate with") || str.contains("Press [")) return;
        }

        // Create a cache key from the tooltip content itself since we don't have an ItemStack
        StringBuilder content = new StringBuilder();
        for (Component t : tooltip) content.append(t.getString());
        if (content.length() < 10) return; // Too short to be an item lore usually

        String lang = config.useGameLanguage ? clientLanguage() : config.targetLanguage.code;
        String cacheKey = "gen_" + Integer.toHexString(content.toString().hashCode()) + "_" + lang;

        handleTooltipStateAndRender(tooltip, cacheKey);
    }

    private static void handleTooltipStateAndRender(List<Component> tooltip, String cacheKey) {
        ModConfig.TranslationProvider currentProvider = TRANSLATED_PROVIDER_CACHE.get(cacheKey);
        boolean isAlreadyTranslated = ACTIVE_TRANSLATED_ITEMS.contains(cacheKey) && ITEM_CACHE.containsKey(cacheKey);

        // Toggle off if same key pressed while already translated with that provider
        if (googleJustPressed && config.enableGoogleItem && !TOGGLED_THIS_TICK.contains(cacheKey + "_G")) {
            TOGGLED_THIS_TICK.add(cacheKey + "_G");
            if (isAlreadyTranslated && currentProvider == ModConfig.TranslationProvider.GOOGLE) {
                // Same key pressed → revert
                ACTIVE_TRANSLATED_ITEMS.remove(cacheKey); REVERTED_ITEMS.add(cacheKey);
                TRANSLATED_PROVIDER_CACHE.remove(cacheKey); ITEM_CACHE.remove(cacheKey);
            } else {
                ITEM_CACHE.remove(cacheKey); PENDING_ITEMS.remove(cacheKey); REVERTED_ITEMS.remove(cacheKey);
                ACTIVE_TRANSLATED_ITEMS.add(cacheKey); TRANSLATED_PROVIDER_CACHE.put(cacheKey, ModConfig.TranslationProvider.GOOGLE);
                triggerTranslation(tooltip, cacheKey, ModConfig.TranslationProvider.GOOGLE);
            }
        }
        if (geminiJustPressed && config.enableGeminiItem && !TOGGLED_THIS_TICK.contains(cacheKey + "_X")) {
            TOGGLED_THIS_TICK.add(cacheKey + "_X");
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
                List<Component> translated = ITEM_CACHE.get(cacheKey);
                try {
                    tooltip.clear();
                    tooltip.addAll(translated);
                    tooltip.add(Component.empty());
                    tooltip.add(Component.translatable(
                        provider == ModConfig.TranslationProvider.GOOGLE ? "npctranslator.tooltip.translated.google" :
                        provider == ModConfig.TranslationProvider.GEMINI ? "npctranslator.tooltip.translated.gemini" :
                        provider == ModConfig.TranslationProvider.MISTRAL ? "npctranslator.tooltip.translated.mistral" :
                        provider == ModConfig.TranslationProvider.OPENROUTER ? "npctranslator.tooltip.translated.openrouter" :
                        "npctranslator.tooltip.translated.groq"
                    ).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
                } catch (Exception ignored) {}
            }
        } else if (PENDING_ITEMS.contains(cacheKey)) {
            try {
                // Animated spinner: cycles through frames every ~10 ticks
                String[] spinnerFrames = {"⏳", "⌛"};
                String[] dotFrames = {"", ".", "..", "..."};
                String spinner = spinnerFrames[(int)((animationTick / 10) % spinnerFrames.length)];
                String dots = dotFrames[(int)((animationTick / 8) % dotFrames.length)];
                tooltip.add(Component.empty());
                tooltip.add(Component.literal(spinner + " ")
                    .append(Component.translatable("npctranslator.translating.item"))
                    .append(dots)
                    .withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC));
            } catch (Exception ignored) {}
        } else if (hasError) {
            // Show the error message stored in cache
            try {
                tooltip.add(Component.empty());
                ITEM_CACHE.get(cacheKey).forEach(tooltip::add);
            } catch (Exception ignored) {}
        } else if (config.autoTranslateItems && !REVERTED_ITEMS.contains(cacheKey) && !ACTIVE_TRANSLATED_ITEMS.contains(cacheKey)) {
            // Auto translate items logic
            ACTIVE_TRANSLATED_ITEMS.add(cacheKey);
            TRANSLATED_PROVIDER_CACHE.put(cacheKey, ModConfig.TranslationProvider.GOOGLE);
            triggerTranslation(tooltip, cacheKey, ModConfig.TranslationProvider.GOOGLE);
        }

        renderShortcutsFooter(tooltip, isTranslated, PENDING_ITEMS.contains(cacheKey), cacheKey);
    }

    private static boolean isPendingTooltip(String cacheKey) {
        return PENDING_ITEMS.contains(cacheKey);
    }

    private static void renderShortcutsFooter(List<Component> tooltip, boolean isTranslated, boolean isPending, String cacheKey) {
        try {
            ModConfig.TranslationProvider currentProvider = TRANSLATED_PROVIDER_CACHE.get(cacheKey);

            if (isTranslated) {
                // Only show revert button when already translated
                tooltip.add(Component.empty());
                String k = getKeyName(keyRevertTranslation, "V");
                tooltip.add(Component.translatable("npctranslator.tooltip.revert", k).withStyle(ChatFormatting.DARK_GRAY));
            } else if (!isPending) {
                // Show all translate buttons only when not translated and not loading
                tooltip.add(Component.empty());
                if (config.enableGoogleItem) {
                    String k = getKeyName(keyTranslateGoogle, "G");
                    tooltip.add(Component.translatable("npctranslator.tooltip.translate.google", k).withStyle(ChatFormatting.DARK_GRAY));
                }
                if (config.enableGeminiItem) {
                    String k = getKeyName(keyTranslateGemini, "X");
                    tooltip.add(Component.translatable("npctranslator.tooltip.translate.gemini", k).withStyle(ChatFormatting.DARK_GRAY));
                }
                if (config.enableGroqItem) {
                    String k = getKeyName(keyTranslateGroq, "C");
                    tooltip.add(Component.translatable("npctranslator.tooltip.translate.groq", k).withStyle(ChatFormatting.DARK_GRAY));
                }
                if (config.enableMistralItem) {
                    String k = getKeyName(keyTranslateMistral, "Yok");
                    tooltip.add(Component.translatable("npctranslator.tooltip.translate.mistral", k).withStyle(ChatFormatting.DARK_GRAY));
                }
                if (config.enableOpenRouterItem) {
                    String k = getKeyName(keyTranslateOpenRouter, "Yok");
                    tooltip.add(Component.translatable("npctranslator.tooltip.translate.openrouter", k).withStyle(ChatFormatting.DARK_GRAY));
                }
            }
        } catch (Exception ignored) {}
    }

    private static String getKeyName(KeyMapping bind, String def) {
        try {
            String n = bind.getTranslatedKeyMessage().getString().toUpperCase();
            if (n.isEmpty() || n.equals("-") || n.equals("NONE")) return def;
            return n;
        } catch (Exception e) { return def; }
    }

    private static void triggerTranslation(List<Component> tooltip, String cacheKey, ModConfig.TranslationProvider provider) {
        if (PENDING_ITEMS.contains(cacheKey)) return;
        if (provider == ModConfig.TranslationProvider.GROQ && (config.groqApiKey == null || config.groqApiKey.isEmpty())) {
            ACTIVE_TRANSLATED_ITEMS.remove(cacheKey);
            ITEM_CACHE.put(cacheKey, List.of(Component.literal("⚠ Groq API Anahtarı Girilmemiş!").withStyle(ChatFormatting.RED, ChatFormatting.BOLD)));
            return;
        }
        if (provider == ModConfig.TranslationProvider.GEMINI && (config.geminiApiKey == null || config.geminiApiKey.isEmpty())) {
            ACTIVE_TRANSLATED_ITEMS.remove(cacheKey);
            ITEM_CACHE.put(cacheKey, List.of(Component.literal("⚠ Gemini API Anahtarı Girilmemiş!").withStyle(ChatFormatting.RED, ChatFormatting.BOLD)));
            return;
        }

        
        PENDING_ITEMS.add(cacheKey);

        String targetLang = config.useGameLanguage ? clientLanguage() : config.targetLanguage.englishName;
        String langCode = config.useGameLanguage ? (clientLanguage().contains("_") ? clientLanguage().split("_")[0] : "tr") : config.targetLanguage.code;

        // Create a snapshot of the tooltip on the main thread to avoid ConcurrentModificationException
        // since the main thread might modify the tooltip (e.g. adding shortcuts) while we iterate async.
        List<Component> tooltipSnapshot = new ArrayList<>(tooltip);

        CompletableFuture.runAsync(() -> {
            try {
                List<String> missingLines = new ArrayList<>();
                List<String> allLinesOriginal = new ArrayList<>();
                String providerName = provider.name(); // e.g. "GOOGLE", "GEMINI", "GROQ"
                for (Component line : tooltipSnapshot) {
                    String parsed = toFormattedString(line).trim();
                    if (!parsed.isEmpty()) {
                        allLinesOriginal.add(parsed);
                        if (!TranslationDictionary.has(parsed, providerName)) {
                            missingLines.add(parsed);
                        }
                    }
                }

                if (!missingLines.isEmpty()) {
                    StringBuilder batch = new StringBuilder();
                    for (int i = 0; i < missingLines.size(); i++) {
                        batch.append(i).append("|").append(missingLines.get(i)).append("\n");
                    }
                    
                    String translatedContent = getTranslatedText(batch.toString(), langCode, targetLang, true, provider);
                    String[] transLines = translatedContent.split("\n");
                    
                    for (String tLine : transLines) {
                        int pipeIdx = tLine.indexOf('|');
                        if (pipeIdx != -1) {
                            try {
                                int id = Integer.parseInt(tLine.substring(0, pipeIdx).trim());
                                if (id >= 0 && id < missingLines.size()) {
                                    String translatedText = tLine.substring(pipeIdx + 1).trim();
                                    TranslationDictionary.put(missingLines.get(id), translatedText, providerName);
                                }
                            } catch (Exception e) {}
                        }
                    }
                    TranslationDictionary.save();
                }

                List<Component> translatedTooltip = new ArrayList<>();
                for (String orig : allLinesOriginal) {
                    String tr = TranslationDictionary.get(orig, providerName);
                    if (tr != null && !tr.isEmpty()) {
                        translatedTooltip.add(parseColorCodes(tr));
                    } else {
                        translatedTooltip.add(parseColorCodes(orig));
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
                ITEM_CACHE.put(cacheKey, List.of(Component.translatable(errorMsgKey, providerName).withStyle(Style.EMPTY.withColor(ChatFormatting.RED).withBold(true))));
                ACTIVE_TRANSLATED_ITEMS.remove(cacheKey);
            } finally {
                PENDING_ITEMS.remove(cacheKey);
            }
        });
    }

    private static String toFormattedString(Component text) {
        StringBuilder sb = new StringBuilder();
        text.visit((style, string) -> {
            net.minecraft.network.chat.TextColor color = style.getColor();
            if (color != null) {
                // Use our reverse map: rgb -> §code
                int rgb = color.getValue() & 0xFFFFFF;
                Character code = COLOR_TO_CODE.get(rgb);
                if (code != null) {
                    sb.append('\u00A7').append(code);
                }
            }
            if (style.isBold()) sb.append("\u00A7l");
            if (style.isItalic()) sb.append("\u00A7o");
            if (style.isUnderlined()) sb.append("\u00A7n");
            if (style.isStrikethrough()) sb.append("\u00A7m");
            if (style.isObfuscated()) sb.append("\u00A7k");
            sb.append(string);
            return Optional.empty();
        }, Style.EMPTY);
        return sb.toString();
    }

    private static Component parseColorCodes(String text) {
        MutableComponent root = Component.empty();
        String[] parts = text.split("\u00A7");
        if (parts.length == 0) return Component.literal(text);
        root.append(Component.literal(parts[0]));
        List<ChatFormatting> activeFormats = new ArrayList<>();
        for (int i = 1; i < parts.length; i++) {
            String part = parts[i];
            if (part.isEmpty()) continue;
            char code = Character.toLowerCase(part.charAt(0));
            ChatFormatting format = CODE_TO_FORMAT.get(code);
            if (format != null) {
                if (COLOR_FORMATS.contains(format) || format == ChatFormatting.RESET) activeFormats.clear();
                if (format != ChatFormatting.RESET) activeFormats.add(format);
                if (part.length() > 1) {
                    MutableComponent t = Component.literal(part.substring(1));
                    for (ChatFormatting f : activeFormats) t.withStyle(f);
                    root.append(t);
                }
            } else {
                root.append(Component.literal("\u00A7" + part));
            }
        }
        return root;
    }
    
    private static String clientLanguage() {
        Minecraft client = Minecraft.getInstance();
        return (client != null && client.options != null) ? client.options.languageCode : "en_us";
    }
}
