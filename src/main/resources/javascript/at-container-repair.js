function initializeCoreMod() {
    return {
        'coremodmethod': {
            'target': {
                'type': 'METHOD',
                'class': 'com.tfar.anviltweaks.RepairContainerv2',
                'methodName': 'updateRepairOutput',
                'methodDesc': '()V'
            },
            'transformer': function(method) {
                print('[ApotheosisCore]: Patching AnvilTweaks\' RepairContainerv2#updateRepairOutput');

                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
                var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
                var LdcInsnNode = Java.type('org.objectweb.asm.tree.LdcInsnNode');
                var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
				var instr = method.instructions;
				var i;
				for (i = 0; i < instr.size(); i++) {
					var n = instr.get(i);
					if (n.getOpcode() == Opcodes.INVOKEVIRTUAL) {
						var is = n.name.equals(ASMAPI.mapMethod("func_77325_b"));
						if (is) { 
							instr.set(n, new MethodInsnNode(Opcodes.INVOKESTATIC, "shadows/ench/asm/EnchHooks", "getMaxLevel", "(Lnet/minecraft/enchantment/Enchantment;)I", false));
						}
					}
				}

                return method;
            }
        }
    }
}