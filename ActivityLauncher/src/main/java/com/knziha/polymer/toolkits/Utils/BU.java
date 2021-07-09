/*  Copyright 2018 KnIfER Zenjio-Kang

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	    http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
	
	Mdict-Java Query Library
*/

package com.knziha.polymer.toolkits.Utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.zip.Adler32;
import java.util.zip.Deflater;
import java.util.zip.InflaterOutputStream;


/**
 * @author KnIfER
 * @date 2018/05/31
 */
public class  BU{//byteUtils

	public static int calcChecksum(byte[] bytes) {
        Adler32 a32 = new Adler32();
        a32.update(bytes);
        int sum = (int) a32.getValue();
        return sum;
    }
	public static int calcChecksum(byte[] bytes,int off,int len) {
        Adler32 a32 = new Adler32();
        a32.update(bytes,off,len);
        int sum = (int) a32.getValue();
        return sum;
    }
	
	
	
    //解压等utils
	public static byte[] zlib_decompress(byte[] encdata,int offset) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			InflaterOutputStream inf = new InflaterOutputStream(out);
			inf.write(encdata,offset, encdata.length-offset);
			inf.close();
			return out.toByteArray();
		} catch (Exception ex) {
			ex.printStackTrace();
			return "ERR".getBytes();
		}
	}
	
	public static byte[] zlib_compress(byte[] encdata) {
		byte[] buffer = new byte[1024];
		ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
		Deflater df = new Deflater();
		df.setInput(encdata, 0, encdata.length);
		df.finish();
		while (!df.finished()) {
			int n1 = df.deflate(buffer);
			baos.write(buffer, 0, n1);
		}
		return baos.toByteArray();
	}
	
	@Deprecated
    public static byte[] toLH(int n) {  
    	  byte[] b = new byte[4];  
    	  b[0] = (byte) (n & 0xff);  
    	  b[1] = (byte) (n >> 8 & 0xff);  
    	  b[2] = (byte) (n >> 16 & 0xff);  
    	  b[3] = (byte) (n >> 24 & 0xff);  
    	  return b;  
    	} 
    
    
    
    public static long toLong(byte[] buffer,int offset) {   
        long  values = 0;   
        for (int i = 0; i < 8; i++) {    
            values <<= 8; values|= (buffer[offset+i] & 0xff);   
        }   
        return values;  
     } 
    public static int toInt(byte[] buffer,int offset) {   
        int  values = 0;   
        for (int i = 0; i < 4; i++) {    
            values <<= 8; values|= (buffer[offset+i] & 0xff);   
        }   
        return values;  
     }     
    
    
	static byte[] _fast_decrypt(byte[] data,byte[] key){ 
	    long previous = 0x36;
	    for(int i=0;i<data.length;i++){
	    	//INCONGRUENT CONVERTION FROM byte to int
	    	int ddd = data[i]&0xff;
	    	long t = (ddd >> 4 | ddd << 4) & 0xff;
	        t = t ^ previous ^ (i & 0xff) ^ (key[(i % key.length)]&0xff);
	        previous = ddd;
	        data[i] = (byte) t;
        }
	    return data;
    }



    @Deprecated
	public static void printBytes2(byte[] b) {
		for(int i=0;i<b.length;i++)
    		System.out.print((int)(b[i]&0xff)+",");
    	System.out.println();
	}
    @Deprecated
    public static void printBytes(byte[] b){
    	for(int i=0;i<b.length;i++)
    		System.out.print("0x"+byteTo16(b[i])+",");
    	System.out.println();
    }
    @Deprecated
    public static void printBytes(byte[] b,int off,int ln){
    	for(int i=off;i<off+ln;i++)
    		System.out.print("0x"+byteTo16(b[i])+",");
    	System.out.println();
    }
    @Deprecated
    public static void printFile(byte[] b,int off,int ln,String path){
    	try {
			FileOutputStream fo = new FileOutputStream(new File(path));
			fo.write(b);
			fo.flush();
			fo.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    @Deprecated
    public static void printFile(byte[] b, String path){
		printFile(b,0,b.length,path);
    }
    public static String byteTo16(byte bt){
        String[] strHex={"0","1","2","3","4","5","6","7","8","9","a","b","c","d","e","f"};
        String resStr="";
        int low =(bt & 15);
        int high = bt>>4 & 15;
        resStr = strHex[high]+strHex[low];
        return resStr;
    }
	
	
	public static void putInt(byte[] b, int off, int val) {
		b[off + 3] = (byte) (val       );
		b[off + 2] = (byte) (val >>>  8);
		b[off + 1] = (byte) (val >>> 16);
		b[off    ] = (byte) (val >>> 24);
	}
	
	public static int getInt(byte[] b, int off) {
		return ((b[off + 3] & 0xFF)      ) +
				((b[off + 2] & 0xFF) <<  8) +
				((b[off + 1] & 0xFF) << 16) +
				((b[off    ]       ) << 24);
	}
	
	
	
	public static char toChar(byte[] buffer,int offset) {
        char  values = 0;   
        for (int i = 0; i < 2; i++) {    
            values <<= 8; values|= (buffer[offset+i] & 0xff);   
        }   
        return values;  
    }


	public static ByteArrayInputStream fileToBytes(File f) {
		return new ByteArrayInputStream(fileToByteArr(f));
	}

	public static byte[] fileToByteArr(File f) {
		try {
			FileInputStream fin = new FileInputStream(f);
			byte[] data = new byte[(int) f.length()];
			fin.read(data);
			return data;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String fileToString(File f) {
		try {
			FileInputStream fin = new FileInputStream(f);
			byte[] data = new byte[(int) f.length()];
			fin.read(data);
			return new String(data, "utf8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}


	public static String fileToString(String path, byte[] buffer, ReusableByteOutputStream bo, Charset charset) {
		try {
			FileInputStream fin = new FileInputStream(path);
			bo.reset();
			int len;
			while((len = fin.read(buffer))>0)
				bo.write(buffer, 0, len);
			return new String(bo.data(),0, bo.size(), charset);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}


	@Deprecated
    public long toLong1(byte[] b,int offset)
	{
		long l = 0;
		l = b[offset+0];
		l |= ((long) b[offset+1] << 8);
		l |= ((long) b[offset+2] << 16);
		l |= ((long) b[offset+3] << 24);
		l |= ((long) b[offset+4] << 32);
		l |= ((long) b[offset+5] << 40);
		l |= ((long) b[offset+6] << 48);
		l |= ((long) b[offset+7] << 56);
		return l;
	}
	
    
    public static String unwrapMdxName(String in) {
    	if(in.toLowerCase().endsWith(".mdx"))
    		return in.substring(0,in.length()-4);
    	return in;
    }
    public static String unwrapMddName(String in) {
    	if(in.toLowerCase().endsWith(".mdd"))
    		return in.substring(0,in.length()-4);
    	return in;
    }
}
	


