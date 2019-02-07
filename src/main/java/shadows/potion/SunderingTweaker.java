package shadows.potion;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.SortingIndex;
import shadows.ApotheosisCore;
import shadows.CustomClassWriter;

@SortingIndex(1001)
public class SunderingTweaker implements IClassTransformer {

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if ("net.minecraft.entity.EntityLivingBase".equals(transformedName)) return transformEntityLiving(basicClass);
		return basicClass;
	}

	static byte[] transformEntityLiving(byte[] basicClass) {
		ApotheosisCore.LOG.info("Transforming EntityLivingBase...");
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(basicClass);
		classReader.accept(classNode, 0);
		MethodNode applyPotionDamageCalculations = null;
		for (MethodNode m : classNode.methods) {
			if (ApotheosisCore.isCalcDamage(m)) {
				applyPotionDamageCalculations = m;
				break;
			}
		}
		if (applyPotionDamageCalculations != null) {
			InsnList insn = new InsnList();
			insn.add(new VarInsnNode(Opcodes.ALOAD, 0));
			insn.add(new VarInsnNode(Opcodes.ALOAD, 1));
			insn.add(new VarInsnNode(Opcodes.FLOAD, 2));
			insn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "shadows/potion/PotionModule", "applyPotionDamageCalculations", "(Ljava/lang/Object;Ljava/lang/Object;F)F", false));
			insn.add(new InsnNode(Opcodes.FRETURN));
			applyPotionDamageCalculations.instructions.insert(insn);
			CustomClassWriter writer = new CustomClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
			classNode.accept(writer);
			ApotheosisCore.LOG.info("Successfully transformed EntityLivingBase");
			return writer.toByteArray();
		}
		ApotheosisCore.LOG.info("Failed transforming EntityLivingBase");
		return basicClass;
	}

}
