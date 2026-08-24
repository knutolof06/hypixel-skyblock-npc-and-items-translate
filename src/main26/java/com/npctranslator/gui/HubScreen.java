package com.npctranslator.gui;

import com.npctranslator.NPCTranslatorClient;
import com.npctranslator.TranslationDictionary;
import com.npctranslator.config.ModMenuIntegration;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class HubScreen extends Screen {

    public HubScreen() {
        super(Component.literal("NPC & Items Translator"));
    }

    @Override
    protected void init() {
        super.init();

        int buttonWidth = 160;
        int buttonHeight = 20;
        int startY = this.height / 2 - 30;
        int spacing = 28;

        // Mod Ayarları Butonu
        this.addRenderableWidget(Button.builder(Component.translatable("npctranslator.welcome.button.modmenu"), button -> {
            Screen screen = new ModMenuIntegration().getModConfigScreenFactory().create(this);
            NPCTranslatorClient.setScreen(this.minecraft, screen);
        }).bounds(this.width / 2 - buttonWidth / 2, startY, buttonWidth, buttonHeight).build());

        // Sözlüğü Sil Butonu
        this.addRenderableWidget(Button.builder(Component.translatable("npctranslator.option.clearDict"), button -> {
            int count = TranslationDictionary.size();
            TranslationDictionary.clear();
            NPCTranslatorClient.ITEM_CACHE.clear();
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.sendSystemMessage(Component.translatable("npctranslator.dict_cleared", count));
            }
            NPCTranslatorClient.setScreen(this.minecraft, null);
        }).bounds(this.width / 2 - buttonWidth / 2, startY + spacing, buttonWidth, buttonHeight).build());

        // Kapat Butonu
        this.addRenderableWidget(Button.builder(Component.translatable("npctranslator.welcome.button.close"), button -> {
            NPCTranslatorClient.setScreen(this.minecraft, null);
        }).bounds(this.width / 2 - buttonWidth / 2, startY + spacing * 2, buttonWidth, buttonHeight).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        context.centeredText(this.font, this.title, this.width / 2, this.height / 2 - 60, 0xFFFFFFFF);
        super.extractRenderState(context, mouseX, mouseY, delta);
    }
}
