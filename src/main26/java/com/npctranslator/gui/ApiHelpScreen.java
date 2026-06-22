package com.npctranslator.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class ApiHelpScreen extends Screen {

    private final Screen parent;

    public ApiHelpScreen(Screen parent) {
        super(Component.translatable("npctranslator.api_help.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int buttonWidth = 200;
        int buttonHeight = 20;
        int x = this.width / 2 - buttonWidth / 2;
        int startY = this.height / 2;
        int padding = 35;

        // Groq API Butonu
        this.addRenderableWidget(Button.builder(Component.translatable("npctranslator.api_help.groq"), button -> {
            try {
                java.awt.Desktop.getDesktop().browse(new java.net.URI("https://console.groq.com/keys"));
            } catch (Exception e) {}
        }).bounds(x, startY, buttonWidth, buttonHeight).build());

        // Gemini API Butonu
        this.addRenderableWidget(Button.builder(Component.translatable("npctranslator.api_help.gemini"), button -> {
            try {
                java.awt.Desktop.getDesktop().browse(new java.net.URI("https://aistudio.google.com/app/apikey"));
            } catch (Exception e) {}
        }).bounds(x, startY + padding, buttonWidth, buttonHeight).build());

        // Geri Butonu
        this.addRenderableWidget(Button.builder(Component.translatable("npctranslator.welcome.button.close"), button -> {
            com.npctranslator.NPCTranslatorClient.setScreen(this.minecraft, this.parent);
        }).bounds(x, startY + padding * 2, buttonWidth, buttonHeight).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        context.centeredText(this.font, this.title, this.width / 2, this.height / 2 - 90, 0xFFFFFFFF);
        
        Component[] desc = {
            Component.translatable("npctranslator.api_help.desc1"),
            Component.translatable("npctranslator.api_help.desc2")
        };
        
        int y = this.height / 2 - 60;
        for (Component line : desc) {
            context.centeredText(this.font, line, this.width / 2, y, 0xFFAAAAAA);
            y += 15;
        }

        super.extractRenderState(context, mouseX, mouseY, delta);
    }
}
