/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.DragonAPI.ModInteract;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;

import minetweaker.util.IEventHandler;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import Reika.DragonAPI.Exception.MisuseException;
import Reika.DragonAPI.IO.ReikaFileReader;
import Reika.DragonAPI.IO.ReikaFileReader.LineEditor;
import Reika.DragonAPI.Instantiable.Data.KeyedItemStack;
import Reika.DragonAPI.Instantiable.Data.Collections.OneWayCollections.OneWayMap;
import Reika.DragonAPI.Libraries.Java.ReikaJavaLibrary;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;

/** Used to prevent Minetweaker from interfering with the balance of a mod; mods can register items whose recipes cannot be removed or items which
 * cannot have additional recipes added. It does not allow for the prevention of creating recipes that <i>use</i> the items, as there is no harm that
 * can result from such use.
 * <br><br>
 * <b>--Message from Reika--</b>
 * <br>
 * As I can already imagine the complaints of some 500 upset server admins and mod authors, who will object to the whole purpose of this class,
 * I pose the following question:
 * <br>
 * Given that the rules that I have in place on some of my mods, which disallows using Minetweaker or other tools to change the core recipes - with the
 * reason for that being the abuse I received day in and day out from people screwing things up and blaming me back before I put such rules in place,
 * <a href="https://sites.google.com/site/reikasminecraft/modifications">as detailed on my website</a> - and the fact that, as I have
 * recently seen firsthand, some people take rules as suggestions and make such changes anyway, what <i>exactly</i> is the problem with the fact I am
 * now enforcing the rules? If you wish to say that this implementation causes problems, that there is a better way to enforce this, then I will by
 * all means listen, but if your argument is one of moralistic proclamations that I have no right to enforce my own rules, no matter what, or that
 * I am committing some offence by having rules in the first place, then I will still listen...but for my own amusement.
 * <br><br>
 * This class is <u>NOT</u> an attack on Minetweaker or its author StanHebben (who I must say left me extremely impressed with the complexity,
 * versatility, and sophistication of MT's internal workings and the ZenScript system), nor
 * do I have any animosity towards them (and I hope they feel the same for me). I am in full agreement that MT is a very useful tool, and one I have
 * used myself on several occasions, with excellent results. This class is purely to provide me - and anyone else using DragonAPI with a similar
 * need - a means to save myself from once again dealing with hordes of people who make changes to a mod with a dim understanding at best of their
 * effects, then chew out the developer of said mod when their pack or server balance implodes due to exploits/missing dependencies/endgame shortcuts.
 */
public final class MTInteractionManager {

	public static final MTInteractionManager instance = new MTInteractionManager();

	private final OneWayMap<Prevention, OneWayMap<KeyedItemStack, String>> data = new OneWayMap();

	private final Method reloadMethod;
	private final Method addReloadWatcher;

