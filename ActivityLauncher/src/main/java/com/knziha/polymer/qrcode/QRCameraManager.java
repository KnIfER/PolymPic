/*
 * Copyright (C) 2008 ZXing authors
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

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;

import com.google.zxing.InvertedLuminanceSource;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.knziha.polymer.R;
import com.knziha.polymer.Utils.CMN;

import java.io.IOException;
import java.lang.ref.WeakReference;

import static com.knziha.polymer.qrcode.QRCameraUtils.*;

/** This object wraps the Camera service object and expects to be the only one
 * talking to it. The implementation encapsulates the steps needed to take
 * preview-sized images, which are used for both preview and decoding.
 * @author dswitkin@google.com (Daniel Switkin) */
public final class QRCameraManager implements SensorEventListener {
	public Camera camera;
	
	private ImageListener imageListener;
	
	private Rect framingRect=new Rect();
	
	private boolean previewing;
	
	private int requestedCameraId = -1;
	private int requestedFramingRectWidth;
	private int requestedFramingRectHeight;
	
	private boolean ContinuousFocusing;
	
	QRActivity a;
	
	/** Preview frames are delivered here, which we pass on to the registered
	 * handler. Make sure to clear the handler so it will only receive one
	 * message. */
	private Handler previewHandler;
	
	private SensorManager sensorManager;
	
	private Sensor directionSensor;
	
	private boolean focusing;
	
	private boolean registeredSensorListener;
	
	float mLastX;
	float mLastY;
	float mLastZ;
	
	DisplayMetrics dm;
	
	WeakReference<byte[]> TmpData = new WeakReference<>(null);
	
	public Point screenResolution = new Point();
	public Point cameraResolution = new Point();
	
	/** 摄像机参数 */
	private Camera.Parameters parameters;
	
	public QRCameraManager(QRActivity a, DisplayMetrics dm) {
		this.a=a;
		
		this.dm = dm;
		
		imageListener = new ImageListener(a.root);
		
		sensorManager = (SensorManager) a.getSystemService(Context.SENSOR_SERVICE);
		
		directionSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		
		getFramingRect(true);
	}
	
	/** Opens the camera driver and initializes the hardware parameters. */
	public synchronized void open() throws IOException {
		Camera new_camera = camera;
		if(new_camera!=null) {
			close();
		}
		new_camera = openDriver(requestedCameraId);
		
		if (new_camera == null) {
			throw new IOException();
		}
		camera = new_camera;
		
		imageListener.ready();
		
		ResetCameraSettings();
	}
	
	public void ResetCameraSettings() {
		Camera reset_camera = camera;

		screenResolution.x = dm.widthPixels;
		screenResolution.y = dm.heightPixels;
		if(a.isPortrait) { //竖屏更改4 preview size is always something like 480*320, other 320*480
			screenResolution.x = dm.heightPixels;
			screenResolution.y = dm.widthPixels;
		}
		findBestPreviewSizeValue(reset_camera.getParameters(), cameraResolution, screenResolution);
		//CMN.Log("Camera_resolution: " + cameraResolution, dm.widthPixels+"x"+dm.heightPixels);
		
		if (requestedFramingRectWidth > 0 && requestedFramingRectHeight > 0) {
			applyManualFramingRect(requestedFramingRectWidth, requestedFramingRectHeight);
			requestedFramingRectWidth=requestedFramingRectHeight=0;
		} else {
			getFramingRect(true);
		}
		
		parameters = reset_camera.getParameters();
		
		String parmsBackup = parameters.flatten();
		
		try {
			setDesiredCameraParameters(reset_camera, false);
		} catch (RuntimeException e) {
			// Driver failed. Reset:
			//CMN.Log("Camera_rejected!!!", e);
			if (parmsBackup != null) {
				parameters.unflatten(parmsBackup);
				try {
					reset_camera.setParameters(parameters);
					setDesiredCameraParameters(reset_camera, true);
				} catch (RuntimeException e1) {
					parameters = reset_camera.getParameters();
					// Well, darn. Give up
					//CMN.Log("Camera_rejected even in safe-mode !!!", e1);
				}
			}
		}
		
		Camera.Size previewSize = parameters.getPreviewSize();
		
		ContinuousFocusing = a.opt.getContinuousFocus() && getContinuousFocusing(parameters.getFocusMode());
		
		if(a.isPortrait) {
			a.onNewVideoViewLayout(previewSize.height, previewSize.width);
		} else {
			a.onNewVideoViewLayout(previewSize.width, previewSize.height);
		}
		
		decorateCameraSettings();
		
		imageListener.ready();
	}
	
