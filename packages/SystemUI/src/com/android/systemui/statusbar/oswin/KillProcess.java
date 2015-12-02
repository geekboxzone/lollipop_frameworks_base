package com.android.systemui.statusbar.oswin;

import java.io.IOException;
import java.io.OutputStream;

public class KillProcess {

	private static Process mProcess = null;

	public static void kill(String packageName) {
		if(0 == initmProcess()){
			killmProcess(packageName);
			closeStream();
		}
	}


	private static int initmProcess() {
        try {
	        mProcess = Runtime.getRuntime().exec("su");
	    } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
		return 0;
	}


	private static void killmProcess(String packageName) {
		OutputStream out = mProcess.getOutputStream();
		String cmd = "am force-stop " + packageName + " \n";
		try {
			out.write(cmd.getBytes());
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void closeStream() {
	    try {
		    if(null != mProcess){
			    mProcess.getOutputStream().close();
			    mProcess = null;
			}
		} catch (IOException e) {
	        e.printStackTrace();
		}
	}
}
