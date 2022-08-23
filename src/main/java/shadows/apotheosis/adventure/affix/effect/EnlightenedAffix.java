package shadows.apotheosis.adventure.affix.effect;

import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import shadows.apotheosis.adventure.AdventureConfig;
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.affix.AffixType;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.apotheosis.util.StepFunction;

public class EnlightenedAffix extends Affix {

	protected static final StepFunction COST_FUNC = AffixHelper.step(9, 8, -1);

	public EnlightenedAffix() {
		super(AffixType.EFFECT);
	}

	@Override
	public boolean canApplyTo(ItemStack stack, LootRarity rarity) {
		return LootCategory.forItem(stack) == LootCategory.BREAKER;
	}

	@Override
	public void addInformation(ItemStack stack, LootRarity rarity, float level, Consumer<Component> list) {
		list.accept(new TranslatableComponent("affix." + this.getRegistryName() + ".desc", COST_FUNC.getInt(level)).withStyle(ChatFormatting.YELLOW));
	}

	@Override
	public InteractionResult onItemUse(ItemStack stack, LootRarity rarity, float level, UseOnContext ctx) {
		Player player = ctx.getPlayer();
		if (AdventureConfig.torchItem.get().useOn(ctx).consumesAction()) {
			if (ctx.getItemInHand().isEmpty()) ctx.getItemInHand().grow(1);
			player.getItemInHand(ctx.getHand()).hurtAndBreak(COST_FUNC.getInt(level), player, p -> p.broadcastBreakEvent(ctx.getHand()));
			return InteractionResult.SUCCESS;
		}
		return super.onItemUse(stack, rarity, level, ctx);
	}

}
