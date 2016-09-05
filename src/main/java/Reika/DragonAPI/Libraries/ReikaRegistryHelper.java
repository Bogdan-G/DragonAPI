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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import Reika.DragonAPI.DragonAPICore;
import Reika.DragonAPI.ModList;
import Reika.DragonAPI.Base.DragonAPIMod;
import Reika.DragonAPI.Exception.RegistrationException;
import Reika.DragonAPI.Instantiable.ItemBlockCustomLocalization;
import Reika.DragonAPI.Interfaces.Registry.BlockEnum;
import Reika.DragonAPI.Interfaces.Registry.EntityEnum;
import Reika.DragonAPI.Interfaces.Registry.ItemEnum;
import Reika.DragonAPI.Interfaces.Registry.RegistrationList;
import Reika.DragonAPI.Libraries.Java.ReikaReflectionHelper;
import Reika.DragonAPI.ModInteract.LegacyWailaHelper;
import Reika.DragonAPI.ModRegistry.InterfaceCache;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.LoaderState;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

public final class ReikaRegistryHelper extends DragonAPICore {

	private static final java.util.Map<BlockEnum, ArrayList<Integer>> blockVariants = new org.eclipse.collections.impl.map.mutable.UnifiedMap();
	private static final java.util.Map<ItemEnum, ArrayList<Integer>> itemVariants = new org.eclipse.collections.impl.map.mutable.UnifiedMap();
	private static final IdentityHashMap<Object, RegistrationList> registries = new IdentityHashMap();
	private static final IdentityHashMap<Object, EntityCollection> modEntityRegistries = new IdentityHashMap();

	/** Instantiates all blocks and registers them to the game. Uses an Enum[] that implements RegistrationList.
	 * Args: Mod, Enum.values(), Target Block[] array to save instances. */
	public static void instantiateAndRegisterBlocks(DragonAPIMod mod, BlockEnum[] enumr, Block[] target) {
		if (enumr.length != target.length)
			throw new RegistrationException(mod, "Invalid storage array!");
		boolean canLoad = !Loader.instance().hasReachedState(LoaderState.INITIALIZATION);
		if (!canLoad)
			throw new RegistrationException(mod, "This mod is loading blocks too late in the setup!");
		for (int i = 0; i < enumr.length; i++) {
			BlockEnum r = enumr[i];
			target[i] = registerBlock(mod, r, i);
		}
	}

	private static Block registerBlock(DragonAPIMod mod, BlockEnum r, int idx) {
		Block b = null;
		if (!r.isDummiedOut()) {
			b = ReikaReflectionHelper.createBlockInstance(mod, r);
			String regname = (mod.getTechnicalName()+"_block_"+r.name()).toLowerCase(java.util.Locale.ENGLISH);
			if (r.hasItemBlock())
				GameRegistry.registerBlock(b, r.getItemBlock(), regname);
			else
				GameRegistry.registerBlock(b, ItemBlockCustomLocalization.class, regname);
			registries.put(b, r);
			registries.put(Item.getItemFromBlock(b), r);
			int num = r.getNumberMetadatas();
			for (int k = 0; k < num; k++)
				registerBlockVariant(r, k);
			if (r.hasItemBlock())
				mod.getModLogger().log("Instantiating Block "+r.getBasicName()+" with ID "+b+" to Block Variable "+b.getClass().getSimpleName()+" (enum index "+idx+") with ItemBlock "+r.getItemBlock().getSimpleName());
			else
				mod.getModLogger().log("Instantiating Block "+r.getBasicName()+" with ID "+b+" to Block Variable "+b.getClass().getSimpleName()+" (enum index "+idx+")");
			if (ModList.WAILA.isLoaded() && InterfaceCache.WAILA.instanceOf(r.getObjectClass())) {
				LegacyWailaHelper.registerLegacyWAILACompat(r);
			}
		}
		else {
			mod.getModLogger().log("Not instantiating Item "+r.getBasicName()+", as it is dummied out.");
		}
		return b;
	}

	/** Instantiates all items and registers them to the game. Uses an Enum[] that implements RegistrationList.
	 * Args: Mod, Enum.values(), Target Item[] array to save instances. */
	public static void instantiateAndRegisterItems(DragonAPIMod mod, ItemEnum[] enumr, Item[] target) {
		if (enumr.length != target.length)
			throw new RegistrationException(mod, "Invalid storage array!");
		boolean canLoad = !Loader.instance().hasReachedState(LoaderState.INITIALIZATION);
		if (!canLoad)
			throw new RegistrationException(mod, "This mod is loading items too late in the setup!");
		for (int i = 0; i < enumr.length; i++) {
			ItemEnum r = enumr[i];
			target[i] = registerItem(mod, r, i);
		}
	}

