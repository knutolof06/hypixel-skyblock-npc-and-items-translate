package com.npctranslator.gui;

import com.npctranslator.config.ModConfig;
import com.npctranslator.config.ModMenuIntegration;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class WelcomeScreen extends Screen {

    private final Screen parent;

    public WelcomeScreen() {
        this(null);
    }

    public WelcomeScreen(Screen parent) {
        super(Component.literal("NPC & Items Translator"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int buttonWidth = 200;
        int buttonHeight = 20;
        int x = this.width / 2 - buttonWidth / 2;
        int startY = this.height / 2 + 10;
        int padding = 25;

        // 1. "Mod Menüsü" Butonu
        this.addRenderableWidget(Button.builder(Component.translatable("npctranslator.welcome.button.modmenu"), button -> {
            ModMenuIntegration integration = new ModMenuIntegration();
            com.npctranslator.NPCTranslatorClient.setScreen(this.minecraft, integration.getModConfigScreenFactory().create(this.parent));
        }).bounds(x, startY, buttonWidth, buttonHeight).build());

        // 2. "Kapat" Butonu (Bir daha göstermeyi engellemez)
        this.addRenderableWidget(Button.builder(Component.translatable("npctranslator.welcome.button.close"), button -> {
            com.npctranslator.NPCTranslatorClient.setScreen(this.minecraft, this.parent);
        }).bounds(x, startY + padding, buttonWidth, buttonHeight).build());

        // 3. "Bir Daha Gösterme" Butonu
        this.addRenderableWidget(Button.builder(Component.translatable("npctranslator.welcome.button.dont_show"), button -> {
            ModConfig.INSTANCE.hasSeenWelcomeScreen = true;
            ModConfig.save();
            com.npctranslator.NPCTranslatorClient.setScreen(this.minecraft, this.parent);
        }).bounds(x, startY + padding * 2, buttonWidth, buttonHeight).build());

        // 4. "API Nasıl Alınır?" Butonu
        this.addRenderableWidget(Button.builder(Component.translatable("npctranslator.welcome.button.api_help"), button -> {
            com.npctranslator.NPCTranslatorClient.setScreen(this.minecraft, new ApiHelpScreen(this));
        }).bounds(x, startY + padding * 3, buttonWidth, buttonHeight).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        context.centeredText(this.font, this.title, this.width / 2, this.height / 2 - 80, 0xFFFFFFFF);
        
        Component[] desc = {
            Component.translatable("npctranslator.welcome.desc1"),
            Component.translatable("npctranslator.welcome.desc2"),
            Component.translatable("npctranslator.welcome.desc3"),
            Component.translatable("npctranslator.welcome.desc4")
        };
        
        int y = this.height / 2 - 50;
        for (Component line : desc) {
            context.centeredText(this.font, line, this.width / 2, y, 0xFFAAAAAA);
            y += 15;
        }

        super.extractRenderState(context, mouseX, mouseY, delta);
    }
}
