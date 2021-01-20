function initializeCoreMod() {
    return {
        'apothenchaffix': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.enchantment.EnchantmentHelper',
                'methodName': 'func_77513_b',
                'methodDesc': '(Ljava/util/Random;Lnet/minecraft/item/ItemStack;IZ)Ljava/util/List;'
            },
            'transformer': function(method) {
                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
                var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
                var LdcInsnNode = Java.type('org.objectweb.asm.tree.LdcInsnNode');
                var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
                var instr = method.instructions;
				ASMAPI.log('INFO', 'Patching buildEnchantmentList for the Enchantability affix.');
				
                var i;
                for (i = 0; i < instr.size(); i++) {
                    var n = instr.get(i);
                    if (n.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                        if (n.name.equals("getItemEnchantability")) {
                            instr.set(n, new MethodInsnNode(Opcodes.INVOKESTATIC, "shadows/apotheosis/deadly/asm/DeadlyHooks", "getEnchantability", "(Lnet/minecraft/item/ItemStack;)I", false));
                        }
                    }
                }
                return method;
            }
        }
    }
}