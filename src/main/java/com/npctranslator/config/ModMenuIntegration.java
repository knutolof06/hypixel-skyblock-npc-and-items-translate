package com.npctranslator.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return this::buildScreen;
    }

    private Screen buildScreen(Screen parent) {
        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("text.autoconfig.npc_translator.title"))
                .setDoesConfirmSave(false);

        builder.setSavingRunnable(() -> {
            AutoConfig.getConfigHolder(ModConfig.class).save();
            com.npctranslator.NPCTranslatorClient.reloadConfig();
        });

        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("category.npctranslator.api"));
        ConfigCategory chatCat = builder.getOrCreateCategory(Text.translatable("category.npctranslator.chat"));
        ConfigCategory itemCat = builder.getOrCreateCategory(Text.translatable("category.npctranslator.item"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        general.addEntry(entryBuilder.startBooleanToggle(Text.translatable("text.autoconfig.npc_translator.option.enabled"), config.enabled)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("text.autoconfig.npc_translator.option.enabled.tooltip"))
                .setSaveConsumer(newValue -> config.enabled = newValue)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Text.translatable("text.autoconfig.npc_translator.option.autoFallbackOnLimit"), config.autoFallbackOnLimit)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("text.autoconfig.npc_translator.option.autoFallbackOnLimit.tooltip"))
                .setSaveConsumer(newValue -> config.autoFallbackOnLimit = newValue)
                .build());

        general.addEntry(entryBuilder.startStrField(Text.translatable("text.autoconfig.npc_translator.option.groqApiKey"), config.groqApiKey)
                .setDefaultValue("")
                .setTooltip(Text.translatable("text.autoconfig.npc_translator.option.groqApiKey.tooltip"))
                .setSaveConsumer(newValue -> config.groqApiKey = newValue)
                .build());

        net.minecraft.text.MutableText groqTooltip = Text.translatable("text.autoconfig.npc_translator.option.groqModel.tooltip");
        for (ModConfig.GroqModel m : ModConfig.GroqModel.values()) {
            groqTooltip.append(Text.literal("\n§e" + m.displayName + "§r: " + m.description));
        }

        general.addEntry(entryBuilder.startEnumSelector(Text.translatable("text.autoconfig.npc_translator.option.groqModel"), ModConfig.GroqModel.class, config.groqModel)
                .setDefaultValue(ModConfig.GroqModel.LLAMA_3_3_70B)
                .setTooltip(groqTooltip)
                .setSaveConsumer(newValue -> config.groqModel = newValue)
                .build());

        general.addEntry(entryBuilder.startStrField(Text.translatable("text.autoconfig.npc_translator.option.geminiApiKey"), config.geminiApiKey)
                .setDefaultValue("")
                .setTooltip(Text.translatable("text.autoconfig.npc_translator.option.geminiApiKey.tooltip"))
                .setSaveConsumer(newValue -> config.geminiApiKey = newValue)
                .build());

        net.minecraft.text.MutableText geminiTooltip = Text.translatable("text.autoconfig.npc_translator.option.geminiModel.tooltip");
        for (ModConfig.GeminiModel m : ModConfig.GeminiModel.values()) {
            geminiTooltip.append(Text.literal("\n§e" + m.displayName + "§r: " + m.description));
        }

        general.addEntry(entryBuilder.startEnumSelector(Text.translatable("text.autoconfig.npc_translator.option.geminiModel"), ModConfig.GeminiModel.class, config.geminiModel)
                .setDefaultValue(ModConfig.GeminiModel.GEMINI_2_5_FLASH)
                .setTooltip(geminiTooltip)
                .setSaveConsumer(newValue -> config.geminiModel = newValue)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Text.translatable("text.autoconfig.npc_translator.option.useGameLanguage"), config.useGameLanguage)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("text.autoconfig.npc_translator.option.useGameLanguage.tooltip"))
                .setSaveConsumer(newValue -> config.useGameLanguage = newValue)
                .build());

        general.addEntry(entryBuilder.startEnumSelector(Text.translatable("text.autoconfig.npc_translator.option.targetLanguage"), ModConfig.TranslationLanguage.class, config.targetLanguage)
                .setDefaultValue(ModConfig.TranslationLanguage.TURKISH)
                .setTooltip(Text.translatable("text.autoconfig.npc_translator.option.targetLanguage.tooltip"))
                .setSaveConsumer(newValue -> config.targetLanguage = newValue)
                .build());

        chatCat.addEntry(entryBuilder.startBooleanToggle(Text.translatable("text.autoconfig.npc_translator.option.autoTranslateChat"), config.autoTranslateChat)
                .setDefaultValue(false)
                .setTooltip(Text.translatable("text.autoconfig.npc_translator.option.autoTranslateChat.tooltip"))
                .setSaveConsumer(newValue -> config.autoTranslateChat = newValue)
                .build());

        chatCat.addEntry(entryBuilder.startBooleanToggle(Text.translatable("text.autoconfig.npc_translator.option.onlyTranslateNpcChat"), config.onlyTranslateNpcChat)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("text.autoconfig.npc_translator.option.onlyTranslateNpcChat.tooltip"))
                .setSaveConsumer(newValue -> config.onlyTranslateNpcChat = newValue)
                .build());

        chatCat.addEntry(entryBuilder.startEnumSelector(Text.translatable("text.autoconfig.npc_translator.option.translationProvider"), ModConfig.TranslationProvider.class, config.chatTranslationProvider)
                .setDefaultValue(ModConfig.TranslationProvider.GOOGLE)
                .setTooltip(Text.translatable("text.autoconfig.npc_translator.option.translationProvider.tooltip"))
                .setSaveConsumer(newValue -> config.chatTranslationProvider = newValue)
                .build());

        itemCat.addEntry(entryBuilder.startBooleanToggle(Text.translatable("text.autoconfig.npc_translator.option.autoTranslateItems"), config.autoTranslateItems)
                .setDefaultValue(false)
                .setTooltip(Text.translatable("text.autoconfig.npc_translator.option.autoTranslateItems.tooltip"))
                .setSaveConsumer(newValue -> config.autoTranslateItems = newValue)
                .build());

        itemCat.addEntry(entryBuilder.startBooleanToggle(Text.translatable("text.autoconfig.npc_translator.option.enableGoogleItem"), config.enableGoogleItem)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("text.autoconfig.npc_translator.option.enableGoogleItem.tooltip"))
                .setSaveConsumer(newValue -> config.enableGoogleItem = newValue)
                .build());

        itemCat.addEntry(entryBuilder.startBooleanToggle(Text.translatable("text.autoconfig.npc_translator.option.enableGeminiItem"), config.enableGeminiItem)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("text.autoconfig.npc_translator.option.enableGeminiItem.tooltip"))
                .setSaveConsumer(newValue -> config.enableGeminiItem = newValue)
                .build());

        itemCat.addEntry(entryBuilder.startBooleanToggle(Text.translatable("text.autoconfig.npc_translator.option.enableGroqItem"), config.enableGroqItem)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("text.autoconfig.npc_translator.option.enableGroqItem.tooltip"))
                .setSaveConsumer(newValue -> config.enableGroqItem = newValue)
                .build());

        return builder.build();
    }
}
