function initializeCoreMod() {
    return {
        'apothpotiondmg': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.entity.LivingEntity',
                'methodName': 'func_70672_c',
                'methodDesc': '(Lnet/minecraft/util/DamageSource;F)F'
            },
            'transformer': function(method) {
                var owner = "shadows/apotheosis/potion/asm/PotionHooks";
                var name = "applyPotionDamageCalculations";
                var desc = "(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/util/DamageSource;F)F";
                var instr = method.instructions;

                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
                var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
                var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
				ASMAPI.log('INFO', 'Patching LivingEntity#applyPotionDamageCalculations');

                var insn = new InsnList();
                insn.add(new VarInsnNode(Opcodes.ALOAD, 0));
                insn.add(new VarInsnNode(Opcodes.ALOAD, 1));
                insn.add(new VarInsnNode(Opcodes.FLOAD, 2));
                insn.add(ASMAPI.buildMethodCall(
                    owner,
                    name,
                    desc,
                    ASMAPI.MethodType.STATIC));
                insn.add(new InsnNode(Opcodes.FRETURN));
                instr.insert(insn);

                return method;
            }
        }
    }
}