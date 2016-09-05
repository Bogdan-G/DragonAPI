/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.DragonAPI.Instantiable.IO;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import Reika.DragonAPI.DragonAPICore;
import Reika.DragonAPI.Base.DragonAPIMod;
import Reika.DragonAPI.Exception.InvalidConfigException;
//import Reika.DragonAPI.Exception.MisuseException;
import Reika.DragonAPI.IO.ReikaFileReader;
import Reika.DragonAPI.Instantiable.Data.Maps.MultiMap;
import Reika.DragonAPI.Interfaces.Configuration.BooleanConfig;
import Reika.DragonAPI.Interfaces.Configuration.BoundedConfig;
import Reika.DragonAPI.Interfaces.Configuration.ConfigList;
import Reika.DragonAPI.Interfaces.Configuration.DecimalConfig;
import Reika.DragonAPI.Interfaces.Configuration.IntArrayConfig;
import Reika.DragonAPI.Interfaces.Configuration.IntegerConfig;
import Reika.DragonAPI.Interfaces.Configuration.SegmentedConfigList;
import Reika.DragonAPI.Interfaces.Configuration.StringArrayConfig;
import Reika.DragonAPI.Interfaces.Configuration.StringConfig;
import Reika.DragonAPI.Interfaces.Registry.IDRegistry;
import Reika.DragonAPI.Libraries.Java.ReikaStringParser;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class ControlledConfig {

	protected Configuration config;

	private int readID;
	protected File configFile;

	protected final DragonAPIMod configMod;

	private ConfigList[] optionList;
	private IDRegistry[] IDList;

	protected Object[] controls;
	protected int[] otherIDs;

	private final java.util.Map<SegmentedConfigList, String> specialFiles = new org.eclipse.collections.impl.map.mutable.UnifiedMap();
	private final MultiMap<String, SegmentedConfigList> specialConfigs = new MultiMap();
	private final java.util.Map<String, java.util.Map<String, String>> extraFiles = new org.eclipse.collections.impl.map.mutable.UnifiedMap();

	public ControlledConfig(DragonAPIMod mod, ConfigList[] option, IDRegistry[] id, int cfg) {
		configMod = mod;
		optionList = option;
		IDList = id;

		if (option != null) {
			controls = new Object[optionList.length];
		}
		else {
			controls = new Object[0];
			optionList = new ConfigList[0];
		}

		if (id != null)
			otherIDs = new int[IDList.length];
		else {
			otherIDs = new int[0];
			IDList = new IDRegistry[0];
		}
	}

	private final String getConfigPath() {
		return configFile.getAbsolutePath().substring(0, configFile.getAbsolutePath().length()-4);
	}

	public final File getConfigFolder() {
		return configFile.getParentFile();
	}

	public final Collection<String> getExtraFiles() {
		return Collections.unmodifiableCollection(extraFiles.keySet());
	}

	public Object getControl(int i) {
		return controls[i];
	}

	public int getOtherID(int i) {
		return otherIDs[i];
	}

	private boolean checkReset(Configuration config) {
		//readID = config.get("Control", "Config ID - Edit to have your config auto-deleted", CURRENT_CONFIG_ID).getInt();
		//return readID != CURRENT_CONFIG_ID;
		return false;
	}

	protected final void resetConfigFile() {
		String path = this.getConfigPath()+"_Old_Config_Backup.txt";
		File backup = new File(path);
		if (backup.exists())
			backup.delete();
		try {
			DragonAPICore.log(configMod.getDisplayName().toUpperCase(java.util.Locale.ENGLISH)+": Writing Backup File to "+path);
			DragonAPICore.log(configMod.getDisplayName().toUpperCase(java.util.Locale.ENGLISH)+": Use this to restore custom IDs if necessary.");
			backup.createNewFile();
			if (!backup.exists())
				DragonAPICore.logError(configMod.getDisplayName().toUpperCase(java.util.Locale.ENGLISH)+": Could not create backup file at "+path+"!");
			else {
				PrintWriter p = new PrintWriter(backup);
				p.println("#####----------THESE ARE ALL THE OLD CONFIG SETTINGS YOU WERE USING----------#####");
				p.println("#####---IF THEY DIFFER FROM THE DEFAULTS, YOU MUST RE-EDIT THE CONFIG FILE---#####");

				p.close();
			}
		}
		catch (IOException e) {
			DragonAPICore.logError(configMod.getDisplayName().toUpperCase(java.util.Locale.ENGLISH)+": Could not create backup file due to IOException!");
			e.printStackTrace();
		}
		configFile.delete();
	}

	private void versionCheck(FMLPreInitializationEvent event) {
		if (this.checkReset(config)) {
			DragonAPICore.log(configMod.getDisplayName().toUpperCase(java.util.Locale.ENGLISH)+": Config File Format Changed. Resetting...");
			this.resetConfigFile();
			this.initProps(event);
			return;
		}
	}

	public void loadCustomConfigFile(FMLPreInitializationEvent event, String file) {
		this.loadConfigFile(new File(file));
	}

	public void loadSubfolderedConfigFile(FMLPreInitializationEvent event) {
		String name = ReikaStringParser.stripSpaces(configMod.getDisplayName());
		String author = ReikaStringParser.stripSpaces(configMod.getModAuthorName());
		String file = event.getModConfigurationDirectory()+"/"+author+"/"+name+".cfg";
		this.loadConfigFile(new File(file));
	}

	public void loadDefaultConfigFile(FMLPreInitializationEvent event) {
		this.loadConfigFile(event.getSuggestedConfigurationFile());
	}

	private void loadConfigFile(File f) {
		configFile = f;

		if (optionList != null) {
			for (int i = 0; i < optionList.length; i++) {
				ConfigList c = optionList[i];
				if (c instanceof SegmentedConfigList) {
					SegmentedConfigList sg = (SegmentedConfigList)c;
					String s = sg.getCustomConfigFile();
					if (s != null) {
						s = this.parseFileString(s);
						specialFiles.put(sg, s);
						extraFiles.put(s, new org.eclipse.collections.impl.map.mutable.UnifiedMap());
						specialConfigs.addValue(s, sg);
					}
				}
			}
		}
	}

	private String parseFileString(String s) {
		if (s.charAt(0) == '*') {
			String suffix = s.replaceAll("\\*", "");
			s = configFile.getAbsolutePath()+"*"+suffix;
		}
		String ext = s.substring(s.lastIndexOf('.'));
		int post = ext.indexOf('*');
		if (post >= 0) {
			ext = ext.substring(0, post);
			s = s.replaceAll("\\*", "");
			s = s.replaceAll(ext, "");
			s = s+ext;
		}
		return s;
	}

	public final void initProps(FMLPreInitializationEvent event) {
		if (configFile == null)
			cpw.mods.fml.common.FMLLog.warning("Error loading "+configMod.getDisplayName()+": You must load a config file before reading it!");
		config = new Configuration(configFile);
		this.onInit();
		this.loadConfig();
	}

	protected void onInit() {}

	private void loadConfig() {
		config.load();

		if (!specialFiles.isEmpty())
			this.loadExtraFiles();

		for (int i = 0; i < optionList.length; i++) {
			String label = optionList[i].getLabel();
			if (optionList[i].shouldLoad()) {
				controls[i] = this.loadValue(optionList[i]);
			}
			else {
				controls[i] = this.getDefault(optionList[i]);
			}
		}

		for (int i = 0; i < IDList.length; i++) {
			otherIDs[i] = this.getValueFromConfig(IDList[i], config);
		}

		this.loadAdditionalData();

		/*******************************/
		//save the data
		config.save();

		if (!specialFiles.isEmpty())
			this.saveExtraFiles();
	}

	private Object loadValue(ConfigList cfg) {
		if (cfg instanceof BooleanConfig && ((BooleanConfig)cfg).isBoolean())
			return this.setState((BooleanConfig)cfg);
		if (cfg instanceof IntegerConfig && ((IntegerConfig)cfg).isNumeric())
			return this.setValue((IntegerConfig)cfg);
		if (cfg instanceof DecimalConfig && ((DecimalConfig)cfg).isDecimal())
			return this.setFloat((DecimalConfig)cfg);
		if (cfg instanceof StringConfig && ((StringConfig)cfg).isString())
			return this.setString((StringConfig)cfg);
		if (cfg instanceof IntArrayConfig && ((IntArrayConfig)cfg).isIntArray())
			return this.setIntArray((IntArrayConfig)cfg);
		if (cfg instanceof StringArrayConfig && ((StringArrayConfig)cfg).isStringArray())
			return this.setStringArray((StringArrayConfig)cfg);
		return null;
	}

	private Object getDefault(ConfigList cfg) {
		if (cfg instanceof BooleanConfig && ((BooleanConfig)cfg).isBoolean())
			return ((BooleanConfig)cfg).getDefaultState();
		if (cfg instanceof IntegerConfig && ((IntegerConfig)cfg).isNumeric())
			return ((IntegerConfig)cfg).getDefaultValue();
		if (cfg instanceof DecimalConfig && ((DecimalConfig)cfg).isDecimal())
			return ((DecimalConfig)cfg).getDefaultFloat();
		if (cfg instanceof StringConfig && ((StringConfig)cfg).isString())
			return ((StringConfig)cfg).getDefaultString();
		if (cfg instanceof IntArrayConfig && ((IntArrayConfig)cfg).isIntArray())
			return ((IntArrayConfig)cfg).getDefaultIntArray();
		if (cfg instanceof StringArrayConfig && ((StringArrayConfig)cfg).isStringArray())
			return ((StringArrayConfig)cfg).getDefaultStringArray();
		return null;
	}

	private void loadExtraFiles() {
		for (String s : extraFiles.keySet()) {
			File f = new File(s);
			if (f.exists()) {
				this.readData(extraFiles.get(s), f);
			}
		}

		for (ConfigList cfg : specialFiles.keySet()) {
			String file = specialFiles.get(cfg);
			java.util.Map<String, String> data = extraFiles.get(file);
			String s = data.get(cfg.getLabel());
			if (s == null) {
				controls[cfg.ordinal()] = this.getDefault(cfg);
			}
			else {
				try {
					Object o = this.parseData(cfg, s);
					controls[cfg.ordinal()] = o;
				}
				catch (Exception e) {
					controls[cfg.ordinal()] = this.getDefault(cfg);
				}
			}
		}
	}

	private Object parseData(ConfigList cfg, String o) {
		if (cfg instanceof StringConfig && ((StringConfig)cfg).isString()) {
			return o;
		}
		else if (cfg instanceof IntegerConfig && ((IntegerConfig)cfg).isNumeric()) {
			return Integer.parseInt(o);
		}
		else if (cfg instanceof DecimalConfig && ((DecimalConfig)cfg).isDecimal()) {
			return Float.parseFloat(o);
		}
		else if (cfg instanceof IntArrayConfig && ((IntArrayConfig)cfg).isIntArray()) {
			o = o.replaceAll("[", "").replaceAll("]", "");
			String[] parts = o.split(",");
			int[] dat = new int[parts.length];
			for (int i = 0; i < dat.length; i++) {
				dat[i] = Integer.parseInt(parts[i]);
			}
			return dat;
		}
		else if (cfg instanceof StringArrayConfig && ((StringArrayConfig)cfg).isStringArray()) {
			o = o.replaceAll("[", "").replaceAll("]", "");
			String[] parts = o.split(",");
			return parts;
		}
		else {
			return o;
		}
	}

	private void saveExtraFiles() {
		for (String s : extraFiles.keySet()) {
			try {
				File f = new File(s);
				ArrayList<String> li = this.getDataToWriteToFile(s);
				if (!li.isEmpty()) {
					File parent = new File(f.getParent());
					if (!parent.exists())
						parent.mkdirs();
					if (f.exists())
						f.delete();
					f.createNewFile();
					if (f.exists()) {
						ReikaFileReader.writeLinesToFile(f, li, true);
					}
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private ArrayList<String> getDataToWriteToFile(String s) {
		ArrayList<String> li = new ArrayList();
		for (SegmentedConfigList cfg : specialConfigs.get(s)) {
			Object dat = controls[cfg.ordinal()];
			if (cfg.saveIfUnspecified() || !dat.equals(this.getDefault(cfg))) {
				String val = this.encodeData(cfg, dat);
				String sg = "[\""+cfg.getLabel()+"\"=\""+val+"\"";
				li.add(sg);
			}
		}
		return li;
	}

	private String encodeData(ConfigList cfg, Object o) {
		if (cfg instanceof StringConfig && ((StringConfig)cfg).isString()) {
			return (String)o;
		}
		else if (cfg instanceof BooleanConfig && ((BooleanConfig)cfg).isBoolean()) {
			return String.valueOf(o);
		}
		else if (cfg instanceof IntegerConfig && ((IntegerConfig)cfg).isNumeric()) {
			return String.valueOf(o);
		}
		else if (cfg instanceof DecimalConfig && ((DecimalConfig)cfg).isDecimal()) {
			return String.valueOf(o);
		}
		else if (cfg instanceof IntArrayConfig && ((IntArrayConfig)cfg).isIntArray()) {
			return Arrays.toString((int[])o);
		}
		else if (cfg instanceof StringArrayConfig && ((StringArrayConfig)cfg).isStringArray()) {
			return Arrays.toString((String[])o);
		}
		else {
			return "";
		}
	}

	private void readData(java.util.Map<String, String> map, File f) {
		ArrayList<String> li = ReikaFileReader.getFileAsLines(f, true);
		for (String s : li) {
			if (s.startsWith("[")) {
				int min = s.indexOf('"');
				int max = s.lastIndexOf('"');
				String key = s.substring(min, max).replaceAll("\"", ""); //" [""="" "
				String[] dat = key.split(":");
				map.put(dat[0], dat[1]);
			}
		}
	}

	private boolean setState(BooleanConfig cfg) {
		Property prop = config.get("Control Setup", cfg.getLabel(), cfg.getDefaultState());
		if (cfg.isEnforcingDefaults())
			prop.set(cfg.getDefaultState());
		if (cfg instanceof BoundedConfig && !((BoundedConfig)cfg).isValueValid(prop))
			throw new InvalidConfigException(configMod, (BoundedConfig)cfg, prop);
		return prop.getBoolean(cfg.getDefaultState());
	}

	private int setValue(IntegerConfig cfg) {
		Property prop = config.get("Control Setup", cfg.getLabel(), cfg.getDefaultValue());
		if (cfg.isEnforcingDefaults())
			prop.set(cfg.getDefaultValue());
		if (cfg instanceof BoundedConfig && !((BoundedConfig)cfg).isValueValid(prop))
			throw new InvalidConfigException(configMod, (BoundedConfig)cfg, prop);
		return prop.getInt();
	}

	private float setFloat(DecimalConfig cfg) {
		Property prop = config.get("Control Setup", cfg.getLabel(), cfg.getDefaultFloat());
		if (cfg.isEnforcingDefaults())
			prop.set(cfg.getDefaultFloat());
		if (cfg instanceof BoundedConfig && !((BoundedConfig)cfg).isValueValid(prop))
			throw new InvalidConfigException(configMod, (BoundedConfig)cfg, prop);
		return (float)prop.getDouble(cfg.getDefaultFloat());
	}

	private String setString(StringConfig cfg) {
		Property prop = config.get("Control Setup", cfg.getLabel(), cfg.getDefaultString());
		if (cfg.isEnforcingDefaults())
			prop.set(cfg.getDefaultString());
		if (cfg instanceof BoundedConfig && !((BoundedConfig)cfg).isValueValid(prop))
			throw new InvalidConfigException(configMod, (BoundedConfig)cfg, prop);
		return prop.getString();
	}

	private int[] setIntArray(IntArrayConfig cfg) {
		Property prop = config.get("Control Setup", cfg.getLabel(), cfg.getDefaultIntArray());
		if (cfg.isEnforcingDefaults())
			prop.set(cfg.getDefaultIntArray());
		if (cfg instanceof BoundedConfig && !((BoundedConfig)cfg).isValueValid(prop))
			throw new InvalidConfigException(configMod, (BoundedConfig)cfg, prop);
		return prop.getIntList();
	}

	private String[] setStringArray(StringArrayConfig cfg) {
		Property prop = config.get("Control Setup", cfg.getLabel(), cfg.getDefaultStringArray());
		if (cfg.isEnforcingDefaults())
			prop.set(cfg.getDefaultStringArray());
		if (cfg instanceof BoundedConfig && !((BoundedConfig)cfg).isValueValid(prop))
			throw new InvalidConfigException(configMod, (BoundedConfig)cfg, prop);
		return prop.getStringList();
	}

	private int getValueFromConfig(IDRegistry id, Configuration config) {
		return config.get(id.getCategory(), id.getConfigName(), id.getDefaultID()).getInt();
	}

	protected void loadAdditionalData() {}
	/*
	public void reloadCategoryFromDefaults(String category) {
		config.load();
		ConfigCategory cat = config.getCategory(category);
		cat.clear();
		config.save();
		this.loadConfig();
	}*/

}
