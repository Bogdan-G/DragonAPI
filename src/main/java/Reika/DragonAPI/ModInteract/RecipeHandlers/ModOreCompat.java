/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.DragonAPI.ModInteract.RecipeHandlers;

import java.util.ArrayList;
import java.util.Collection;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import Reika.DragonAPI.DragonOptions;
import Reika.DragonAPI.ModList;
import Reika.DragonAPI.Instantiable.Data.Maps.MultiMap;
import Reika.DragonAPI.Interfaces.Registry.OreType;
import Reika.DragonAPI.Libraries.Java.ReikaJavaLibrary;
import Reika.DragonAPI.Libraries.Registry.ReikaOreHelper;

public final class ModOreCompat {

	public static final ModOreCompat instance = new ModOreCompat();

	private final MultiMap<OreType, byte[]> modOres = new MultiMap();
	private final Collection<OreVariant> variants = new ArrayList();

	private ModOreCompat() {
		if (DragonOptions.GREGORES.getState()) {
			try {
			modOres.addValue(ReikaOreHelper.IRON, "oreBandedIron".getBytes("UTF-8"));
			modOres.addValue(ReikaOreHelper.IRON, "oreBrownLimonite".getBytes("UTF-8"));
			modOres.addValue(ReikaOreHelper.IRON, "oreYellowLimonite".getBytes("UTF-8"));
			modOres.addValue(ReikaOreHelper.QUARTZ, "oreNetherQuartz".getBytes("UTF-8"));
			} catch (Throwable t) {
			cpw.mods.fml.common.FMLLog.log(org.apache.logging.log4j.Level.WARN, t, "DragonAPI stacktrace: %s", t);
			modOres.addValue(ReikaOreHelper.IRON, "oreBandedIron".getBytes());
			modOres.addValue(ReikaOreHelper.IRON, "oreBrownLimonite".getBytes());
			modOres.addValue(ReikaOreHelper.IRON, "oreYellowLimonite".getBytes());
			modOres.addValue(ReikaOreHelper.QUARTZ, "oreNetherQuartz".getBytes());
			}

			this.addVariant("Netherrack", "ore*", ModList.GREGTECH);
			this.addVariant("Endstone", "ore*", ModList.GREGTECH);
			this.addVariant("Blackgranite", "ore*", ModList.GREGTECH);
			this.addVariant("Redgranite", "ore*", ModList.GREGTECH);
		}
	}

	public Collection<ItemStack> load(OreType type) {
		Collection<ItemStack> c = new ArrayList();
		for (OreVariant v : variants) {
			v.addOreBlocks(type, c);
		}
		for (byte[] s0 : modOres.get(type)) {
			String s = new String(s0);
			c.addAll(OreDictionary.getOres(s));
			for (OreVariant v : variants) {
				v.addOreBlocks(s, c);
			}
		}
		return c;
	}

	private void addVariant(String name, String key, ModList... mods) {
		if (ReikaJavaLibrary.isAnyModLoaded(mods))
			variants.add(new OreVariant(name, key));
	}

	private static class OreVariant {

		private final String name;
		private final String regex;

		private OreVariant(String s, String r) {
			name = s;
			regex = r;
		}

		private void addOreBlocks(OreType type, Collection<ItemStack> ores) {
			for (String label : type.getOreDictNames()) {
				this.addOreBlocks(label, ores);
			}
		}

		private void addOreBlocks(String tag, Collection<ItemStack> ores) {
			ores.addAll(OreDictionary.getOres(this.parseTag(tag)));
		}

		private String parseTag(String tag) {
			int idx = regex.indexOf('*');
			String pre = regex.substring(0, idx);
			String suff = tag.substring(pre.length());
			String result = pre+name+suff;
			return result;
		}

	}

}
