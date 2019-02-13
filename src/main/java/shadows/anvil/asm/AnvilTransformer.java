package shadows.anvil.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
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
			AbstractInsnNode toRemove = null;
			for (int i = 0; i < updateRepairOutput.instructions.size(); i++) {
				AbstractInsnNode n = updateRepairOutput.instructions.get(i);
				if (n.getOpcode() == Opcodes.GETFIELD) {
					if (ApotheosisCore.isCapIsCreative((FieldInsnNode) n)) ix++;
				}
				if (ix == 2 && n.getOpcode() == Opcodes.GETSTATIC) {
					if (ApotheosisCore.isEmptyStack((FieldInsnNode) n)) {
						toRemove = n;
						break;
					}
				}
			}
			if (toRemove != null) {
				updateRepairOutput.instructions.set(toRemove, new VarInsnNode(Opcodes.ALOAD, 5));
				CustomClassWriter writer = new CustomClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
				classNode.accept(writer);
				ApotheosisCore.LOG.info("Successfully transformed ContainerRepair");
				return writer.toByteArray();
			}
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
