package net.cardroid.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import com.google.common.collect.Lists;

import net.cardroid.analysis.CommandsAndPatterns;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class PropertyUtil {

	public static Properties readPropertiesFile(Context context, String fileName) {
		Properties properties = new Properties();
		InputStream is = null;
		try {			
			is = new FileInputStream(new File(Environment.getExternalStorageDirectory(), fileName));
			properties.load(is);
			is.close();
		} catch (IOException e) {
			Log.e(CommandsAndPatterns.TAG, "Failed to load file " + fileName, e);
			if (is != null) {
				try {
					is.close();
				} catch (IOException e2) {
					Log.e(CommandsAndPatterns.TAG, "Failed to close file " + fileName, e);					
				}
			}
		}
		return properties;
	}

	public static void writePropertiesFile(Context context, Properties properties, String fileName) {
		OutputStream os = null;
		try {			
			os = new FileOutputStream(new File(Environment.getExternalStorageDirectory(), fileName));
			properties.save(os, "CAN bus patterns for candroid application");
			os.close();
		} catch (IOException e) {
			Log.e(CommandsAndPatterns.TAG, "Failed to save file " + fileName, e);
			if (os != null) {
				try {
					os.close();
				} catch (IOException e2) {
					Log.e(CommandsAndPatterns.TAG, "Failed to close file " + fileName, e);					
				}
			}
		}
	}

	public static ArrayList<Map.Entry<String, String>> sortProperties(
			Properties properties) {
		ArrayList<Map.Entry<String, String>> sortedItems 
			= Lists.newArrayList(((Map<String, String>)(Map)properties).entrySet());
		Collections.sort(sortedItems, new Comparator<Entry<String, String>>() {
			@Override public int compare(Entry<String, String> e1, Entry<String, String> e2) {
				return e1.getKey().compareTo(e2.getKey());
			}
		});
		return sortedItems;
	}

}
