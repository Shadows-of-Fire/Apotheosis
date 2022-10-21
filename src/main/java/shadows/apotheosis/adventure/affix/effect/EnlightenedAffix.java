package shadows.apotheosis.adventure.affix.effect;

import java.util.Map;
import java.util.function.Consumer;

import com.google.gson.JsonObject;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.GsonHelper;
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
import shadows.placebo.util.StepFunction;

public class EnlightenedAffix extends Affix {

	protected final Map<LootRarity, StepFunction> values;

	public EnlightenedAffix(Map<LootRarity, StepFunction> values) {
		super(AffixType.EFFECT);
		this.values = values;
	}

	@Override
	public boolean canApplyTo(ItemStack stack, LootRarity rarity) {
		return LootCategory.forItem(stack) == LootCategory.BREAKER && values.containsKey(rarity);
	}

	@Override
	public void addInformation(ItemStack stack, LootRarity rarity, float level, Consumer<Component> list) {
		list.accept(new TranslatableComponent("affix." + this.getId() + ".desc", values.get(rarity).getInt(level)).withStyle(ChatFormatting.YELLOW));
	}

	@Override
	public InteractionResult onItemUse(ItemStack stack, LootRarity rarity, float level, UseOnContext ctx) {
		Player player = ctx.getPlayer();
		if (AdventureConfig.torchItem.get().useOn(ctx).consumesAction()) {
			if (ctx.getItemInHand().isEmpty()) ctx.getItemInHand().grow(1);
			player.getItemInHand(ctx.getHand()).hurtAndBreak(values.get(rarity).getInt(level), player, p -> p.broadcastBreakEvent(ctx.getHand()));
			return InteractionResult.SUCCESS;
		}
		return super.onItemUse(stack, rarity, level, ctx);
	}

	public static Affix read(JsonObject obj) {
		var values = AffixHelper.readValues(GsonHelper.getAsJsonObject(obj, "values"));
		return new EnlightenedAffix(values);
	}

	public JsonObject write() {
		return new JsonObject();
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeMap(this.values, (b, key) -> b.writeUtf(key.id()), (b, func) -> func.write(b));
	}

	public static Affix read(FriendlyByteBuf buf) {
		Map<LootRarity, StepFunction> values = buf.readMap(b -> LootRarity.byId(b.readUtf()), b -> StepFunction.read(b));
		return new EnlightenedAffix(values);
	}

}
