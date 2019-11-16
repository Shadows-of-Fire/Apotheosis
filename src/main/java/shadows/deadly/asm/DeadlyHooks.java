package shadows.deadly.asm;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenDungeons;
import shadows.deadly.config.DeadlyConfig;
import shadows.deadly.gen.WorldGenerator;
import shadows.deadly.loot.affixes.Affix;
import shadows.deadly.loot.affixes.AffixHelper;

/**
 * ASM methods for the deadly module.
 * @author Shadows
 *
 */
public class DeadlyHooks {

	/**
	 * Injects a custom spawner into a dungeon.
	 * Called from {@link WorldGenDungeons#generate(World, Random, BlockPos)}
	 * Injected by {@link DeadlyTransformer}
	 */
	public static void setDungeonMobSpawner(World world, BlockPos pos, Random rand) {
		if (rand.nextFloat() <= DeadlyConfig.dungeonBrutalChance) {
			WorldGenerator.BRUTAL_SPAWNER.place(world, pos, rand);
		} else if (rand.nextFloat() <= DeadlyConfig.dungeonSwarmChance) {
			WorldGenerator.SWARM_SPAWNER.place(world, pos, rand);
		}
	}

	static Access access = new Access();

	public static UUID getRealUUID(UUID uuid) {
		if (access.getADM().equals(uuid)) return access.getADM();
		if (access.getASM().equals(uuid)) return access.getASM();
		return uuid;
	}

	/**
	 * ASM Hook: Called from {@link SharedMonsterAttributes#readAttributeModifierFromNBT(net.minecraft.nbt.NBTTagCompound)}
	 */
	private static class Access extends Item {
		public UUID getADM() {
			return Item.ATTACK_DAMAGE_MODIFIER;
		}

		public UUID getASM() {
			return Item.ATTACK_SPEED_MODIFIER;
		}
	}

	/**
	 * ASM Hook: Called from {@link EnchantmentHelper#getEnchantmentModifierDamage}
	 */
	public static int getProtectionLevel(Iterable<ItemStack> stacks, DamageSource source) {
		int prot = 0;
		for (ItemStack s : stacks) {
			Map<Affix, Float> affixes = AffixHelper.getAffixes(s);
			for (Map.Entry<Affix, Float> e : affixes.entrySet()) {
				prot += e.getKey().getProtectionLevel(e.getValue(), source);
			}
		}
		return prot;
	}

	/**
	 * ASM Hook: Called from {@link EnchantmentHelper#getModifierForCreature}
	 */
	public static float getExtraDamageFor(ItemStack stack, EnumCreatureAttribute type) {
		float dmg = 0;
		Map<Affix, Float> affixes = AffixHelper.getAffixes(stack);
		for (Map.Entry<Affix, Float> e : affixes.entrySet()) {
			dmg += e.getKey().getExtraDamageFor(e.getValue(), type);
		}
		return dmg;
	}

	/**
	 * ASM Hook: Called from {@link EnchantmentHelper#applyArthropodEnchantments}
	 */
	public static void onEntityDamaged(EntityLivingBase user, Entity target) {
		if (user != null) {
			for (ItemStack s : user.getEquipmentAndArmor()) {
				Map<Affix, Float> affixes = AffixHelper.getAffixes(s);
				for (Map.Entry<Affix, Float> e : affixes.entrySet()) {
					int old = target.hurtResistantTime = 0;
					e.getKey().onEntityDamaged(user, target, e.getValue());
					target.hurtResistantTime = old;
				}
			}
		}
	}

	/**
	 * ASM Hook: Called from {@link EnchantmentHelper#applyThornEnchantments}
	 */
	public static void onUserHurt(EntityLivingBase user, Entity attacker) {
		if (user != null) {
			for (ItemStack s : user.getEquipmentAndArmor()) {
				Map<Affix, Float> affixes = AffixHelper.getAffixes(s);
				for (Map.Entry<Affix, Float> e : affixes.entrySet()) {
					e.getKey().onUserHurt(user, attacker, e.getValue());
				}
			}
		}
	}

}
