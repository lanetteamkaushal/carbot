package net.cardroid.analysis;

import android.content.Context;
import android.util.Log;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.cardroid.can.Can232Adapter;
import net.cardroid.can.CanMessage;
import net.cardroid.can.CanMessageParser;
import net.cardroid.util.PropertyUtil;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class CommandsAndPatterns {
	public static interface CanCommand {
		public void runCommand(Can232Adapter sender) throws IOException;
		public String toString();
	}

	public static final class CanLogCommand implements CanCommand {
		private final String name;
		private final CanLog mmCanLog;

		private CanLogCommand(String name, CanLog canLog) {
			this.name = name;
			this.mmCanLog = canLog;
		}

		@Override public String toString() {
			return name;
		}

		@Override public void runCommand(Can232Adapter sender) throws IOException {
			CanMessage previousMessage = null;
			
			for (final CanLog.Item item : mmCanLog.getItems()) {
				final CanMessage message = item.getCanMessage();
				if (message == null) continue;
				
				if (previousMessage != null) {
					try {
						Thread.sleep(message.getTimestampMillis() - previousMessage.getTimestampMillis());
					} catch (InterruptedException e) {
						Log.e(TAG, e.toString(), e);
					}
				}
				sender.sendMessageSync(message);
				previousMessage = message;
			}								
		}
		
		public CanLog getCanLog() {
			return mmCanLog;
		}
	}
	
	public static final String TAG = "CommandsAndPatterns";  
	private static final String PATTERN_PREFS_NAME = "PatternPreferences";
	private static final String COMMANDS_PREFS_FILE_NAME = "CommandsPreferences";
	private static final String COMMANDS_PREFS_PLAIN_FILE_NAME = "cardroid.commands.txt";
	private static final String PATTERN_PREFS_PLAIN_FILE_NAME = "cardroid.patterns.txt";

	private final Map<Pattern, String> mMessagePatternToName = Maps.newHashMap();
	private final Map<String, Pattern> mMessageNameToPattern = Maps.newHashMap();
	private final List<CanLogCommand> mCanCommands = Lists.newArrayList();
	
	public Map.Entry<Pattern, String> findPattern(String message) {
		for (Map.Entry<Pattern, String> pattern : mMessagePatternToName.entrySet()) {
			Matcher matcher = pattern.getKey().matcher(message);
			if (matcher.matches()) {
				return pattern;
			}
		}

		return null;
	}	

	public List<CanCommand> getCommandsSortedByName() {
		List<CanCommand> allCommands = Lists.newArrayList();
		
		for (Map.Entry<Pattern, String> pattern : mMessagePatternToName.entrySet()) {
			allCommands.add(createMessagePatternCommand(pattern));
		}
		allCommands.addAll(mCanCommands);
		
		Collections.sort(allCommands, new Comparator<CanCommand>() {
			@Override public int compare(CanCommand command1,
					CanCommand command2) {
				return command1.toString().compareTo(command2.toString());
			}
		});
		
		return allCommands;
	}
	
	public static CanCommand createMessagePatternCommand(final Map.Entry<Pattern, String> entry) {
		return new CanCommand() {
			@Override public String toString() {
				return entry.getValue();
			}

			@Override public void runCommand(Can232Adapter sender) throws IOException {
				try {
					sender.sendMessageSync(CanMessageParser.parseMesage(entry.getKey().toString(), 0));
				} catch (ParseException e) {
					throw new IOException("Failed to parse can message " + entry.getKey().toString().getBytes());
				}								
			}
		};
	}

	public static CanLogCommand createCanLogCommand(final String name, final CanLog canLog) {
		return new CanLogCommand(name, canLog);
	}
	
	public void removePatternByName(String value) {
		Pattern pattern = mMessageNameToPattern.get(value);
		mMessageNameToPattern.remove(value);
		mMessagePatternToName.remove(pattern);
	}
	
	public void addCommand(CanLogCommand command) {
		mCanCommands.add(command);
	}

	public void removeCommandByName(String name) {
		for (Iterator<CanLogCommand> i = mCanCommands.iterator(); i.hasNext();) {
			CanLogCommand command = i.next();
			
			if (command.toString().equals(name)) {
				i.remove();
			}
		}
	}

	public void addPattern(String newName, Pattern newPattern) {
		removePatternByName(newName);
		String oldName = mMessagePatternToName.get(newPattern);
		if (oldName != null) {
			removePatternByName(oldName);
		}

		mMessageNameToPattern.put(newName, newPattern);
		mMessagePatternToName.put(newPattern, newName);		
	}
	
	// Load and save methods	
	public void loadSettings(Context context) {
		loadPatternSettingsPlainText(context);
		loadCommandSettingsPlainText(context);
	}

	public void save(Context context) {
		savePatternSettingsPlainText(context);
		saveCommandSettingsPlainText(context);
	}		

	private void loadPatternSettingsPlainText(Context context) {
		Properties properties = PropertyUtil.readPropertiesFile(context, PATTERN_PREFS_PLAIN_FILE_NAME);
		
		mMessageNameToPattern.clear();
		for (Map.Entry property : properties.entrySet()) {
			try {
				addPattern((String)property.getKey(), Pattern.compile((String)property.getValue()));
			} catch (PatternSyntaxException e) {
				Log.e(TAG, "Failed to parse pattern " + property.getKey() + " for property " + property.getValue());
			}
		}
	}

	private void savePatternSettingsPlainText(Context context) {
		Properties properties = new Properties();

		for (Map.Entry<Pattern, String> entry : mMessagePatternToName.entrySet()) {
			properties.setProperty(entry.getValue(), entry.getKey().toString());
		}

		PropertyUtil.writePropertiesFile(context, properties, PATTERN_PREFS_PLAIN_FILE_NAME);
	}

	private void loadCommandSettingsPlainText(Context context) {
		try {
			mCanCommands.clear();
			
			Properties properties = PropertyUtil.readPropertiesFile(context, COMMANDS_PREFS_PLAIN_FILE_NAME);

			ArrayList<Map.Entry<String, String>> sortedItems = PropertyUtil.sortProperties(properties);
			
			String prevName = null;
			
			for (Entry<String, String> entry: sortedItems) {
				String name = entry.getKey().substring(0, entry.getKey().indexOf('.'));
				if (name.equals(prevName)) continue;
				
				CanLog canLog = new CanLog();
				canLog.load(properties, name);
				mCanCommands.add(new CanLogCommand(name, canLog));
				
				prevName = name;
			}
		} catch (Exception e) {
			Log.e(TAG, "Failed to load command settings ", e);
		}
	}

	private void saveCommandSettingsPlainText(Context context) {
		try {
			Properties properties = new Properties();

			for (CanLogCommand command : mCanCommands) {
				command.getCanLog().save(properties, command.toString());
			}

			PropertyUtil.writePropertiesFile(context, properties, COMMANDS_PREFS_PLAIN_FILE_NAME);
		} catch (IOException e) {
			Log.e(TAG, "Failed to save command settings ", e);
		}
	}
}
