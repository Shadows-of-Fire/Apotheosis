function initializeCoreMod() {
    return {
        'apothtempting': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.entity.ai.goal.TemptGoal',
                'methodName': 'func_188508_a',
                'methodDesc': '(Lnet/minecraft/item/ItemStack;)Z'
            },
            'transformer': function(method) {
                var owner = "shadows/apotheosis/ench/asm/EnchHooks";
                var name = "isTempting";
                var desc = "(ZLnet/minecraft/item/ItemStack;)Z";
                var instr = method.instructions;

                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
                var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
				ASMAPI.log('INFO', 'Patching TemptGoal#isTempting');

                var insn = new InsnList();
                insn.add(new VarInsnNode(Opcodes.ALOAD, 1));
                insn.add(ASMAPI.buildMethodCall(
                    owner,
                    name,
                    desc,
                    ASMAPI.MethodType.STATIC));
                var node = instr.getLast().getPrevious();
                instr.insertBefore(node, insn);

                return method;
            }
        }
    }
}