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

import Reika.DragonAPI.Instantiable.Data.Immutable.DecimalPosition;


public class Orbit {

	public final double semimajorAxis;
	public final double eccentricity;
	public final double inclination;
	public final double RAAN;
	public final double argumentOfPerigee;
	public final double zeroAng;

	public Orbit(double a, double e, double i, double raan, double w, double theta) {
		semimajorAxis = a;
		eccentricity = e;
		inclination = Math.toRadians(i);
		RAAN = Math.toRadians(raan);
		argumentOfPerigee = Math.toRadians(w);
		zeroAng = Math.toRadians(theta);
	}

	//TODO
	/*public DecimalPosition getPosition(double x0, double y0, double z0, double time, double mu) {
		double dtheta = 0; //incomplete
		return this.getPosition(x0, y0, z0, time, mu);
	}*/

	public DecimalPosition getPosition(double x0, double y0, double z0, double dtheta) {
		double theta = zeroAng+dtheta;
		double dd = semimajorAxis * (1 - eccentricity*eccentricity) / (1 + eccentricity * org.bogdang.modifications.math.MathHelperLite.cos(Math.toRadians(theta)));
		double x = dd * (org.bogdang.modifications.math.MathHelperLite.cos(RAAN) * org.bogdang.modifications.math.MathHelperLite.cos(Math.toRadians(theta) + argumentOfPerigee) - org.bogdang.modifications.math.MathHelperLite.sin(RAAN) * org.bogdang.modifications.math.MathHelperLite.sin(Math.toRadians(theta)+argumentOfPerigee)*org.bogdang.modifications.math.MathHelperLite.cos(inclination));
		double y = dd * (org.bogdang.modifications.math.MathHelperLite.sin(RAAN) * org.bogdang.modifications.math.MathHelperLite.cos(Math.toRadians(theta)+argumentOfPerigee) + org.bogdang.modifications.math.MathHelperLite.cos(RAAN) * org.bogdang.modifications.math.MathHelperLite.sin(Math.toRadians(theta)+argumentOfPerigee)) * org.bogdang.modifications.math.MathHelperLite.cos(inclination);
		double z = dd * org.bogdang.modifications.math.MathHelperLite.sin(Math.toRadians(theta)+argumentOfPerigee) * org.bogdang.modifications.math.MathHelperLite.sin(inclination);

		return new DecimalPosition(x+x0, y+y0, z+z0);
	}

}
