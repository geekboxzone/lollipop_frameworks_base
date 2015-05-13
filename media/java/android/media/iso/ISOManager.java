package android.media.iso;

import android.media.MediaPlayer;
import android.os.Parcel;
import android.os.Parcelable;
import java.io.File;

import android.net.Uri;
//import andorid.media.iso.ISOVideoInfor;
//import andorid.media.iso.ISOAudioInfor;
//import andorid.media.iso.ISOSubtitleInfor;

import android.os.Parcel;
import android.os.Parcelable;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.MediaStore;
import android.database.Cursor;


import android.util.Log;

public class ISOManager
{
	private final static String TAG = "ISOManager";
	private MediaPlayer mMediaPlayer = null;


	private static final int OPERATE_BASE = 7000;

	public static final int GET_VIDEO_INFOR = OPERATE_BASE;
	public static final int GET_AUDIO_INFOR = OPERATE_BASE+1;
	public static final int GET_SUBTITLE_INFOR = OPERATE_BASE+2;
	public static final int GET_AUDIO_TRACK = OPERATE_BASE+3;
	public static final int GET_SUBTITLE_TRACK = OPERATE_BASE+4;
	public static final int SET_AUDIO_TRACK = OPERATE_BASE+5;
	public static final int SET_SUBTITLE_TRACK = OPERATE_BASE+6;
	public static final int SET_IG_VISIBLE = OPERATE_BASE+7;
	public static final int GET_IG_VISIBLE = OPERATE_BASE+8;
	public static final int SET_SUBTITLE_VISIBLE = OPERATE_BASE+9;
	public static final int GET_SUBITTLE_VISIBLE = OPERATE_BASE+10;
	public static final int SET_IG_OPERATION = OPERATE_BASE+11;
	public static final int GET_CHAPTER = OPERATE_BASE+12;
	public static final int PLAY_CHAPTER = OPERATE_BASE+13;
	public static final int GET_CURRENT_CHAPTER = OPERATE_BASE+14;
	public static final int GET_NUMBER_OF_ANGLE = OPERATE_BASE+15;
	public static final int GET_CURRENT_ANGLE = OPERATE_BASE+16;
	public static final int PLAY_ANGLE = OPERATE_BASE +17;
	public static final int GET_NUMBER_OF_TITLE = OPERATE_BASE+18;
	public static final int GET_CURRENT_TITLE = OPERATE_BASE+19;
	public static final int PLAY_TITLE = OPERATE_BASE+20;
	public static final int PLAY_TOP_TITLE = OPERATE_BASE+21;
	public static final int SKIP_CURRENT_CONTEXT = OPERATE_BASE+22;
	public static final int PLAY_SEEK_TIME_PLAY = OPERATE_BASE+23;
	public static final int PLAY_QUERY_NAVIGATION_MENU = OPERATE_BASE+24;
	public static final int SURFACE_SHOW = 1;
	public static final int SURFACE_HIDE = 0;
	public static final int BUTTON_MOVE_UP = 0;  
	public static final int BUTTON_MOVE_DOWN = 1;
	public static final int BUTTON_MOVE_LEFT = 2;
	public static final int BUTTON_MOVE_RIGHT = 3;
	public static final int BUTTON_ACTIVATE = 4;
	public static final int OPERATION_SUCCESS = 0;
	public static final int OPERATION_FAIL = 1;
	public static final int OPERATION_FAIL_OPEATION_DISABLE = 2;
	public static final int OPERATION_FAIL_PARAMETER_ERROR = 3;
	public static final int OPERATION_FAIL_BDJ = 3;
  
	private static final int OPERATION_BASE = 8000;
	public static final int MEDIA_ISO_MOUNT_START  = OPERATION_BASE;
	public static final int MEDIA_ISO_MOUNT_END = OPERATION_BASE+1;
	public static final int MEDIA_ISO_UNMOUNT_START = OPERATION_BASE+2;
	public static final int MEDIA_ISO_UNMOUNT_END = OPERATION_BASE+3;
	public static final int MEDIA_ISO_MOUNT_FAIL = OPERATION_BASE+4;
	public static final int MEDIA_ISO_UNSUPPORT_FORMAT = OPERATION_BASE+5;
	public static final int MEDIA_ISO_PLAY_START = OPERATION_BASE+6;
	public static final int MEDIA_PLAY_NEXT  = OPERATION_BASE+7;
	
	public ISOManager(MediaPlayer player)
	{
		mMediaPlayer = player;
	}

	public static boolean existInBackUp(String path,String name,boolean isDirectory)
	{
		if((path == null) || (name == null))
			return false;
		File backup = new File(path);
		if(!backup.exists() || !backup.isDirectory())
		{
			return false;
		}
		File file = new File(path+File.separator+name);
		if(file.exists())
		{
			if(isDirectory)
			{
				if(file.isDirectory())
				{
					return true;
				}
				else
				{
					return false;
				}
			}
			else
			{
				if(file.isFile())
				{
					return true;
				}
				else
				{
					return false;
				}
			}
		}
		return false;
	}

