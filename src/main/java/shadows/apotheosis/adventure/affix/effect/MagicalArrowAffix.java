package shadows.apotheosis.adventure.affix.effect;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.affix.AffixType;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootRarity;

public class MagicalArrowAffix extends Affix {

	protected LootRarity minRarity;

	public MagicalArrowAffix(LootRarity minRarity) {
		super(AffixType.ABILITY);
		this.minRarity = minRarity;
	}

	@Override
	public boolean canApplyTo(ItemStack stack, LootRarity rarity) {
		return LootCategory.forItem(stack).isRanged() && rarity.isAtLeast(minRarity);
	}

	// EventPriority.HIGH
	public void onHurt(LivingHurtEvent e) {
		if (e.getSource().getDirectEntity() instanceof AbstractArrow arrow) {
			if (AffixHelper.getAffixes(arrow).containsKey(this)) {
				e.getSource().setMagic();
			}
		}
	}

	public static Affix read(JsonObject obj) {
		return new MagicalArrowAffix(GSON.fromJson(obj.get("min_rarity"), LootRarity.class));
	}

	public JsonObject write() {
		return new JsonObject();
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeUtf(this.minRarity.id());
	}

	public static Affix read(FriendlyByteBuf buf) {
		return new MagicalArrowAffix(LootRarity.byId(buf.readUtf()));
	}

}
