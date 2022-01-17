function initializeCoreMod() {
    return {
        'apothcampfire': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.tileentity.CampfireTileEntity',
                'methodName': 'func_213980_a',
                'methodDesc': '(Lnet/minecraft/item/ItemStack;)Ljava/util/Optional;'
            },
            'transformer': function(method) {
				var owner = "shadows/apotheosis/deadly/asm/DeadlyHooks";
                var name = "getCampfireInv";
                var desc = "(Lnet/minecraft/inventory/IInventory;Lnet/minecraft/tileentity/CampfireTileEntity;)Lnet/minecraft/inventory/IInventory;";
                var instr = method.instructions;
				
                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
                var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
                var LdcInsnNode = Java.type('org.objectweb.asm.tree.LdcInsnNode');
                var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
                var instr = method.instructions;
				ASMAPI.log('INFO', 'Patching CampfireTileEntity#findMatchingRecipe');

                var invoke = null;
                var i;
                for (i = 0; i < instr.size(); i++) {
                    var n = instr.get(i);
                    if (n.getOpcode() == Opcodes.INVOKESPECIAL) {
                        invoke = n;
						break;
                    }
                }

                var insn = new InsnList();
                insn.add(new VarInsnNode(Opcodes.ALOAD, 0));
                insn.add(ASMAPI.buildMethodCall(
                    owner,
                    name,
                    desc,
                    ASMAPI.MethodType.STATIC));
                instr.insert(invoke, insn);

                return method;
            }
        }
    }
}