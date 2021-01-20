function initializeCoreMod() {
    return {
        'crossbowarrowhook': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.item.CrossbowItem',
                'methodName': 'func_220016_a',
                'methodDesc': '(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;FZFFF)V'
            },
            'transformer': function(method) {
                var owner = "shadows/apotheosis/ench/asm/EnchHooks";
                var name = "markGeneratedArrows";
                var desc = "(Lnet/minecraft/entity/projectile/ProjectileEntity;Lnet/minecraft/item/ItemStack;)V";
                var instr = method.instructions;

                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
                var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
				ASMAPI.log('INFO', 'Patching CrossbowItem#fireProjectile');

				var firstInstanceOf = null;
                var i;
                for (i = 0; i < instr.size(); i++) {
                    var n = instr.get(i);
                    if (n.getOpcode() == Opcodes.INSTANCEOF) {
                        firstInstanceOf = n;
						break;
                    }
                }

                var insn = new InsnList();
				insn.add(new VarInsnNode(Opcodes.ALOAD, 11));
                insn.add(new VarInsnNode(Opcodes.ALOAD, 3));
                insn.add(ASMAPI.buildMethodCall(
                    owner,
                    name,
                    desc,
                    ASMAPI.MethodType.STATIC));
                instr.insertBefore(firstInstanceOf, insn);

                return method;
            }
        }
    }
}