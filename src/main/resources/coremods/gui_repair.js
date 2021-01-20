function initializeCoreMod() {
    return {
        'apothanvilscreen': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.client.gui.screen.inventory.AnvilScreen',
                'methodName': 'func_230451_b_',
                'methodDesc': '(Lcom/mojang/blaze3d/matrix/MatrixStack;II)V'
            },
            'transformer': function(method) {
                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var LdcInsnNode = Java.type('org.objectweb.asm.tree.LdcInsnNode');
                var instr = method.instructions;
				ASMAPI.log('INFO', 'Patching AnvilScreen#drawGuiContainerForegroundLayer');

                var levelRestriction = null;
                var i;
                for (i = 0; i < instr.size(); i++) {
                    var n = instr.get(i);
                    if (n.getOpcode() == Opcodes.BIPUSH && n.operand.equals(40)) {
                        levelRestriction = n;
                        break;
                    }
                }

                instr.set(levelRestriction, new LdcInsnNode(0x7fffffff));

                return method;
            }
        }
    }
}