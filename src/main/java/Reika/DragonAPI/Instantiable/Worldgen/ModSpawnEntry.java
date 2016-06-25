/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.DragonAPI.Instantiable.Worldgen;

import net.minecraft.world.biome.BiomeGenBase.SpawnListEntry;
import Reika.DragonAPI.ModList;
//import Reika.DragonAPI.Exception.MisuseException;

public class ModSpawnEntry {

	private final ModClass mc;
	public final int weight;
	public final int minsize;
	public final int maxsize;

	public ModSpawnEntry(ModList mod, String c, int weight, int min, int max) {
		mc = new ModClass(mod, c);
		this.weight = weight;
		minsize = min;
		maxsize = max;
	}

	public boolean isLoadable() {
		return mc.mod.isLoaded();
	}

	public SpawnListEntry getEntry() {
		if (!this.isLoadable())
			cpw.mods.fml.common.FMLLog.warning("You cannot load a spawn entry for a mod that is not loaded!");
		try {
			SpawnListEntry entry = new SpawnListEntry(Class.forName(mc.className), weight, minsize, maxsize);
			return entry;
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static class ModClass {

		public final ModList mod;
		public final String className;

		public ModClass(ModList mod, String c) {
			this.mod = mod;
			className = c;
		}

	}
}
