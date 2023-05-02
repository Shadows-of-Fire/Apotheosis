package shadows.apotheosis.adventure.affix.effect;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerEvent.HarvestCheck;
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.affix.AffixInstance;
import shadows.apotheosis.adventure.affix.AffixType;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.placebo.json.ItemAdapter;
import shadows.placebo.json.PSerializer;

public class OmneticAffix extends Affix {

	//Formatter::off
	public static final Codec<OmneticAffix> CODEC = RecordCodecBuilder.create(inst -> inst
		.group(
			LootRarity.mapCodec(OmneticData.CODEC).fieldOf("values").forGetter(a -> a.values))
			.apply(inst, OmneticAffix::new)
		);
	//Formatter::on
	public static final PSerializer<OmneticAffix> SERIALIZER = PSerializer.fromCodec("Omnetic Affix", CODEC);

	protected final Map<LootRarity, OmneticData> values;

	public OmneticAffix(Map<LootRarity, OmneticData> values) {
		super(AffixType.ABILITY);
		this.values = values;
	}

	@Override
	public boolean canApplyTo(ItemStack stack, LootRarity rarity) {
		return LootCategory.forItem(stack).isBreaker() && values.containsKey(rarity);
	}

	@Override
	public void addInformation(ItemStack stack, LootRarity rarity, float level, Consumer<Component> list) {
		list.accept(Component.translatable("affix." + this.getId() + ".desc", Component.translatable("misc.apotheosis." + this.values.get(rarity).name)).withStyle(ChatFormatting.YELLOW));
	}

	public void harvest(HarvestCheck e) {
		ItemStack stack = e.getEntity().getMainHandItem();
		if (!stack.isEmpty()) {
			AffixInstance inst = AffixHelper.getAffixes(stack).get(this);
			if (inst != null) {
				OmneticData data = values.get(inst.rarity());
				for (ItemStack item : data.items()) {
					if (item.isCorrectToolForDrops(e.getTargetBlock())) {
						e.setCanHarvest(true);
						return;
					}
				}
			}
		}
	}

	// EventPriority.HIGHEST
	public void speed(BreakSpeed e) {
		ItemStack stack = e.getEntity().getMainHandItem();
		if (!stack.isEmpty()) {
			AffixInstance inst = AffixHelper.getAffixes(stack).get(this);
			if (inst != null) {
				float speed = e.getOriginalSpeed();
				OmneticData data = values.get(inst.rarity());
				for (ItemStack item : data.items()) {
					speed = Math.max(getBaseSpeed(e.getEntity(), item, e.getState(), e.getPosition().orElse(BlockPos.ZERO)), speed);
				}
				e.setNewSpeed(speed);
			}
		}
	}

	@Override
	public PSerializer<? extends Affix> getSerializer() {
		return SERIALIZER;
	}

	static record OmneticData(String name, ItemStack[] items) {

		//Formatter::off
		public static Codec<OmneticData> CODEC = RecordCodecBuilder.create(inst -> inst
			.group(
				Codec.STRING.fieldOf("name").forGetter(OmneticData::name),
				Codec.list(ItemAdapter.CODEC).xmap(l -> l.toArray(new ItemStack[0]), Arrays::asList).fieldOf("items").forGetter(OmneticData::items))
				.apply(inst, OmneticData::new)
			);
		//Formatter::on
	}

	static float getBaseSpeed(Player player, ItemStack tool, BlockState state, BlockPos pos) {
		float f = tool.getDestroySpeed(state);
		if (f > 1.0F) {
			int i = EnchantmentHelper.getBlockEfficiency(player);
			ItemStack itemstack = player.getMainHandItem();
			if (i > 0 && !itemstack.isEmpty()) {
				f += i * i + 1;
			}
		}

		if (MobEffectUtil.hasDigSpeed(player)) {
			f *= 1.0F + (MobEffectUtil.getDigSpeedAmplification(player) + 1) * 0.2F;
		}

		if (player.hasEffect(MobEffects.DIG_SLOWDOWN)) {
			float f1;
			switch (player.getEffect(MobEffects.DIG_SLOWDOWN).getAmplifier()) {
			case 0:
				f1 = 0.3F;
				break;
			case 1:
				f1 = 0.09F;
				break;
			case 2:
				f1 = 0.0027F;
				break;
			case 3:
			default:
				f1 = 8.1E-4F;
			}

			f *= f1;
		}

		if (player.isEyeInFluidType(ForgeMod.WATER_TYPE.get()) && !EnchantmentHelper.hasAquaAffinity(player)) {
			f /= 5.0F;
		}

		if (!player.isOnGround()) {
			f /= 5.0F;
		}
		return f;
	}

}
