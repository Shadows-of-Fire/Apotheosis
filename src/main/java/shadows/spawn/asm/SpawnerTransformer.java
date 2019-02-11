package shadows.spawn.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import shadows.ApotheosisCore;
import shadows.ApotheosisTransformer.IApotheosisTransformer;
import shadows.CustomClassWriter;

public class SpawnerTransformer implements IApotheosisTransformer {

	@Override
	public boolean accepts(String name, String transformedName) {
		return "net.minecraft.tileentity.TileEntityMobSpawner$2".equals(transformedName);
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		ApotheosisCore.LOG.info("Transforming TileEntityMobSpawner$2...");
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(basicClass);
		classReader.accept(classNode, 0);
		MethodNode method = classNode.methods.get(1);
		if (method != null) {
			AbstractInsnNode toRemove = null;
			for (int i = 0; i < method.instructions.size(); i++) {
				AbstractInsnNode n = method.instructions.get(i);
				if (n.getOpcode() == Opcodes.LDC) {
					toRemove = n;
					break;
				}
			}
			if (toRemove != null) {
				LdcInsnNode loaded = new LdcInsnNode("shadows.spawn.TileSpawnerExt");
				method.instructions.set(toRemove, loaded);
				method.instructions.insert(loaded, new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Class", "forName", "(Ljava/lang/String;)Ljava/lang/Class;", false));
				CustomClassWriter writer = new CustomClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
				classNode.accept(writer);
				ApotheosisCore.LOG.info("Successfully transformed TileEntityMobSpawner$2");
				return writer.toByteArray();
			}
		}
		ApotheosisCore.LOG.info("Failed transforming TileEntityMobSpawner$2");
		return basicClass;
	}

}
