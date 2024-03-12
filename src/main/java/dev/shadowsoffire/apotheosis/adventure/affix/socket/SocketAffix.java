package dev.shadowsoffire.apotheosis.adventure.affix.socket;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableFloat;

import com.google.common.base.Predicates;
import com.mojang.serialization.Codec;

import dev.shadowsoffire.apotheosis.adventure.AdventureModule;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixType;
import dev.shadowsoffire.apotheosis.adventure.affix.socket.gem.GemInstance;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;

public final class SocketAffix extends Affix {

    public static final Codec<SocketAffix> CODEC = Codec.unit(SocketAffix::new);

    public SocketAffix() {
        super(AffixType.SOCKET);
    }

    @Override
    public boolean canApplyTo(ItemStack socketed, LootCategory cat, LootRarity rarity) {
        return !cat.isNone();
    }

    @Override
    public void addModifiers(ItemStack socketed, LootRarity itemRarity, float numSockets, EquipmentSlot type, BiConsumer<Attribute, AttributeModifier> map) {
        LootCategory cat = LootCategory.forItem(socketed);
        if (cat.isNone()) {
            AdventureModule.LOGGER.debug("Attempted to apply the attributes of affix {} on item {}, but it is not an affix-compatible item!", this.getId(), socketed.getHoverName().getString());
            return;
        }

        SocketHelper.getGemInstances(socketed).forEach(inst -> inst.addModifiers(type, map));
    }

    @Override
    public int getDamageProtection(ItemStack socketed, LootRarity itemRarity, float numSockets, DamageSource source) {
        return SocketHelper.getGemInstances(socketed).map(inst -> inst.getDamageProtection(source)).reduce(0, Integer::sum);
    }

    @Override
    public float getDamageBonus(ItemStack socketed, LootRarity itemRarity, float numSockets, MobType creatureType) {
        return SocketHelper.getGemInstances(socketed).map(inst -> inst.getDamageBonus(creatureType)).reduce(Float::sum).orElse(0F);
    }

    @Override
    public void doPostAttack(ItemStack socketed, LootRarity itemRarity, float numSockets, LivingEntity user, Entity target) {
        SocketHelper.getGemInstances(socketed).forEach(inst -> inst.doPostAttack(user, target));
    }

    @Override
    public void doPostHurt(ItemStack socketed, LootRarity itemRarity, float numSockets, LivingEntity user, Entity attacker) {
        SocketHelper.getGemInstances(socketed).forEach(inst -> inst.doPostHurt(user, attacker));
    }

    @Override
    public void onArrowFired(ItemStack socketed, LootRarity itemRarity, float numSockets, LivingEntity user, AbstractArrow arrow) {
        SocketHelper.getGemInstances(socketed).forEach(inst -> inst.onArrowFired(user, arrow));
    }

    @Override
    @Nullable
    public InteractionResult onItemUse(ItemStack socketed, LootRarity itemRarity, float numSockets, UseOnContext useinst) {
        return SocketHelper.getGemInstances(socketed).map(inst -> inst.onItemUse(useinst)).filter(Predicates.notNull()).max(InteractionResult::compareTo).orElse(null);
    }

    @Override
    public void onArrowImpact(AbstractArrow arrow, LootRarity itemRarity, float numSockets, HitResult res, Type type) {
        gems(arrow).forEach(inst -> inst.onArrowImpact(arrow, res, type));
    }

    @Override
    public float onShieldBlock(ItemStack socketed, LootRarity itemRarity, float numSockets, LivingEntity entity, DamageSource source, float amount) {
        return SocketHelper.getGemInstances(socketed).map(inst -> inst.onShieldBlock(entity, source, amount)).max(Float::compareTo).orElse(amount);
    }

    @Override
    public void onBlockBreak(ItemStack socketed, LootRarity itemRarity, float numSockets, Player player, LevelAccessor world, BlockPos pos, BlockState state) {
        SocketHelper.getGemInstances(socketed).forEach(inst -> inst.onBlockBreak(player, world, pos, state));
    }

    @Override
    public float getDurabilityBonusPercentage(ItemStack socketed, LootRarity rarity, float level, ServerPlayer user) {
        return (float) SocketHelper.getGemInstances(socketed).mapToDouble(inst -> inst.getDurabilityBonusPercentage(user)).sum();
    }

    @Override
    public float onHurt(ItemStack socketed, LootRarity rarity, float level, DamageSource src, LivingEntity ent, float amount) {
        MutableFloat mFloat = new MutableFloat(amount);
        SocketHelper.getGemInstances(socketed).forEachOrdered(inst -> mFloat.setValue(inst.onHurt(src, ent, mFloat.getValue())));
        return mFloat.getValue();
    }

    @Override
    public void getEnchantmentLevels(ItemStack socketed, LootRarity rarity, float level, Map<Enchantment, Integer> enchantments) {
        SocketHelper.getGemInstances(socketed).forEach(inst -> inst.getEnchantmentLevels(enchantments));
    }

    @Override
    public void modifyLoot(ItemStack socketed, LootRarity rarity, float level, ObjectArrayList<ItemStack> loot, LootContext ctx) {
        SocketHelper.getGemInstances(socketed).forEach(inst -> inst.modifyLoot(loot, ctx));
    }

    @Override
    public Codec<? extends Affix> getCodec() {
        return CODEC;
    }

    private static Stream<GemInstance> gems(AbstractArrow arrow) {
        return SocketHelper.getGemInstances(arrow);
    }

}
