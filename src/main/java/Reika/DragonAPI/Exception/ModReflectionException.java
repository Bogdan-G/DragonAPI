/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.DragonAPI.Exception;

import Reika.DragonAPI.DragonAPIInit;
import Reika.DragonAPI.ModList;
import Reika.DragonAPI.Base.DragonAPIMod;

public class ModReflectionException extends DragonAPIException {

	public ModReflectionException(ModList target, String msg) {
		this(DragonAPIInit.instance, target, msg, false);
	}

	public ModReflectionException(DragonAPIMod mod, ModList target, String msg) {
		message.append(mod.getDisplayName()+" had an error reading "+target.getDisplayName()+":\n");
		message.append(msg+"\n");
		message.append("Please notify "+mod.getModAuthorName()+" as soon as possible, and include your version of "+target.getDisplayName());
		this.crash();
	}

	public ModReflectionException(DragonAPIMod mod, ModList target, String msg, boolean fatal) {
		message.append(mod.getDisplayName()+" had an error reading "+target.getDisplayName()+":\n");
		message.append(msg+"\n");
		message.append("Please notify "+mod.getModAuthorName()+" as soon as possible, and include your version of "+target.getDisplayName());
		if (fatal)
			this.crash();
	}

}
