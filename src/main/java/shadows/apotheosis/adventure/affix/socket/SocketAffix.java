package shadows.apotheosis.adventure.affix.socket;

import java.util.function.BiConsumer;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Predicates;

import net.minecraft.core.BlockPos;
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
import shadows.apotheosis.adventure.affix.socket.gem.Gem;
import shadows.apotheosis.adventure.affix.socket.gem.GemItem;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootRarity;

public final class SocketAffix extends Affix {

	public SocketAffix() {
		super(AffixType.SOCKET);
	}

	@Override
	public boolean canApplyTo(ItemStack stack, LootRarity rarity) {
		return LootCategory.forItem(stack) != null;
	}

	@Override
	public void addModifiers(ItemStack stack, LootRarity itemRarity, float numSockets, EquipmentSlot type, BiConsumer<Attribute, AttributeModifier> map) {
		LootCategory cat = LootCategory.forItem(stack);
		if (cat.isNone()) {
			AdventureModule.LOGGER.debug("Attempted to apply the attributes of affix {} on item {}, but it is not an affix-compatible item!", this.getId(), stack.getHoverName().getString());
			return;
		}

		gems(stack).forEach(pair -> pair.getLeft().addModifiers(stack, GemItem.getLootRarity(pair.getRight()), GemItem.getFacets(pair.getRight()), type, map, pair.getRight()));
	}

	@Override
	public int getDamageProtection(ItemStack stack, LootRarity itemRarity, float numSockets, DamageSource source) {
		return gems(stack).map(pair -> pair.getLeft().getDamageProtection(stack, GemItem.getLootRarity(pair.getRight()), GemItem.getFacets(pair.getRight()), source, pair.getRight())).reduce(0, Integer::sum);
	}

	@Override
	public float getDamageBonus(ItemStack stack, LootRarity itemRarity, float numSockets, MobType creatureType) {
		return gems(stack).map(pair -> pair.getLeft().getDamageBonus(stack, GemItem.getLootRarity(pair.getRight()), GemItem.getFacets(pair.getRight()), creatureType, pair.getRight())).reduce(Float::sum).orElse(0F);
	}

	@Override
	public void doPostAttack(ItemStack stack, LootRarity itemRarity, float numSockets, LivingEntity user, Entity target) {
		gems(stack).forEach(pair -> pair.getLeft().doPostAttack(stack, GemItem.getLootRarity(pair.getRight()), GemItem.getFacets(pair.getRight()), user, target, pair.getRight()));
	}

	@Override
	public void doPostHurt(ItemStack stack, LootRarity itemRarity, float numSockets, LivingEntity user, Entity attacker) {
		gems(stack).forEach(pair -> pair.getLeft().doPostHurt(stack, GemItem.getLootRarity(pair.getRight()), GemItem.getFacets(pair.getRight()), user, attacker, pair.getRight()));
	}

	@Override
	public void onArrowFired(ItemStack stack, LootRarity itemRarity, float numSockets, LivingEntity user, AbstractArrow arrow) {
		gems(stack).forEach(pair -> pair.getLeft().onArrowFired(stack, GemItem.getLootRarity(pair.getRight()), GemItem.getFacets(pair.getRight()), user, arrow, pair.getRight()));
	}

	@Override
	@Nullable
	public InteractionResult onItemUse(ItemStack stack, LootRarity itemRarity, float numSockets, UseOnContext ctx) {
		return gems(stack).map(pair -> pair.getLeft().onItemUse(stack, GemItem.getLootRarity(pair.getRight()), GemItem.getFacets(pair.getRight()), ctx, pair.getRight())).filter(Predicates.notNull()).max(InteractionResult::compareTo).orElse(null);
	}

	@Override
	public void onArrowImpact(LootRarity itemRarity, float numSockets, AbstractArrow arrow, HitResult res, Type type) {
		gems(arrow).forEach(pair -> pair.getLeft().onArrowImpact(GemItem.getLootRarity(pair.getRight()), GemItem.getFacets(pair.getRight()), arrow, res, type, pair.getRight()));
	}

	@Override
	public float onShieldBlock(ItemStack stack, LootRarity itemRarity, float numSockets, LivingEntity entity, DamageSource source, float amount) {
		return gems(stack).map(pair -> pair.getLeft().onShieldBlock(stack, GemItem.getLootRarity(pair.getRight()), GemItem.getFacets(pair.getRight()), entity, source, amount, pair.getRight())).max(Float::compareTo).orElse(amount);
	}

	@Override
	public void onBlockBreak(ItemStack stack, LootRarity itemRarity, float numSockets, Player player, LevelAccessor world, BlockPos pos, BlockState state) {
		gems(stack).forEach(pair -> pair.getLeft().onBlockBreak(stack, GemItem.getLootRarity(pair.getRight()), GemItem.getFacets(pair.getRight()), player, world, pos, state, pair.getRight()));
	}

	private static Stream<Pair<Gem, ItemStack>> gems(ItemStack stack) {
		return SocketHelper.getGems(stack).stream().map(gemStack -> Pair.of(GemItem.getGemOrLegacy(gemStack), gemStack)).filter(pair -> pair.getLeft() != null && pair.getLeft().canApplyTo(stack, GemItem.getLootRarity(pair.getRight()), pair.getRight()));
	}

	private static Stream<Pair<Gem, ItemStack>> gems(AbstractArrow arrow) {
		return Stream.empty(); // TODO: Implement
	}

}
