package dev.shadowsoffire.apotheosis.potion;

import java.util.List;

import dev.shadowsoffire.apotheosis.Apoth;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PotionModuleClient {

    // Static mod bus events

    @SubscribeEvent
    public static void colors(RegisterColorHandlersEvent.Item e) {
        e.register((stack, tint) -> PotionUtils.getColor(stack), Apoth.Items.POTION_CHARM.get());
    }

    // Instance forge bus events

    @SubscribeEvent
    public void tooltips(ItemTooltipEvent e) {
        ItemStack stack = e.getItemStack();
        if (stack.getItem() instanceof PotionItem) {
            List<MobEffectInstance> effects = PotionUtils.getMobEffects(stack);
            if (effects.size() == 1) {
                MobEffect effect = effects.get(0).getEffect();
                String key = effect.getDescriptionId() + ".desc";
                if (I18n.exists(key)) {
                    e.getToolTip().add(Component.translatable(key).withStyle(ChatFormatting.GRAY));
                }
            }
        }
    }

}
