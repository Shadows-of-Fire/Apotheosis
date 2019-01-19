package shadows.spawn;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.SortingIndex;
import shadows.ApotheosisCore;
import shadows.CustomClassWriter;

@SortingIndex(1001)
public class SpawnerFixerTransformer implements IClassTransformer {

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if ("net.minecraft.tileentity.TileEntityMobSpawner$2".equals(transformedName)) try {
			return transform(basicClass);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		return basicClass;
	}

	static byte[] transform(byte[] basicClass) throws NoSuchMethodException, SecurityException {
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
				method.instructions.insert(loaded, new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Class", "forName", Type.getMethodDescriptor(Class.class.getMethod("forName", String.class)), false));
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
