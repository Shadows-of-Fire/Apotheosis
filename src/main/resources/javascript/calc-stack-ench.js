function initializeCoreMod() {
    return {
        'coremodmethod': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.enchantment.EnchantmentHelper',
                'methodName': 'calcItemStackEnchantability',
                'methodDesc': '(Ljava/util/Random;IILnet/minecraft/item/ItemStack;)I;'
            },
            'transformer': function(method) {
                print('[ApotheosisCore]: Patching EnchantmentHelper#calcItemStackEnchantability');

                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
                var JumpInsnNode = Java.type('org.objectweb.asm.tree.JumpInsnNode');
                var IntInsnNode = Java.type('org.objectweb.asm.tree.IntInsnNode');
                var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
                var instr = method.instructions;

                var jumpNode = null;
                var i;
                for (i = 0; i < instr.size(); i++) {
                    var n = instr.get(i);
                    if (n.getOpcode() == Opcodes.BIPUSH && n.operand == 15) {
                        jumpNode = n.getNext();
                        break;
                    }
                }
                if (jumpNode != null) {
                    instr.insert(jumpNode, new JumpInsnNode(Opcodes.GOTO, jumpNode.label));
                }

                return method;
            }
        }
    }
}