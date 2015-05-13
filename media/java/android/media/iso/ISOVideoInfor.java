package android.media.iso;

/*
* this file is defined by hh@rock-chips.com
* Define the some ISO Video inforamtions for Rockchip's bluray
*
*/
import android.os.Parcel;
import android.os.Parcelable;

import android.util.Log;


public class ISOVideoInfor implements Parcelable
{
	// The Video Code Type
	public static final int VIDEO_Code_MPEG1 = 0x01;
	public static final int VIDEO_Code_MPEG2 = 0x02;
	public static final int VIDEO_Code_AVC = 0xea;
	public static final int VIDEO_Code_VC1 = 0x1b;
	public static final int VIDEO_Code_MVC = 0x20;

	// The Video Format
	public static final int VIDEO_Format_480i = 1;
	public static final int VIDEO_Format_576i = 2;
	public static final int VIDEO_Format_480p = 3;
	public static final int VIDEO_Format_1080i = 4;
	public static final int VIDEO_Format_720p = 5;
	public static final int VIDEO_Format_1080p = 6;
	public static final int VIDEO_Format_576p = 7;

	// Frame Rate
	public static final int VIDEO_FrameRate_23 = 1;   // 24000/1001 FPS
	public static final int VIDEO_FrameRate_24 = 2;   // 24 FPS
	public static final int VIDEO_FrameRate_25 = 3;   // 25 FPS
	public static final int VIDEO_FrameRate_30 = 4;   // 30000/1001 FPS
	public static final int VIDEO_FrameRate_50 = 6;   // 50 FPS
	public static final int VIDEO_FrameRate_60 = 7;   // 60000/1001 FPS

	public int mCodeType = -1;
	public int mFormat = -1;
	public int mFrameRate = -1;

	private ISOVideoInfor(Parcel in)	
	{		
		mCodeType = in.readInt();		
		mFormat = in.readInt();		
		mFrameRate = in.readInt();
	}

	public static final Parcelable.Creator<ISOVideoInfor> CREATOR =            
		new Parcelable.Creator<ISOVideoInfor>()     	
	{       	
		public ISOVideoInfor createFromParcel(Parcel in)         	
		{            	
			return new ISOVideoInfor(in);        	
		}  
		
		public ISOVideoInfor[] newArray(int size) 		
		{            	
			return new ISOVideoInfor[size];        	
		}    	
	};		

	public void writeToParcel(Parcel out, int flags) 	
	{        		
		out.writeInt(mCodeType);		
		out.writeInt(mFormat);		
		out.writeInt(mFrameRate);   	
	}	  

	public int describeContents() 	
	{        	
		return 0;    	
	}

	public void tostring()
	{
		String code = null;
		String format = null;
		String frameRate = null;
		switch(mCodeType)
		{
			case VIDEO_Code_MPEG1:
				code = "MPEG1";
				break;
			case VIDEO_Code_MPEG2:
				code = "MPEG2";
				break;
			case VIDEO_Code_AVC:
				code = "AVC";
				break;
			case VIDEO_Code_VC1:
				code = "VC1";
				break;
			case VIDEO_Code_MVC:
				code = "MVC";
				break;
			default:
				code = "Error Code";
		}

		switch(mFormat)
		{
			case VIDEO_Format_480i:
				format = "480i";
				break;
			case VIDEO_Format_576i:
				format = "576i";
				break;
			case VIDEO_Format_480p:
				format = "480p";
				break;
			case VIDEO_Format_1080i:
				format = "1080i";
				break;
			case VIDEO_Format_720p:
				format = "720p";
				break;
			case VIDEO_Format_1080p:
				format = "1080p";
				break;
			case VIDEO_Format_576p:
				format = "576p";
				break;
			default:
				code = "Error Format";
				break;
		}

		switch(mFrameRate)
		{
			case VIDEO_FrameRate_23:
				frameRate = "24000/1001 FPS";
				break;
			case VIDEO_FrameRate_24:
				frameRate = "24 FPS";
				break;
			case VIDEO_FrameRate_25:
				frameRate = "25 FPS";
				break;
			case VIDEO_FrameRate_30:
				frameRate = "30000/1001 FPS";
				break;
			case VIDEO_FrameRate_50:
				frameRate = "50 FPS";
				break;
			case VIDEO_FrameRate_60:
				frameRate = "60000/1001 FPS";
				break;
			default:
				frameRate = "Error FrameRate";
				break;
		}

		Log.d("ISO Video","Code Type = "+code+"("+mCodeType+")");
		Log.d("ISO Video","Format = "+format+"("+mFormat+")");
		Log.d("ISO Video","Frame Rate = "+frameRate+"("+mFrameRate+")");
	}
}

