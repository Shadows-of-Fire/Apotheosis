package shadows.potion.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import shadows.ApotheosisCore;
import shadows.ApotheosisTransformer.IApotheosisTransformer;
import shadows.CustomClassWriter;

public class PotionTransformer implements IApotheosisTransformer {

	@Override
	public boolean accepts(String name, String transformedName) {
		return "net.minecraft.item.ItemArrow".equals(transformedName) || "net.minecraft.potion.PotionEffect".equals(transformedName);
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (!ApotheosisCore.enablePotion) return basicClass;
		if ("net.minecraft.item.ItemArrow".equals(transformedName)) return transformArrow(basicClass);
		return transformPotionEffect(basicClass);
	}

	static byte[] transformArrow(byte[] basicClass) {
		ApotheosisCore.LOG.info("Transforming ItemArrow...");
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(basicClass);
		classReader.accept(classNode, 0);
		MethodNode isInfinite = ApotheosisCore.findMethod(classNode, m -> m.name.equals("isInfinite"));

		if (isInfinite != null) {
			InsnList insn = new InsnList();
			insn.add(new VarInsnNode(Opcodes.ALOAD, 1));
			insn.add(new VarInsnNode(Opcodes.ALOAD, 2));
			insn.add(new VarInsnNode(Opcodes.ALOAD, 3));
			insn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "shadows/potion/PotionModule", "isInfinite", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Z", false));
			insn.add(new InsnNode(Opcodes.IRETURN));
			isInfinite.instructions.insert(insn);
			CustomClassWriter writer = new CustomClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
			classNode.accept(writer);
			ApotheosisCore.LOG.info("Successfully transformed ItemArrow");
			return writer.toByteArray();
		}
		ApotheosisCore.LOG.info("Failed transforming ItemArrow");
		return basicClass;
	}

	static byte[] transformPotionEffect(byte[] basicClass) {
		ApotheosisCore.LOG.info("Transforming PotionEffect...");
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(basicClass);
		classReader.accept(classNode, 0);
		MethodNode doesShowParticles = ApotheosisCore.findMethod(classNode, ApotheosisCore::isShowParticles);

		if (doesShowParticles != null) {
			InsnList insn = new InsnList();
			insn.add(new VarInsnNode(Opcodes.ALOAD, 0));
			insn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "shadows/potion/PotionModule", "doesShowParticles", "(Ljava/lang/Object;)Z", false));
			insn.add(new InsnNode(Opcodes.IRETURN));
			doesShowParticles.instructions.insert(insn);
			CustomClassWriter writer = new CustomClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
			classNode.accept(writer);
			ApotheosisCore.LOG.info("Successfully transformed PotionEffect");
			return writer.toByteArray();
		}
		ApotheosisCore.LOG.info("Failed transforming PotionEffect");
		return basicClass;
	}

}
