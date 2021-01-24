package com.shockwave.pdfium;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.Surface;

import com.shockwave.pdfium.util.Size;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class PdfPage {
	final Size size;
	
	public PdfPage() {
		size = null;
	}
	
	public int getHorizontalOffset() {
		return 0;
	}
	
	public long getScrollAxisOffset() {
		return 0;
	}
	
	public long getPagePtr() {
		return 0;
	}
	
}