	public static String isBDDirectory(Context context, Uri uri)
	{
		String path = null;
		String mine = null;
		ContentResolver resolver = context.getContentResolver();
		if(resolver != null)
		{
			mine = resolver.getType(uri);
			Log.d(TAG,"mine = "+mine);
		}
		if((mine != null) && (mine.equals("video/iso")))
		{
			String pathString = uri.toString();
			if(pathString != null && pathString.startsWith("file://"))
			{
				String prefix = new String("file://");
				if(pathString.startsWith(prefix))
				{
					path = pathString.substring(prefix.length());
				}
			}
			else
			{
				try
				{
					String[] mCols = new String[] 
					{
						MediaStore.Video.Media.DISPLAY_NAME,
						MediaStore.Video.Media.DURATION,
						MediaStore.Video.Media.MIME_TYPE,
						MediaStore.Video.Media.SIZE,
						MediaStore.Video.Media._ID,
						MediaStore.Video.Media.DATA,
						MediaStore.Video.Media.BOOKMARK
					};
					
					Cursor cursor = resolver.query(uri, mCols, null, null, null);
					if(cursor != null && cursor.getCount() >= 0)
					{
						cursor.moveToFirst();
						path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
						cursor.close();
					}
				}
				finally 
				{

				}
			}
		}
             
		return path;
	}
	
	public static boolean isBDDirectory(String pathStr)
	{
		if(pathStr == null)
		{
			return false;
		}

		Uri uri = Uri.parse(pathStr);
		String path = pathStr;
		String scheme = uri.getScheme();
		if(scheme == null || scheme.equals("file"))
		{
			String temp = uri.toString();
			String prefix = new String("file://");
			if(temp != null && temp.startsWith(prefix))
			{
				path = temp.substring(prefix.length());
			}
		}


		Log.d(TAG,"isBDDirectory(), pathStr = "+pathStr);
 		Log.d(TAG,"isBDDirectory(), path = "+path);
		
		File file = new File(path);
		if(file.isDirectory())
		{
			String bdmvName = path+File.separator+"BDMV";
			String backup = bdmvName+File.separator+"BACKUP";
			File bdmv = new File(bdmvName);
			if(bdmv.exists() && bdmv.isDirectory())
			{
				String stream = bdmvName+File.separator+"STREAM";
				File streamFile = new File(stream);
				if(!streamFile.exists() && !existInBackUp(backup,"STREAM",true))
				{
					return false;
				}
				String playlist = bdmvName+File.separator+"PLAYLIST";
				File playlistFile = new File(playlist);
				if(!playlistFile.exists() && !existInBackUp(backup,"PLAYLIST",true))
				{
					return false;
				}
				String clip = bdmvName+File.separator+"CLIPINF";
				File clipFile = new File(clip);
				if(!clipFile.exists() && !existInBackUp(backup,"CLIPINF",true))
				{
					return false;
				}
				return true;
 			}
			else
			{
				return false;
			}
		}
		return false;
	}

	public ISOVideoInfor  getVideoInfor()
	{
		if(mMediaPlayer != null)
		{
			Parcel reply = mMediaPlayer.getParcelParameter(GET_VIDEO_INFOR);
			if(reply != null)
			{
				ISOVideoInfor videoInfo[] = reply.createTypedArray(ISOVideoInfor.CREATOR);
				if(videoInfo != null)
				{
					for(int i = 0; i < videoInfo.length; i++)
					{
						videoInfo[i].tostring();
					}
					
					return videoInfo[0];
				}
				
			}
		}
		return null;
	}

	public ISOAudioInfor  getAudioInfor()
	{
		if(mMediaPlayer != null)
		{
			Parcel reply = mMediaPlayer.getParcelParameter(GET_AUDIO_INFOR);
			if(reply != null)
			{
				ISOAudioInfor audioInfo[] = reply.createTypedArray(ISOAudioInfor.CREATOR);
				if((audioInfo != null) && (audioInfo.length > 0))
				{
					for(int i = 0; i < audioInfo.length; i++)
					{
						audioInfo[i].tostring();
					}
					
					return audioInfo[0];
				}
			}
		}
		return null;
	}

	public ISOSubtitleInfor  getSubtitleInfor()
	{
		if(mMediaPlayer != null)
		{
			Parcel reply = mMediaPlayer.getParcelParameter(GET_SUBTITLE_INFOR);
			if(reply != null)
			{
				ISOSubtitleInfor subtitleInfo[] = reply.createTypedArray(ISOSubtitleInfor.CREATOR);;
				if((subtitleInfo != null) && (subtitleInfo.length > 0))
				{
					for(int i = 0; i < subtitleInfo.length; i++)
					{
						subtitleInfo[i].tostring();
					}
					
					return subtitleInfo[0];
				}
			}
		}
		return null;
	}

