package com.npctranslator.mixin;

import com.npctranslator.NPCTranslatorClient;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(method = "getTooltipLines", at = @At("RETURN"), cancellable = true)
    private void onGetTooltip(Item.TooltipContext context, Player player, TooltipFlag type, CallbackInfoReturnable<List<Component>> cir) {
        if (NPCTranslatorClient.isItemTranslationEnabled()) {
            List<Component> tooltip = new java.util.ArrayList<>(cir.getReturnValue());
            NPCTranslatorClient.handleItemTooltip((ItemStack) (Object) this, tooltip);
            NPCTranslatorClient.PROCESSED_TOOLTIPS.put(tooltip, true);
            cir.setReturnValue(tooltip);
        }
    }
}
