package net.cardroid.can.analysis;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Map.Entry;

import junit.framework.TestCase;

import android.test.MoreAsserts;

import com.google.common.collect.Lists;

import net.cardroid.analysis.CanLog;
import net.cardroid.analysis.CanLog.Item;
import net.cardroid.can.Can232Adapter;
import net.cardroid.can.CanMessage;
import net.cardroid.can.CanMessageParser;
import net.cardroid.util.PropertyUtil;

public class CanLogTest extends TestCase {
	private List<CanMessage> mMessages;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mMessages = genMessages();
	}

	// generating > 10 messages. I had a bug when it would not load/store over 10 messages
	private List<CanMessage> genMessages() throws ParseException {
		ArrayList<CanMessage> messages = Lists.newArrayList(
				CanMessageParser.parseMesage("t10224455", 1),
				CanMessageParser.parseMesage("t1043224455", 2));
		
		for (int i = 0; i < 10; i++) {
			messages.add(CanMessageParser.parseMesage("t1022445", i));			
		}
		return messages;
	}

	public void testSaveLoad () throws IOException {
		CanLog canLog = new CanLog();
		for (CanMessage message : mMessages) {
			canLog.add(message);
		}
		Properties properties = new Properties();
		canLog.save(properties, "prefix");
		// 2 properties per CanMessage: command and timestamp
		assertEquals(mMessages.size() * 2, properties.size());
		
		CanLog loadedLog = new CanLog();
		loadedLog.load(properties, "prefix");
		
		assertEquals(canLog.getItems().size(), loadedLog.getItems().size());
		MoreAsserts.assertContentsInOrder(loadedLog.getItems(), canLog.getItems().toArray());
	}	
}
