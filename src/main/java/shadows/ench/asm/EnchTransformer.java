package shadows.ench.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import shadows.ApotheosisCore;
import shadows.ApotheosisTransformer.IApotheosisTransformer;
import shadows.CustomClassWriter;

public class EnchTransformer implements IApotheosisTransformer {

	@Override
	public boolean accepts(String name, String transformedName) {
		return "net.minecraft.enchantment.EnchantmentHelper".equals(transformedName);
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		ApotheosisCore.LOG.info("Transforming EnchantmentHelper...");
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(basicClass);
		classReader.accept(classNode, 0);
		MethodNode calcStackEnch = null;
		MethodNode getEnchantmentDatas = null;
		for (MethodNode m : classNode.methods) {
			if (ApotheosisCore.isCalcStackEnch(m)) {
				calcStackEnch = m;
			} else if (ApotheosisCore.isEnchDatas(m)) {
				getEnchantmentDatas = m;
			}
		}
		if (calcStackEnch != null) {
			JumpInsnNode jumpNode = null;
			for (int i = 0; i < calcStackEnch.instructions.size(); i++) {
				AbstractInsnNode n = calcStackEnch.instructions.get(i);
				if (n.getOpcode() == Opcodes.BIPUSH && ((IntInsnNode) n).operand == 15) {
					jumpNode = (JumpInsnNode) n.getNext();
					break;
				}
			}
			if (jumpNode != null) {
				calcStackEnch.instructions.insert(jumpNode, new JumpInsnNode(Opcodes.GOTO, jumpNode.label));
				ApotheosisCore.LOG.info("Successfully transformed EnchantmentHelper.calcItemStackEnchantability");
			}
		}
		if (getEnchantmentDatas != null) {
			InsnList insn = new InsnList();
			insn.add(new VarInsnNode(Opcodes.ILOAD, 0));
			insn.add(new VarInsnNode(Opcodes.ALOAD, 1));
			insn.add(new VarInsnNode(Opcodes.ILOAD, 2));
			insn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "shadows/ench/EnchModule", "getEnchantmentDatas", "(ILjava/lang/Object;Z)Ljava/util/List;", false));
			insn.add(new InsnNode(Opcodes.ARETURN));
			getEnchantmentDatas.instructions.insert(insn);
			ApotheosisCore.LOG.info("Successfully transformed EnchantmentHelper.getEnchantmentDatas");
		}
		if (calcStackEnch != null && getEnchantmentDatas != null) {
			CustomClassWriter writer = new CustomClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
			classNode.accept(writer);
			ApotheosisCore.LOG.info("Successfully transformed EnchantmentHelper");
			return writer.toByteArray();
		}
		ApotheosisCore.LOG.info("Failed transforming EnchantmentHelper");
		return basicClass;
	}

}
