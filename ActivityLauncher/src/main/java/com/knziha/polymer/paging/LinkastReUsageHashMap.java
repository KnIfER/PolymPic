package com.knziha.polymer.paging;


import com.knziha.polymer.Utils.CMN;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;

public class LinkastReUsageHashMap<K,V> extends LinkedHashMap<K,V> {
	int mCapacity;
	private Field f_accessOrder;

	public LinkastReUsageHashMap(int Capacity) {
		super(Capacity, 1, true);
		mCapacity = Capacity-6;
	}
	
	public LinkastReUsageHashMap(int initialCapacity, int capacity) {
		super(initialCapacity, 1, capacity != 0);
		this.mCapacity = capacity;
	}

	@Override
	protected boolean removeEldestEntry(Entry<K, V> eldest) {
		if(mCapacity > 0) {
			return size() > mCapacity;
		}
		return false;
	}

	public V getSafe(K key) {
		try {
			if(f_accessOrder==null) {
				f_accessOrder = LinkedHashMap.class.getDeclaredField("accessOrder");
				f_accessOrder.setAccessible(true);
			}
			f_accessOrder.set(this, false);
			V val = get(key);
			f_accessOrder.set(this, true);
			return val;
		} catch (Exception ignored) {
			CMN.Log(ignored);
		}
		return super.get(key);
	}
}
