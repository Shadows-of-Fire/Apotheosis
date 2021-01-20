function initializeCoreMod() {
    return {
        'apotharrowinfinity': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.item.ArrowItem',
                'methodName': 'isInfinite',
                'methodDesc': '(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;)Z'
            },
            'transformer': function(method) {
                var owner = "shadows/apotheosis/potion/asm/PotionHooks";
                var name = "isInfinite";
                var desc = "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;)Z";
                var instr = method.instructions;

                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
                var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
                var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
				ASMAPI.log('INFO', 'Patching ItemArrow#isInfinite');

                var insn = new InsnList();
                insn.add(new VarInsnNode(Opcodes.ALOAD, 1));
                insn.add(new VarInsnNode(Opcodes.ALOAD, 2));
                insn.add(new VarInsnNode(Opcodes.ALOAD, 3));
                insn.add(ASMAPI.buildMethodCall(
                    owner,
                    name,
                    desc,
                    ASMAPI.MethodType.STATIC));
                insn.add(new InsnNode(Opcodes.IRETURN));
                instr.insert(insn);

                return method;
            }
        }
    }
}