package net.cardroid.analysis;

import android.util.Log;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.cardroid.can.CanMessage;
import net.cardroid.can.CanMessageParser;

import java.io.IOException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;


public class CanLog {
	private static final String TAG = "CanLog";

	public interface Item {
		boolean matches(CanMessage message);
		boolean matchesDestination(int destination);
		String toString();
		CanMessage getCanMessage();
		boolean equals(Object o);
		int hashCode();
	}
	
	abstract static class RealItem {
		abstract void load(Properties properties, String prefix) throws ParseException;
		abstract void save(Properties properties, String prefix);

		abstract int getDestination();
		abstract CanMessage getCanMessage();		
		abstract boolean matches(CanMessage message);

		public abstract String toString();
		public abstract boolean equals(Object o);
		public abstract int hashCode();
	}

	private final List<ItemProxy> mItems = Lists.newArrayList();
	
	public CanLog () {		
	}
	
	public CanLog copy() {
		CanLog canLog = new CanLog();
		// items are immutable
		canLog.mItems.addAll(mItems);
		return canLog;
	}

	public Item add(CanMessage message) {
		CanMessageItem item = new CanMessageItem(message);
		ItemProxy proxy = new ItemProxy(item);
		mItems.add(proxy);
		return proxy;
	}
	
	public Item findItem(CanMessage message) {
		for (Item item : mItems) {
			if (item.matches(message)) {
				return item;
			}
		}
		return null;
	}

	public Item findItemByDestination(int destination) {
		for (Item item : mItems) {
			if (item.matchesDestination(destination)) {
				return item;
			}
		}
		return null;
	}

	public void setItemDestinationMatcher(Item item) {
		RealItem realItem = ((ItemProxy)item).getItem();
		((ItemProxy)item).setItem(new CanMessageDestinationItem(realItem));
	}

	public void remove(Item item) {
		mItems.remove(item);
	}
	
	public List<? extends Item> getItems() {
		return ImmutableList.copyOf(mItems);
	}

	public void clear() {
		mItems.clear();
	}
	
	public Iterator<? extends Item> iterator() {
		return mItems.iterator();
	}

	public int size() {
		return mItems.size();
	}

	public void load(Properties properties, String prefix) throws IOException {
		mItems.clear();
						
		for (int i = 0; ; i++) {
			String entryPrefix = prefix + "." + i + ".";
			String entryDestinationPrefix = entryPrefix + "destination";
			String entryCommandPrefix = entryPrefix + "command";
			RealItem item;
			if (properties.containsKey(entryDestinationPrefix)) {
				item = new CanMessageDestinationItem();				
			} else if (properties.containsKey(entryCommandPrefix)) {
				item = new CanMessageItem();
			} else {
				break;
			}
			try {
				item.load(properties, entryPrefix);
				mItems.add(new ItemProxy(item));
			} catch (ParseException e) {
				Log.e(TAG, "Failed to parse " + entryPrefix, e);
			}
		}
	}

	public void save(Properties properties, String prefix) throws IOException {
		int i = 0;
		for (ItemProxy proxy : mItems) {
			RealItem item = proxy.getItem();
			item.save(properties, prefix + "." + i + ".");
			i++;
		}
	}

	private static class ItemProxy implements Item {
		private RealItem mmItem;
		
		public ItemProxy(RealItem item) {
			mmItem = item;
		}

		@Override public String toString() {
			return mmItem.toString();
		}

		@Override public boolean matches(CanMessage message) {
			return mmItem.matches(message);
		}

		@Override public boolean matchesDestination(int destination) {
			return mmItem.getDestination() == destination;
		}
		
		@Override public CanMessage getCanMessage() {
			return mmItem.getCanMessage();
		}

		@Override public boolean equals(Object o) {
			if (!(o instanceof ItemProxy)) return false;
			ItemProxy item = ((ItemProxy)o);
			return item.mmItem.equals(mmItem);
		}

		@Override
		public int hashCode() {
			return mmItem.hashCode();
		}

		private RealItem getItem() {
			return mmItem;
		}

		private void setItem(RealItem item) {
			mmItem = item;
		}
	}

	private static class CanMessageItem extends RealItem {
		private CanMessage mmMessage;
		
		CanMessageItem() {} // for persistence

		public CanMessageItem(CanMessage message) {
			mmMessage = message;
		}

		@Override public String toString() {
			return mmMessage.toString();
		}

		@Override public boolean matches(CanMessage message) {
			return message.sameMessage(mmMessage);
		}

		@Override public int getDestination() {
			return mmMessage.getDestination();
		}
		
		@Override public CanMessage getCanMessage() {
			return mmMessage;
		}		

		@Override void load(Properties properties, String prefix) throws ParseException {
			long timestamp = Long.parseLong((String)properties.get(prefix + "timestamp"));
			mmMessage = CanMessageParser.parseMesage((String)properties.get(prefix + "command"), timestamp);
		}

		@Override public void save(Properties properties, String prefix) {
			properties.put(prefix + "command", mmMessage.toString());
			properties.put(prefix + "timestamp", Long.toString(mmMessage.getTimestampMillis()));
		}

		@Override public boolean equals(Object o) {
			if (!(o instanceof CanMessageItem)) return false;
			CanMessageItem item = ((CanMessageItem)o);
			return item.mmMessage.sameMessage(mmMessage);
		}

		@Override
		public int hashCode() {
			return mmMessage.hashCode();
		}
	}

	private static class CanMessageDestinationItem extends RealItem {
		private int mmDestination;
		private CanMessage mmOriginalMessage;

		CanMessageDestinationItem() {} // for persistence
		
		public CanMessageDestinationItem(RealItem item) {
			mmOriginalMessage = item.getCanMessage();
			mmDestination = item.getCanMessage().getDestination();
		}

		@Override public String toString() {
			return Integer.toHexString(mmDestination).toUpperCase();
		}

		@Override public boolean matches(CanMessage message) {
			return message.getDestination() == mmDestination;
		}

		@Override public int getDestination() {
			return mmDestination;
		}

		@Override public CanMessage getCanMessage() {
			return mmOriginalMessage;
		}		

		@Override void load(Properties properties, String prefix) {
			mmDestination = Integer.parseInt((String) properties.get(prefix + "destination"), 16);
            try {
                mmOriginalMessage = CanMessageParser.parseMesage((String) properties.get(prefix + "original"), -1);
            } catch (ParseException e) {
                Log.e(TAG, e.toString(), e);
            }
        }

		@Override void save(Properties properties, String prefix) {
			properties.put(prefix + "destination", mmDestination);
		}

		@Override public boolean equals(Object o) {
			if (!(o instanceof CanMessageDestinationItem)) return false;
			CanMessageDestinationItem item = ((CanMessageDestinationItem)o);
			return item.mmDestination == mmDestination;
		}

		@Override
		public int hashCode() {
			return mmDestination;
		}
	}
}
