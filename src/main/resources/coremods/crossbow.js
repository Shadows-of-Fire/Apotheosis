function initializeCoreMod() {
    return {
        'crossbowuse': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.item.CrossbowItem',
                'methodName': 'func_77659_a',
                'methodDesc': '(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;'
            },
            'transformer': function(method) {
                var owner = "shadows/apotheosis/ench/asm/EnchHooks";
                var name = "onArrowFired";
                var desc = "(Lnet/minecraft/item/ItemStack;)V";
                var instr = method.instructions;

                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
                var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
				ASMAPI.log('INFO', 'Patching CrossbowItem#onItemRightClick');

                var fireProjCall = null;
                var firstReturn = null;
                var i;
                for (i = 0; i < instr.size(); i++) {
                    var n = instr.get(i);
                    if (firstReturn == null && n.getOpcode() == Opcodes.ARETURN) {
                        firstReturn = n;
                    }
                    if (fireProjCall == null && n.getOpcode() == Opcodes.INVOKESTATIC && ASMAPI.mapMethod('func_220014_a') === n.name) {
                        fireProjCall = n;
                    }
                    if (firstReturn != null && fireProjCall != null) break;
                }

                var insn = new InsnList();
                insn.add(new VarInsnNode(Opcodes.ALOAD, 4));
                insn.add(ASMAPI.buildMethodCall(
                    owner,
                    name,
                    desc,
                    ASMAPI.MethodType.STATIC));
                instr.insertBefore(firstReturn, insn);

                name = "preArrowFired";

                var insn = new InsnList();
                insn.add(new VarInsnNode(Opcodes.ALOAD, 4));
                insn.add(ASMAPI.buildMethodCall(
                    owner,
                    name,
                    desc,
                    ASMAPI.MethodType.STATIC));
                instr.insertBefore(fireProjCall, insn);

                return method;
            }
        }
    }
}