	public void decorateCameraSettings() {
		Camera decor_camera = camera;
		if(decor_camera!=null) {
			Camera.Parameters params = parameters;
			if(params==null) {
				params = parameters = decor_camera.getParameters();
			}
			setBestExposure(params, true);
			setTorch(params, a.suspensed?a.isTorchLighting:a.opt.getTorchLight());
			//setBestPreviewFPS(params);
			//setBarcodeSceneMode(params);
			decor_camera.setParameters(params);
		}
	}
	
	
	void setDesiredCameraParameters(Camera camera, boolean safeMode) {
		//CMN.Log( ":::Initial camera parameters: " + parameters.flatten());
		
		if (safeMode) {
			CMN.Log("safe mode");
		}
		
		setFocusMode(parameters, a.opt.getContinuousFocus(), safeMode);
		
		parameters.setPreviewSize(cameraResolution.x, cameraResolution.y);
		//CMN.Log("setPreviewSize==", cameraResolution.x+"x"+cameraResolution.y);
		if(previewing) camera.stopPreview();
		
		camera.setParameters(parameters);
		
		if(previewing) camera.startPreview();
		
		
		/****************** 竖屏更改2 *********************/
		camera.setDisplayOrientation(a.isPortrait?90:0);
		
		Camera.Parameters afterParameters = camera.getParameters();
		
		Camera.Size afterSize = afterParameters.getPreviewSize();
		
		if (afterSize != null && (cameraResolution.x != afterSize.width || cameraResolution.y != afterSize.height)) {
			//CMN.Log("Camera said it supported preview size ", cameraResolution.x , 'x', cameraResolution.y, "| but after setting it, preview size is " , afterSize.width, 'x', afterSize.height);
			cameraResolution.x = afterSize.width;
			cameraResolution.y = afterSize.height;
		}
		//CMN.Log( ":::Final camera parameters: " + parameters.flatten());
	}
	
	public synchronized boolean isOpen() {
		return camera != null;
	}
	
	/** Closes the camera driver if still in use. */
	public void close() {
		if (camera != null) {
			camera.release();
			camera = null;
		}
	}
	
	public void release() {
		pause();
		close();
		a.root.removeCallbacks(imageListener.FocusRunnable);
		imageListener.root=null;
		a=null;
	}
	
	/** Asks the camera hardware to begin drawing preview frames to the screen. */
	public void startPreview(SurfaceTexture texture) {
		Camera preview_camera = camera;
		if (preview_camera != null && !previewing) {
			previewing = true;
			if(texture!=null) {
				try {
					preview_camera.setPreviewTexture(texture);
				} catch (IOException e) {
					CMN.Log(e);
				}
			}
			preview_camera.startPreview();
			imageListener.start();
		}
	}
	
	/**Tells the camera to stop drawing preview frames.*/
	public void pause() {
		if (camera != null) {
			previewing = false;
			imageListener.stop();
			try {
				camera.stopPreview();
			} catch (Exception e) {
				CMN.Log(e);
			}
		}
		pauseSensor();
	}
	
	/** A single preview frame will be returned to the handler supplied. The data will arrive as byte[] in the message.obj field */
	public synchronized void requestPreviewFrame(Handler handler) {
		if (camera != null && previewing) {
			//CMN.Log("setOneShotPreviewCallback", handler);
			camera.setOneShotPreviewCallback(imageListener.ready(handler));
		}
	}
	
