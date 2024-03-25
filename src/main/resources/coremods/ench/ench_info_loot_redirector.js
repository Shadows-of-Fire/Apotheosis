var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var Handle = Java.type('org.objectweb.asm.Handle');
var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');

function maxLootLevelNode(){
	return new MethodInsnNode(
			Opcodes.INVOKESTATIC, 
			"dev/shadowsoffire/apotheosis/ench/asm/EnchHooks", 
			"getMaxLootLevel", 
			"(Lnet/minecraft/world/item/enchantment/Enchantment;)I", 
			false);
}

function lootableNode(){
	return new MethodInsnNode(
			Opcodes.INVOKESTATIC, 
			"dev/shadowsoffire/apotheosis/ench/asm/EnchHooks", 
			"isLootable", 
			"(Lnet/minecraft/world/item/enchantment/Enchantment;)Z", 
			false);
}

function treasureNode(){
	return new MethodInsnNode(
			Opcodes.INVOKESTATIC, 
			"dev/shadowsoffire/apotheosis/ench/asm/EnchHooks", 
			"isTreasureOnly", 
			"(Lnet/minecraft/world/item/enchantment/Enchantment;)Z", 
			false);
}

function tradeableNode(){
	return new MethodInsnNode(
			Opcodes.INVOKESTATIC, 
			"dev/shadowsoffire/apotheosis/ench/asm/EnchHooks", 
			"isTradeable", 
			"(Lnet/minecraft/world/item/enchantment/Enchantment;)Z", 
			false);
}

function search(name, replacements){
	for(var i = 0; i < replacements.length; i++){
		if(name == replacements[i].name) return replacements[i];
	}
	return null;
}

function initializeCoreMod() {
	return {
		'ench_info_loot_redirects': {
			'target': {
				'type': 'CLASS',
				'names': function(listofclasses) {
					return [
						'org.violetmoon.quark.content.tools.loot.EnchantTome',
						'net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction',
						'net.minecraft.world.entity.npc.VillagerTrades$EnchantBookForEmeralds'
					]
				}
			},
			'transformer': function(classNode) {
				var replacements = [
					{'name': ASMAPI.mapMethod("m_6586_"), 'factory': maxLootLevelNode, 'count': 0, 'logName': 'Enchantment#getMaxLevel()'},
					{'name': ASMAPI.mapMethod("m_6592_"), 'factory': lootableNode, 'count': 0, 'logName': 'Enchantment#isDiscoverable()'},
					{'name': ASMAPI.mapMethod("m_6591_"), 'factory': treasureNode, 'count': 0, 'logName': 'Enchantment#isTreasureOnly()'},
					{'name': ASMAPI.mapMethod("m_6594_"), 'factory': tradeableNode, 'count': 0, 'logName': 'Enchantment#isTradeable()'}
				];
				var methods = classNode.methods;
				var count = 0;
				for(var i = 0; i < methods.size(); i++){
					var instr = methods.get(i).instructions;
					for(var ix = 0; ix < instr.size(); ix++){
						var node = instr.get(ix);
						var temp = search(node.name, replacements);
						if (node.getOpcode() == Opcodes.INVOKEVIRTUAL && temp != null) {
							instr.set(node, temp.factory());
							temp.count++;
						} else if (node.getOpcode() == Opcodes.INVOKEDYNAMIC){
							var args = node.bsmArgs;
							for(var k = 0; k < args.length; k++){
								if(args[k] instanceof Handle){
									var handle = args[k];
									temp = search(handle.getName(), replacements);
									if(handle.getOwner() == 'net/minecraft/world/item/enchantment/Enchantment' && temp != null){
										var mNode = temp.factory();
										args[k] = new Handle(Opcodes.H_INVOKESTATIC, mNode.owner, mNode.name, mNode.desc, false);
										temp.count++;
									}
								}
							}
						}
					}
				}
				for(var i = 0; i < replacements.length; i++){
					if(replacements[i].count > 0) ASMAPI.log('INFO', 'Replaced ' + replacements[i].count + ' calls to ' + replacements[i].logName +  ' in ' + classNode.name);
				}
				return classNode;
			}
		}
	}
}