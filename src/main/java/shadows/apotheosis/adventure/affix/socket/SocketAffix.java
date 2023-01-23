package shadows.apotheosis.adventure.affix.socket;

import java.util.function.BiConsumer;
import java.util.stream.Stream;

import javax.annotation.Nullable;

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
import shadows.apotheosis.adventure.affix.socket.gem.Gem;
import shadows.apotheosis.adventure.affix.socket.gem.GemItem;
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

		gems(socketed).forEach(pair -> pair.gem().addModifiers(socketed, pair.gemStack(), GemItem.getLootRarity(pair.gemStack()), GemItem.getFacets(pair.gemStack()), type, map));
	}

	@Override
	public int getDamageProtection(ItemStack socketed, LootRarity itemRarity, float numSockets, DamageSource source) {
		return gems(socketed).map(pair -> pair.gem().getDamageProtection(socketed, pair.gemStack(), GemItem.getLootRarity(pair.gemStack()), GemItem.getFacets(pair.gemStack()), source)).reduce(0, Integer::sum);
	}

	@Override
	public float getDamageBonus(ItemStack socketed, LootRarity itemRarity, float numSockets, MobType creatureType) {
		return gems(socketed).map(pair -> pair.gem().getDamageBonus(socketed, pair.gemStack(), GemItem.getLootRarity(pair.gemStack()), GemItem.getFacets(pair.gemStack()), creatureType)).reduce(Float::sum).orElse(0F);
	}

	@Override
	public void doPostAttack(ItemStack socketed, LootRarity itemRarity, float numSockets, LivingEntity user, Entity target) {
		gems(socketed).forEach(pair -> pair.gem().doPostAttack(socketed, pair.gemStack(), GemItem.getLootRarity(pair.gemStack()), GemItem.getFacets(pair.gemStack()), user, target));
	}

	@Override
	public void doPostHurt(ItemStack socketed, LootRarity itemRarity, float numSockets, LivingEntity user, Entity attacker) {
		gems(socketed).forEach(pair -> pair.gem().doPostHurt(socketed, pair.gemStack(), GemItem.getLootRarity(pair.gemStack()), GemItem.getFacets(pair.gemStack()), user, attacker));
	}

	@Override
	public void onArrowFired(ItemStack socketed, LootRarity itemRarity, float numSockets, LivingEntity user, AbstractArrow arrow) {
		gems(socketed).forEach(pair -> pair.gem().onArrowFired(socketed, pair.gemStack(), GemItem.getLootRarity(pair.gemStack()), GemItem.getFacets(pair.gemStack()), user, arrow));
	}

	@Override
	@Nullable
	public InteractionResult onItemUse(ItemStack socketed, LootRarity itemRarity, float numSockets, UseOnContext ctx) {
		return gems(socketed).map(pair -> pair.gem().onItemUse(socketed, pair.gemStack(), GemItem.getLootRarity(pair.gemStack()), GemItem.getFacets(pair.gemStack()), ctx)).filter(Predicates.notNull()).max(InteractionResult::compareTo).orElse(null);
	}

	@Override
	public void onArrowImpact(AbstractArrow arrow, LootRarity itemRarity, float numSockets, HitResult res, Type type) {
		gems(arrow).forEach(pair -> pair.gem().onArrowImpact(arrow, pair.gemStack(), GemItem.getLootRarity(pair.gemStack()), GemItem.getFacets(pair.gemStack()), res, type));
	}

	@Override
	public float onShieldBlock(ItemStack socketed, LootRarity itemRarity, float numSockets, LivingEntity entity, DamageSource source, float amount) {
		return gems(socketed).map(pair -> pair.gem().onShieldBlock(socketed, pair.gemStack(), GemItem.getLootRarity(pair.gemStack()), GemItem.getFacets(pair.gemStack()), entity, source, amount)).max(Float::compareTo).orElse(amount);
	}

	@Override
	public void onBlockBreak(ItemStack socketed, LootRarity itemRarity, float numSockets, Player player, LevelAccessor world, BlockPos pos, BlockState state) {
		gems(socketed).forEach(pair -> pair.gem().onBlockBreak(socketed, pair.gemStack(), GemItem.getLootRarity(pair.gemStack()), GemItem.getFacets(pair.gemStack()), player, world, pos, state));
	}

	@Override
	public float getDurabilityBonusPercentage(ItemStack socketed, LootRarity rarity, float level, ServerPlayer user) {
		return (float) gems(socketed).mapToDouble(pair -> pair.gem().getDurabilityBonusPercentage(socketed, pair.gemStack(), GemItem.getLootRarity(pair.gemStack()), GemItem.getFacets(pair.gemStack()), user)).sum();
	}

	private static Stream<GemAndStack> gems(ItemStack socketed) {
		return SocketHelper.getGems(socketed).stream().map(gemStack -> new GemAndStack(GemItem.getGemOrLegacy(gemStack), gemStack)).filter(pair -> pair.gem() != null && pair.gem().canApplyTo(socketed, pair.gemStack(), GemItem.getLootRarity(pair.gemStack())));
	}

	private static Stream<GemAndStack> gems(AbstractArrow arrow) {
		return Stream.empty(); // TODO: Implement
	}

	private static record GemAndStack(Gem gem, ItemStack gemStack) {
	}

}
