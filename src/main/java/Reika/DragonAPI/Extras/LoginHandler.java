/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.DragonAPI.Extras;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.MinecraftForge;
import Reika.DragonAPI.APIPacketHandler.PacketIDs;
import Reika.DragonAPI.DragonAPIInit;
import Reika.DragonAPI.ModList;
//import Reika.DragonAPI.Auxiliary.Trackers.CommandableUpdateChecker;
import Reika.DragonAPI.Auxiliary.Trackers.PlayerFirstTimeTracker;
import Reika.DragonAPI.Auxiliary.Trackers.PlayerHandler.PlayerTracker;
import Reika.DragonAPI.Libraries.ReikaPlayerAPI;
import Reika.DragonAPI.Libraries.IO.ReikaChatHelper;
import Reika.DragonAPI.Libraries.IO.ReikaPacketHelper;
import Reika.DragonAPI.Libraries.Java.ReikaObfuscationHelper;
import cpw.mods.fml.common.eventhandler.Event;

public final class LoginHandler implements PlayerTracker {

	public static final LoginHandler instance = new LoginHandler();

	private static final String reikaMessage = getReikaWelcome();

	private LoginHandler() {

	}

	private static String getReikaWelcome() {
		StringBuilder sb = new StringBuilder(86);
		sb.append("Welcome ").append(EnumChatFormatting.LIGHT_PURPLE.toString()+"Reika").append(EnumChatFormatting.WHITE.toString()+", Developer of:\n").append(EnumChatFormatting.GOLD.toString()+"   DragonAPI\n");
		List<ModList> li = ModList.getReikasMods();
		for (int i = 0; i < li.size(); i++) {
			ModList mod = li.get(i);
			sb.append(EnumChatFormatting.GOLD.toString()+"   ").append(mod.getModLabel()).append(" ");
			if (mod.isLoaded()) {
				sb.append(EnumChatFormatting.GREEN.toString()+"(Installed)");
			}
			else {
				sb.append(EnumChatFormatting.RED.toString()+"(Not Installed)");
			}
			sb.append(EnumChatFormatting.WHITE.toString()+"\n");
		}
		sb.append("to the server!");
		return sb.toString();
	}

	@Override
	public void onPlayerLogin(EntityPlayer ep) {
		boolean flag = ReikaObfuscationHelper.isDeObfEnvironment();
		if (ReikaPlayerAPI.isReika(ep)) {
			ReikaChatHelper.sendChatToAllOnServer(reikaMessage);
		}
		else {

		}

		PlayerFirstTimeTracker.checkPlayer(ep);
		//CommandableUpdateChecker.instance.notifyPlayer(ep);
		if (ep instanceof EntityPlayerMP) {
			EntityPlayerMP emp = (EntityPlayerMP)ep;
			ReikaPacketHelper.sendDataPacket(DragonAPIInit.packetChannel, PacketIDs.LOGIN.ordinal(), emp);
			syncPlayer(emp);
		}
		MinecraftForge.EVENT_BUS.post(new PlayerEnteredDimensionEvent(ep, ep.worldObj.provider.dimensionId));
	}

	@Override
	public void onPlayerLogout(EntityPlayer player) {

	}

	@Override
	public void onPlayerChangedDimension(EntityPlayer player, int from, int to) {
		MinecraftForge.EVENT_BUS.post(new PlayerEnteredDimensionEvent(player, player.worldObj.provider.dimensionId));
		if (player instanceof EntityPlayerMP) {
			syncPlayer((EntityPlayerMP)player);
		}
	}

	@Override
	public void onPlayerRespawn(EntityPlayer player) {
		MinecraftForge.EVENT_BUS.post(new PlayerEnteredDimensionEvent(player, player.worldObj.provider.dimensionId));
		if (player instanceof EntityPlayerMP) {
			syncPlayer((EntityPlayerMP)player);
		}
	}

	private static void syncPlayer(EntityPlayerMP player) {
		ReikaPlayerAPI.syncCustomData(player);
		ReikaPlayerAPI.syncAttributes(player);
	}

	public static final class PlayerEnteredDimensionEvent extends Event {

		public final int dimensionID;
		public final EntityPlayer player;

		public PlayerEnteredDimensionEvent(EntityPlayer ep, int dim) {
			player = ep;
			dimensionID = dim;
		}
	}

}
