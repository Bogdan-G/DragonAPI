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
import java.util.*;
import org.eclipse.collections.impl.map.mutable.primitive.ObjectDoubleHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.DoubleObjectHashMap;

public class WeightedRandom<V> {

	private static final Random r = new org.bogdang.modifications.random.XSTR();

	private final /*java.util.Map<V, Double>*/ObjectDoubleHashMap data = new ObjectDoubleHashMap();
	private double weightSum;

	public void addEntry(V obj, double weight) {
		data.put(obj, weight);
		this.weightSum += weight;
	}

	public V getRandomEntry() {
		double d = r.nextFloat()*this.weightSum;
		double p = 0;
		for (Object obj : data.keySet()) {
			p += data.get(obj);
			if (d <= p) {
				return (V)obj;
			}
		}
		return null;
	}

	public V getRandomEntry(V fallback, double wt) {
		double sum = this.weightSum+wt;
		double d = r.nextFloat()*sum;
		double p = 0;
		for (Object obj : data.keySet()) {
			p += data.get(obj);
			if (d <= p) {
				return (V)obj;
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
		private final DoubleObjectHashMap/*<Double, V>*/ data = new DoubleObjectHashMap/*<Double, V>*/();
		private double weightSum;

		public void addEntry(double weight, V result) {
			weightSum += weight;
			data.put(weightSum, result);
		}

		public V getRandomEntry() {
			double value = r.nextFloat()*this.weightSum;
			boolean pos = false;
			//ReikaJavaLibrary.pConsole(value+" of "+this.data.toString());
			for (double key : data.keySet().toArray()) {
			  if (key == value) pos = true;
			  if (pos && (key > value)) return (V)data.get(key);
			}
			return (V)null;
			//return data.ceilingEntry(value).getValue();
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