	public ISOAudioInfor[]  getAudioTrack()
	{
		if(mMediaPlayer != null)
		{
			Parcel reply = mMediaPlayer.getParcelParameter(GET_AUDIO_TRACK);
			if(reply != null)
			{
				ISOAudioInfor trackInfo[] = reply.createTypedArray(ISOAudioInfor.CREATOR);
				if((trackInfo != null) && (trackInfo.length > 0))
				{
					Log.d(TAG,"Audio Track Count = "+trackInfo.length);
					for(int i = 0; i < trackInfo.length; i++)
					{
						trackInfo[i].tostring();
					}
				}
				return trackInfo;
			}
		}
		return null;
	}

	public ISOSubtitleInfor[]  getSubtitleTrack()
	{
		if(mMediaPlayer != null)
		{
            Parcel reply = mMediaPlayer.getParcelParameter(GET_SUBTITLE_TRACK);
			if(reply != null)
			{
				ISOSubtitleInfor trackInfo[] = reply.createTypedArray(ISOSubtitleInfor.CREATOR);
				if((trackInfo != null) && (trackInfo.length > 0))
				{
					Log.d(TAG,"Subtitle Track Count = "+trackInfo.length);
					for(int i = 0; i < trackInfo.length; i++)
					{
						trackInfo[i].tostring();
					}
				}
				return trackInfo;
			}

		}
		return null;
	}

	public boolean  setAudioTrack(int index)
	{
		if(mMediaPlayer != null)
		{
			return mMediaPlayer.setParameter(SET_AUDIO_TRACK,index);
		}

		return false;
	}

	public boolean  setSubtitleTrack(int index)
	{
		if(mMediaPlayer != null)
		{
			return mMediaPlayer.setParameter(SET_SUBTITLE_TRACK,index);
		}

		return false;
	}

	public boolean setSubtitleSurfaceVisible(int visible)
	{
		if(mMediaPlayer != null)
		{
			return mMediaPlayer.setParameter(SET_SUBTITLE_VISIBLE,visible);
		}

		return false;
	}

	public int getSubtitleSurfaceVisible()
	{
		if(mMediaPlayer != null)
		{
			return mMediaPlayer.getIntParameter(GET_SUBITTLE_VISIBLE);
		}

		return SURFACE_HIDE;
	}

	public boolean setIGSurfaceVisible(int visible)
	{
		if(mMediaPlayer != null)
		{
			return mMediaPlayer.setParameter(SET_IG_VISIBLE,visible);
		}

		return false;
	}

	public int getIGSurfaceVisible()
	{
		if(mMediaPlayer != null)
		{
			return mMediaPlayer.getIntParameter(GET_IG_VISIBLE);
		}

		return SURFACE_HIDE;
	}

	public boolean  moveNavigationButton(int direction)
	{
		if(mMediaPlayer != null)
		{
			return mMediaPlayer.setParameter(SET_IG_OPERATION,direction);
		}

		return false;
	}

	public int getNumberOfChapter()
	{
		if(mMediaPlayer != null)
		{
			return mMediaPlayer.getIntParameter(GET_CHAPTER);
		}
		return 0;
	}

	public boolean playChapter(int chapter)
	{
		if(mMediaPlayer != null)
		{
			return mMediaPlayer.setParameter(PLAY_CHAPTER,chapter);
		}
		
		return false;
	}

	public int getCurrentChapter()
	{
		if(mMediaPlayer != null)
		{
			return mMediaPlayer.getIntParameter(GET_CURRENT_CHAPTER);
		}
		
		return 0;
	}

	public int getNumberOfAngle()
	{
		if(mMediaPlayer != null)
		{
			return mMediaPlayer.getIntParameter(GET_NUMBER_OF_ANGLE);
		}
		
		return 0;
	}

	public int getCurrentAngle()
	{
		if(mMediaPlayer != null)
		{
			return mMediaPlayer.getIntParameter(GET_CURRENT_ANGLE);
		}
		
		return 0;
	}

	public boolean playAngle(int angle)
	{
		if(mMediaPlayer != null)
		{
			return mMediaPlayer.setParameter(PLAY_ANGLE,angle);
		}
		
		return false;
	}

	public int getNumberOfTitle()
	{
		if(mMediaPlayer != null)
		{
			return mMediaPlayer.getIntParameter(GET_NUMBER_OF_TITLE);
		}
		
		return 0;
	}

	public int getCurrentTitle()
	{
		if(mMediaPlayer != null)
		{
			return mMediaPlayer.getIntParameter(GET_CURRENT_TITLE);
		}
		
		return 0;
	}

	public boolean playTitle(int title)
	{
		if(mMediaPlayer != null)
		{
			return mMediaPlayer.setParameter(PLAY_TITLE,title);
		}
		
		return false;
	}

	public boolean playTopTitle()
	{
		if(mMediaPlayer != null)
		{
			return mMediaPlayer.setParameter(PLAY_TOP_TITLE,0);
		}
		
		return false;
	}

	public boolean playNextContent()
	{
		if(mMediaPlayer != null)
		{
			return mMediaPlayer.setParameter(SKIP_CURRENT_CONTEXT,0);
		}

		return false;
	}

	public int queryNavigationMenu()
	{
		if(mMediaPlayer != null)
		{
			return mMediaPlayer.getIntParameter(PLAY_QUERY_NAVIGATION_MENU);
		}

		return 0;
	}
}

