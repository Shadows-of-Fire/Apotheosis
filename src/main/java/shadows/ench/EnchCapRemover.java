package shadows.ench;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodNode;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.SortingIndex;
import shadows.ApotheosisCore;
import shadows.CustomClassWriter;

@SortingIndex(1001)
public class EnchCapRemover implements IClassTransformer {

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (!ApotheosisCore.enableEnch) return basicClass;
		if ("net.minecraft.enchantment.EnchantmentHelper".equals(transformedName)) return transformEnchHelper(basicClass);
		return basicClass;
	}

	static byte[] transformEnchHelper(byte[] basicClass) {
		ApotheosisCore.LOG.info("Transforming EnchantmentHelper...");
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(basicClass);
		classReader.accept(classNode, 0);
		MethodNode calcStackEnch = null;
		for (MethodNode m : classNode.methods) {
			if (ApotheosisCore.isCalcStackEnch(m)) {
				calcStackEnch = m;
				break;
			}
		}
		if (calcStackEnch != null) {
			JumpInsnNode jumpNode = null;
			for (int i = 0; i < calcStackEnch.instructions.size(); i++) {
				AbstractInsnNode n = calcStackEnch.instructions.get(i);
				if (n.getOpcode() == Opcodes.BIPUSH && 
						((IntInsnNode) n).operand == 15) {
					jumpNode = (JumpInsnNode) n.getNext();
					break;
				}
			}
			if (jumpNode != null) {
				calcStackEnch.instructions.insert(jumpNode, new JumpInsnNode(Opcodes.GOTO, jumpNode.label));
				CustomClassWriter writer = new CustomClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
				classNode.accept(writer);
				ApotheosisCore.LOG.info("Successfully transformed EnchantmentHelper");
				return writer.toByteArray();
			}
		}
		ApotheosisCore.LOG.info("Failed transforming EnchantmentHelper");
		return basicClass;
	}

}
