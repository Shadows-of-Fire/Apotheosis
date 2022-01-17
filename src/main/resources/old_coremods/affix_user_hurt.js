function initializeCoreMod() {
    return {
        'apothlure': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.enchantment.EnchantmentHelper',
                'methodName': 'func_151384_a',
                'methodDesc': '(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/Entity;)V'
            },
            'transformer': function(method) {
                var owner = "shadows/apotheosis/ench/asm/EnchHooks";
                var name = "getTicksCaughtDelay";
                var desc = "(Lnet/minecraft/entity/projectile/EnchantmentHelper;)I";
                var instr = method.instructions;

                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var AbstractInsnNode = Java.type('org.objectweb.asm.tree.AbstractInsnNode');
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
                var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
                var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
                var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
				ASMAPI.log('INFO', 'Patching EnchantmentHelper#applyThornEnchantments');

                var i;
                for (i = 0; i < instr.size(); i++) {
                    var n = instr.get(i);
                    if (n.getOpcode() == Opcodes.RETURN) {
                        var insn = new InsnList();
                        insn.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        insn.add(new VarInsnNode(Opcodes.ALOAD, 1));
                        insn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "shadows/apotheosis/deadly/asm/DeadlyHooks", "onUserHurt", "(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/Entity;)V", false));
                        instr.insertBefore(n, insn);
                        break;
                    }
                }

                return method;
            }
        }
    }
}