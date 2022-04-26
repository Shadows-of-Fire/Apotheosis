function initializeCoreMod() {
    return {
        'apothmythicrepair': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.inventory.AnvilMenu',
                'methodName': 'm_6640_',
                'methodDesc': '()V'
            },
            'transformer': function(method) {
                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var instr = method.instructions;
				ASMAPI.log('INFO', 'Patching AnvilMenu#createResult');

                var owner = "shadows/apotheosis/deadly/asm/DeadlyHooks";
                var name = "isTrulyDamageable";
                var desc = "(Lnet/minecraft/item/ItemStack;)Z";

                var hook = ASMAPI.buildMethodCall(
                    owner,
                    name,
                    desc,
                    ASMAPI.MethodType.STATIC);

                var ix = 0;
                var i;
                for (i = 0; i < instr.size(); i++) {
                    var n = instr.get(i);
                    if (n.getOpcode() == Opcodes.INVOKEVIRTUAL && n.name.equals(ASMAPI.mapMethod('m_41763_'))) {
                        ix++;
                        instr.set(n, hook.clone({}));
                    }
                }

                if(ix == 3) {
					ASMAPI.log('INFO', 'Successfully allowed nbt-unbreakable items in the anvil.');
				} else {
					ASMAPI.log('ERROR', 'Failed to allow nbt-unbreakable items in the anvil!');
				}

                return method;
            }
        }
    }
}