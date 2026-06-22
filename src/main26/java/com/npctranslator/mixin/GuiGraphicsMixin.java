package com.npctranslator.mixin;

import com.npctranslator.NPCTranslatorClient;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

@Mixin(GuiGraphicsExtractor.class)
public class GuiGraphicsMixin {

    @Inject(method = "setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/resources/Identifier;)V", at = @At("HEAD"), require = 0)
    private void onSetTooltipForNextFrame(Font font, List<Component> components, Optional<?> extra, int x, int y, net.minecraft.resources.Identifier id, CallbackInfo ci) {
        if (NPCTranslatorClient.isItemTranslationEnabled()) {
            NPCTranslatorClient.handleGeneralTooltip(components);
        }
    }

    @Inject(method = "setComponentTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;IILnet/minecraft/resources/Identifier;)V", at = @At("HEAD"), require = 0)
    private void onSetComponentTooltipForNextFrame(Font font, List<Component> components, int x, int y, net.minecraft.resources.Identifier id, CallbackInfo ci) {
        if (NPCTranslatorClient.isItemTranslationEnabled()) {
            NPCTranslatorClient.handleGeneralTooltip(components);
        }
    }
}
