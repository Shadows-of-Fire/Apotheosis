function initializeCoreMod() {
    return {
        'coremodmethod': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.client.gui.screen.inventory.AnvilScreen',
                'methodName': 'drawGuiContainerForegroundLayer',
                'methodDesc': '(II)V'
            },
            'transformer': function(method) {
                print('[ApotheosisCore]: Patching AnvilScreen#drawGuiContainerForegroundLayer');

                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var LdcInsnNode = Java.type('org.objectweb.asm.tree.LdcInsnNode');
                var instr = method.instructions;

                var levelRestriction = null;
                var i;
                for (i = 0; i < instr.size(); i++) {
                    var n = instr.get(i);
                    if (n.getOpcode() == Opcodes.LDC && n.cst.equals(40)) {
                        levelRestriction = n;
                        break;
                    }
                }

                if (levelRestriction != null) instr.set(levelRestriction, new LdcInsnNode(0x7fffffff));

                return method;
            }
        }
    }
}