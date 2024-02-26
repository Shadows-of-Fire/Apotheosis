var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');

function findPostLoopStatement(instr) {
    // IINC i 1
    // GOTO ?
    for (var i = 0; i < instr.size(); i++) {
        var nodeIInc = instr.get(i);
        if (nodeIInc.getOpcode() !== Opcodes.IINC || nodeIInc.incr !== 1) {
            continue;
        }
        var nodeGoto = nodeIInc.getNext();
        if (nodeGoto.getOpcode() !== Opcodes.GOTO) {
            continue;
        }
        return nodeIInc;
    }
    return null;
}

function findLoopCondition(instr, loopVariableIndex) {
    // ILOAD i
    // ALOAD listtag
    // INVOKEVIRTUAL net/minecraft/nbt/ListTag.size()I
    // IF_ICMPGE L
    for (var i = 0; i < instr.size(); i++) {
        var nodeILoad = instr.get(i);
        if (nodeILoad.getOpcode() !== Opcodes.ILOAD || nodeILoad.var !== loopVariableIndex) {
            continue;
        }
        var nodeALoad = nodeILoad.getNext();
        if (nodeALoad.getOpcode() !== Opcodes.ALOAD) {
            continue;
        }
        var nodeInvokeVirtual = nodeALoad.getNext();
        if (nodeInvokeVirtual.getOpcode() !== Opcodes.INVOKEVIRTUAL
            || nodeInvokeVirtual.owner !== 'net/minecraft/nbt/ListTag'
            // ListTag implements 'List<T>.size()I' so no need to do mapping.
            || nodeInvokeVirtual.name !== 'size'
            || nodeInvokeVirtual.desc !== '()I') {
            continue;
        }
        var nodeJumpIfGreaterOrEqual = nodeInvokeVirtual.getNext();
        if (nodeJumpIfGreaterOrEqual.getOpcode() !== Opcodes.IF_ICMPGE) {
            continue;
        }
        return nodeILoad;
    }
    return null;
}

function findLoopInitializer(instr, loopVariableIndex) {
    // ICONST_0
    // ISTORE i
    for (var i = 0; i < instr.size(); i++) {
        var nodeIConst0 = instr.get(i);
        if (nodeIConst0.getOpcode() !== Opcodes.ICONST_0) {
            continue;
        }
        var nodeIStore = nodeIConst0.getNext();
        if (nodeIStore.getOpcode() !== Opcodes.ISTORE || nodeIStore.var !== loopVariableIndex) {
            continue;
        }
        return nodeIConst0;
    }
    return null;
}

function initializeCoreMod() {
    return {
        'ench_duplicate_consistency_getTagEnchantmentLevel': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.item.enchantment.EnchantmentHelper',
                // This is method patched in by neo, so no need to do any mapping for the name.
                'methodName': 'getTagEnchantmentLevel',
                'methodDesc': '(Lnet/minecraft/world/item/enchantment/Enchantment;Lnet/minecraft/world/item/ItemStack;)I'
            },
            'transformer': function (methodNode) {
                var instr = methodNode.instructions;

                // Find the loop post statement: i++
                var postLoopStatement = findPostLoopStatement(instr);
                if (postLoopStatement == null) {
                    throw "Could not find the post loop statement.";
                }
                var loopVariableIndex = postLoopStatement.var;

                // Find the loop condition: i < listtag.size()
                // Technically it is checking i >= listtag.size() and exiting the loop if so.
                var loopCondition = findLoopCondition(instr, loopVariableIndex);
                if (loopCondition == null) {
                    throw "Could not find the loop condition.";
                }
                var enchListTagVariableIndex = loopCondition.getNext().var;

                // Find the loop initializer: int i = 0
                var loopInitializer = findLoopInitializer(instr, loopVariableIndex);
                if (loopInitializer == null) {
                    throw "Could not find the loop initializer.";
                }

                // Modify the post loop statement: i++ -> i--
                postLoopStatement.incr = -1;

                // Modify the loop initializer: int i = 0 -> int i = listtag.size() - 1
                // ICONST_0
                // -- Into:
                // ALOAD listtag
                // INVOKEVIRTUAL net/minecraft/nbt/ListTag.size()I
                // ICONST_1
                // ISUB
                instr.insertBefore(loopInitializer, new VarInsnNode(Opcodes.ALOAD, enchListTagVariableIndex));
                // ListTag implements 'List<T>.size()I' so no need to do mapping.
                instr.insertBefore(loopInitializer, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, 'net/minecraft/nbt/ListTag', 'size', '()I'));
                instr.insertBefore(loopInitializer, new InsnNode(Opcodes.ICONST_1));
                instr.insertBefore(loopInitializer, new InsnNode(Opcodes.ISUB));
                instr.remove(loopInitializer);

                // Modify the loop condition: i < listtag.size() -> i >= 0
                // Technically i >= listtag.size() -> i < 0
                // ILOAD i
                // ALOAD listtag
                // INVOKEVIRTUAL net/minecraft/nbt/ListTag.size()I
                // IF_ICMPGE L
                // -- Into:
                // ILOAD i
                // ICONST_0
                // IF_ICMPLT L
                var loopConditionALoad = loopCondition.getNext()
                var loopConditionJumpIfGreaterOrEqual = loopConditionALoad.getNext().getNext()
                instr.insertBefore(loopConditionALoad, new InsnNode(Opcodes.ICONST_0));
                instr.remove(loopConditionALoad.getNext())
                instr.remove(loopConditionALoad)
                loopConditionJumpIfGreaterOrEqual.setOpcode(Opcodes.IF_ICMPLT);

                ASMAPI.log('INFO', 'Reversed loop order inside EnchantmentHelper.getTagEnchantmentLevel')

                return methodNode;
            }
        }
    }
}