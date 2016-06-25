/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.DragonAPI.Instantiable.Data.Collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import Reika.DragonAPI.Instantiable.Data.Immutable.InventorySlot;
import Reika.DragonAPI.Instantiable.Data.Maps.ItemHashMap;
import Reika.DragonAPI.Libraries.ReikaInventoryHelper;

public class ItemCollection {

	private final ItemHashMap<Collection<InventorySlot>> data = new ItemHashMap();
	private final Collection<IInventory> inventories = new HashSet();

	public ItemCollection() {

	}

	public ItemCollection addInventory(IInventory ii) {
		for (int i = 0; i < ii.getSizeInventory(); i++) {
			this.addSlot(new InventorySlot(i, ii));
		}
		inventories.add(ii);
		return this;
	}

	public ItemCollection addSlot(InventorySlot slot) {
		ItemStack is = slot.getStack();
		if (is != null) {
			this.addItemToData(is, slot);
		}
		inventories.add(slot.inventory);
		return this;
	}

	private void addItemToData(ItemStack is, InventorySlot slot) {
		int has = this.getItemCount(is);
		Collection<InventorySlot> li = data.get(is);
		if (li == null) {
			li = new ArrayList();
			data.put(is, li);
		}
		li.add(slot);
	}

	public boolean hasItem(ItemStack is) {
		return data.containsKey(is);
	}

	/** Returns how many items left over. */
	public int addItemsToUnderlyingInventories(ItemStack is, boolean simulate) {
		int left = is.stackSize;
		for (IInventory ii : inventories) {
			left = ReikaInventoryHelper.addToInventoryWithLeftover(is, ii, simulate);
			if (!simulate)
				is.stackSize = left;
			if (left <= 0)
				return 0;
		}
		return left;
	}

	public int getItemCount(ItemStack is) {
		Collection<InventorySlot> li = data.get(is);
		if (li != null) {
			int count = 0;
			for (InventorySlot slot : li) {
				count += slot.getStackSize();
			}
			return count;
		}
		else {
			return 0;
		}
	}
	/*
	public int getItemCountWithOreEquivalence(ItemStack is) {
		int[] ids = OreDictionary.getOreIDs(is);
		if (ids.length == 0) {
			return this.getItemCount(is);
		}
		else {
			Collection<ItemStack> valid = new ArrayList();
			String name = OreDictionary.getOreName(ids[0]);
			List<ItemStack> li = OreDictionary.getOres(name);
			for (ItemStack ore : li) {
				int[] ids2 = OreDictionary.getOreIDs(ore);
				if (Arrays.equals(ids, ids2))
					valid.add(ore);
			}
			int count = 0;
			for (ItemStack c : valid) {
				count += this.getItemCount(c);
			}
			//ReikaJavaLibrary.pConsole(count+"/"+data.toString());
			return count;
		}
	}
	 */
	public int removeXItems(ItemStack is, int amt) {
		Collection<InventorySlot> li = data.get(is);
		int rem = 0;
		if (li != null) {
			Iterator<InventorySlot> it = li.iterator();
			while (it.hasNext()) {
				InventorySlot slot = it.next();
				int dec = slot.decrement(amt);
				rem += dec;
				amt -= dec;
				if (slot.isEmpty())
					it.remove();
				if (amt <= 0)
					return rem;
			}
		}
		return rem;
	}

	public void clear() {
		data.clear();
	}

	@Override
	public String toString() {
		return data.toString();
	}

}
