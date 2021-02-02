package com.knziha.polymer.webstorage;

import android.os.Bundle;

import com.knziha.polymer.Utils.CMN;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/** Backward compatible serializer */
public class WebStacksSer implements WebStacks{
	public byte[] bakeData(Bundle bundle) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try (ObjectOutputStream oos = new ObjectOutputStream(bos)){
			oos.writeObject(123456789);
			Object item;
			for(String key:bundle.keySet()) {
				item = bundle.get(key);
				if(item instanceof Serializable) {
					oos.writeObject(key);
					try {
						oos.writeObject(item);
					} catch (Exception e) {
						oos.writeObject(0);
						CMN.Log(e);
					}
				}
			}
		} catch (Exception e) {
			//CMN.Log(e);
		}
		return bos.toByteArray();
	}
	
	public void readData(Bundle bundle, byte[] data) {
		readStream(bundle, new ByteArrayInputStream(data));
	}
	
	public void readStream(Bundle bundle, InputStream input) {
		try (ObjectInputStream oos = new ObjectInputStream(input)){
			Object len = oos.readObject();
			if(!((Integer)123456789).equals(len)) {
				CMN.Log("wrong format...");
				return;
			}
			while(input.available()>0) {
				String key = (String) oos.readObject();
				Object val = oos.readObject();
				if(key!=null && val instanceof Serializable && !((Integer)0).equals(val)) {
					bundle.putSerializable(key, (Serializable) val);
				}
			}
		} catch (Exception e) {
			//CMN.Log(e);
		}
	}
}
