function initializeCoreMod() {
    return {
        'apothstackenchcalc': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.enchantment.EnchantmentHelper',
                'methodName': 'func_77514_a',
                'methodDesc': '(Ljava/util/Random;IILnet/minecraft/item/ItemStack;)I;'
            },
            'transformer': function(method) {
                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
                var JumpInsnNode = Java.type('org.objectweb.asm.tree.JumpInsnNode');
                var IntInsnNode = Java.type('org.objectweb.asm.tree.IntInsnNode');
                var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
                var instr = method.instructions;
				ASMAPI.log('INFO', 'Patching EnchantmentHelper#calcItemStackEnchantability');

                var jumpNode = null;
                var i;
                for (i = 0; i < instr.size(); i++) {
                    var n = instr.get(i);
                    if (n.getOpcode() == Opcodes.BIPUSH && n.operand == 15) {
                        jumpNode = n.getNext();
                        break;
                    }
                }
                instr.insert(jumpNode, new JumpInsnNode(Opcodes.GOTO, jumpNode.label));

                return method;
            }
        }
    }
}