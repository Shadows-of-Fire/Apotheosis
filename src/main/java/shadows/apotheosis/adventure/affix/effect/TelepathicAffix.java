package shadows.apotheosis.adventure.affix.effect;

import java.util.function.Consumer;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.affix.AffixInstance;
import shadows.apotheosis.adventure.affix.AffixType;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.placebo.json.PSerializer;

/**
 * Teleport Drops
 */
public class TelepathicAffix extends Affix {

	//Formatter::off
	public static final Codec<TelepathicAffix> CODEC = RecordCodecBuilder.create(inst -> inst
		.group(
			LootRarity.CODEC.fieldOf("min_rarity").forGetter(a -> a.minRarity))
			.apply(inst, TelepathicAffix::new)
		);
	//Formatter::on
	public static final PSerializer<TelepathicAffix> SERIALIZER = PSerializer.fromCodec("Telepathic Affix", CODEC);

	public static Vec3 blockDropTargetPos = null;

	protected LootRarity minRarity;

	public TelepathicAffix(LootRarity minRarity) {
		super(AffixType.ABILITY);
		this.minRarity = minRarity;
	}

	@Override
	public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
		return (cat.isRanged() || cat.isLightWeapon() || cat.isBreaker()) && rarity.isAtLeast(minRarity);
	}

	@Override
	public void addInformation(ItemStack stack, LootRarity rarity, float level, Consumer<Component> list) {
		LootCategory cat = LootCategory.forItem(stack);
		String type = cat.isRanged() || cat.isWeapon() ? "weapon" : "tool";
		list.accept(Component.translatable("affix." + this.getId() + ".desc." + type));
	}

	@Override
	public boolean enablesTelepathy() {
		return true;
	}

	@Override
	public PSerializer<? extends Affix> getSerializer() {
		return SERIALIZER;
	}

	// EventPriority.LOWEST
	public static void drops(LivingDropsEvent e) {
		DamageSource src = e.getSource();
		boolean canTeleport = false;
		Vec3 targetPos = null;
		if (src.getDirectEntity() instanceof AbstractArrow arrow && arrow.getOwner() != null) {
			canTeleport = AffixHelper.streamAffixes(arrow).anyMatch(AffixInstance::enablesTelepathy);
			targetPos = arrow.getOwner().position();
		} else if (src.getDirectEntity() instanceof LivingEntity living) {
			ItemStack weapon = living.getMainHandItem();
			canTeleport = AffixHelper.streamAffixes(weapon).anyMatch(AffixInstance::enablesTelepathy);
			targetPos = living.position();
		}

		if (canTeleport) {
			for (ItemEntity item : e.getDrops()) {
				item.setPos(targetPos.x, targetPos.y, targetPos.z);
				item.setPickUpDelay(0);
			}
		}
	}

	public static Affix read(JsonObject obj) {
		return new TelepathicAffix(GSON.fromJson(obj.get("min_rarity"), LootRarity.class));
	}

	public JsonObject write() {
		return new JsonObject();
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeUtf(this.minRarity.id());
	}

	public static Affix read(FriendlyByteBuf buf) {
		return new TelepathicAffix(LootRarity.byId(buf.readUtf()));
	}

}
