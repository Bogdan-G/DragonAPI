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

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;

import org.lwjgl.opengl.GL11;

import Reika.DragonAPI.DragonAPICore;
import Reika.DragonAPI.Libraries.IO.ReikaRenderHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public final class ReikaAABBHelper extends DragonAPICore {

	/** Renders an AABB bounding box in the world. Very useful for debug purposes, or as a user-friendliness feature.
	 * Args: World, AABB, Render par2,4,6, x,y,z of machine, root alpha value (-ve for solid color), RGB, solid outline yes/no */
	@SideOnly(Side.CLIENT)
	public static void renderAABB(AxisAlignedBB box, double par2, double par4, double par6, int x, int y, int z, int a, int r, int g, int b, boolean line) {
		int[] color = {r, g, b, a};
		ReikaRenderHelper.prepareGeoDraw(true);
		GL11.glPushMatrix();
		GL11.glTranslatef((float)par2, (float)par4 + 2.0F, (float)par6 + 1.0F);
		GL11.glScalef(1.0F, -1.0F, -1.0F);
		GL11.glTranslatef(0.5F, 0.5F, 0.5F);
		GL11.glPopMatrix();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glEnable(GL11.GL_BLEND);
		if (color[3] > 255 && color[3] > 0)
			color[3] = 255;
		if (color[3] < 0)
			color[3] *= -1;
		boolean filled = true;
		Tessellator var5 = Tessellator.instance;
		double xdiff = box.minX-x;
		double ydiff = box.minY-y;
		double zdiff = box.minZ-z;
		double xdiff2 = box.maxX-x;
		double ydiff2 = box.maxY-y;
		double zdiff2 = box.maxZ-z;

		double px = par2+xdiff;
		double py = par4+ydiff;
		double pz = par6+zdiff;
		double px2 = par2+xdiff2;
		double py2 = par4+ydiff2;
		double pz2 = par6+zdiff2;
		if (var5.isDrawing)
			var5.draw();
		if (line) {
			var5.startDrawing(GL11.GL_LINE_LOOP);
			var5.setColorRGBA(color[0], color[1], color[2], color[3]);
			var5.addVertex(px2, py2, pz);
			var5.addVertex(px, py2, pz);
			var5.addVertex(px, py2, pz2);
			var5.addVertex(px2, py2, pz2);
			var5.draw();
			var5.startDrawing(GL11.GL_LINE_LOOP);
			var5.setColorRGBA(color[0], color[1], color[2], color[3]);
			var5.addVertex(px2, py, pz);
			var5.addVertex(px, py, pz);
			var5.addVertex(px, py, pz2);
			var5.addVertex(px2, py, pz2);
			var5.draw();
			var5.startDrawing(GL11.GL_LINE_LOOP);
			var5.setColorRGBA(color[0], color[1], color[2], color[3]);
			var5.addVertex(px, py, pz);
			var5.addVertex(px, py2, pz);
			var5.draw();
			var5.startDrawing(GL11.GL_LINE_LOOP);
			var5.setColorRGBA(color[0], color[1], color[2], color[3]);
			var5.addVertex(px2, py, pz);
			var5.addVertex(px2, py2, pz);
			var5.draw();
			var5.startDrawing(GL11.GL_LINE_LOOP);
			var5.setColorRGBA(color[0], color[1], color[2], color[3]);
			var5.addVertex(px2, py, pz2);
			var5.addVertex(px2, py2, pz2);
			var5.draw();
			var5.startDrawing(GL11.GL_LINE_LOOP);
			var5.setColorRGBA(color[0], color[1], color[2], color[3]);
			var5.addVertex(px, py, pz2);
			var5.addVertex(px, py2, pz2);
			var5.draw();
		}
		if (filled)
		{
			var5.startDrawing(GL11.GL_QUADS);
			//var5.setBrightness(255);
			var5.setColorRGBA(color[0], color[1], color[2], (int)(color[3]*0.375F));

			var5.addVertex(px, py, pz);
			var5.addVertex(px2, py, pz);
			var5.addVertex(px2, py, pz2);
			var5.addVertex(px, py, pz2);

			var5.addVertex(px2, py, pz);
			var5.addVertex(px2, py2, pz);
			var5.addVertex(px2, py2, pz2);
			var5.addVertex(px2, py, pz2);

			var5.addVertex(px, py2, pz);
			var5.addVertex(px, py, pz);
			var5.addVertex(px, py, pz2);
			var5.addVertex(px, py2, pz2);

			var5.addVertex(px, py2, pz2);
			var5.addVertex(px, py, pz2);
			var5.addVertex(px2, py, pz2);
			var5.addVertex(px2, py2, pz2);

			var5.addVertex(px, py, pz);
			var5.addVertex(px, py2, pz);
			var5.addVertex(px2, py2, pz);
			var5.addVertex(px2, py, pz);

			var5.addVertex(px2, py2, pz);
			var5.addVertex(px, py2, pz);
			var5.addVertex(px, py2, pz2);
			var5.addVertex(px2, py2, pz2);
			var5.draw();
		}

		ReikaRenderHelper.exitGeoDraw();
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}

	/** Returns a 1-block bounding box. Args: x, y, z */
	public static AxisAlignedBB getBlockAABB(int x, int y, int z) {
		return AxisAlignedBB.getBoundingBox(x, y, z, x+1, y+1, z+1);
	}

	/** Returns a sized bounding box centered on a Blocks. Args: x, y, z */
	public static AxisAlignedBB getBlockCenteredAABB(int x, int y, int z, int range) {
		return AxisAlignedBB.getBoundingBox(x, y, z, x+1, y+1, z+1).expand(range, range, range);
	}

	public static AxisAlignedBB getEntityCenteredAABB(Entity e, int range) {
		return AxisAlignedBB.getBoundingBox(e.posX, e.posY, e.posZ, e.posX, e.posY, e.posZ).expand(range, range, range);
	}

	public static AxisAlignedBB getSizedBlockAABB(int x, int y, int z, float size) {
		size = size/2F;
		return AxisAlignedBB.getBoundingBox(x+0.5-size, y+0.5-size, z+0.5-size, x+0.5+size, y+0.5+size, z+0.5+size);
	}

}
