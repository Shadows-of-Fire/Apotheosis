package shadows;

import java.util.Map;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;

import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

public class ApotheosisCore implements IFMLLoadingPlugin {

	public static boolean enableAnvil = true;
	public static boolean enableEnch = true;
	public static boolean enableInvis = true;

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
		getEnchantmentDatas = dev ? "getEnchantmentDatas" : "a";
		if (!dev) {
			playerCapabilities = FMLDeobfuscatingRemapper.INSTANCE.unmap(playerCapabilities);
			itemStack = FMLDeobfuscatingRemapper.INSTANCE.unmap(itemStack);
			damageSource = FMLDeobfuscatingRemapper.INSTANCE.unmap(damageSource);
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

}
