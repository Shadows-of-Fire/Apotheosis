function initializeCoreMod() {
    return {
        'coremodmethod': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.World',
                'methodName': 'markAndNotifyBlock',
                'methodDesc': '(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;I)V'
            },
            'transformer': function(method) {
                print('[ObserverLib]: Adding onBlockChange ASM Patch...');

                var endPoint = "hellfirepvp/observerlib/common/util/ASMHookEndpoint";
                var endPointMethod = "onBlockChange";
                var endPointDescriptor = "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;)V";
                var instr = method.instructions;

                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
                var LineNumberNode = Java.type('org.objectweb.asm.tree.LineNumberNode');
                var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
                var offset = instr.getFirst();

                for (var i = 0; i < instr.size(); i++) {
                    offset = instr.get(i);
                    if (offset instanceof LineNumberNode) {
                        break;
                    }
                }

                var onBlockChangeCall = ASMAPI.buildMethodCall(
                    endPoint,
                    endPointMethod,
                    endPointDescriptor,
                    ASMAPI.MethodType.STATIC);

                instr.insert(offset, onBlockChangeCall);
                instr.insertBefore(onBlockChangeCall, new VarInsnNode(Opcodes.ALOAD, 0));
                instr.insertBefore(onBlockChangeCall, new VarInsnNode(Opcodes.ALOAD, 1));
                instr.insertBefore(onBlockChangeCall, new VarInsnNode(Opcodes.ALOAD, 2));
                instr.insertBefore(onBlockChangeCall, new VarInsnNode(Opcodes.ALOAD, 3));
                instr.insertBefore(onBlockChangeCall, new VarInsnNode(Opcodes.ALOAD, 4));

                print('[ObserverLib]: Added onBlockChange ASM Patch!');
                return method;
            }
        }
    }
}