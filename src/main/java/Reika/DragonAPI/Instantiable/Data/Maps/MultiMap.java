/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.DragonAPI.Instantiable.Data.Maps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import Reika.DragonAPI.Libraries.Java.ReikaJavaLibrary;

public final class MultiMap<K, V> {

	private final java.util.Map<K, Collection<V>> data = new org.eclipse.collections.impl.map.mutable.UnifiedMap();

	private boolean modifiable = true;
	private boolean nullEmpty = false;
	private Comparator<V> ordering = null;

	private final CollectionFactory factory;

	public MultiMap() {
		this(null);
	}

	public MultiMap(CollectionFactory cf) {
		factory = cf != null ? cf : new ListFactory();
	}

	public Collection<V> put(K key, Collection<V> value) {
		if (!modifiable)
			throw new UnsupportedOperationException("Map "+this+" is locked!");
		if (ordering != null && value instanceof List)
			Collections.sort((List)value, ordering);
		return data.put(key, value);
	}

	public Collection<V> putValue(K key, V value) {
		Collection<V> ret = this.remove(key);
		this.addValue(key, value, false, false);
		return ret;
	}

	public boolean addValue(K key, V value) {
		return this.addValue(key, value, false);
	}

	public boolean addValue(K key, V value, boolean allowCopies) {
		return this.addValue(key, value, true, allowCopies);
	}

	private boolean addValue(K key, V value, boolean load, boolean copy) {
		if (!modifiable)
			throw new UnsupportedOperationException("Map "+this+" is locked!");
		Collection<V> li = null;
		if (load)
			li = data.get(key);
		if (!load || li == null) {
			li = this.createCollection();
			data.put(key, li);
		}
		if (copy || !li.contains(value)) {
			li.add(value);
			if (ordering != null && li instanceof List)
				Collections.sort((List)li, ordering);
			return true;
		}
		return false;
	}

	private Collection<V> createCollection() {
		return this.factory.createCollection();
	}

	public Collection<V> remove(K key) {
		if (!modifiable)
			throw new UnsupportedOperationException("Map "+this+" is locked!");
		Collection<V> ret = data.get(key);
		data.remove(key);
		return ret != null ? ret : new ArrayList();
	}

	public Collection<V> get(K key) {
		Collection<V> c = data.get(key);
		if (c == null && this.nullEmpty)
			return null;
		return c != null ? (this.modifiable ? c : Collections.unmodifiableCollection(c)) : this.factory.createCollection(); //Internal NPE protection
	}

	public boolean containsKey(K key) {
		return data.containsKey(key);
	}

	public int getSize() {
		return data.size();
	}

	public boolean isEmpty() {
		return data.isEmpty();
	}

	public void clear() {
		if (!modifiable)
			throw new UnsupportedOperationException("Map "+this+" is locked!");
		data.clear();
	}

	public Collection<K> keySet() {
		return Collections.unmodifiableCollection(data.keySet());
	}

	public Collection<Collection<V>> values() {
		if (!modifiable)
			throw new UnsupportedOperationException("Map "+this+" is locked!");
		return Collections.unmodifiableCollection(data.values());
	}

	public Collection<V> allValues(boolean duplicates) {
		Collection<V> li = duplicates ? new ArrayList() : new HashSet();
		for (Collection<V> c : data.values()) {
			li.addAll(c);
		}
		return li;
	}

	public void shuffleValues() {
		for (Collection c : data.values()) {
			if (c instanceof List)
				Collections.shuffle((List)c);
		}
	}

	public int totalSize() {
		return this.allValues(true).size();
	}

	public boolean containsValue(V value) {
		return ReikaJavaLibrary.collectionMapContainsValue(data, value);
	}

	public boolean containsValueForKey(K key, V value) {
		Collection<V> c = data.get(key);
		return c != null && c.contains(value);
	}

	public boolean remove(K key, V value) {
		if (!modifiable)
			throw new UnsupportedOperationException("Map "+this+" is locked!");
		Collection<V> c = data.get(key);
		boolean flag = c != null && c.remove(value);
		if (flag && c.isEmpty()) {
			this.data.remove(key);
		}
		return flag;
	}

	@Override
	public String toString() {
		return data.toString();
	}

	@Override
	public int hashCode() {
		return data.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof MultiMap && this.data.equals(((MultiMap)o).data);
	}

	public MultiMap<K, V> lock() {
		modifiable = false;
		return this;
	}

	public MultiMap<K, V> setNullEmpty() {
		nullEmpty = true;
		return this;
	}

	public MultiMap<K, V> setOrdered(Comparator<V> order) {
		this.ordering = order;
		return this;
	}

	public void putAll(Map<K, V> map) {
		for (K k : map.keySet()) {
			this.addValue(k, map.get(k));
		}
	}

	public void putAll(MultiMap<K, V> map) {
		for (K k : map.keySet()) {
			for (V v : map.get(k)) {
				this.addValue(k, v);
			}
		}
	}

	public static interface CollectionFactory {

		public Collection createCollection();

	}

	public static final class ListFactory implements CollectionFactory {

		@Override
		public Collection createCollection() {
			return new ArrayList();
		}

	}

	public static final class HashSetFactory implements CollectionFactory {

		@Override
		public Collection createCollection() {
			return new HashSet();
		}

	}

}