	private MTInteractionManager() {
		if (!isMTLoaded()) {
			reloadMethod = null;
			addReloadWatcher = null;
			return;
		}
		Method rl = null;
		Method rlw = null;
		try {
			Class api = Class.forName("minetweaker.MineTweakerImplementationAPI");
			rl = getReloadMethod(api);
			rlw = api.getDeclaredMethod("onReloadEvent", IEventHandler.class);
			rlw.setAccessible(true);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		reloadMethod = rl;
		addReloadWatcher = rlw;
		try {
			addReloadWatcher.invoke(null, new ReloadHandler());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static Method getReloadMethod(Class api) {
		String[] names = {
				"reload",
				"reloadScripts",
				"reloadAllScripts",
				"rebuild",
				"rebuildScripts",
				"rebuildAllScripts",
		};
		for (int i = 0; i < names.length; i++) {
			try {
				Method m = api.getDeclaredMethod(names[i]);
				m.setAccessible(true);
				return m;
			}
			catch (NoSuchMethodException e) {

			}
		}
		return null;
	}

	public void scanAndRevert() {
		this.scanAndRemove();
		this.reloadMT();
	}

	private void scanAndRemove() {
		ArrayList<File> files = this.getFiles();
		for (File f : files) {
			this.parseFile(f);
		}
	}

	private void parseFile(File f) {
		for (Prevention p : data.keySet()) {
			MTScriptScanner scan = new MTScriptScanner(p);
			scan.performChanges(f);
		}
	}

	/** Reloads the MT scripts, by invoking the same method that /mt reload does. */
	private void reloadMT() {
		if (reloadMethod == null)
			return;
		try {
			reloadMethod.invoke(null);
		}
		catch (Exception e) {

		}
	}

	private ArrayList<File> getFiles() {
		String main = System.getProperty("user.dir").replaceAll("\\\\", "/");
		File folder = new File(main+"/scripts");
		return ReikaFileReader.getAllFilesInFolder(folder, ".zs");
	}

	public void blacklistNewRecipesFor(ItemStack is) {
		this.addEntry(Prevention.NEWRECIPE, is, false, true);
	}

	public void blacklistRecipeRemovalFor(ItemStack is) {
		this.addEntry(Prevention.REMOVERECIPE, is, false, true);
	}

	public void blacklistNewRecipesFor(Block i) {
		this.addEntry(Prevention.NEWRECIPE, new ItemStack(i), true, true);
	}

	public void blacklistRecipeRemovalFor(Block i) {
		this.addEntry(Prevention.REMOVERECIPE, new ItemStack(i), true, true);
	}

	public void blacklistNewRecipesFor(Item i) {
		this.addEntry(Prevention.NEWRECIPE, new ItemStack(i), true, true);
	}

	public void blacklistRecipeRemovalFor(Item i) {
		this.addEntry(Prevention.REMOVERECIPE, new ItemStack(i), true, true);
	}

	public void blacklistNewRecipesFor(ItemStack is, boolean ignoreMetadata, boolean ignoreNBT) {
		this.addEntry(Prevention.NEWRECIPE, is, ignoreMetadata, ignoreNBT);
	}

	public void blacklistRecipeRemovalFor(ItemStack is, boolean ignoreMetadata, boolean ignoreNBT) {
		this.addEntry(Prevention.REMOVERECIPE, is, ignoreMetadata, ignoreNBT);
	}

	private void addEntry(Prevention p, ItemStack is, boolean ignoreMetadata, boolean ignoreNBT) {
		if (is == null || is.getItem() == null) {
			throw new MisuseException("Cannot block MT scripts on null items!");
		}
		ModContainer mc = Loader.instance().activeModContainer();
		if (mc == null) {
			throw new MisuseException("Cannot block MT scripts from outside a mod!");
		}
		UniqueIdentifier id = GameRegistry.findUniqueIdentifierFor(is.getItem());
		if (id == null) {
			throw new MisuseException("Cannot block MT scripts for non-mod items!");
		}
		if (!mc.getModId().equalsIgnoreCase(id.modId)) {
			throw new MisuseException("Cannot block MT scripts on items from another mod!");
		}
		OneWayMap<KeyedItemStack, String> li = data.get(p);
		if (li == null) {
			li = new OneWayMap();
			data.put(p, li);
		}
		KeyedItemStack ks = new KeyedItemStack(is);
		ks.setIgnoreMetadata(ignoreMetadata);
		ks.setIgnoreNBT(ignoreNBT);
		ks.setSimpleHash(true);
		ks.lock();
		if (li.containsKey(ks)) {
			for (Map.Entry<KeyedItemStack, String> e : li.entrySet()) {
				KeyedItemStack key = e.getKey();
				if (key.equals(ks) && !key.exactMatch(ks))
					throw new MisuseException("This item, '"+is.getDisplayName()+"' ("+ks+"), is already listed, but under the wider tag "+key+".");
			}
		}
		li.put(ks, mc.getModId());
	}

	private static enum Prevention {
		NEWRECIPE(),
		REMOVERECIPE();
	}

	private static final class MTItemEntry {

		private final String id;
		private final int meta;

		public MTItemEntry(String item) { //text inside <>
			String[] parts = item.split(":");
			int m = 0;
			boolean metapar = false;
			try {
				m = Integer.parseInt(parts[parts.length-1]);
				metapar = true;
			}
			catch (NumberFormatException e) {

			}
			meta = m;
			id = metapar ? item.substring(0, item.lastIndexOf(':')) : item;
		}

		@Override
		public int hashCode() {
			return id.hashCode()^meta;
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof MTItemEntry) {
				MTItemEntry mt = (MTItemEntry)o;
				return id.equals(mt.id) && mt.meta == meta;
			}
			return false;
		}

		private KeyedItemStack asKeyStack() {
			Item item = (Item)Item.itemRegistry.getObject(id);
			if (item == null)
				return null;
			KeyedItemStack ks = new KeyedItemStack(new ItemStack(item, 1, meta)).setSimpleHash(true).lock();
			return ks;
		}

		@Override
		public String toString() {
			return id+"%"+meta;
		}

	}

	private static final class MTScriptScanner extends LineEditor {

		private final OneWayMap<KeyedItemStack, String> set;
		private String lastItemMod;

		private MTScriptScanner(Prevention p) {
			set = instance.data.get(p);
		}

		@Override
		protected String getReplacementLine(String s, String newline) { //comment, plus note
			return "//"+s+newline+"//The above line was commented out because the mod registering the item for which a recipe is being added or "+
			"removed ("+lastItemMod+") has requested not to allow this. See your logs for more information, including on who to go to if you have "+
			"further questions.";
		}

		@Override
		public boolean editLine(String s) {
			boolean flag = this.parseLine(s);
			if (flag) {
				ReikaJavaLibrary.pConsole("DRAGONAPI: The line '"+s+"' has been commented out of the Minetweaker script, as "+lastItemMod+" has "+
						"requested to disallow such actions. This is NOT a bug in either that mod or Minetweaker; do not bother StanHebben "+
						"with it. You may ask the developer of "+lastItemMod+" for further questions or to request a removal. Be civil.");
			}
			return flag;
		}

		private boolean parseLine(String s) {
			if (s.startsWith("//")) { //comment
				return false;
			}
			else {
				int period = s.indexOf('.');
				if (period >= 0) {
					return this.parseTruncLine(s.substring(period+1));
				}
				else { //not a recipe line
					return false;
				}
			}
		}

		private boolean parseTruncLine(String s) {
			if (s.startsWith("add")) {
				return this.parse(s, Prevention.NEWRECIPE);
			}
			else if (s.startsWith("remove")) {
				return this.parse(s, Prevention.REMOVERECIPE);
			}
			return false;
		}

		private boolean parse(String s, Prevention p) {
			MTItemEntry item = new MTItemEntry(s.substring(s.indexOf('<')+1, s.indexOf('>')));
			KeyedItemStack ks = item.asKeyStack().setSimpleHash(true).lock();
			if (ks != null) {
				String mod = set.get(ks);
				if (mod != null) {
					lastItemMod = mod;
					return true;
				}
			}
			lastItemMod = "[null]";
			return false;
		}

	}

	public static boolean isMTLoaded() {
		try {
			return Class.forName("minetweaker.MineTweakerImplementationAPI") != null;
		}
		catch (ClassNotFoundException e) {
			return false;
		}
	}

	private class ReloadHandler implements IEventHandler {

		@Override
		public void handle(Object r) {
			instance.scanAndRemove(); //Do not reload, because inf loop
		}

	}

}
