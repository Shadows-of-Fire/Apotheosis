function initializeCoreMod() {
    return {
        'maxlevelredirects': {
            'target': {
                'type': 'CLASS',
				'names': function(listofclasses) {
					return [
						'vazkii.quark.tools.module.AncientTomesModule',
						'net.minecraft.command.impl.EnchantCommand',
						'net.minecraft.item.EnchantedBookItem',
						'net.minecraft.loot.functions.EnchantRandomly',
						'net.minecraft.entity.merchant.villager.VillagerTrades$EnchantedBookForEmeraldsTrade',
						'net.minecraft.inventory.container.RepairContainer'
					]
				}
            },
            'transformer': function(classNode) {
                print('[ApotheosisCore]: Patching ' + classNode.name);

                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
                var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
                var LdcInsnNode = Java.type('org.objectweb.asm.tree.LdcInsnNode');
                var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
                var methods = classNode.methods;
				for(var i = 0; i < methods.size(); i++){
					var instr = methods.get(i).instructions;
					for(var ix = 0; ix < instr.size(); ix++){
						var node = instr.get(ix);
                        if (node.getOpcode() == Opcodes.INVOKEVIRTUAL && node.name.equals(ASMAPI.mapMethod("func_77325_b"))) {
							instr.set(node,
							new MethodInsnNode(Opcodes.INVOKESTATIC, 
							"shadows/apotheosis/ench/asm/EnchHooks", 
							"getMaxLevel", 
							"(Lnet/minecraft/enchantment/Enchantment;)I", false));
						}
					}
				}
                return classNode;
            }
        }
    }
}