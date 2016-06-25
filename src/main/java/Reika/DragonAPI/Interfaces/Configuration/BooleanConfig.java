/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.DragonAPI.Interfaces.Configuration;

public interface BooleanConfig extends ConfigList {

	public boolean isBoolean();

	//public boolean setState(Configuration config);

	public boolean getState();

	public boolean getDefaultState();

}