	/** Calculates the framing rect which the UI should draw to show the user
	 * where to place the barcode. This target helps with alignment as well as
	 * forces the user to hold the device far enough away to ensure the image
	 * will be in focus. 计算这个条形码的扫描框；便于声明的同时，也强制用户通过改变距离来扫描到整个条形码
	 *
	 * @return The rectangle to draw on screen in window coordinates. */
	public synchronized Rect getFramingRect(boolean recalculate) {
		if (recalculate) {
			int width = (int) (Math.min(dm.widthPixels, dm.heightPixels)*0.7);
			int height = (int) (Math.min(dm.widthPixels, dm.heightPixels)*0.7);
			
			int leftOffset = (dm.widthPixels - width) / 2;
			int topOffset = (dm.heightPixels - height) / ((a.getResources().getConfiguration().orientation== ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)?3:2);
			
			framingRect.set(leftOffset, topOffset, leftOffset + width, topOffset + height);
			
			//CMN.Log("Calculated framing rect: " + framingRect);
		}
		return framingRect;
	}

	/** Allows third party apps to specify the camera ID, rather than determine
	 * it automatically based on available cameras and their orientation.. */
	public synchronized void setManualCameraId(int cameraId/* -1 */) {
		requestedCameraId = cameraId;
	}
	
	/** Allows third party apps to specify the scanning rectangle dimensions,
	 * rather than determine them automatically based on screen resolution. */
	public synchronized void applyManualFramingRect(int width, int height) {
		if (width > dm.widthPixels) {
			width = dm.widthPixels;
		}
		if (height > dm.heightPixels) {
			height = dm.heightPixels;
		}
		int leftOffset = (dm.widthPixels - width) / 2;
		int topOffset = (dm.heightPixels - height) / 2;
		framingRect.set(leftOffset, topOffset, leftOffset + width, topOffset + height);
		//CMN.Log("Calculated manual framing rect: " + framingRect);
	}
	
	/**Build and decode the appropriate LuminanceSource object.
	 * @param data A preview frame.
	 * @param sWidth Source Width
	 * @param sHeight Source Height
	 * @param rotated In portrait mode, Camera Display Is rotated, but data remains the same.
*                	Just swap the dimensions to get real image size and framing rect.
	 * @param rotate Whether to try again with rotated data for orientation-sensitive bar-codes such as ISBN.*/
	public Result decodeLuminanceSource(QRActivity.QRActivityHandler handler, byte[] data, int sWidth, int sHeight, boolean rotated, boolean rotate, boolean invert) throws NotFoundException {
		Rect rect = getFramingRect(false);
		// Go ahead and assume it's YUV rather than die.
		//if(true) return new PlanarYUVLuminanceSource(data, sWidth, sHeight, 0, 0, sWidth, sHeight, false);
		//CMN.Log("buildLuminanceSource", width, height, rect.left, rect.top, rect.width(), rect.height());
		//CMN.Log("scale="+a.scale, "trans="+a.vTranslate);
		
		float scale = a.scale; // 源图像显示后的缩放比，比如0.5、0.9
		
		int left=(int)((rect.left-a.vTranslate.x)/scale);
		int top=(int)((rect.top-a.vTranslate.y)/scale);
		
		if(rotated) { //竖屏模式下，图像的数据其实还是横屏的老样子。所以交换一下宽高。
			int tmp=sWidth;
			sWidth = sHeight;
			sHeight = tmp;
			tmp=left;
			left=top;
			top=tmp;
		}
		
		if(left<0) left=0;
		int widthwidth=(int)(rect.height()/scale);
		if(widthwidth+left>sWidth) {
			widthwidth=sWidth-left;
		}
		
		if(top<0) top=0;
		int heightheight=(int)(rect.height()/scale);
		
		if(heightheight+top>sHeight) {
			heightheight=sHeight-top;
		}
		
		if(widthwidth<=0||heightheight<=0) {
			return null;
		}
		LuminanceSource source;
		if(rotate) {//必须旋转数据时，为节省CPU时间，只处理必要的部分。
			int size=heightheight*widthwidth;
			if(heightheight+left-1+(widthwidth+top-1)*sWidth<size) {
				CMN.Log("oops", size, data.length);
				return null;
			}
			byte[] rotatedData = acquireTmpData(size);
			for (int y = 0; y < heightheight; y++) {
				for (int x = 0; x < widthwidth; x++)
					rotatedData[x + y * widthwidth] =  data[(y+left)  + (x+top) * sWidth];
			}
			data = rotatedData;
			source =  new PlanarYUVLuminanceSource(data, heightheight, widthwidth, 0, 0,  heightheight, widthwidth, false);
		} else {
			source = new PlanarYUVLuminanceSource(data, sWidth, sHeight, left, top,  widthwidth, heightheight, false);
		}
		
		if(invert) {
			source = new InvertedLuminanceSource(source);
		}
		//CMN.Log(sWidth, sHeight, left, top,  widthwidth, heightheight);
		//source = PlanarYUVLuminanceSource(data, sWidth, sHeight, 0, 0,  sWidth, sHeight, false);
		return handler.try_decode_source(source);
	}
	
