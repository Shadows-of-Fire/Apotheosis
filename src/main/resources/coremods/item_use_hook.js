function initializeCoreMod() {
    return {
        'apothitemusehook': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.item.ItemStack',
                'methodName': 'func_196084_a',
                'methodDesc': '(Lnet/minecraft/item/ItemUseContext;)Lnet/minecraft/util/ActionResultType;'
            },
            'transformer': function(method) {
                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var AbstractInsnNode = Java.type('org.objectweb.asm.tree.AbstractInsnNode');
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
                var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
                var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
                var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
                var instr = method.instructions;
				ASMAPI.log('INFO', 'Patching ItemStack#onItemUse');

                var insn = new InsnList();
                insn.add(new VarInsnNode(Opcodes.ALOAD, 0));
                insn.add(new VarInsnNode(Opcodes.ALOAD, 1));
                insn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "shadows/apotheosis/deadly/asm/DeadlyHooks", "onItemUse", "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemUseContext;)Lnet/minecraft/util/ActionResultType;", false));
                insn.add(new InsnNode(Opcodes.ARETURN));
                instr.insert(insn);

                return method;
            }
        }
    }
}