package com.knziha.polymer.paging;

public class SimpleClassConstructor<T> implements ConstructorInterface<T> {
	final Class<T> clazz;
	
	public SimpleClassConstructor(Class<T> clazz) {
		this.clazz = clazz;
	}
	
	@Override
	public T newInstance(int length) {
		try {
			return clazz.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
