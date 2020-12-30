//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.knziha.polymer.toolkits.Utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ReusableByteOutputStream extends OutputStream {
    protected byte[] buf;
    protected int count;

    public byte[] data(){
        return buf;
    }

    public ReusableByteOutputStream() {
        this(1024);
    }

    public ReusableByteOutputStream(int size) {
        this.count = 0;
        this.buf = new byte[size];
    }

    public void write(InputStream in) throws IOException {
        int cap;
        if (in instanceof ByteArrayInputStream) {
            cap = in.available();
            this.ensureCapacity(cap);
            this.count += in.read(this.buf, this.count, cap);
        } else {
            while(true) {
                cap = this.buf.length - this.count;
                int sz = in.read(this.buf, this.count, cap);
                if (sz < 0) {
                    return;
                }

                this.count += sz;
                if (cap == sz) {
                    this.ensureCapacity(this.count);
                }
            }
        }
    }

    public void write(int b) {
        this.ensureCapacity(1);
        this.buf[this.count] = (byte)b;
        ++this.count;
    }

    public void ensureCapacity(int space) {
        int newcount = space + this.count;
        if (newcount > this.buf.length) {
            byte[] newbuf = new byte[Math.max(this.buf.length << 1, newcount)];
            if(this.count>0) {
				System.arraycopy(this.buf, 0, newbuf, 0, this.count);
			}
            this.buf = newbuf;
        }
    }

    public void write(byte[] b, int off, int len) {
        this.ensureCapacity(len);
        System.arraycopy(b, off, this.buf, this.count, len);
        this.count += len;
    }

    public void write(byte[] b) {
        this.write(b, 0, b.length);
    }

    public void writeAsAscii(String s) {
        int len = s.length();
        this.ensureCapacity(len);
        int ptr = this.count;

        for(int i = 0; i < len; ++i) {
            this.buf[ptr++] = (byte)s.charAt(i);
        }

        this.count = ptr;
    }

    public void writeTo(OutputStream out) throws IOException {
        out.write(this.buf, 0, this.count);
    }

    public void reset() {
        this.count = 0;
    }

    /** @deprecated */
    public byte[] toByteArray() {
        byte[] newbuf = new byte[this.count];
        System.arraycopy(this.buf, 0, newbuf, 0, this.count);
        return newbuf;
    }

    public int size() {
        return this.count;
    }

    public String toString() {
        return new String(this.buf, 0, this.count);
    }

    public void close() {
    }

    public byte[] getBytes() {
        return this.buf;
    }

    public int getCount() {
        return this.count;
    }
    
}
