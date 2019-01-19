package shadows.deadly.feature;
/*
import java.util.Random;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import shadows.deadly.util.AttributeHelper;
import shadows.deadly.util.EffectHelper;

public class Skeleturret implements WorldFeature {
	/// Mob stats.
	public static final double FIRE_CHANCE = Properties.getDouble(Properties.TURRETS, "fire_bow_chance");
	public static final double HEALTH_MULT = Properties.getDouble(Properties.TURRETS, "health_multiplier");
	/// The equipment for turrets.
	public static final ItemStack[] EQUIPMENT = new ItemStack[5];
	static {
		Skeleturret.EQUIPMENT[0] = new ItemStack(Items.bow);
		Skeleturret.EQUIPMENT[0].setStackDisplayName("\u00a7bTurret Cannon");
		Skeleturret.EQUIPMENT[0].addEnchantment(Enchantment.power, Math.max(0, Math.min(10, Properties.getInt(Properties.TURRETS, "bow_power"))));
		EffectHelper.addModifier(Skeleturret.EQUIPMENT[0], SharedMonsterAttributes.movementSpeed.getAttributeUnlocalizedName(), -1.0, 2);

		Skeleturret.EQUIPMENT[4] = new ItemStack(Blocks.dispenser);
		Skeleturret.EQUIPMENT[3] = new ItemStack(Items.leather_chestplate);
		Skeleturret.EQUIPMENT[2] = new ItemStack(Items.leather_leggings);
		Skeleturret.EQUIPMENT[1] = new ItemStack(Items.leather_boots);
		for (int i = 4; i-- > 1;) {
			Skeleturret.EQUIPMENT[i].setStackDisplayName("\u00a7bTurret Plating");
			EffectHelper.dye(Skeleturret.EQUIPMENT[i], 0x707070);
			EffectHelper.addModifier(Skeleturret.EQUIPMENT[i], SharedMonsterAttributes.movementSpeed.getAttributeUnlocalizedName(), -0.33, 1);
		}
	}

	/// The chance for this to appear in any given chunk. Determined by the properties file.
	public final double frequency;

	public Skeleturret(double freq) {
		this.frequency = freq;
	}

	/// Attempts to generate this feature in the given chunk. Block position is given.
	@Override
	public void generate(World world, Random random, int x, int z) {
		if (this.frequency <= random.nextDouble()) return;
		x += random.nextInt(16);
		z += random.nextInt(16);
		int y = random.nextInt(30) + 12;
		for (byte state = 0; y > 5; y--) {
			if (world.isBlockNormalCubeDefault(x, y, z, true)) {
				if (state == 0) {
					if (this.canBePlaced(world, random, x, y, z)) {
						this.place(world, random, x, y, z);
						return;
					}
					state = -1;
				}
			} else {
				state = 0;
			}
		}
	}

	/// Returns true if this feature can be placed at the location.
	@Override
	public boolean canBePlaced(World world, Random random, int x, int y, int z) {
		return world.isBlockNormalCubeDefault(x, y - 1, z, false) && world.isBlockNormalCubeDefault(x - 1, y, z, false) && world.isBlockNormalCubeDefault(x + 1, y, z, false) && world.isBlockNormalCubeDefault(x, y, z - 1, false) && world.isBlockNormalCubeDefault(x, y, z + 1, false) && world.isAirBlock(x, y + 1, z);
	}

	/// Places this feature at the location.
	@Override
	public void place(World world, Random random, int x, int y, int z) {
		world.setBlockToAir(x, y, z);
		EntitySkeleton turret = new EntitySkeleton(world);
		AttributeHelper.baseMult(turret, SharedMonsterAttributes.maxHealth, "DW|TurretHealthMult", Skeleturret.HEALTH_MULT);
		AttributeHelper.shift(turret, SharedMonsterAttributes.knockbackResistance, "DW|TurretKnockbackResist", 1.0);
		AttributeHelper.mult(turret, SharedMonsterAttributes.movementSpeed, "DW|TurretSpeedMult", -1.0);
		AttributeHelper.set(turret, SharedMonsterAttributes.followRange, "DW|TurretFollowRange", 14.0);
		turret.setHealth(turret.getMaxHealth());
		turret.setPositionAndRotation(x + 0.5, y, z + 0.5, random.nextFloat() * 360.0F, 0.0F);
		turret.func_110163_bv(); /// sets the entity to not be despawned

		ItemStack bow = Skeleturret.EQUIPMENT[0].copy();
		if (random.nextDouble() < Skeleturret.FIRE_CHANCE) {
			bow.addEnchantment(Enchantment.flame, 1);
		}
		turret.setCurrentItemOrArmor(0, bow);
		for (int i = Skeleturret.EQUIPMENT.length; i-- > 1;) {
			turret.setCurrentItemOrArmor(i, Skeleturret.EQUIPMENT[i].copy());
		}

		turret.addPotionEffect(new PotionEffect(Potion.fireResistance.id, Integer.MAX_VALUE, 0, true));
		turret.addPotionEffect(new PotionEffect(Potion.waterBreathing.id, Integer.MAX_VALUE, 0, true));
		turret.addPotionEffect(new PotionEffect(Potion.invisibility.id, Integer.MAX_VALUE, 0, true));
		world.spawnEntityInWorld(turret);
	}
}*/