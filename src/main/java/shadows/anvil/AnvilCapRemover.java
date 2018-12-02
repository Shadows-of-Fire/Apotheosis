package shadows.anvil;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.SortingIndex;
import shadows.ApotheosisCore;
import shadows.CustomClassWriter;

@SortingIndex(1001)
public class AnvilCapRemover implements IClassTransformer {

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (!ApotheosisCore.enableAnvil) return basicClass;
		if ("net.minecraft.inventory.ContainerRepair".equals(transformedName)) return transformContainerRepair(basicClass);
		if ("net.minecraft.client.gui.GuiRepair".equals(transformedName)) return transformGuiRepair(basicClass);
		return basicClass;
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
