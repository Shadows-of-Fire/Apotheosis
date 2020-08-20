package net.minecraft.entity;

import net.minecraft.item.ItemStack;

/**
 * For some reason the crossbow coremod is attempting to load this, so it's being provided so the verifier shuts the hell up.
 * This is a modlauncher/FML bug, and this can be removed once it is fixed.
 */
public interface IRendersAsItem {
	ItemStack func_184543_l();
}