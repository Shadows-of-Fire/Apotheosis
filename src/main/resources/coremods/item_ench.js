function initializeCoreMod() {
    return {
        'apothitemench': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.item.Item',
                'methodName': 'func_77619_b',
                'methodDesc': '()I'
            },
            'transformer': function(method) {
				var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var LdcInsnNode = Java.type('org.objectweb.asm.tree.LdcInsnNode');
                var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
                var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
                var instr = method.instructions;
				ASMAPI.log('INFO', 'Patching Item#getItemEnchantability');

                var insn = new InsnList();
                insn.add(new LdcInsnNode(10));
                insn.add(new InsnNode(Opcodes.IRETURN));
                instr.insert(insn);

                return method;
            }
        }
    }
}