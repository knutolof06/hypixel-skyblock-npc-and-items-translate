package com.npctranslator.gui;

import com.npctranslator.NPCTranslatorClient;
import com.npctranslator.config.ModMenuIntegration;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class HubScreen extends Screen {

    public HubScreen() {
        super(Text.literal("NPC & Items Translator"));
    }

    @Override
    protected void init() {
        super.init();

        int buttonWidth = 160;
        int buttonHeight = 20;
        int startY = this.height / 2 - 30;
        int spacing = 28;

        // Mod Ayarları Butonu
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("npctranslator.welcome.button.modmenu"), button -> {
            Screen screen = new ModMenuIntegration().getModConfigScreenFactory().create(this);
            this.client.setScreen(screen);
        }).dimensions(this.width / 2 - buttonWidth / 2, startY, buttonWidth, buttonHeight).build());

        // Sözlüğü Sil Butonu
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("npctranslator.option.clearDict"), button -> {
            NPCTranslatorClient.ITEM_CACHE.clear();
            if (this.client != null && this.client.player != null) {
                this.client.player.sendMessage(Text.translatable("npctranslator.dict_cleared", 0), false);
            }
            this.client.setScreen(null);
        }).dimensions(this.width / 2 - buttonWidth / 2, startY + spacing, buttonWidth, buttonHeight).build());

        // Kapat Butonu
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("npctranslator.welcome.button.close"), button -> {
            this.client.setScreen(null);
        }).dimensions(this.width / 2 - buttonWidth / 2, startY + spacing * 2, buttonWidth, buttonHeight).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.height / 2 - 60, 0xFFFFFFFF);
    }
}
