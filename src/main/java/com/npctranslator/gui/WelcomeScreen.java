package com.npctranslator.gui;

import com.npctranslator.config.ModConfig;
import com.npctranslator.config.ModMenuIntegration;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

@Environment(EnvType.CLIENT)
public class WelcomeScreen extends Screen {

    private final Screen parent;

    public WelcomeScreen() {
        this(null);
    }

    public WelcomeScreen(Screen parent) {
        super(Text.translatable("npctranslator.welcome.title").formatted(Formatting.AQUA, Formatting.BOLD));
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
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("npctranslator.welcome.button.modmenu"), button -> {
            ModMenuIntegration integration = new ModMenuIntegration();
            this.client.setScreen(integration.getModConfigScreenFactory().create(this.parent));
        }).dimensions(x, startY, buttonWidth, buttonHeight).build());

        // 2. "Kapat" Butonu (Bir daha göstermeyi engellemez)
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("npctranslator.welcome.button.close"), button -> {
            this.client.setScreen(this.parent);
        }).dimensions(x, startY + padding, buttonWidth, buttonHeight).build());

        // 3. "Bir Daha Gösterme" Butonu
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("npctranslator.welcome.button.dont_show"), button -> {
            ModConfig.INSTANCE.hasSeenWelcomeScreen = true;
            ModConfig.save();
            this.client.setScreen(this.parent);
        }).dimensions(x, startY + padding * 2, buttonWidth, buttonHeight).build());

        // 4. "API Nasıl Alınır?" Butonu
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("npctranslator.welcome.button.api_help"), button -> {
            this.client.setScreen(new ApiHelpScreen(this));
        }).dimensions(x, startY + padding * 3, buttonWidth, buttonHeight).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.height / 2 - 80, 0xFFFFFFFF);
        
        Text[] desc = {
            Text.translatable("npctranslator.welcome.desc1"),
            Text.translatable("npctranslator.welcome.desc2"),
            Text.translatable("npctranslator.welcome.desc3"),
            Text.translatable("npctranslator.welcome.desc4")
        };
        
        int y = this.height / 2 - 50;
        for (Text line : desc) {
            context.drawCenteredTextWithShadow(this.textRenderer, line, this.width / 2, y, 0xFFAAAAAA);
            y += 15;
        }

        super.render(context, mouseX, mouseY, delta);
    }
}
