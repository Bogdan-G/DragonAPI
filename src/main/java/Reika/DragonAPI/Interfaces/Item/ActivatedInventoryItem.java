/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.DragonAPI.Interfaces.Item;

import net.minecraft.item.ItemStack;

public interface ActivatedInventoryItem {

	public ItemStack[] getInventory(ItemStack is);

	public void decrementSlot(ItemStack is, int slot);

	public boolean isSlotActive(ItemStack is, int slot);

}
