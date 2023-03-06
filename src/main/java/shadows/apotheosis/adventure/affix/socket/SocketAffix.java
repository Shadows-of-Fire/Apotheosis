package shadows.apotheosis.adventure.affix.socket;

import java.util.function.BiConsumer;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableFloat;

import com.google.common.base.Predicates;

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
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import shadows.apotheosis.adventure.AdventureModule;
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.affix.AffixType;
import shadows.apotheosis.adventure.affix.socket.gem.GemInstance;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootRarity;

public final class SocketAffix extends Affix {

	public SocketAffix() {
		super(AffixType.SOCKET);
	}

	@Override
	public boolean canApplyTo(ItemStack socketed, LootRarity rarity) {
		return LootCategory.forItem(socketed) != null;
	}

	@Override
	public void addModifiers(ItemStack socketed, LootRarity itemRarity, float numSockets, EquipmentSlot type, BiConsumer<Attribute, AttributeModifier> map) {
		LootCategory cat = LootCategory.forItem(socketed);
		if (cat.isNone()) {
			AdventureModule.LOGGER.debug("Attempted to apply the attributes of affix {} on item {}, but it is not an affix-compatible item!", this.getId(), socketed.getHoverName().getString());
			return;
		}

		legacyGems(socketed).forEach(ctx -> ctx.gem().addModifiers(socketed, ctx.gemStack(), ctx.rarity(), ctx.facets(), type, map));
	}

	@Override
	public int getDamageProtection(ItemStack socketed, LootRarity itemRarity, float numSockets, DamageSource source) {
		return gems(socketed).map(ctx -> ctx.gem().getDamageProtection(socketed, ctx.gemStack(), ctx.rarity(), ctx.facets(), source)).reduce(0, Integer::sum);
	}

	@Override
	public float getDamageBonus(ItemStack socketed, LootRarity itemRarity, float numSockets, MobType creatureType) {
		return gems(socketed).map(ctx -> ctx.gem().getDamageBonus(socketed, ctx.gemStack(), ctx.rarity(), ctx.facets(), creatureType)).reduce(Float::sum).orElse(0F);
	}

	@Override
	public void doPostAttack(ItemStack socketed, LootRarity itemRarity, float numSockets, LivingEntity user, Entity target) {
		gems(socketed).forEach(ctx -> ctx.gem().doPostAttack(socketed, ctx.gemStack(), ctx.rarity(), ctx.facets(), user, target));
	}

	@Override
	public void doPostHurt(ItemStack socketed, LootRarity itemRarity, float numSockets, LivingEntity user, Entity attacker) {
		gems(socketed).forEach(ctx -> ctx.gem().doPostHurt(socketed, ctx.gemStack(), ctx.rarity(), ctx.facets(), user, attacker));
	}

	@Override
	public void onArrowFired(ItemStack socketed, LootRarity itemRarity, float numSockets, LivingEntity user, AbstractArrow arrow) {
		gems(socketed).forEach(ctx -> ctx.gem().onArrowFired(socketed, ctx.gemStack(), ctx.rarity(), ctx.facets(), user, arrow));
	}

	@Override
	@Nullable
	public InteractionResult onItemUse(ItemStack socketed, LootRarity itemRarity, float numSockets, UseOnContext useCtx) {
		return gems(socketed).map(ctx -> ctx.gem().onItemUse(socketed, ctx.gemStack(), ctx.rarity(), ctx.facets(), useCtx)).filter(Predicates.notNull()).max(InteractionResult::compareTo).orElse(null);
	}

	@Override
	public void onArrowImpact(AbstractArrow arrow, LootRarity itemRarity, float numSockets, HitResult res, Type type) {
		gems(arrow).forEach(ctx -> ctx.gem().onArrowImpact(arrow, ctx.gemStack(), ctx.rarity(), ctx.facets(), res, type));
	}

	@Override
	public float onShieldBlock(ItemStack socketed, LootRarity itemRarity, float numSockets, LivingEntity entity, DamageSource source, float amount) {
		return gems(socketed).map(ctx -> ctx.gem().onShieldBlock(socketed, ctx.gemStack(), ctx.rarity(), ctx.facets(), entity, source, amount)).max(Float::compareTo).orElse(amount);
	}

	@Override
	public void onBlockBreak(ItemStack socketed, LootRarity itemRarity, float numSockets, Player player, LevelAccessor world, BlockPos pos, BlockState state) {
		gems(socketed).forEach(ctx -> ctx.gem().onBlockBreak(socketed, ctx.gemStack(), ctx.rarity(), ctx.facets(), player, world, pos, state));
	}

	@Override
	public float getDurabilityBonusPercentage(ItemStack socketed, LootRarity rarity, float level, ServerPlayer user) {
		return (float) gems(socketed).mapToDouble(ctx -> ctx.gem().getDurabilityBonusPercentage(socketed, ctx.gemStack(), ctx.rarity(), ctx.facets(), user)).sum();
	}

	@Override
	public float onHurt(ItemStack socketed, LootRarity rarity, float level, DamageSource src, LivingEntity ent, float amount) {
		MutableFloat mFloat = new MutableFloat(amount);
		gems(socketed).forEachOrdered(ctx -> mFloat.setValue(ctx.gem().onHurt(socketed, ctx.gemStack(), ctx.rarity(), ctx.facets(), src, ent, mFloat.getValue())));
		return mFloat.getValue();
	}

	private static Stream<GemInstance> gems(ItemStack socketed) {
		return SocketHelper.getGems(socketed).stream().map(GemInstance::new).filter(ctx -> ctx.isValidIn(socketed));
	}

	private static Stream<GemInstance> legacyGems(ItemStack socketed) {
		return SocketHelper.getGems(socketed).stream().map(GemInstance::orLegacy).filter(ctx -> ctx.isValidIn(socketed));
	}

	private static Stream<GemInstance> gems(AbstractArrow arrow) {
		return Stream.empty(); // TODO: Implement
	}

}
