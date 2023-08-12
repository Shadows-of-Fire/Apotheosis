function initializeCoreMod() {
    return {
        'apothlure': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.entity.projectile.FishingHook',
                'methodName': 'm_37145_',
                'methodDesc': '(Lnet/minecraft/core/BlockPos;)V'
            },
            'transformer': function(method) {
                var owner = "dev/shadowsoffire/apotheosis/ench/asm/EnchHooks";
                var name = "getTicksCaughtDelay";
                var desc = "(Lnet/minecraft/world/entity/projectile/FishingHook;)I";
                var instr = method.instructions;

                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
                var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
                var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
				ASMAPI.log('INFO', 'Patching FishingHook#catchingFish');

                var insertion = null;
                var i;
                for (i = instr.size() - 1; i > 0; i--) {
                    var n = instr.get(i);
                    if (n.getOpcode() == Opcodes.PUTFIELD) {
                        insertion = n;
                        break;
                    }
                }

                var insn = new InsnList();
                insn.add(new InsnNode(Opcodes.POP));
                insn.add(new VarInsnNode(Opcodes.ALOAD, 0));
                insn.add(ASMAPI.buildMethodCall(
                    owner,
                    name,
                    desc,
                    ASMAPI.MethodType.STATIC));
                instr.insertBefore(insertion, insn);

                return method;
            }
        }
    }
}