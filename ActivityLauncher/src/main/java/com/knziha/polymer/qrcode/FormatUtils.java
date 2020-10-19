/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.knziha.polymer.qrcode;

import android.content.Intent;
import android.net.Uri;

import com.google.zxing.BarcodeFormat;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public final class FormatUtils {
	/**
	 * By default, sending this will decode all barcodes that we understand. However it
	 * may be useful to limit scanning to certain formats. Use
	 * {@link Intent#putExtra(String, String)} with one of the values below.
	 *
	 * Setting this is effectively shorthand for setting explicit formats with {@link #FORMATS}.
	 * It is overridden by that setting.
	 */
	public static final String MODE = "SCAN_MODE";
	
	public static final String PRODUCT_MODE = "PRODUCT_MODE"; //Decode only UPC and EAN barcodes. This is the right choice for shopping apps which get prices, reviews, etc. for products.
	
	public static final String ONE_D_MODE = "ONE_D_MODE";//Decode only 1D barcodes.
	
	public static final String QR_CODE_MODE = "QR_CODE_MODE";//Decode only QR codes.

	public static final String DATA_MATRIX_MODE = "DATA_MATRIX_MODE";//Decode only Data Matrix codes.

	public static final String AZTEC_MODE = "AZTEC_MODE";// Decode only Aztec.
	
	public static final String PDF417_MODE = "PDF417_MODE"; //Decode only PDF417.
	
	public static final String FORMATS = "SCAN_FORMATS";
	
	private static final Pattern COMMA_PATTERN = Pattern.compile(",");
	
	public static final Set<BarcodeFormat> PRODUCT_FORMATS;
	public static final Set<BarcodeFormat> INDUSTRIAL_FORMATS;
	public static final Set<BarcodeFormat> ONE_D_FORMATS;
	public static final Set<BarcodeFormat> QR_CODE_FORMATS = EnumSet.of(BarcodeFormat.QR_CODE);
	public static final Set<BarcodeFormat> DATA_MATRIX_FORMATS = EnumSet.of(BarcodeFormat.DATA_MATRIX);
	public static final Set<BarcodeFormat> AZTEC_FORMATS = EnumSet.of(BarcodeFormat.AZTEC);
	public static final Set<BarcodeFormat> PDF417_FORMATS = EnumSet.of(BarcodeFormat.PDF_417);
	static {
		PRODUCT_FORMATS = EnumSet.of(BarcodeFormat.UPC_A,
				BarcodeFormat.UPC_E,
				BarcodeFormat.EAN_13,
				BarcodeFormat.EAN_8,
				BarcodeFormat.RSS_14,
				BarcodeFormat.RSS_EXPANDED);
		INDUSTRIAL_FORMATS = EnumSet.of(BarcodeFormat.CODE_39,
				BarcodeFormat.CODE_93,
				BarcodeFormat.CODE_128,
				BarcodeFormat.ITF,
				BarcodeFormat.CODABAR);
		ONE_D_FORMATS = EnumSet.copyOf(PRODUCT_FORMATS);
		ONE_D_FORMATS.addAll(INDUSTRIAL_FORMATS);
	}
	private static final Map<String,Set<BarcodeFormat>> FORMATS_FOR_MODE;
	static {
		FORMATS_FOR_MODE = new HashMap<>();
		FORMATS_FOR_MODE.put(ONE_D_MODE, ONE_D_FORMATS);
		FORMATS_FOR_MODE.put(PRODUCT_MODE, PRODUCT_FORMATS);
		FORMATS_FOR_MODE.put(QR_CODE_MODE, QR_CODE_FORMATS);
		FORMATS_FOR_MODE.put(DATA_MATRIX_MODE, DATA_MATRIX_FORMATS);
		FORMATS_FOR_MODE.put(AZTEC_MODE, AZTEC_FORMATS);
		FORMATS_FOR_MODE.put(PDF417_MODE, PDF417_FORMATS);
	}
	
	private FormatUtils() {}
	
	public static Set<BarcodeFormat> parseDecodeFormats(Intent intent) {
		Iterable<String> scanFormats = null;
		CharSequence scanFormatsString = intent.getStringExtra(FORMATS);
		if (scanFormatsString != null) {
			scanFormats = Arrays.asList(COMMA_PATTERN.split(scanFormatsString));
		}
		return parseDecodeFormats(scanFormats, intent.getStringExtra(MODE));
	}
	
	public static Set<BarcodeFormat> parseDecodeFormats(Uri inputUri) {
		List<String> formats = inputUri.getQueryParameters(FORMATS);
		if (formats != null && formats.size() == 1 && formats.get(0) != null){
			formats = Arrays.asList(COMMA_PATTERN.split(formats.get(0)));
		}
		return parseDecodeFormats(formats, inputUri.getQueryParameter(MODE));
	}
	
	private static Set<BarcodeFormat> parseDecodeFormats(Iterable<String> scanFormats, String decodeMode) {
		if (scanFormats != null) {
			Set<BarcodeFormat> formats = EnumSet.noneOf(BarcodeFormat.class);
			try {
				for (String format : scanFormats) {
					formats.add(BarcodeFormat.valueOf(format));
				}
				return formats;
			} catch (IllegalArgumentException iae) {
				// ignore it then
			}
		}
		if (decodeMode != null) {
			return FORMATS_FOR_MODE.get(decodeMode);
		}
		return null;
	}
	
}
