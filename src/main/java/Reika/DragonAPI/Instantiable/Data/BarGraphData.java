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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class BarGraphData {
	private /*java.util.Map<Integer, Integer>*/org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap data = new org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap();
	private /*ArrayList<Integer>*/org.eclipse.collections.impl.list.mutable.primitive.IntArrayList values = new org.eclipse.collections.impl.list.mutable.primitive.IntArrayList();

	public BarGraphData() {

	}

	/** The entries are self-sorting. */
	public BarGraphData addEntries(int x, int number) {
		if (values.contains(x)) {
			int amt = data.get(x);
			data.put(x, amt+number);
		}
		else {
			int place = 0;
			for (int i = 0; i < values.size(); i++) {
				int p = values.get(i);
				if (x < p) {
					i = values.size();
					place = i;
				}
			}
			values.addAtIndex(place, x);
			data.put(x, number);
		}
		return this;
	}

	public BarGraphData addOneEntry(int x) {
		return this.addEntries(x, 1);
	}

	public int getNumberEntries() {
		return values.size();
	}

	public int getYOfX(int x) {
		return data.get(x);
	}

	public org.eclipse.collections.api.list.primitive.ImmutableIntList getXValues() {
		return values.toImmutable();
	}

	public void clear() {
		data.clear();
		values.clear();
	}
}
