package shadows.spawn.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import shadows.ApotheosisCore;
import shadows.ApotheosisTransformer.IApotheosisTransformer;
import shadows.CustomClassWriter;

/**
 * This is the stupidest transformer i've ever written.
 * Vanilla makes a call to TileEntity.getKey(TileEntityMobSpawner.class), which is null due to replacements.
 * This fixes that.
 * @author Shadows
 *
 */
public class SpawnerTransformer implements IApotheosisTransformer {

	@Override
	public boolean accepts(String name, String transformedName) {
		return "net.minecraft.tileentity.TileEntityMobSpawner$2".equals(transformedName);
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (!ApotheosisCore.enableSpawner) return basicClass;
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
				method.instructions.set(toRemove, new MethodInsnNode(Opcodes.INVOKESTATIC, "shadows/spawn/SpawnerModule", "getSpawnerClass", "()Ljava/lang/Class;", false));
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
