package shadows.potion;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.SortingIndex;
import shadows.ApotheosisCore;
import shadows.CustomClassWriter;

@SortingIndex(1001)
public class InfinityTweaker implements IClassTransformer {

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (!ApotheosisCore.enablePInf) return basicClass;
		if ("net.minecraft.item.ItemArrow".equals(transformedName)) return transformPotionEffect(basicClass);
		return basicClass;
	}

	static byte[] transformPotionEffect(byte[] basicClass) {
		ApotheosisCore.LOG.info("Transforming ItemArrow...");
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(basicClass);
		classReader.accept(classNode, 0);
		MethodNode isInfinite = null;
		for (MethodNode m : classNode.methods) {
			if (m.name.equals("isInfinite")) {
				isInfinite = m;
				break;
			}
		}

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

}
