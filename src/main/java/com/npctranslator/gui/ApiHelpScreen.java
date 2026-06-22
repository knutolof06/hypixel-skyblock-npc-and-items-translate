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

        int buttonWidth = 200;
        int buttonHeight = 20;
        int x = this.width / 2 - buttonWidth / 2;
        int startY = this.height / 2;
        int padding = 35;

        // Groq API Butonu
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("npctranslator.api_help.groq"), button -> {
            Util.getOperatingSystem().open("https://console.groq.com/keys");
        }).dimensions(x, startY, buttonWidth, buttonHeight).build());

        // Gemini API Butonu
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("npctranslator.api_help.gemini"), button -> {
            Util.getOperatingSystem().open("https://aistudio.google.com/app/apikey");
        }).dimensions(x, startY + padding, buttonWidth, buttonHeight).build());

        // Geri Butonu
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("npctranslator.welcome.button.close"), button -> {
            this.client.setScreen(this.parent);
        }).dimensions(x, startY + padding * 2, buttonWidth, buttonHeight).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.height / 2 - 90, 0xFFFFFFFF);
        
        Text[] desc = {
            Text.translatable("npctranslator.api_help.desc1"),
            Text.translatable("npctranslator.api_help.desc2")
        };
        
        int y = this.height / 2 - 60;
        for (Text line : desc) {
            context.drawCenteredTextWithShadow(this.textRenderer, line, this.width / 2, y, 0xFFAAAAAA);
            y += 15;
        }

        super.render(context, mouseX, mouseY, delta);
    }
}
