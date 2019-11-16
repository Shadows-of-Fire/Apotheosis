package shadows.deadly.asm;

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

import shadows.ApotheosisCore;
import shadows.ApotheosisTransformer.IApotheosisTransformer;
import shadows.CustomClassWriter;

public class DeadlyTransformer implements IApotheosisTransformer {

	@Override
	public boolean accepts(String name, String transformedName) {
		if ("net.minecraft.world.gen.feature.WorldGenDungeons".equals(transformedName)) return true;
		if ("net.minecraft.entity.SharedMonsterAttributes".equals(transformedName)) return true;
		return false;
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if ("net.minecraft.world.gen.feature.WorldGenDungeons".equals(transformedName)) return transformDungeons(basicClass);
		if ("net.minecraft.entity.SharedMonsterAttributes".equals(transformedName)) return transformSMA(basicClass);
		return basicClass;
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
			insn.add(new VarInsnNode(Opcodes.ALOAD, 3));
			insn.add(new VarInsnNode(Opcodes.ALOAD, 2));
			insn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "shadows/deadly/asm/DeadlyHooks", "setDungeonMobSpawner", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Ljava/util/Random;)V", false));

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

	public static void transformEnchHelper(ClassNode classNode) {
		ApotheosisCore.LOG.info("[Deadly] Transforming EnchantmentHelper...");
		MethodNode getEnchantmentModifierDamage = ApotheosisCore.findMethod(classNode, ApotheosisCore::isGetEnchantmentModifierDamage);
		MethodNode getModifierForCreature = ApotheosisCore.findMethod(classNode, ApotheosisCore::isGetModifierForCreature);
		MethodNode applyArthropodEnchantments = ApotheosisCore.findMethod(classNode, ApotheosisCore::isApplyArthropodEnchantments);
		MethodNode applyThornEnchantments = ApotheosisCore.findMethod(classNode, ApotheosisCore::isApplyThornEnchantments);

		for (int i = 0; i < getEnchantmentModifierDamage.instructions.size(); i++) {
			AbstractInsnNode n = getEnchantmentModifierDamage.instructions.get(i);
			if (n.getOpcode() == Opcodes.IRETURN) {
				InsnList insn = new InsnList();
				insn.add(new VarInsnNode(Opcodes.ALOAD, 0));
				insn.add(new VarInsnNode(Opcodes.ALOAD, 1));
				insn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "shadows/deadly/asm/DeadlyHooks", "getProtectionLevel", "(Ljava/lang/Iterable;Lnet/minecraft/util/DamageSource;)I", false));
				insn.add(new InsnNode(Opcodes.IADD));
				getEnchantmentModifierDamage.instructions.insertBefore(n, insn);
				break;
			}
		}

		for (int i = 0; i < getModifierForCreature.instructions.size(); i++) {
			AbstractInsnNode n = getModifierForCreature.instructions.get(i);
			if (n.getOpcode() == Opcodes.FRETURN) {
				InsnList insn = new InsnList();
				insn.add(new VarInsnNode(Opcodes.ALOAD, 0));
				insn.add(new VarInsnNode(Opcodes.ALOAD, 1));
				insn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "shadows/deadly/asm/DeadlyHooks", "getExtraDamageFor", "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/EnumCreatureAttribute;)F", false));
				insn.add(new InsnNode(Opcodes.FADD));
				getModifierForCreature.instructions.insertBefore(n, insn);
				break;
			}
		}

		for (int i = 0; i < applyArthropodEnchantments.instructions.size(); i++) {
			AbstractInsnNode n = applyArthropodEnchantments.instructions.get(i);
			if (n.getOpcode() == Opcodes.RETURN) {
				InsnList insn = new InsnList();
				insn.add(new VarInsnNode(Opcodes.ALOAD, 0));
				insn.add(new VarInsnNode(Opcodes.ALOAD, 1));
				insn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "shadows/deadly/asm/DeadlyHooks", "onEntityDamaged", "(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/entity/Entity;)V", false));
				applyArthropodEnchantments.instructions.insertBefore(n, insn);
				break;
			}
		}

		for (int i = 0; i < applyThornEnchantments.instructions.size(); i++) {
			AbstractInsnNode n = applyThornEnchantments.instructions.get(i);
			if (n.getOpcode() == Opcodes.RETURN) {
				InsnList insn = new InsnList();
				insn.add(new VarInsnNode(Opcodes.ALOAD, 0));
				insn.add(new VarInsnNode(Opcodes.ALOAD, 1));
				insn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "shadows/deadly/asm/DeadlyHooks", "onUserHurt", "(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/entity/Entity;)V", false));
				applyThornEnchantments.instructions.insertBefore(n, insn);
				break;
			}
		}
	}

	static byte[] transformSMA(byte[] basicClass) {
		ApotheosisCore.LOG.info("Transforming SharedMonsterAttributes...");
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(basicClass);
		classReader.accept(classNode, 0);
		MethodNode readAttributeModifierFromNBT = ApotheosisCore.findMethod(classNode, ApotheosisCore::isReadAttributeModifierFromNBT);

		if (readAttributeModifierFromNBT != null) {
			InsnList insn = new InsnList();
			insn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "shadows/deadly/asm/DeadlyHooks", "getRealUUID", "(Ljava/util/UUID;)Ljava/util/UUID;", false));

			AbstractInsnNode n = null;
			for (int i = 0; i < readAttributeModifierFromNBT.instructions.size(); i++) {
				n = readAttributeModifierFromNBT.instructions.get(i);
				if (n.getOpcode() == Opcodes.ASTORE) {
					break;
				}
			}

			readAttributeModifierFromNBT.instructions.insertBefore(n, insn);
			CustomClassWriter writer = new CustomClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
			classNode.accept(writer);
			ApotheosisCore.LOG.info("Successfully transformed SharedMonsterAttributes");
			return writer.toByteArray();
		}
		ApotheosisCore.LOG.info("Failed transforming SharedMonsterAttributes");
		return basicClass;
	}

}
