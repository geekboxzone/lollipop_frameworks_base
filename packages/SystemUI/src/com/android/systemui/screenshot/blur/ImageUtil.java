package com.android.systemui.screenshot.blur;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Bitmap.Config;

public class ImageUtil {
	public static Bitmap scaleImage(Bitmap bitmap, int width, int height) {
		float scale_w = 1.0f;
		float scale_h = 1.0f;
		if(null != bitmap){
			scale_w = ( width*1.0f)/bitmap.getWidth();
			scale_h = (height*1.0f)/bitmap.getHeight();
			Matrix matrix = new Matrix(); 
			matrix.postScale(scale_w,scale_h);
			return Bitmap.createBitmap(bitmap, 0, 0, 
					bitmap.getWidth(),bitmap.getHeight(),matrix,true);
		}
		return null;
	}
	
	public static Bitmap CropImageForBar(Bitmap bitmap, int barHeight) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int new_w = w;
        int new_h = barHeight;

        int retX = 0;
        int retY = h - barHeight;

        return Bitmap.createBitmap(bitmap, retX, retY, new_w, new_h, null, false);
    }

        public static Bitmap CropImageForStatusBar(Bitmap bitmap, int barHeight) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int new_w = w;
        int new_h = barHeight;

        int retX = 0;
        int retY = barHeight;

        return Bitmap.createBitmap(bitmap, retX, retY, new_w, new_h, null, false);
        }
	
	public static Bitmap CropImageForWinstart(Bitmap bitmap, int width, int height, int barHeight) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int new_w = width;
        int new_h = height;

        int retX = 0;
        int retY = h - height - barHeight;

        return Bitmap.createBitmap(bitmap, retX, retY, new_w, new_h, null, false);
    }
	
    public static Bitmap loadImageFromUrl(String url, int def_w, int def_h) {
        
    	BitmapFactory.Options options = new BitmapFactory.Options();
    	options.inJustDecodeBounds = true;
    	BitmapFactory.decodeFile(url, options);
	    
    	options.inJustDecodeBounds = false;
        options.inSampleSize = calculateInSampleSize(options, def_w, def_h);
        return BitmapFactory.decodeFile(url, options).copy(Config.ARGB_8888, true);  
    }
    
    private static int calculateInSampleSize(BitmapFactory.Options options,  
            int reqWidth, int reqHeight) {  
        // Raw height and width of image  
        final int height = options.outHeight;  
        final int width = options.outWidth;  
        int inSampleSize = 1;  

        while (width / inSampleSize > reqWidth) {  
            inSampleSize++;  
        }  

        while (height / inSampleSize > reqHeight) {  
            inSampleSize++;  
        }  
        return inSampleSize;  
    }
}
