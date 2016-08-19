/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.DragonAPI.Instantiable.Data;

import java.util.HashMap;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

public class WeightedRandom<V> {

	private static final Random r = new org.bogdang.modifications.random.XSTR();

	private final HashMap<V, Double> data = new HashMap();
	private double weightSum;

	public void addEntry(V obj, double weight) {
		data.put(obj, weight);
		this.weightSum += weight;
	}

	public V getRandomEntry() {
		double d = r.nextFloat()*this.weightSum;
		double p = 0;
		for (V obj : data.keySet()) {
			p += data.get(obj);
			if (d <= p) {
				return obj;
			}
		}
		return null;
	}

	public V getRandomEntry(V fallback, double wt) {
		double sum = this.weightSum+wt;
		double d = r.nextFloat()*sum;
		double p = 0;
		for (V obj : data.keySet()) {
			p += data.get(obj);
			if (d <= p) {
				return obj;
			}
		}
		return fallback;
	}

	public boolean isEmpty() {
		return data.isEmpty();
	}

	public int size() {
		return data.size();
	}

	@Override
	public String toString() {
		return data.toString();
	}

	public static class InvertedWeightedRandom<V> {
		private final NavigableMap<Double, V> data = new TreeMap<Double, V>();
		private double weightSum;

		public void addEntry(double weight, V result) {
			weightSum += weight;
			data.put(weightSum, result);
		}

		public V getRandomEntry() {
			double value = r.nextFloat()*this.weightSum;
			//ReikaJavaLibrary.pConsole(value+" of "+this.data.toString());
			return data.ceilingEntry(value).getValue();
		}

		public boolean isEmpty() {
			return data.isEmpty();
		}

		public int size() {
			return data.size();
		}

		@Override
		public String toString() {
			return data.toString();
		}
	}

}
