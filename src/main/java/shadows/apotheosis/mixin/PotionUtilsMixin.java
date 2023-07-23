package shadows.apotheosis.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.mojang.datafixers.util.Pair;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import shadows.apotheosis.core.attributeslib.AttributesLib;
import shadows.apotheosis.core.attributeslib.api.IFormattableAttribute;

@Mixin(PotionUtils.class)
public class PotionUtilsMixin {

    /**
     * Redirects the {@link List#isEmpty()} call that is checked before adding tooltips to potions to replace vanilla tooltip handling.
     *
     * @param list           The potion's attribute modifiers.
     * @param stack          The potion stack.
     * @param tooltips       The tooltip list.
     * @param durationFactor The duration factor of the potion.
     * @return True, unconditionally, so that the vanilla tooltip logic is ignored.
     * @see PotionUtils#addPotionTooltip(ItemStack, List, float)
     */
    @Redirect(method = "addPotionTooltip(Lnet/minecraft/world/item/ItemStack;Ljava/util/List;F)V", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z", ordinal = 1), require = 1)
    private static boolean attributeslib_potionTooltips(List<Pair<Attribute, AttributeModifier>> list, ItemStack stack, List<Component> tooltips, float durationFactor) {
        if (!list.isEmpty()) {
            tooltips.add(CommonComponents.EMPTY);
            tooltips.add(Component.translatable("potion.whenDrank").withStyle(ChatFormatting.DARK_PURPLE));

            for (Pair<Attribute, AttributeModifier> pair : list) {
                tooltips.add(IFormattableAttribute.toComponent(pair.getFirst(), pair.getSecond(), AttributesLib.getTooltipFlag()));
            }
        }
        return true;
    }

}
