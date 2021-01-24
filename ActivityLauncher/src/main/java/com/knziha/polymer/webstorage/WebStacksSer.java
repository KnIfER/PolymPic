package com.knziha.polymer.webstorage;

import android.os.Bundle;

import com.knziha.polymer.Utils.CMN;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/** Backward compatible serializer */
public class WebStacksSer implements WebStacks{
	public byte[] bakeData(Bundle bundle) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try (ObjectOutputStream oos = new ObjectOutputStream(bos)){
			oos.writeObject(123456789);
			for(String key:bundle.keySet()) {
				oos.writeObject(key);
				try {
					oos.writeObject(bundle.get(key));
				} catch (Exception e) {
					oos.writeObject(0);
					CMN.Log(e);
				}
			}
		} catch (Exception e) {
			//CMN.Log(e);
		}
		return bos.toByteArray();
	}
	
	public void readData(Bundle bundle, byte[] data) {
		ByteArrayInputStream bos = new ByteArrayInputStream(data);
		try (ObjectInputStream oos = new ObjectInputStream(bos)){
			Object len = oos.readObject();
			if(!((Integer)123456789).equals(len)) {
				CMN.Log("wrong format...");
				return;
			}
			while(bos.available()>0) {
				String key = (String) oos.readObject();
				Object val = oos.readObject();
				if(key!=null && val!=null && !((Integer)0).equals(val)) {
					bundle.putSerializable(key, (Serializable) val);
				}
			}
		} catch (Exception e) {
			//CMN.Log(e);
		}
	}
}
