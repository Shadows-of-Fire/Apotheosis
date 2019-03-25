package shadows;

import java.util.Map;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

public class ApotheosisCore implements IFMLLoadingPlugin {

	public static boolean enableEnch = true;
	public static boolean enableSpawner = true;
	public static boolean enablePotion = true;
	public static boolean enableDeadly = true;

	static String updateRepair;
	static String capsIsCreative;
	static String empty;
	static String drawForeground;
	static String format;
	static String calcStackEnch;
	static String doesShowParticles;
	static String applyPotionDamageCalculations;
	static String playerCapabilities = "net/minecraft/entity/player/PlayerCapabilities";
	static String itemStack = "net/minecraft/item/ItemStack";
	static String damageSource = "net/minecraft/util/DamageSource";
	static String getEnchantmentDatas;
	static String isTempting;
	static String getItemEnchantability;
	static String blockUsingShield;
	static String entityLivingBase = "net/minecraft/entity/EntityLivingBase";
	static String enchantment = "net/minecraft/enchantment/Enchantment";
	static String getMaxLevel;
	static String world = "net/minecraft/world/World";
	static String blockPos = "net/minecraft/util/math/BlockPos";
	static String generate;
	static String onBlockActivated;
	static String iBlockState = "net/minecraft/block/state/IBlockState";
	static String entityPlayer = "net/minecraft/entity/player/EntityPlayer";
	static String enumHand = "net/minecraft/util/EnumHand";
	static String enumFacing = "net/minecraft/util/EnumFacing";

	public static final Logger LOG = LogManager.getLogger("Apotheosis : Core");

	@Override
	public String[] getASMTransformerClass() {
		return new String[] { "shadows.ApotheosisTransformer" };
	}

	@Override
	public String getModContainerClass() {
		return null;
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
		boolean dev = !(Boolean) data.get("runtimeDeobfuscationEnabled");
		updateRepair = dev ? "updateRepairOutput" : "e";
		capsIsCreative = dev ? "isCreativeMode" : "d";
		empty = dev ? "EMPTY" : "a";
		drawForeground = dev ? "drawGuiContainerForegroundLayer" : "c";
		calcStackEnch = dev ? "calcItemStackEnchantability" : "a";
		doesShowParticles = dev ? "doesShowParticles" : "e";
		applyPotionDamageCalculations = dev ? "applyPotionDamageCalculations" : "c";
		if (!dev) {
			playerCapabilities = FMLDeobfuscatingRemapper.INSTANCE.unmap(playerCapabilities);
			itemStack = FMLDeobfuscatingRemapper.INSTANCE.unmap(itemStack);
			damageSource = FMLDeobfuscatingRemapper.INSTANCE.unmap(damageSource);
			entityLivingBase = FMLDeobfuscatingRemapper.INSTANCE.unmap(entityLivingBase);
			enchantment = FMLDeobfuscatingRemapper.INSTANCE.unmap(enchantment);
			world = FMLDeobfuscatingRemapper.INSTANCE.unmap(world);
			blockPos = FMLDeobfuscatingRemapper.INSTANCE.unmap(blockPos);
			iBlockState = FMLDeobfuscatingRemapper.INSTANCE.unmap(iBlockState);
			entityPlayer = FMLDeobfuscatingRemapper.INSTANCE.unmap(entityPlayer);
			enumHand = FMLDeobfuscatingRemapper.INSTANCE.unmap(enumHand);
			enumFacing = FMLDeobfuscatingRemapper.INSTANCE.unmap(enumFacing);
		}
		getEnchantmentDatas = dev ? "getEnchantmentDatas" : "a";
		isTempting = dev ? "isTempting" : "a";
		getItemEnchantability = dev ? "getItemEnchantability" : "c";
		blockUsingShield = dev ? "blockUsingShield" : "c";
		getMaxLevel = dev ? "getMaxLevel" : "b";
		generate = dev ? "generate" : "a";
		onBlockActivated = dev ? "onBlockActivated" : "a";
	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}

	public static MethodNode findMethod(ClassNode node, Predicate<MethodNode> finder) {
		for (MethodNode m : node.methods) {
			if (finder.test(m)) return m;
		}
		return null;
	}

	public static boolean isRepairOutput(MethodNode m) {
		return m.name.equals(updateRepair) && m.desc.equals("()V");
	}

	public static boolean isCapIsCreative(FieldInsnNode fn) {
		return fn.owner.equals(playerCapabilities) && fn.name.equals(capsIsCreative);
	}

	public static boolean isEmptyStack(FieldInsnNode fn) {
		return fn.owner.equals(itemStack) && fn.name.equals(empty);
	}

	public static boolean isDrawForeground(MethodNode m) {
		return m.name.equals(drawForeground) && m.desc.equals("(II)V");
	}

	public static boolean isCalcStackEnch(MethodNode m) {
		return m.name.equals(calcStackEnch) && m.desc.equals(String.format("(Ljava/util/Random;IIL%s;)I", itemStack));
	}

	public static boolean isShowParticles(MethodNode m) {
		return m.name.equals(doesShowParticles) && m.desc.equals("()Z");
	}

	public static boolean isCalcDamage(MethodNode m) {
		return m.name.equals(applyPotionDamageCalculations) && m.desc.equals(String.format("(L%s;F)F", damageSource));
	}

	public static boolean isEnchDatas(MethodNode m) {
		return m.name.equals(getEnchantmentDatas) && m.desc.equals(String.format("(IL%s;Z)Ljava/util/List;", itemStack));
	}

	public static boolean isTempting(MethodNode m) {
		return m.name.equals(isTempting) && m.desc.equals(String.format("(L%s;)Z", itemStack));
	}

	public static boolean isItemEnch(MethodNode m) {
		return m.name.equals(getItemEnchantability) && m.desc.equals("()I");
	}

	public static boolean isBlockWithShield(MethodNode m) {
		return m.name.equals(blockUsingShield) && m.desc.equals(String.format("(L%s;)V", entityLivingBase));
	}

	public static boolean isGetMaxLevel(MethodInsnNode m) {
		return m.owner.equals(enchantment) && m.name.equals(getMaxLevel) && m.desc.equals("()I");
	}

	public static boolean isGenerate(MethodNode m) {
		return m.name.equals(generate) && m.desc.equals(String.format("(L%s;Ljava/util/Random;L%s;)Z", world, blockPos));
	}

	public static boolean isOnBlockActivated(MethodNode m) {
		return m.name.equals(onBlockActivated) && m.desc.equals(String.format("(L%s;L%s;L%s;L%s;L%s;L%s;FFF)Z", world, blockPos, iBlockState, entityPlayer, enumHand, enumFacing));
	}

}
