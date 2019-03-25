package shadows.util;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import shadows.ApotheosisCore;
import shadows.ApotheosisTransformer.IApotheosisTransformer;
import shadows.CustomClassWriter;

public class BedTransformer implements IApotheosisTransformer {

	@Override
	public boolean accepts(String name, String transformedName) {
		return transformedName.equals("net.minecraft.block.BlockBed");
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		ApotheosisCore.LOG.info("Transforming BlockBed...");
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(basicClass);
		classReader.accept(classNode, 0);
		MethodNode onBlockActivated = null;
		for (MethodNode m : classNode.methods) {
			if (ApotheosisCore.isOnBlockActivated(m)) {
				onBlockActivated = m;
				break;
			}
		}
		if (onBlockActivated != null) {
			for (int i = onBlockActivated.instructions.size() - 1; i >= 0; i--) {
				AbstractInsnNode n = onBlockActivated.instructions.get(i);
				if (n.getOpcode() == Opcodes.ACONST_NULL) {
					InsnList list = new InsnList();
					list.add(new VarInsnNode(Opcodes.ALOAD, 2));
					list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "shadows/util/BedTransformer", "fakesplode", "(Ljava/lang/Object;Ljava/lang/Object;)V", false));
					list.add(new InsnNode(Opcodes.ICONST_1));
					list.add(new InsnNode(Opcodes.IRETURN));
					onBlockActivated.instructions.insertBefore(n, list);
					CustomClassWriter writer = new CustomClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
					classNode.accept(writer);
					ApotheosisCore.LOG.info("Successfully transformed BlockBed");
					return writer.toByteArray();
				}
			}
		}
		ApotheosisCore.LOG.info("Failed transforming BlockBed");
		return basicClass;
	}

	public static void fakesplode(Object w, Object p) {
		WorldServer world = (WorldServer) w;
		BlockPos pos = (BlockPos) p;
		world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F, (1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F) * 0.7F);
		world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1, 0, 0, 0, 1D);
	}

}
