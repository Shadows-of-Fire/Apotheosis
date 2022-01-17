function initializeCoreMod() {
    return {
        'apothmagicdmg': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.util.CombatRules',
                'methodName': 'func_188401_b',
                'methodDesc': '(FF)F'
            },
            'transformer': function(method) {
                var owner = "shadows/apotheosis/ench/asm/EnchHooks";
                var name = "getDamageAfterMagicAbsorb";
                var desc = "(FF)F";
                var instr = method.instructions;

                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
                var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
                var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
                ASMAPI.log('INFO', 'Patching CombatRules#getDamageAfterMagicAbsorb');

                var insn = new InsnList();
                insn.add(new VarInsnNode(Opcodes.FLOAD, 0));
                insn.add(new VarInsnNode(Opcodes.FLOAD, 1));
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