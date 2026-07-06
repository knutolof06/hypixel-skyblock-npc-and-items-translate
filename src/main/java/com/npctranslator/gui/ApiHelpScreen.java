package com.npctranslator.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

@Environment(EnvType.CLIENT)
public class ApiHelpScreen extends Screen {

    private final Screen parent;

    public ApiHelpScreen(Screen parent) {
        super(Text.translatable("npctranslator.api_help.title").formatted(Formatting.GOLD, Formatting.BOLD));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int buttonWidth = 220;
        int buttonHeight = 20;
        int x = this.width / 2 - buttonWidth / 2;
        int padding = 26;

        // Start from a fixed center point going upward so all buttons are visible
        int totalButtons = 5; // groq, gemini, mistral, openrouter, back
        int totalHeight = totalButtons * buttonHeight + (totalButtons - 1) * (padding - buttonHeight);
        int startY = this.height / 2 - totalHeight / 2 + 20;

        // Groq API Button
        this.addDrawableChild(ButtonWidget.builder(
                Text.translatable("npctranslator.api_help.groq"), button ->
                    Util.getOperatingSystem().open("https://console.groq.com/keys")
        ).dimensions(x, startY, buttonWidth, buttonHeight).build());

        // Gemini API Button
        this.addDrawableChild(ButtonWidget.builder(
                Text.translatable("npctranslator.api_help.gemini"), button ->
                    Util.getOperatingSystem().open("https://aistudio.google.com/app/apikey")
        ).dimensions(x, startY + padding, buttonWidth, buttonHeight).build());

        // Mistral API Button
        this.addDrawableChild(ButtonWidget.builder(
                Text.translatable("npctranslator.api_help.mistral"), button ->
                    Util.getOperatingSystem().open("https://console.mistral.ai/api-keys/")
        ).dimensions(x, startY + padding * 2, buttonWidth, buttonHeight).build());

        // OpenRouter API Button
        this.addDrawableChild(ButtonWidget.builder(
                Text.translatable("npctranslator.api_help.openrouter"), button ->
                    Util.getOperatingSystem().open("https://openrouter.ai/settings/keys")
        ).dimensions(x, startY + padding * 3, buttonWidth, buttonHeight).build());

        // Back Button
        this.addDrawableChild(ButtonWidget.builder(
                Text.translatable("npctranslator.api_help.back"), button ->
                    this.client.setScreen(parent)
        ).dimensions(x, startY + padding * 3 + 36, buttonWidth, buttonHeight).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.height / 2 - 90, 0xFFFFFFFF);
        
        Text[] desc = {
            Text.translatable("npctranslator.api_help.desc1"),
            Text.translatable("npctranslator.api_help.desc2")
        };
        
        int y = this.height / 2 - 65;
        for (Text line : desc) {
            context.drawCenteredTextWithShadow(this.textRenderer, line, this.width / 2, y, 0xFFAAAAAA);
            y += 14;
        }

        super.render(context, mouseX, mouseY, delta);
    }
}
