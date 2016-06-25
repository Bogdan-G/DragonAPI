/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.DragonAPI.Instantiable;

import java.awt.Point;
import java.util.Comparator;

import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;


public class Comparators {

	public static class CoordinateDistanceComparator implements Comparator<Coordinate> {

		private final Coordinate target;

		public CoordinateDistanceComparator(Coordinate p) {
			target = p;
		}

		@Override
		public int compare(Coordinate o1, Coordinate o2) {
			return (int)Math.signum(ReikaMathLibrary.py3d(o1.xCoord-target.xCoord, o1.yCoord-target.yCoord, o1.zCoord-target.zCoord));
		}

	}

	public static class PointDistanceComparator implements Comparator<Point> {

		private final Point target;

		public PointDistanceComparator(Point p) {
			target = p;
		}

		@Override
		public int compare(Point o1, Point o2) {
			return (int)Math.signum(ReikaMathLibrary.py3d(o1.x-target.x, 0, o1.y-target.y));
		}

	}

}
