package shadows.deadly.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import shadows.ApotheosisCore;
import shadows.ApotheosisTransformer.IApotheosisTransformer;
import shadows.CustomClassWriter;

public class DeadlyTransformer implements IApotheosisTransformer {

	@Override
	public boolean accepts(String name, String transformedName) {
		return "net.minecraft.world.gen.feature.WorldGenDungeons".equals(transformedName);
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (!ApotheosisCore.enableDeadly) return basicClass;
		return transformDungeons(basicClass);
	}

	static byte[] transformDungeons(byte[] basicClass) {
		ApotheosisCore.LOG.info("Transforming WorldGenDungeons...");
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(basicClass);
		classReader.accept(classNode, 0);
		MethodNode generate = ApotheosisCore.findMethod(classNode, ApotheosisCore::isGenerate);

		if (generate != null) {
			InsnList insn = new InsnList();
			insn.add(new VarInsnNode(Opcodes.ALOAD, 1));
			insn.add(new VarInsnNode(Opcodes.ALOAD, 2));
			insn.add(new VarInsnNode(Opcodes.ALOAD, 3));
			insn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "shadows/deadly/DeadlyModule", "setDungeonMobSpawner", "(Ljava/lang/Object;Ljava/util/Random;Ljava/lang/Object;)V", false));

			int size = generate.instructions.size();
			AbstractInsnNode trueReturn = null;

			for (int i = size - 1; i >= 0; i--) {
				AbstractInsnNode n = generate.instructions.get(i);
				if (n.getOpcode() == Opcodes.ICONST_1) {
					trueReturn = n;
					break;
				}
			}

			if (trueReturn != null) generate.instructions.insertBefore(trueReturn, insn);
			else ApotheosisCore.LOG.error("Failed to find return node in WorldGenDungeons!");
			CustomClassWriter writer = new CustomClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
			classNode.accept(writer);
			ApotheosisCore.LOG.info("Successfully transformed WorldGenDungeons");
			return writer.toByteArray();
		}
		ApotheosisCore.LOG.info("Failed transforming WorldGenDungeons");
		return basicClass;
	}

}
