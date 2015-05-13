package android.media.iso;


/*
* this file is defined by hh@rock-chips.com
* Define the some ISO Audio inforamtions  
*
*/
import android.os.Parcel;
import android.os.Parcelable;
import java.lang.String;

import android.util.Log;


public class ISOAudioInfor implements Parcelable
{
	// The Audio Code Type
	public static final int Audio_Code_MPEG1 = 0x03;
	public static final int Audio_Code_MPEG2 = 0x04;
	public static final int Audio_Code_LPCM = 0x80;
	public static final int Audio_Code_AC3 = 0x81;
	public static final int Audio_Code_DTS = 0x82;
	public static final int Audio_Code_AC3_TRUE_HD = 0x83;
	public static final int Audio_Code_AC3_PLUS = 0x84;
	public static final int Audio_Code_DTS_HD = 0x85;
	public static final int Audio_Code_DTS_HD_MASTER = 0x86;
	public static final int Audio_Code_AC3_PLUS_SECONDARY = 0xa1;
	public static final int Audio_Code_DTS_HD_SECONDARY = 0xa2;

	// The Audio Sample Freq
	public static final int Audio_Sample_48k = 0x01;
	public static final int Audio_Sample_96k = 0x04;
	public static final int Audio_Sample_192k = 0x05;
	public static final int Audio_Sample_48_192k = 0x0C;
	public static final int Audio_Sample_48_96k = 0x0E;

	// The Audio Channel
	public static final int Audio_Channel_Mono = 0x01;
	public static final int Audio_Channel_Stereo = 0x03;
	public static final int Audio_Channel_MultiChannel = 0x06;
	public static final int Audio_Channel_Stereo_Plus_MultiChannel = 0x0C;

	// 
	public int mIndex = -1;
	public int mCodeType  = -1;
	public int mSample = -1;
	public int mChannel = -1;
	public int mPid = -1;
	public String mLang = null;
	
	private ISOAudioInfor(Parcel in)	
	{
		mIndex = in.readInt();
		mCodeType = in.readInt();
		mSample = in.readInt();
		mChannel = in.readInt();
		mPid = in.readInt();
		mLang = in.readString();
	}

	public static final Parcelable.Creator<ISOAudioInfor> CREATOR =            
		new Parcelable.Creator<ISOAudioInfor>()     	
	{
		public ISOAudioInfor createFromParcel(Parcel in)         	
		{            	
			return new ISOAudioInfor(in);        	
		}  
		
		public ISOAudioInfor[] newArray(int size) 		
		{            	
			return new ISOAudioInfor[size];        	
		}    	
	};		

	public void writeToParcel(Parcel out, int flags) 	
	{
		out.writeInt(mIndex);
		out.writeInt(mCodeType);		
		out.writeInt(mSample);
		out.writeInt(mChannel);
		out.writeInt(mPid);
		out.writeString(mLang);
	}	  

	public int describeContents() 	
	{        	
		return 0;    	
	}

	public void tostring()
	{
		Log.d("Iso Audio","Index = "+mIndex);
		Log.d("Iso Audio","Code Type = "+mCodeType);
		Log.d("Iso Audio","Sample = "+mSample);
		Log.d("Iso Audio","Channel = "+mChannel);
		Log.d("Iso Audio","Pid = "+mPid);
		Log.d("Iso Audio","Lang = "+mLang);
	}
}

