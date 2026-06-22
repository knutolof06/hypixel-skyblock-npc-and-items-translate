package com.npctranslator.mixin;

import com.npctranslator.NPCTranslatorClient;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class CommandInterceptMixin {

    @Inject(method = "sendCommand", at = @At("HEAD"), cancellable = true)
    private void interceptTranslateCommand(String command, CallbackInfo ci) {
        if (command.startsWith("translate_npc ")) {
            String msgId = command.substring("translate_npc ".length()).trim();
            NPCTranslatorClient.handleTranslation(msgId);
            ci.cancel();
        } else if (command.startsWith("npc_setkey ")) {
            String apiKey = command.substring("npc_setkey ".length()).trim();
            NPCTranslatorClient.setApiKey(apiKey);
            ci.cancel();
        } else if (command.startsWith("revert_npc ")) {
            String msgId = command.substring("revert_npc ".length()).trim().split(" ")[0];
            NPCTranslatorClient.handleRevert(msgId, "");
            ci.cancel();
        } else if (command.equals("translate")) {
            NPCTranslatorClient.openSettings();
            ci.cancel();
        }
    }

    @Inject(method = "sendUnattendedCommand", at = @At("HEAD"), cancellable = true)
    private void interceptClickTranslateCommand(String command, net.minecraft.client.gui.screens.Screen screen, CallbackInfo ci) {
        if (command.startsWith("/translate_npc ")) {
            String msgId = command.substring("/translate_npc ".length()).trim();
            NPCTranslatorClient.handleTranslation(msgId);
            ci.cancel();
        } else if (command.startsWith("translate_npc ")) {
            String msgId = command.substring("translate_npc ".length()).trim();
            NPCTranslatorClient.handleTranslation(msgId);
            ci.cancel();
        } else if (command.startsWith("/revert_npc ")) {
            String msgId = command.substring("/revert_npc ".length()).trim().split(" ")[0];
            NPCTranslatorClient.handleRevert(msgId, "");
            ci.cancel();
        } else if (command.startsWith("revert_npc ")) {
            String msgId = command.substring("revert_npc ".length()).trim().split(" ")[0];
            NPCTranslatorClient.handleRevert(msgId, "");
            ci.cancel();
        }
    }
}
