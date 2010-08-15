package net.cardroid;

import android.util.Log;
import android.widget.TextView;

public class Logger {
	private static TextView log;
	
	private final String tag;
	
	private Logger(String tag) {
		this.tag = tag;
	}
	
	public void i(String text) {
		log.append(text + "\n");
		Log.i(tag, text);
	}

	public void e(String text) {
		log.append(text + "\n");
		Log.e(tag, text);
	}
	
	public static Logger getInstance(String tag) {
		return new Logger(tag);
	}
	
	public static void setTextView(TextView logView) {
		log = logView;
	}
}
