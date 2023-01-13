package shadows.apotheosis.adventure.affix.effect;

import java.util.Map;
import java.util.function.Consumer;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
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

// TODO: Add items other than pickaxe/axe/shovel like hoe/sword for specific items.
public class OmneticAffix extends Affix {

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
				for (int i = 0; i < 3; i++) {
					ItemStack item = values.get(inst.rarity()).getArray()[i];
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
				for (int i = 0; i < 3; i++) {
					ItemStack item = values.get(inst.rarity()).getArray()[i];
					speed = Math.max(getBaseSpeed(e.getEntity(), item, e.getState(), e.getPosition().orElse(BlockPos.ZERO)), speed);
				}
				e.setNewSpeed(speed);
			}
		}
	}

	static class OmneticData {
		final String name;
		final ItemStack axe, shovel, pickaxe;
		transient ItemStack[] _arr;

		public OmneticData(String name, ItemStack axe, ItemStack shovel, ItemStack pickaxe) {
			this.name = name;
			this.axe = axe;
			this.shovel = shovel;
			this.pickaxe = pickaxe;
		}

		public ItemStack[] getArray() {
			if (_arr == null) _arr = new ItemStack[] { axe, shovel, pickaxe };
			return _arr;
		}

		public void write(FriendlyByteBuf buf) {
			buf.writeUtf(name);
			buf.writeItem(axe);
			buf.writeItem(shovel);
			buf.writeItem(pickaxe);
		}

		public static OmneticData read(FriendlyByteBuf buf) {
			return new OmneticData(buf.readUtf(), buf.readItem(), buf.readItem(), buf.readItem());
		}
	}

	public static Affix read(JsonObject obj) {
		Map<LootRarity, OmneticData> values = GSON.fromJson(obj.get("values"), new TypeToken<Map<LootRarity, OmneticData>>() {
		}.getType());
		return new OmneticAffix(values);
	}

	public JsonObject write() {
		return new JsonObject();
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeMap(this.values, (b, key) -> b.writeUtf(key.id()), (b, func) -> func.write(b));
	}

	public static Affix read(FriendlyByteBuf buf) {
		Map<LootRarity, OmneticData> values = buf.readMap(b -> LootRarity.byId(b.readUtf()), b -> OmneticData.read(b));
		return new OmneticAffix(values);
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
