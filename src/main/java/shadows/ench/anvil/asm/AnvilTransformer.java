package shadows.ench.anvil.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import shadows.ApotheosisCore;
import shadows.ApotheosisTransformer.IApotheosisTransformer;
import shadows.CustomClassWriter;

public class AnvilTransformer implements IApotheosisTransformer {

	@Override
	public boolean accepts(String name, String transformedName) {
		return "net.minecraft.inventory.ContainerRepair".equals(transformedName) || "net.minecraft.client.gui.GuiRepair".equals(transformedName);
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (!ApotheosisCore.enableEnch) return basicClass;
		if ("net.minecraft.inventory.ContainerRepair".equals(transformedName)) return transformContainerRepair(basicClass);
		return transformGuiRepair(basicClass);
	}

	static byte[] transformContainerRepair(byte[] basicClass) {
		ApotheosisCore.LOG.info("Transforming ContainerRepair...");
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(basicClass);
		classReader.accept(classNode, 0);
		MethodNode updateRepairOutput = null;
		for (MethodNode m : classNode.methods) {
			if (ApotheosisCore.isRepairOutput(m)) {
				updateRepairOutput = m;
				break;
			}
		}
		if (updateRepairOutput != null) {
			int ix = 0;
			AbstractInsnNode levelRestriction = null;
			MethodInsnNode getMaxLevel1 = null;
			MethodInsnNode getMaxLevel2 = null;
			for (int i = 0; i < updateRepairOutput.instructions.size(); i++) {
				AbstractInsnNode n = updateRepairOutput.instructions.get(i);
				if (n.getOpcode() == Opcodes.GETFIELD) {
					if (ApotheosisCore.isCapIsCreative((FieldInsnNode) n)) ix++;
				}
				if (ix == 2 && n.getOpcode() == Opcodes.GETSTATIC) {
					if (ApotheosisCore.isEmptyStack((FieldInsnNode) n)) {
						levelRestriction = n;
					}
				}
				if (n.getOpcode() == Opcodes.INVOKEVIRTUAL) {
					MethodInsnNode mNode = (MethodInsnNode) n;
					boolean is = ApotheosisCore.isGetMaxLevel(mNode);
					if (is && getMaxLevel1 == null) {
						getMaxLevel1 = mNode;
					} else if (is) getMaxLevel2 = mNode;
				}
			}

			if (levelRestriction != null) {
				updateRepairOutput.instructions.set(levelRestriction, new VarInsnNode(Opcodes.ALOAD, 5));
				ApotheosisCore.LOG.info("Successfully removed the anvil level cap.");
			}

			if (getMaxLevel1 != null) {
				updateRepairOutput.instructions.set(getMaxLevel1, new MethodInsnNode(Opcodes.INVOKESTATIC, "shadows/ench/asm/EnchHooks", "getMaxLevel", "(Lnet/minecraft/enchantment/Enchantment;)I", false));
				ApotheosisCore.LOG.info("Replaced ContainerRepair Enchantment#getMaxLevel #1.");
			}

			if (getMaxLevel2 != null) {
				updateRepairOutput.instructions.set(getMaxLevel2, new MethodInsnNode(Opcodes.INVOKESTATIC, "shadows/ench/asm/EnchHooks", "getMaxLevel", "(Lnet/minecraft/enchantment/Enchantment;)I", false));
				ApotheosisCore.LOG.info("Replaced ContainerRepair Enchantment#getMaxLevel #2.");
			}

			CustomClassWriter writer = new CustomClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
			classNode.accept(writer);
			if (levelRestriction != null && getMaxLevel1 != null && getMaxLevel2 != null) ApotheosisCore.LOG.info("Successfully transformed ContainerRepair");
			return writer.toByteArray();
		}
		ApotheosisCore.LOG.info("Failed transforming ContainerRepair");
		return basicClass;
	}

	static byte[] transformGuiRepair(byte[] basicClass) {
		ApotheosisCore.LOG.info("Transforming GuiRepair...");
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(basicClass);
		classReader.accept(classNode, 0);
		MethodNode drawForegroundLayer = null;
		for (MethodNode m : classNode.methods) {
			if (ApotheosisCore.isDrawForeground(m)) {
				drawForegroundLayer = m;
				break;
			}
		}
		if (drawForegroundLayer != null) {
			JumpInsnNode jumpNode = null;
			for (int i = 0; i < drawForegroundLayer.instructions.size(); i++) {
				AbstractInsnNode n = drawForegroundLayer.instructions.get(i);
				if (n.getOpcode() == Opcodes.GETFIELD) {
					if (ApotheosisCore.isCapIsCreative((FieldInsnNode) n)) {
						jumpNode = (JumpInsnNode) n.getNext();
						break;
					}
				}
			}
			if (jumpNode != null) {
				drawForegroundLayer.instructions.insert(jumpNode, new JumpInsnNode(Opcodes.GOTO, jumpNode.label));
				CustomClassWriter writer = new CustomClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
				classNode.accept(writer);
				ApotheosisCore.LOG.info("Successfully transformed GuiRepair");
				return writer.toByteArray();
			}
		}
		ApotheosisCore.LOG.info("Failed transforming GuiRepair");
		return basicClass;
	}

}
