function initializeCoreMod() {
    return {
        'apothshieldblock': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.entity.LivingEntity',
                'methodName': 'func_190629_c',
                'methodDesc': '(Lnet/minecraft/entity/EntityLivingBase;)V'
            },
            'transformer': function(method) {
                print('[ApotheosisCore]: Patching LivingEntity#blockUsingShield');

                var owner = "shadows/apotheosis/ench/asm/EnchHooks";
                var name = "reflectiveHook";
                var desc = "(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/entity/EntityLivingBase;)V";
                var instr = method.instructions;

                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
                var InsnList = Java.type('org.objectweb.asm.tree.InsnList');

                var insn = new InsnList();
                insn.add(new VarInsnNode(Opcodes.ALOAD, 0));
                insn.add(new VarInsnNode(Opcodes.ALOAD, 1));
                insn.add(ASMAPI.buildMethodCall(
                    owner,
                    name,
                    desc,
                    ASMAPI.MethodType.STATIC));
                instr.insert(insn);

                return method;
            }
        }
    }
}