	private static Item registerItem(DragonAPIMod mod, ItemEnum r, int idx) {
		Item it = null;
		if (!r.isDummiedOut()) {
			it = ReikaReflectionHelper.createItemInstance(mod, r);
			String regname = (mod.getTechnicalName()+"_item_"+r.name()).toLowerCase(java.util.Locale.ENGLISH);
			int num = r.getNumberMetadatas();
			for (int j = 0; j < num; j++) {
				registerItemVariant(r, j);
			}
			GameRegistry.registerItem(it, regname);
			registries.put(it, r);
			mod.getModLogger().log("Instantiating Item "+r.getBasicName()+" with ID "+it+" to Item Variable "+it.getClass().getSimpleName()+" (enum index "+idx+"). Has "+r.getNumberMetadatas()+" metadatas.");
		}
		else {
			mod.getModLogger().log("Not instantiating Item "+r.getBasicName()+", as it is dummied out.");
		}
		return it;
	}

	private static void registerBlockVariant(BlockEnum e, int meta) {
		ArrayList<Integer> li = blockVariants.get(e);
		if (li == null) {
			li = new ArrayList();
			blockVariants.put(e, li);
		}
		li.add(meta);
	}

	private static void registerItemVariant(ItemEnum e, int meta) {
		ArrayList<Integer> li = itemVariants.get(e);
		if (li == null) {
			li = new ArrayList();
			itemVariants.put(e, li);
		}
		li.add(meta);
	}

	public static int getNumberVariantsFor(BlockEnum b) {
		return blockVariants.containsKey(b) ? blockVariants.get(b).size() : 1;
	}

	public static int getNumberVariantsFor(ItemEnum e) {
		return itemVariants.containsKey(e) ? itemVariants.get(e).size() : 1;
	}

	public static ArrayList<Integer> getVariantsFor(BlockEnum b) {
		ArrayList<Integer> ret = new ArrayList();
		if (blockVariants.containsKey(b)) {
			ArrayList li = blockVariants.get(b);
			ret.addAll(li);
		}
		return ret;
	}

	public static ArrayList<Integer> getVariantsFor(ItemEnum e) {
		ArrayList<Integer> ret = new ArrayList();
		if (itemVariants.containsKey(e)) {
			ArrayList li = itemVariants.get(e);
			ret.addAll(li);
		}
		return ret;
	}

	public static void loadNames() {/*
		for (BlockEnum b : blockVariants.keySet()) {
			ArrayList<Integer> metas = blockVariants.get(b);
			if (metas == null || metas.isEmpty()) {
				LanguageRegistry.addName(b.getBlockInstance(), b.getBasicName());
			}
			else {
				for (int i = 0; i < metas.size(); i++) {
					int meta = metas.get(i);
					ItemStack is = new ItemStack(b.getBlockInstance(), 1, meta);
					LanguageRegistry.addName(is, b.getMultiValuedName(meta));
				}
			}
		}

		for (ItemEnum e : itemVariants.keySet()) {
			ArrayList<Integer> metas = itemVariants.get(e);
			if (metas == null || metas.isEmpty()) {
				LanguageRegistry.addName(e.getItemInstance(), e.getBasicName());
			}
			else {
				for (int i = 0; i < metas.size(); i++) {
					int meta = metas.get(i);
					ItemStack is = new ItemStack(e.getItemInstance(), 1, meta);
					LanguageRegistry.addName(is, e.getMultiValuedName(meta));
				}
			}
		}*/

		for (BlockEnum b : blockVariants.keySet()) {
			Item item = Item.getItemFromBlock(b.getBlockInstance());
			if (item instanceof ItemBlockCustomLocalization) {
				/*if (b == null) {
					b = (BlockEnum)getRegistryForObject(((ItemBlock)item).field_150939_a);
				}*/
				((ItemBlockCustomLocalization)item).setEnumObject(b);

			}
			else {

			}
		}
	}

	public static LoaderState getForgeLoadState() {
		LoaderState[] list = LoaderState.values();
		for (int i = list.length-1; i >= 0; i--) {
			if (Loader.instance().hasReachedState(list[i]))
				return list[i];
		}
		return list[0];
	}

