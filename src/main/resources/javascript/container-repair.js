function initializeCoreMod() {
    return {
        'coremodmethod': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.inventory.container.RepairContainer',
                'methodName': 'updateRepairOutput',
                'methodDesc': '()V'
            },
            'transformer': function(method) {
                print('[ApotheosisCore]: Patching RepairContainer#updateRepairOutput');

                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
                var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
                var LdcInsnNode = Java.type('org.objectweb.asm.tree.LdcInsnNode');
                var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
				var instr = method.instructions;

				var ix = 0;
				var levelRestriction = null;
				var getMaxLevel1 = null;
				var getMaxLevel2 = null;
				var i;
				for (i = 0; i < instr.size(); i++) {
					var n = instr.get(i);
					if (n.getOpcode() == Opcodes.LDC && n.cst.equals(40)) {
						if (ix++ == 2) {
							levelRestriction = n;
						}
					}
					if (n.getOpcode() == Opcodes.INVOKEVIRTUAL) {
						var mNode = n;
						var is = mNode.name.equals("getMaxLevel");
						if (is && getMaxLevel1 == null) {
							getMaxLevel1 = mNode;
						} else if (is) getMaxLevel2 = mNode;
					}
				}

				if (levelRestriction != null) {
					instr.set(levelRestriction, new LdcInsnNode(0x7fffffff));
					print('[ApotheosisCore]: Successfully removed the anvil level cap.');
				}

				if (getMaxLevel1 != null) {
					instr.set(getMaxLevel1, new MethodInsnNode(Opcodes.INVOKESTATIC, "shadows/ench/asm/EnchHooks", "getMaxLevel", "(Lnet/minecraft/enchantment/Enchantment;)I", false));
					print('[ApotheosisCore]: Replaced ContainerRepair Enchantment#getMaxLevel #1.');
				}

				if (getMaxLevel2 != null) {
					instr.set(getMaxLevel2, new MethodInsnNode(Opcodes.INVOKESTATIC, "shadows/ench/asm/EnchHooks", "getMaxLevel", "(Lnet/minecraft/enchantment/Enchantment;)I", false));
					print('[ApotheosisCore]: Replaced ContainerRepair Enchantment#getMaxLevel #2.');
				}

                return method;
            }
        }
    }
}