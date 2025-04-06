package dev.shadowsoffire.apotheosis.item;

import java.util.List;

import dev.shadowsoffire.apotheosis.AdventureConfig;
import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apoth.Components;
import dev.shadowsoffire.placebo.tabs.ITabFiller;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

public class PotionCharmItem extends Item implements ITabFiller {

    public PotionCharmItem() {
        super(new Item.Properties().stacksTo(1).durability(192).setNoRepair().component(Components.CHARM_ENABLED, false));
    }

    @Override
    public ItemStack getDefaultInstance() {
        // Long long ago, there was only an invisibility charm, instead of this entire thing.
        return PotionContents.createItemStack(this, Potions.LONG_INVISIBILITY);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean isSelected) {
        if (!hasEffect(stack) || (AdventureConfig.charmsInCuriosOnly && slot != -1)) {
            return;
        }
        if (stack.get(Components.CHARM_ENABLED) && entity instanceof ServerPlayer player) {
            MobEffectInstance contained = getEffect(stack);
            MobEffectInstance active = player.getEffect(contained.getEffect());
            if (active == null || active.getDuration() < getCriticalDuration(active.getEffect())) {
                int durationOffset = getCriticalDuration(contained.getEffect());
                if (contained.getEffect() == MobEffects.REGENERATION) {
                    durationOffset += 50 >> contained.getAmplifier();
                }
                MobEffectInstance newEffect = new MobEffectInstance(contained.getEffect(), (int) Math.ceil(contained.getDuration() / 24D) + durationOffset, contained.getAmplifier(), false, false);
                player.addEffect(newEffect);

                int damage = contained.getEffect() == MobEffects.REGENERATION ? 2 : 1;

                if (isSelected) {
                    stack.hurtAndBreak(damage, player, EquipmentSlot.MAINHAND);
                }
                else {
                    stack.hurtAndBreak(damage, (ServerLevel) player.level(), player, item -> {});
                }
            }
        }
    }

    private static int getCriticalDuration(Holder<MobEffect> effect) {
        return effect.is(Apoth.Tags.EXTENDED_CHARM_DURATION) ? 210 : 5;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.get(Components.CHARM_ENABLED);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!world.isClientSide) {
            stack.set(Components.CHARM_ENABLED, !stack.get(Components.CHARM_ENABLED));
        }
        else if (!stack.get(Components.CHARM_ENABLED)) {
            world.playSound(player, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1, 0.3F);
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment) {
        return false;
    }

    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        if (AdventureConfig.charmsInCuriosOnly) {
            tooltip.add(Component.translatable(this.getDescriptionId() + ".curios_only").withStyle(ChatFormatting.RED));
        }
        if (hasEffect(stack)) {
            MobEffectInstance inst = getEffect(stack);
            MutableComponent potionCmp = Component.translatable(inst.getDescriptionId());
            if (inst.getAmplifier() > 0) {
                potionCmp = Component.translatable("potion.withAmplifier", potionCmp, Component.translatable("potion.potency." + inst.getAmplifier()));
            }

            MobEffect effect = inst.getEffect().value();

            potionCmp.withStyle(effect.getCategory().getTooltipFormatting());
            tooltip.add(Component.translatable(this.getDescriptionId() + ".desc", potionCmp).withStyle(ChatFormatting.GRAY));
            boolean enabled = stack.get(Components.CHARM_ENABLED);
            MutableComponent enabledCmp = Component.translatable(this.getDescriptionId() + (enabled ? ".enabled" : ".disabled"));
            enabledCmp.withStyle(enabled ? ChatFormatting.BLUE : ChatFormatting.RED);
            if (inst.getDuration() > 20) {
                potionCmp = Component.translatable("potion.withDuration", potionCmp, MobEffectUtil.formatDuration(inst, 1, ctx.tickRate()));
            }
            potionCmp.withStyle(effect.getCategory().getTooltipFormatting());
            tooltip.add(Component.translatable(this.getDescriptionId() + ".desc3", potionCmp).withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        if (!hasEffect(stack)) {
            return 1;
        }
        return 192;
    }

    @Override
    public Component getName(ItemStack stack) {
        if (!hasEffect(stack)) {
            return Component.translatable("item.apotheosis.potion_charm_broke");
        }
        MobEffectInstance effect = getEffect(stack);
        MutableComponent potionCmp = Component.translatable(effect.getDescriptionId());
        if (effect.getAmplifier() > 0) {
            potionCmp = Component.translatable("potion.withAmplifier", potionCmp, Component.translatable("potion.potency." + effect.getAmplifier()));
        }
        return Component.translatable("item.apotheosis.potion_charm", potionCmp);
    }

    /**
     * Returns true if the charm's NBT data contains a valid mob effect instance.
     * <p>
     * This will check the encoded potion type and then fall back to encoded custom NBT effects.
     */
    public static boolean hasEffect(ItemStack stack) {
        PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        return contents.getAllEffects().iterator().hasNext();
    }

    /**
     * Returns the mob effect instance stored in the charm's NBT data.
     * <p>
     * This will check the encoded potion type and then fall back to encoded custom NBT effects.
     * <p>
     * Only a single effect is permitted, even when using custom NBT.
     */
    public static MobEffectInstance getEffect(ItemStack stack) {
        PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        return contents.getAllEffects().iterator().next();
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, BuildCreativeModeTabContentsEvent out) {
        BuiltInRegistries.POTION.holders()
            .filter(PotionCharmItem::isValidPotion)
            .forEach(potion -> {
                out.accept(PotionContents.createItemStack(this, potion));
            });
    }

    @Override
    public String getCreatorModId(ItemStack itemStack) {
        ResourceLocation potionKey = itemStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion().map(Holder::getKey).map(ResourceKey::location).orElse(null);
        if (potionKey != null) {
            return potionKey.getNamespace();
        }
        return BuiltInRegistries.ITEM.getKey(this).getNamespace();
    }

    @Override
    public int getEnchantmentValue() {
        return 0;
    }

    /**
     * Checks if a potion may be converted into a potion charm.
     * <p>
     * By default, only single-effect potions that are not instantaneous are allowed.
     * Additional potions may be blacklisted via config file.
     *
     * @return True if the potion may be converted into a potion charm.
     */
    @SuppressWarnings("deprecation")
    public static boolean isValidPotion(Holder<Potion> holder) {
        Potion potion = holder.value();
        if (potion.getEffects().size() != 1) {
            return false;
        }

        MobEffect effect = potion.getEffects().get(0).getEffect().value();
        if (effect.isInstantenous()) {
            return false;
        }

        return !holder.is(Apoth.Tags.POTION_CHARM_BLACKLIST);
    }

}
