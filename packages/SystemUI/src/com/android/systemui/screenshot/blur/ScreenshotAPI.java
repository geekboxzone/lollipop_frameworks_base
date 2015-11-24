package com.android.systemui.screenshot.blur;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.graphics.Bitmap;
import android.util.Log;

public class ScreenshotAPI {
    private static final String TAG = "ScreenshotAPI";
    //private static String HOME_SHOT_PATH ="/data/system/recent_images/home_task_thumbnail.png";
    private static String HOME_SHOT_PATH ="/mnt/sdcard/home_task_thumbnail.png";
	public static boolean mShotReady = false;
 
	public static Bitmap takeHomeScreenshot(int width, int height) {
        // Take the screenshot
		mShotReady= false;
        long now = System.currentTimeMillis();
		Bitmap screen = invokeTakeScreenshot(width,height);
        if (screen == null) {
            return null;
        }
        screen.prepareToDraw();
        Log.e(TAG, "takeHomeScreenshot ConsumedTime:" + (System.currentTimeMillis()-now) + "ms"
                    + "; " + screen.toString());

        return screen;
	}

	
	private static void saveHomeScreenshot(Bitmap bitmap){
		mShotReady= false;
        File file=new File(HOME_SHOT_PATH);
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
            bos.flush();
            bos.close();
            mShotReady= true;
        } catch (IOException e) {
        	mShotReady= false;
            e.printStackTrace();
        }
    }

    public static Bitmap makeBottomBlur(Bitmap homeImage, int barheight) {
        long now = System.currentTimeMillis();
        Bitmap bottom = ImageUtil.CropImageForBar(homeImage, barheight);
        //Bitmap half = ImageUtil.loadImageFromUrl(HOME_SHOT_PATH, dst_w/2, dst_h/2);
        //homeImage.recycle();
        if(bottom == null){
        	return null;
        }
        Log.e(TAG, "CropBottom ConsumedTime:" + (System.currentTimeMillis()-now) + "ms"
                    + "; " + bottom.toString());
        
        GaussianFastBlur fastBlur = new GaussianFastBlur();
        //NdkStackBlur fastBlur = new NdkStackBlur();
        //SuperFastBlur   fastBlur = new SuperFastBlur();
        Bitmap blur = fastBlur.blur(5, bottom.copy(Bitmap.Config.ARGB_8888, true));
        if(blur == null){
        	return null;
        }
        Log.e(TAG, "fastBlur.blur ConsumedTime:" + (System.currentTimeMillis()-now) + "ms"
                    + "; " + blur.toString());
        bottom.recycle();


        // Optimizations
        blur.setHasAlpha(true);
        blur.prepareToDraw();
        
        return blur;
    }
      
    public static Bitmap makeTopBlur(Bitmap homeImage, int barheight) {
        long now = System.currentTimeMillis();
        Bitmap top = ImageUtil.CropImageForStatusBar(homeImage, barheight);
        //Bitmap half = ImageUtil.loadImageFromUrl(HOME_SHOT_PATH, dst_w/2, dst_h/2);
        //homeImage.recycle();
        if(top == null){
                return null;
        }
        Log.w(TAG, "CropTop ConsumedTime:" + (System.currentTimeMillis()-now) + "ms"
                    + "; " + top.toString());

        GaussianFastBlur fastBlur = new GaussianFastBlur();
        //NdkStackBlur fastBlur = new NdkStackBlur();
        //SuperFastBlur   fastBlur = new SuperFastBlur();
        Bitmap blur = fastBlur.blur(5, top.copy(Bitmap.Config.ARGB_8888, true));
        if(blur == null){
                return null;
        }
        Log.w(TAG, "fastBlur.blur ConsumedTime:" + (System.currentTimeMillis()-now) + "ms"
                    + "; " + blur.toString());
        top.recycle();


        // Optimizations
        blur.setHasAlpha(true);
        blur.prepareToDraw();

        return blur;
    }

	public static Bitmap makeHomeBlur(Bitmap homeImage, int dst_w, int dst_h) {
	    long now = System.currentTimeMillis();
        
        Bitmap full = ImageUtil.scaleImage(homeImage, dst_w, dst_h);
        if(full == null){
        	return null;
        }

        // Optimizations
        full.setHasAlpha(true);
        full.prepareToDraw();
        
        return full;
    }
    
    public static Bitmap invokeTakeScreenshot(int width, int height){
    	Class<?> sc;
		try {
			sc = Class.forName("android.view.SurfaceControl");
	        Method[] methods = sc.getMethods();
	        for (int i = 0; i < methods.length; i++) {
	        	//Log.e(TAG, "method -->" + methods[i]);
	         //System.out.println("method -- >" + methods[i]);
	        }
	        Method screenshot = sc.getMethod("screenshot", 
	        		 new Class[]{int.class ,int.class});
	        return (Bitmap)screenshot.invoke(sc, new Object[]{width,height});  
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    }
}