	public static void setupModData(DragonAPIMod mod, FMLPreInitializationEvent evt) {
		ModMetadata dat = evt.getModMetadata();
		dat.authorList.clear();
		dat.authorList.add(mod.getModAuthorName());
		dat.version = mod.getModVersion().toString();
		dat.credits = mod.getModAuthorName();
		dat.name = mod.getDisplayName();
		dat.url = mod.getDocumentationSite().toString();
		dat.updateUrl = mod.getUpdateCheckURL();
		dat.autogenerated = false; //no idea why this needs to be set, but OK
		//dat.description = "Hello";
		String path = mod.getModAuthorName()+"/"+mod.getDisplayName()+"/logo.png";
		dat.logoFile = path;
	}

	/** Overrides one block in the Block database with another. The new block must
	 * have an ID, material constructor! Returns true if successful. */
	public static boolean overrideBlock(DragonAPIMod mod, String blockField, Class<?extends Block> toOverride) {
		mod.getModLogger().log("Overriding Blocks."+blockField+" with "+toOverride);
		try {
			Field f = Blocks.class.getField(blockField);
			Block target = (Block)f.get(null);
			Constructor c = toOverride.getConstructor(Material.class);
			Block block = (Block)c.newInstance(target.getMaterial());
			block.setTickRandomly(target.getTickRandomly());
			block.setBlockName(target.getUnlocalizedName().substring(5));
			block.setLightOpacity(target.getLightOpacity());
			//block.opaqueCubeLookup[target.blockID] = target.isOpaqueCube();
			block.setLightLevel(block.getLightValue());
			block.slipperiness = target.slipperiness;
			block.blockHardness = target.blockHardness;
			block.blockResistance = target.blockResistance;
			int id = Block.getIdFromBlock(target);
			String name = Block.blockRegistry.getNameForObject(target);
			Block.blockRegistry.addObject(id, name, block);
			ReikaReflectionHelper.setFinalField(f, null, block);
			return true;
		}
		catch (Exception e) {
			mod.getModLogger().logError("Could not override Blocks."+blockField+" with "+toOverride);
			e.printStackTrace();
			return false;
		}
	}

	public static RegistrationList getRegistryForObject(Object o) {
		return registries.get(o);
	}

	public static void registerModEntities(Object mod, EntityEnum[] list) {
		for (int i = 0; i < list.length; i++) {
			EntityEnum e = list[i];
			boolean upd = e.sendsVelocityUpdates();
			int dist = e.getTrackingDistance();
			EntityCollection ec = getNextCollectionFor(mod);
			int id = ec.maxID;
			EntityRegistry.registerModEntity(e.getObjectClass(), e.getUnlocalizedName(), id, mod, dist, upd ? 20 : 1, upd);
			if (e.hasSpawnEgg()) {
				//EntityList.entityEggs.put(id, new EntityEggInfo(id, e.eggColor1(), e.eggColor2()));
			}
			ec.addEntry(e);
		}
	}

	public static List<EntityEnum> getEntityEnumForMod(Object mod) {
		EntityCollection ec = modEntityRegistries.get(mod);
		return ec != null ? Collections.unmodifiableList(ec.entities) : null;
	}

	private static EntityCollection getNextCollectionFor(Object mod) {
		EntityCollection ec = modEntityRegistries.get(mod);
		if (ec == null) {
			ec = new EntityCollection();
			modEntityRegistries.put(mod, ec);
		}
		return ec;
	}

	private static class EntityCollection {

		private int maxID = 1;
		private ArrayList<EntityEnum> entities = new ArrayList();

		private EntityCollection() {

		}

		private EntityCollection addEntry(EntityEnum e) {
			entities.add(e);
			maxID++;
			return this;
		}

	}

	public static ArrayList<String> getMods() {
		ArrayList<String> ret = new ArrayList();
		List<ModContainer> li = Loader.instance().getActiveModList();
		for (ModContainer mc : li) {
			Object mod = mc.getMod();
			String name = mc.getModId();
			if (mod instanceof DragonAPIMod)
				name = ((DragonAPIMod)mod).getDisplayName();
			else {
				ModList en = ModList.getModFromID(mc.getModId());
				if (en != null) {
					name = en.getDisplayName();
				}
			}
			String ver = mc.getDisplayVersion();
			if (mod instanceof DragonAPIMod) {
				ver = ((DragonAPIMod)mod).getModVersion().toString();
			}
			ret.add(name+": "+ver);
		}
		return ret;
	}
}
