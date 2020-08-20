function initializeCoreMod() {
    return {
        'apothquarkmatrix': {
            'target': {
                'type': 'METHOD',
                'class': 'vazkii.quark.oddities.module.MatrixEnchantingModule',
                'methodName': 'construct',
                'methodDesc': '()V'
            },
            'transformer': function(method) {
                print('[ApotheosisCore]: Patching MatrixEnchantingModule#construct');

                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
                var FieldInsnNode = Java.type('org.objectweb.asm.tree.FieldInsnNode');
                var JumpInsnNode = Java.type('org.objectweb.asm.tree.JumpInsnNode');
                var LabelNode = Java.type('org.objectweb.asm.tree.LabelNode');
                var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
                var InsnList = Java.type('org.objectweb.asm.tree.InsnList');

                var insn = new InsnList();
                insn.add(new FieldInsnNode(Opcodes.GETSTATIC, "shadows/apotheosis/Apotheosis", "enableEnch", "Z"));
                var label = new LabelNode();
                insn.add(new JumpInsnNode(Opcodes.IFEQ, label));
                insn.add(new InsnNode(Opcodes.RETURN));
                insn.add(label);
                method.instructions.insert(insn);

                return method;
            }
        }
    }
}