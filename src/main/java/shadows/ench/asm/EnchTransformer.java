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
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import shadows.ApotheosisCore;
import shadows.ApotheosisTransformer.IApotheosisTransformer;
import shadows.CustomClassWriter;

public class EnchTransformer implements IApotheosisTransformer {

	@Override
	public boolean accepts(String name, String transformedName) {
		return "net.minecraft.enchantment.EnchantmentHelper".equals(transformedName) || "net.minecraft.entity.ai.EntityAITempt".equals(transformedName) || "net.minecraft.item.Item".equals(transformedName) || "net.minecraft.entity.EntityLivingBase".equals(transformedName);
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (!ApotheosisCore.enableEnch) return basicClass;
		if ("net.minecraft.enchantment.EnchantmentHelper".equals(transformedName)) return transformEnchHelper(basicClass);
		else if ("net.minecraft.item.Item".equals(transformedName)) return transformItem(basicClass);
		else if ("net.minecraft.entity.EntityLivingBase".equals(transformedName)) return transformELB(basicClass);
		return transformAI(basicClass);
	}

	public byte[] transformEnchHelper(byte[] basicClass) {
		ApotheosisCore.LOG.info("Transforming EnchantmentHelper...");
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(basicClass);
		classReader.accept(classNode, 0);
		MethodNode calcStackEnch = ApotheosisCore.findMethod(classNode, ApotheosisCore::isCalcStackEnch);
		MethodNode getEnchantmentDatas = ApotheosisCore.findMethod(classNode, ApotheosisCore::isEnchDatas);
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
			insn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "shadows/ench/asm/EnchHooks", "getEnchantmentDatas", "(ILnet/minecraft/item/ItemStack;Z)Ljava/util/List;", false));
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

	public byte[] transformAI(byte[] basicClass) {
		ApotheosisCore.LOG.info("Transforming EntityAITempt...");
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(basicClass);
		classReader.accept(classNode, 0);
		MethodNode isTempting = null;
		for (MethodNode m : classNode.methods) {
			if (ApotheosisCore.isTempting(m)) {
				isTempting = m;
				break;
			}

		}
		if (isTempting != null) {
			InsnList insn = new InsnList();
			insn.add(new VarInsnNode(Opcodes.ALOAD, 1));
			insn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "shadows/ench/asm/EnchHooks", "isTempting", "(ZLnet/minecraft/item/ItemStack;)Z", false));
			AbstractInsnNode node = isTempting.instructions.getLast().getPrevious();
			isTempting.instructions.insertBefore(node, insn);
			CustomClassWriter writer = new CustomClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
			classNode.accept(writer);
			ApotheosisCore.LOG.info("Successfully transformed EntityAITempt");
			return writer.toByteArray();
		}
		ApotheosisCore.LOG.info("Failed transforming EntityAITempt");
		return basicClass;
	}

	public byte[] transformItem(byte[] basicClass) {
		ApotheosisCore.LOG.info("Transforming Item...");
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(basicClass);
		classReader.accept(classNode, 0);
		MethodNode getItemEnchantability = null;
		for (MethodNode m : classNode.methods) {
			if (ApotheosisCore.isItemEnch(m)) {
				getItemEnchantability = m;
				break;
			}
		}
		if (getItemEnchantability != null) {
			InsnList insn = new InsnList();
			insn.add(new LdcInsnNode(10));
			insn.add(new InsnNode(Opcodes.IRETURN));
			getItemEnchantability.instructions.insert(insn);
			CustomClassWriter writer = new CustomClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
			classNode.accept(writer);
			ApotheosisCore.LOG.info("Successfully transformed Item");
			return writer.toByteArray();
		}
		ApotheosisCore.LOG.info("Failed transforming Item");
		return basicClass;
	}

	public byte[] transformELB(byte[] basicClass) {
		ApotheosisCore.LOG.info("Transforming EntityLivingBase...");
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(basicClass);
		classReader.accept(classNode, 0);
		MethodNode blockUsingShield = null;
		for (MethodNode m : classNode.methods) {
			if (ApotheosisCore.isBlockWithShield(m)) {
				blockUsingShield = m;
				break;
			}
		}
		if (blockUsingShield != null) {
			InsnList insn = new InsnList();
			insn.add(new VarInsnNode(Opcodes.ALOAD, 0));
			insn.add(new VarInsnNode(Opcodes.ALOAD, 1));
			insn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "shadows/ench/asm/EnchHooks", "reflectiveHook", "(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/entity/EntityLivingBase;)V", false));
			blockUsingShield.instructions.insert(insn);
			CustomClassWriter writer = new CustomClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
			classNode.accept(writer);
			ApotheosisCore.LOG.info("Successfully transformed EntityLivingBase");
			return writer.toByteArray();
		}
		ApotheosisCore.LOG.info("Failed transforming EntityLivingBase");
		return basicClass;
	}

}
