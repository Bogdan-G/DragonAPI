/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.DragonAPI.Libraries;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import Reika.DragonAPI.DragonAPICore;

public class ReikaEnchantmentHelper extends DragonAPICore {

	public static final Comparator<Enchantment> enchantmentNameSorter = new EnchantmentNameComparator();
	public static final Comparator<Enchantment> enchantmentTypeSorter = new EnchantmentTypeComparator();

	/** Get a listing of all enchantments on an ItemStack. Args: ItemStack */
	public static HashMap<Enchantment,Integer> getEnchantments(ItemStack is) {
		Map<Integer, Integer> enchants = EnchantmentHelper.getEnchantments(is);
		if (enchants == null)
			return null;
		HashMap<Enchantment,Integer> ench = new HashMap();
		for (Integer id : enchants.keySet()) {
			Enchantment e = Enchantment.enchantmentsList[id];
			int level = enchants.get(id);
			ench.put(e, level);
		}
		return ench;
	}

	/** Applies all enchantments to an ItemStack. Args: ItemStack, enchantment map */
	public static void applyEnchantments(ItemStack is, HashMap<Enchantment,Integer> en) {
		for (Enchantment e : en.keySet()) {
			int level = en.get(e);
			if (level > 0) {
				if (is.getItem() == Items.enchanted_book) {
					Items.enchanted_book.addEnchantment(is, new EnchantmentData(e, level));
				}
				else {
					is.addEnchantment(e, level);
				}
			}
		}
	}

	/** Returns the enchantment level of an ItemStack. Args: Enchantment, ItemStack */
	public static int getEnchantmentLevel(Enchantment e, ItemStack is) {
		if (is == null)
			return 0;
		Map enchants = EnchantmentHelper.getEnchantments(is);
		if (enchants == null)
			return 0;
		if (enchants.containsKey(e.effectId)) {
			int level = (Integer)enchants.get(e.effectId);
			return level;
		}
		return 0;
	}

	/** Test whether an ItemStack has an enchantment. Args: Enchantment, ItemStack */
	public static boolean hasEnchantment(Enchantment e, ItemStack is) {
		if (is == null)
			return false;
		Map enchants = EnchantmentHelper.getEnchantments(is);
		if (enchants == null)
			return false;
		return enchants.containsKey(e.effectId);
	}

	/** Returns the speed bonus that efficiency that gives. Args: Level */
	public static float getEfficiencyMultiplier(int level) {
		return (float)Math.pow(1.3, level);
	}

	/** Returns true iff all the enchantments are compatible with each other. */
	public static boolean areCompatible(Collection<Enchantment> enchantments) {
		Iterator<Enchantment> it = enchantments.iterator();
		Iterator<Enchantment> it2 = enchantments.iterator();
		while (it.hasNext()) {
			Enchantment e = it.next();
			while (it2.hasNext()) {
				Enchantment e2 = it2.next();
				if (!areEnchantsCompatible(e, e2))
					return false;
			}
		}
		return true;
	}

	/** Returns true iff the new enchantment is compatible all the other enchantments. */
	public static boolean isCompatible(Collection<Enchantment> enchantments, Enchantment addition) {
		Iterator<Enchantment> it = enchantments.iterator();
		Iterator<Enchantment> it2 = enchantments.iterator();
		while (it.hasNext()) {
			Enchantment e = it.next();
			if (!areEnchantsCompatible(e, addition))
				return false;
		}
		return true;
	}

	public static boolean areEnchantsCompatible(Enchantment e, Enchantment e2) {
		return e.canApplyTogether(e2);
	}

	public static boolean hasEnchantments(ItemStack is) {
		Map map = EnchantmentHelper.getEnchantments(is);
		return map != null && !map.isEmpty();
	}

	private static class EnchantmentTypeComparator implements Comparator<Enchantment> {

		@Override
		public int compare(Enchantment o1, Enchantment o2) {
			return o1.type.ordinal()-o2.type.ordinal();
		}

	}

	private static class EnchantmentNameComparator implements Comparator<Enchantment> {

		@Override
		public int compare(Enchantment o1, Enchantment o2) {
			return o1.getName().compareTo(o2.getName());
		}

	}

	public static void addEnchantment(NBTTagCompound tag, Enchantment enchantment, int level) {
		if (!tag.hasKey("ench", 9))
			tag.setTag("ench", new NBTTagList());
		NBTTagList li = tag.getTagList("ench", 10);
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setShort("id", (short)enchantment.effectId);
		nbt.setShort("lvl", ((byte)level));
		li.appendTag(nbt);
	}

}
