function initializeCoreMod() {
    return {
        'apothstackmodifier': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.entity.ai.attributes.AttributeModifier',
                'methodName': 'func_233800_a_',
                'methodDesc': '(Lnet/minecraft/nbt/CompoundNBT;)Lnet/minecraft/entity/ai/attributes/AttributeModifier;'
            },
            'transformer': function(method) {
                var owner = "shadows/apotheosis/deadly/asm/DeadlyHooks";
                var name = "getRealUUID";
                var desc = "(Ljava/util/UUID;)Ljava/util/UUID;";
                var instr = method.instructions;

                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var AbstractInsnNode = Java.type('org.objectweb.asm.tree.AbstractInsnNode');
				ASMAPI.log('INFO', 'Patching SharedMonsterAttributes#readAttributeModifier');

                var n = null;
                var i;
                for (i = 0; i < instr.size(); i++) {
                    n = instr.get(i);
                    if (n.getOpcode() == Opcodes.ASTORE) {
                        break;
                    }
                }

                instr.insertBefore(n, ASMAPI.buildMethodCall(
                    owner,
                    name,
                    desc,
                    ASMAPI.MethodType.STATIC));

                return method;
            }
        }
    }
}