	/**复用临时数据*/
	public byte[] acquireTmpData(int size) {
		byte[] ret = TmpData.get();
		if(ret==null||ret.length<size) {
			TmpData.clear();
			TmpData = new WeakReference<>(ret=new byte[size]);
		}
		//else CMN.Log("reusing……", ret.length, size);
		return ret;
	}
	
	public void autoFocus() {
		imageListener.start();
	}
	
	public void resumeSensor() {
		if(!ContinuousFocusing && a.opt.getSensorAutoFocus()) {
			registeredSensorListener=true;
			sensorManager.registerListener(this, directionSensor, SensorManager.SENSOR_DELAY_NORMAL);
		}
	}
	
	public void pauseSensor() {
		if(registeredSensorListener) {
			registeredSensorListener=false;
			sensorManager.unregisterListener(this);
		}
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		//CMN.Log("onSensorChanged");
		if(ContinuousFocusing) {
			pauseSensor();
		} else if(camera!=null && !focusing) {
			float x = event.values[0];
			float y = event.values[1];
			float z = event.values[2];
			float theta=0.23f;
			if ((Math.abs(mLastX - x) > theta || Math.abs(mLastY - y) > theta || Math.abs(mLastZ - z) > theta)) {
				//CMN.Log("onSensorChanged");
				
				autoFocus();
				
				mLastX = x;
				mLastY = y;
				mLastZ = z;
			}
		}
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	
	}
	
	/** 摄像机回调 */
	class ImageListener implements Camera.AutoFocusCallback, Camera.PreviewCallback {
		private View root;
		private boolean useAutoFocus;
		private Runnable FocusRunnable=this::start;
		
		ImageListener(View root) {
			this.root = root;
		}
		
		public void ready() {
			String currentFocusMode = camera.getParameters().getFocusMode();
			useAutoFocus = /*Prefs*/ currentFocusMode.equals(Camera.Parameters.FOCUS_MODE_AUTO) || currentFocusMode.equals(Camera.Parameters.FOCUS_MODE_MACRO);
			//CMN.Log("Current focus mode '" + currentFocusMode + "'; use auto focus? " + useAutoFocus);
		}
		
		@Override
		public synchronized void onAutoFocus(boolean success, Camera theCamera) {
			//CMN.Log("onAutoFocus::", success);
			focusing=false;
			if(useAutoFocus) {
				if(a.opt.getLoopAutoFocus()) {
					postAutoFocus();
				}
			}
		}
		
		private void postAutoFocus() {
			View root=this.root;
			if(root!=null) {
				root.removeCallbacks(FocusRunnable);
				if(previewing && !ContinuousFocusing) {
					root.postDelayed(FocusRunnable, 1200L);
				}
			}
		}
		
		public synchronized void start() {
			if (useAutoFocus) {
				try {
					camera.autoFocus(this);
					focusing=true;
				} catch (Exception e) {
					CMN.Log(e);
					postAutoFocus();
				}
			}
		}
		
		public synchronized void stop() {
			postAutoFocus();
			if (useAutoFocus) {
				// Doesn't hurt to call this even if not focusing
				try {
					camera.cancelAutoFocus();
				} catch (Exception re) {
					// Have heard RuntimeException reported in Android 4.0.x+; continue?
					CMN.Log("Unexpected exception while cancelling focusing", re);
				}
			}
			ready(null);
		}
		
		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			//CMN.Log("onPreviewFrame", CMN.tid());
			if (previewHandler != null) {
				Message message = previewHandler.obtainMessage(R.id.decode, data);
				//CMN.Log("data_sent::", CMN.id(data));
				message.sendToTarget();
				previewHandler = null;
			} else {
				CMN.Log("Got preview callback, but no handler or resolution available");
			}
		}
		
		public Camera.PreviewCallback ready(Handler handler) {
			previewHandler=handler;
			return this;
		}
	}
}
