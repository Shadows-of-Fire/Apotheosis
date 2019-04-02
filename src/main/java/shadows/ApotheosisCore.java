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
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.SortingIndex;

@MCVersion("1.12.2")
@SortingIndex(1001)
public class ApotheosisCore implements IFMLLoadingPlugin {

	public static boolean enableEnch = true;
	public static boolean enableSpawner = true;
	public static boolean enablePotion = true;
	public static boolean enableDeadly = true;

	//ContainerRepair#updateRepairOutput
	private static String updateRepair;

	//PlayerCapabilities#isCreativeMode
	private static String capsIsCreative;

	//ItemStack.EMPTY
	private static String empty;

	//GuiRepair#drawContainerForegroundLayer
	private static String drawForeground;

	//EnchantmentHelper#calcItemStackEnchantability
	private static String calcStackEnch;

	//PotionEffect#doesShowParticles
	private static String doesShowParticles;

	//EntityLivingBase#applyPotionDamageCalculations
	private static String applyPotionDamageCalculations;

	//EnchantmentHelper#getEnchantmentDatas
	private static String getEnchantmentDatas;

	//EntityAITempt#isTempting
	private static String isTempting;

	//Item#getItemEnchantability
	private static String getItemEnchantability;

	//EntityLivingBase#blockUsingShield
	private static String blockUsingShield;

	//Enchantment#getMaxLevel
	private static String getMaxLevel;

	//WorldGenerator#generate
	private static String generate;

	//Block#onBlockActivated
	private static String onBlockActivated;

	private static String enchantment = "net/minecraft/enchantment/Enchantment";
	private static String playerCapabilities = "net/minecraft/entity/player/PlayerCapabilities";
	private static String itemStack = "net/minecraft/item/ItemStack";

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
		updateRepair = dev ? "updateRepairOutput" : "func_82848_d";
		capsIsCreative = dev ? "isCreativeMode" : "field_75098_d";
		empty = dev ? "EMPTY" : "field_190927_a";
		drawForeground = dev ? "drawGuiContainerForegroundLayer" : "func_146979_b";
		calcStackEnch = dev ? "calcItemStackEnchantability" : "func_77514_a";
		doesShowParticles = dev ? "doesShowParticles" : "func_188418_e";
		applyPotionDamageCalculations = dev ? "applyPotionDamageCalculations" : "func_70672_c";
		getEnchantmentDatas = dev ? "getEnchantmentDatas" : "func_185291_a";
		isTempting = dev ? "isTempting" : "func_188508_a";
		getItemEnchantability = dev ? "getItemEnchantability" : "func_77619_b";
		blockUsingShield = dev ? "blockUsingShield" : "func_190629_c";
		getMaxLevel = dev ? "getMaxLevel" : "func_77325_b";
		generate = dev ? "generate" : "func_180709_b";
		onBlockActivated = dev ? "onBlockActivated" : "func_180639_a";

		if (!dev) {
			playerCapabilities = FMLDeobfuscatingRemapper.INSTANCE.unmap(playerCapabilities);
			itemStack = FMLDeobfuscatingRemapper.INSTANCE.unmap(itemStack);
			enchantment = FMLDeobfuscatingRemapper.INSTANCE.unmap(enchantment);
		}
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
		return m.name.equals(updateRepair);
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
		return m.name.equals(calcStackEnch);
	}

	public static boolean isShowParticles(MethodNode m) {
		return m.name.equals(doesShowParticles) && m.desc.equals("()Z");
	}

	public static boolean isCalcDamage(MethodNode m) {
		return m.name.equals(applyPotionDamageCalculations);
	}

	public static boolean isEnchDatas(MethodNode m) {
		return m.name.equals(getEnchantmentDatas);
	}

	public static boolean isTempting(MethodNode m) {
		return m.name.equals(isTempting);
	}

	public static boolean isItemEnch(MethodNode m) {
		return m.name.equals(getItemEnchantability) && m.desc.equals("()I");
	}

	public static boolean isBlockWithShield(MethodNode m) {
		return m.name.equals(blockUsingShield);
	}

	public static boolean isGetMaxLevel(MethodInsnNode m) {
		return m.owner.equals(enchantment) && m.name.equals(getMaxLevel) && m.desc.equals("()I");
	}

	public static boolean isGenerate(MethodNode m) {
		return m.name.equals(generate);
	}

	public static boolean isOnBlockActivated(MethodNode m) {
		return m.name.equals(onBlockActivated);
	}

}
