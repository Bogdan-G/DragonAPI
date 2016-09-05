/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.DragonAPI.Auxiliary.Trackers;

import java.util.HashMap;

import net.minecraft.client.audio.SoundCategory;
import net.minecraft.util.ResourceLocation;
import Reika.DragonAPI.IO.DirectResourceManager;

@Deprecated
public class CustomSoundHandler {

	public static final CustomSoundHandler instance = new CustomSoundHandler();

	private java.util.Map<String, ResourceLocation> sounds = new org.eclipse.collections.impl.map.mutable.UnifiedMap();

	private CustomSoundHandler() {

	}

	/** Do not add ".ogg" to the file name. All strings are forced to lowercase.
	 * To load the sound, put it in /assets/mod folder/sound */
	public void addSound(String mod, String sound, SoundCategory cat) {
		String name = mod.toLowerCase(java.util.Locale.ENGLISH);
		String file = sound.toLowerCase(java.util.Locale.ENGLISH);
		sounds.put(sound, new ResourceLocation(mod, sound));
		DirectResourceManager.getInstance().registerSound(mod, sound+".ogg", cat);
	}

	public ResourceLocation getSoundResource(String file) {
		return sounds.get(file);
	}

}
