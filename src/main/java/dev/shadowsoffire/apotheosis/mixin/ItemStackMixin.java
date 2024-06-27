package dev.shadowsoffire.apotheosis.mixin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.DoubleStream;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.ench.asm.EnchHooks;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

@Mixin(value = ItemStack.class, priority = 500)
public class ItemStackMixin {

    @Inject(method = "getHoverName", at = @At("RETURN"), cancellable = true)
    public void apoth_affixItemName(CallbackInfoReturnable<Component> cir) {
        ItemStack ths = (ItemStack) (Object) this;
        CompoundTag afxData = ths.getTagElement(AffixHelper.AFFIX_DATA);
        if (afxData != null && afxData.contains(AffixHelper.NAME, 8)) {
            try {
                Component component = AffixHelper.getName(ths);
                if (component.getContents() instanceof TranslatableContents tContents) {
                    int idx = "misc.apotheosis.affix_name.four".equals(tContents.getKey()) ? 2 : 1;
                    tContents.getArgs()[idx] = cir.getReturnValue();
                    cir.setReturnValue(component);
                }
                else afxData.remove(AffixHelper.NAME);
            }
            catch (Exception exception) {
                afxData.remove(AffixHelper.NAME);
            }
        }

        DynamicHolder<LootRarity> rarity = AffixHelper.getRarity(afxData);
        if (rarity.isBound()) {
            Component recolored = cir.getReturnValue().copy().withStyle(s -> s.withColor(rarity.get().getColor()));
            cir.setReturnValue(recolored);
        }
    }

    /**
     * Injects before the first call to {@link ItemStack#getDamageValue()} inside of {@link ItemStack#hurt(int, RandomSource, ServerPlayer)} to reduce durability
     * damage.
     * Modifies the pAmount parameter, reducing it by the result of randomly rolling each point of damage against the block chance.
     */
    @ModifyVariable(at = @At(value = "INVOKE", target = "net/minecraft/world/item/ItemStack.getDamageValue()I"), method = "hurt", argsOnly = true, ordinal = 0)
    public int swapDura(int amount, int amountCopy, RandomSource pRandom, @Nullable ServerPlayer pUser) {
        int blocked = 0;
        DoubleStream socketBonuses = SocketHelper.getGems((ItemStack) (Object) this).getDurabilityBonusPercentage(pUser);
        DoubleStream afxBonuses = AffixHelper.streamAffixes((ItemStack) (Object) this).mapToDouble(inst -> inst.getDurabilityBonusPercentage(pUser));
        DoubleStream bonuses = DoubleStream.concat(socketBonuses, afxBonuses);
        double chance = bonuses.reduce(0, (res, ele) -> res + (1 - res) * ele);

        int delta = 1;
        if (chance < 0) {
            delta = -1;
            chance = -chance;
        }

        if (chance > 0) {
            for (int i = 0; i < amount; i++) {
                if (pRandom.nextFloat() <= chance) blocked += delta;
            }
        }
        return amount - blocked;
    }

    private static void appendModifiedEnchTooltip(List<Component> tooltip, Enchantment ench, int realLevel, int nbtLevel) {
        MutableComponent mc = ench.getFullname(realLevel).copy();
        mc.getSiblings().clear();
        Component nbtLevelComp = Component.translatable("enchantment.level." + nbtLevel);
        Component realLevelComp = Component.translatable("enchantment.level." + realLevel);
        if (realLevel != 1 || EnchHooks.getMaxLevel(ench) != 1) mc.append(CommonComponents.SPACE).append(realLevelComp);

        int diff = realLevel - nbtLevel;
        char sign = diff > 0 ? '+' : '-';
        Component diffComp = Component.translatable("(%s " + sign + " %s)", nbtLevelComp, Component.translatable("enchantment.level." + Math.abs(diff))).withStyle(ChatFormatting.DARK_GRAY);
        mc.append(CommonComponents.SPACE).append(diffComp);
        if (realLevel == 0) {
            mc.withStyle(ChatFormatting.DARK_GRAY);
        }
        tooltip.add(mc);
    }

    /**
     * Rewrites the enchantment tooltip lines to include the effective level, as well as the (NBT + bonus) calculation.
     */
    @SuppressWarnings("deprecation")
    @Redirect(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;appendEnchantmentNames(Ljava/util/List;Lnet/minecraft/nbt/ListTag;)V"))
    public void apoth_enchTooltipRewrite(List<Component> tooltip, ListTag tagEnchants) {
        ItemStack ths = (ItemStack) (Object) this;
        Map<Enchantment, Integer> realLevels = new HashMap<>(ths.getAllEnchantments());
        List<Component> enchTooltips = new ArrayList<>();

        // Iterate backwards to address duplicate enchantment logic (always use the last-positioned copy).
        for (int i = tagEnchants.size() - 1; i >= 0; i--) {
            CompoundTag compoundtag = tagEnchants.getCompound(i);

            Enchantment ench = BuiltInRegistries.ENCHANTMENT.get(EnchantmentHelper.getEnchantmentId(compoundtag));
            if (ench == null || !realLevels.containsKey(ench)) continue;

            int nbtLevel = EnchantmentHelper.getEnchantmentLevel(compoundtag);
            int realLevel = realLevels.remove(ench);

            if (nbtLevel == realLevel) {
                // Default logic when levels are the same
                enchTooltips.add(ench.getFullname(EnchantmentHelper.getEnchantmentLevel(compoundtag)));
            }
            else {
                // Show the change vs nbt level
                appendModifiedEnchTooltip(enchTooltips, ench, realLevel, nbtLevel);
            }
        }

        // Reverse and add to tooltip. Honestly we probably don't even need to reverse, but for consistency's sake.
        Collections.reverse(enchTooltips);
        tooltip.addAll(enchTooltips);

        // Show the tooltip for any modified enchantments not present in NBT.
        for (Map.Entry<Enchantment, Integer> real : realLevels.entrySet()) {
            if (real.getValue() > 0) appendModifiedEnchTooltip(tooltip, real.getKey(), real.getValue(), 0);
        }
    }

}
