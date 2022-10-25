package shadows.apotheosis.adventure.affix.effect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.google.gson.JsonObject;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.affix.AffixInstance;
import shadows.apotheosis.adventure.affix.AffixType;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.placebo.util.StepFunction;

/**
 * Loot Pinata
 */
public class FestiveAffix extends Affix {

	protected final Map<LootRarity, StepFunction> values;

	public FestiveAffix(Map<LootRarity, StepFunction> values) {
		super(AffixType.EFFECT);
		this.values = values;
	}

	@Override
	public void addInformation(ItemStack stack, LootRarity rarity, float level, Consumer<Component> list) {
		list.accept(Component.translatable("affix." + this.getId() + ".desc", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(100 * getTrueLevel(rarity, level))).withStyle(ChatFormatting.YELLOW));
	}

	@Override
	public boolean canApplyTo(ItemStack stack, LootRarity rarity) {
		return LootCategory.forItem(stack).isLightWeapon() && this.values.containsKey(rarity);
	}

	private float getTrueLevel(LootRarity rarity, float level) {
		return this.values.get(rarity).get(level);
	}

	private static String MARKER = "apoth.equipment";

	// EventPriority.LOW
	public void markEquipment(LivingDeathEvent e) {
		e.getEntity().getAllSlots().forEach(i -> {
			if (!i.isEmpty()) i.getOrCreateTag().putBoolean(MARKER, true);
		});
	}

	// EventPriority.LOW
	public void drops(LivingDropsEvent e) {
		LivingEntity dead = e.getEntity();
		if (e.getSource().getEntity() instanceof Player player && !e.getDrops().isEmpty() && !(e.getEntity() instanceof Player)) {
			AffixInstance inst = AffixHelper.getAffixes(player.getMainHandItem()).get(this);
			if (inst == null) return;
			if (player.level.random.nextFloat() < getTrueLevel(inst.rarity(), inst.level())) {
				player.level.playSound(null, dead.getX(), dead.getY(), dead.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 4.0F, (1.0F + (player.level.random.nextFloat() - player.level.random.nextFloat()) * 0.2F) * 0.7F);
				((ServerLevel) player.level).sendParticles(ParticleTypes.EXPLOSION_EMITTER, dead.getX(), dead.getY(), dead.getZ(), 2, 1.0D, 0.0D, 0.0D, 0);
				List<ItemEntity> drops = new ArrayList<>(e.getDrops());
				for (ItemEntity item : drops) {
					if (item.getItem().hasTag() && item.getItem().getTag().contains(MARKER)) continue;
					for (int i = 0; i < 20; i++) {
						e.getDrops().add(new ItemEntity(player.level, item.getX(), item.getY(), item.getZ(), item.getItem().copy()));
					}
				}
				for (ItemEntity item : e.getDrops()) {
					if (!item.getItem().getItem().canBeDepleted()) {
						item.setPos(dead.getX(), dead.getY(), dead.getZ());
						item.setDeltaMovement(-0.3 + dead.level.random.nextDouble() * 0.6, 0.3 + dead.level.random.nextDouble() * 0.3, -0.3 + dead.level.random.nextDouble() * 0.6);
					}
				}
			}
		}
		e.getDrops().stream().forEach(ent -> {
			ItemStack s = ent.getItem();
			if (s.hasTag() && s.getTag().contains(MARKER)) {
				s.getTag().remove(MARKER);
				if (s.getTag().isEmpty()) s.setTag(null);
			}
			ent.setItem(s);
		});
	}

	public static Affix read(JsonObject obj) {
		var values = AffixHelper.readValues(GsonHelper.getAsJsonObject(obj, "values"));
		return new FestiveAffix(values);
	}

	public JsonObject write() {
		return new JsonObject();
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeMap(this.values, (b, key) -> b.writeUtf(key.id()), (b, func) -> func.write(b));
	}

	public static Affix read(FriendlyByteBuf buf) {
		Map<LootRarity, StepFunction> values = buf.readMap(b -> LootRarity.byId(b.readUtf()), b -> StepFunction.read(b));
		return new FestiveAffix(values);
	}
}
