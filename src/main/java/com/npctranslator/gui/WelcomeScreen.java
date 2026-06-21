package com.npctranslator.gui;

import com.npctranslator.config.ModConfig;
import com.npctranslator.config.ModMenuIntegration;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class WelcomeScreen extends Screen {

    public WelcomeScreen() {
        super(Text.literal("NPC & Items Translator"));
    }

    @Override
    protected void init() {
        super.init();
        
        int buttonWidth = 150;
        int buttonHeight = 20;
        int startY = this.height / 2 + 10;
        int spacing = 24;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Mod Menüsü"), button -> {
            Screen screen = new ModMenuIntegration().getModConfigScreenFactory().create(this);
            this.client.setScreen(screen);
        }).dimensions(this.width / 2 - buttonWidth / 2, startY, buttonWidth, buttonHeight).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Kapat"), button -> {
            this.client.setScreen(null);
        }).dimensions(this.width / 2 - buttonWidth / 2, startY + spacing, buttonWidth, buttonHeight).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Bir Daha Gösterme"), button -> {
            ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
            config.hasSeenWelcomeScreen = true;
            AutoConfig.getConfigHolder(ModConfig.class).save();
            this.client.setScreen(null);
        }).dimensions(this.width / 2 - buttonWidth / 2, startY + spacing * 2, buttonWidth, buttonHeight).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.height / 2 - 80, 0xFFFFFFFF);
        
        String[] desc = {
            "NPC & Items Translator moduna hoş geldiniz!",
            "NPC konuşmalarını ve eşya bilgilerini kolayca çevirebilirsiniz.",
            "Varsayılan olarak Google Translate seçilidir.",
            "Ayarlar için /translate komutu veya Z tuşunu kullanın."
        };
        
        int y = this.height / 2 - 50;
        for (String line : desc) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(line), this.width / 2, y, 0xFFAAAAAA);
            y += 15;
        }

        super.render(context, mouseX, mouseY, delta);
    }
}
