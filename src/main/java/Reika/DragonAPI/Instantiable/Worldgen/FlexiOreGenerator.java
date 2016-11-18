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

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

public class FlexiOreGenerator extends WorldGenerator {

	private final Block[] replaceables;
	private final int clusterSize;
	private final int generatedMeta;
	private final Block generatedID;

	public FlexiOreGenerator(Block id, int meta, int number, Block... target)
	{
		generatedID = id;
		generatedMeta = meta;
		clusterSize = number;
		replaceables = new Block[target.length];
		System.arraycopy(target, 0, replaceables, 0, target.length);
	}

	@Override
	public boolean generate(World world, Random par2Random, int x, int y, int z)
	{
		final float f = par2Random.nextFloat() * (float)Math.PI;
		final float msin = MathHelper.sin(f);
		final float mcos = (float)Math.sqrt(1.0f-msin*msin);
		final float d0 = x + 8 + msin * clusterSize / 8.0F;
		final float d1 = x + 8 - msin * clusterSize / 8.0F;
		final float d2 = z + 8 + mcos * clusterSize / 8.0F;
		final float d3 = z + 8 - mcos * clusterSize / 8.0F;
		final float d4 = y + par2Random.nextInt(3) - 2;
		final float d5 = y + par2Random.nextInt(3) - 2;

		for (int l = 0; l <= clusterSize; ++l) {
			float d6 = d0 + (d1 - d0) * l / clusterSize;
			float d7 = d4 + (d5 - d4) * l / clusterSize;
			float d8 = d2 + (d3 - d2) * l / clusterSize;
			float d9 = par2Random.nextFloat() * clusterSize / 16.0f;
			float d10 = (MathHelper.sin(l * (float)Math.PI / clusterSize) + 1.0F) * d9 + 1.0f;
			//float d11 = (MathHelper.sin(l * (float)Math.PI / clusterSize) + 1.0F) * d9 + 1.0f;
			final float d10n2 = d10 / 2.0f;
			int i1 = MathHelper.floor_float(d6 - d10n2);
			int j1 = MathHelper.floor_float(d7 - d10n2);
			int k1 = MathHelper.floor_float(d8 - d10n2);
			int l1 = MathHelper.floor_float(d6 + d10n2);
			int i2 = MathHelper.floor_float(d7 + d10n2);
			int j2 = MathHelper.floor_float(d8 + d10n2);

			for (int k2 = i1; k2 <= l1; ++k2) {
				float d12 = (k2 + 0.5f - d6) / (d10n2);
				final float d12b = d12 * d12;
				if (d12b < 1.0f) {
					for (int l2 = j1; l2 <= i2; l2++) {
						float d13 = (l2 + 0.5f - d7) / (d10n2);
						if (d12b + d13 * d13 < 1.0f) {
							for (int i3 = k1; i3 <= j2; i3++) {
								float d14 = (i3 + 0.5f - d8) / (d10n2);

								Block block = world.getBlock(k2, l2, i3);
								for (int rr = 0; rr < replaceables.length; rr++) {
									if (d12b + d13 * d13 + d14 * d14 < 1.0f && (block != null && block.isReplaceableOreGen(world, k2, l2, i3, replaceables[rr])))
										world.setBlock(k2, l2, i3, generatedID, generatedMeta, 2);
								}
							}
						}
					}
				}
			}
		}
		return true;
	}

}
