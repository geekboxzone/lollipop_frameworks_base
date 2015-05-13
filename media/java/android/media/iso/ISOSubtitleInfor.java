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

public class ISOSubtitleInfor  implements Parcelable
{
	// The Sutitle Code Type
	public static final int Sutitle_Code_PG = 0x90;    // PG
	public static final int Sutitle_Code_TEXT = 0x92;  // Text

	// The Text Subtitle Character Code
	public static final int Sutitle_Character_UTF8 = 0x01;
	public static final int Sutitle_Character_UTF16BE = 0x02;
	public static final int Sutitle_Character_Shirf_JIS = 0x03;
	public static final int Sutitle_Character_KSC = 0x04;
	public static final int Sutitle_Character_GB18030= 0x05;
	public static final int Sutitle_Character_GB2312 = 0x06;
	public static final int Sutitle_Character_BIG5 = 0x07;

	// 
	public int mIndex = -1;
	public int mType  = -1;
	// this field is valid only when mType == Sutitle_Code_TEXT
	public int mCharacter = -1;
	public int mPid = -1;
	public String mLang = null;
	
	private ISOSubtitleInfor(Parcel in)	
	{
		mIndex = in.readInt();
		mType = in.readInt();		
		mCharacter = in.readInt();
		mPid = in.readInt();
		mLang = in.readString();
	}

	public static final Parcelable.Creator<ISOSubtitleInfor> CREATOR =            
		new Parcelable.Creator<ISOSubtitleInfor>()     	
	{       	
		public ISOSubtitleInfor createFromParcel(Parcel in)         	
		{            	
			return new ISOSubtitleInfor(in);        	
		}  
		
		public ISOSubtitleInfor[] newArray(int size) 		
		{            	
			return new ISOSubtitleInfor[size];        	
		}    	
	};		

	public void writeToParcel(Parcel out, int flags) 	
	{
		out.writeInt(mIndex);
		out.writeInt(mType);		
		out.writeInt(mCharacter);
		out.writeInt(mPid);	
		out.writeString(mLang);	
	}	  

	public int describeContents() 	
	{        	
		return 0;    	
	}

	public void tostring()
	{
		Log.d("Iso Subtitle","mIndex = "+mIndex);
		Log.d("Iso Subtitle","Type = "+mType);
		Log.d("Iso Subtitle","mCharacter = "+mCharacter);
		Log.d("Iso Subtitle","mPid = "+mPid);
		Log.d("Iso Subtitle","mLang = "+mLang);
	}
}

