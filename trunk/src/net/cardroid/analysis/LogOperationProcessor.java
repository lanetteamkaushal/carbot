package net.cardroid.analysis;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import net.cardroid.can.CanMessage;

import java.util.Iterator;
import java.util.Set;

public class LogOperationProcessor {
	public static final int OPERATION_ADD = 0;
	public static final int OPERATION_SUBTRACT_MESSAGE = 1;
	public static final int OPERATION_SUBTRACT_ID = 2;
	public static final int OPERATION_INTERSECT_MESSAGE = 3;
	public static final int OPERATION_INTERSECT_ID = 4;
	public static final int OPERATION_FOLLOW_TRIGGER = 5;
	
	private static interface Operation {
		public void start();
		public void stop();
		public boolean applyMessage(CanMessage message);
	}

	private final Operation [] mOperations = new Operation [] { new NewLog(), new SubtractMessages(), new SubtractIds(),
			new IntersectMessages(), new IntersectIds(), new FollowTrigger(), new ValueCompare(-1), new ValueCompare(1)};
	
	private CanLog mCanLog;	
	private Operation mOperation;

	public LogOperationProcessor() {
	}
	
	public void startOperation(int operation, CanLog log) {
		Preconditions.checkArgument(operation >= 0 && operation < mOperations.length, "Unexpected operation " + operation);
		mCanLog = log;
		mOperation = mOperations[operation];
		mOperation.start();
	}
	
	public void stopOperation() {
		mOperation.stop();
		mOperation = null;
	}
	
	public boolean applyMessage(CanMessage message) {
		return mOperation.applyMessage(message);
	}	
	
	private class NewLog implements Operation { 
		@Override public boolean applyMessage(CanMessage message) {
			mCanLog.add(message);
			return true;
		}

		@Override public void start() {
			mCanLog.clear();
		}

		@Override public void stop() {
		}		
	}

	private class SubtractIds implements Operation {
		private Set<Integer> mmSubstractIds;
		
		@Override public boolean applyMessage(CanMessage message) {
            if (mCanLog.findItem(message) != null) {
                mmSubstractIds.add(message.getDestination());
            }
			return false;
		}

		@Override public void start() {
			mmSubstractIds = Sets.newHashSet();
		}

		@Override public void stop() {
			for (Iterator<? extends CanLog.Item> i = mCanLog.iterator(); i.hasNext();) {
				CanLog.Item item = i.next();
				
				for (Integer id : mmSubstractIds) {
					if (item.matchesDestination(id)) {
						i.remove();
						break;
					}
				}
			}
		}		
	}

	private class SubtractMessages implements Operation { 
		private Set<CanMessage> mmSubstractMessages;

		@Override public boolean applyMessage(CanMessage message) {
            if (mCanLog.findItem(message) != null) {
                mmSubstractMessages.add(message);
            }
			return false;
		}

		@Override public void start() {
			mmSubstractMessages = Sets.newHashSet();
		}

		@Override public void stop() {
			for (Iterator<? extends CanLog.Item> i = mCanLog.iterator(); i.hasNext();) {
				CanLog.Item item = i.next(); 
				for (CanMessage message: mmSubstractMessages) {
					if (item.matches(message)) {
						i.remove();
						break;
					}
				}
			}
		}		
	}

	private class IntersectIds implements Operation { 
		private Set<CanMessage> mmAppliedMessages;

		@Override public boolean applyMessage(CanMessage message) {
            if (mCanLog.findItemByDestination(message.getDestination()) != null) {
                mmAppliedMessages.add(message);
                return true;
            }
            else {
                return false;
            }
		}

		@Override public void start() {
			mmAppliedMessages = Sets.newHashSet();
		}

		@Override public void stop() {
			for (Iterator<? extends CanLog.Item> i = mCanLog.iterator(); i.hasNext();) {
				CanLog.Item item = i.next();
				
				boolean matches = false;				
				boolean matchesDestination = false;				
				for (CanMessage message: mmAppliedMessages) {
					if (item.matches(message)) {
						matches = true;
						break;
					}
					if (item.matchesDestination(message.getDestination())) {
						matchesDestination = true;
					}
				}
				
				if (!matches && !matchesDestination) {
					i.remove();
				} else if (matchesDestination) {
					mCanLog.setItemDestinationMatcher(item);
				}
			}
		}		
	}

	private class IntersectMessages implements Operation { 
		private Set<CanMessage> mmAppliedMessages;

		@Override public boolean applyMessage(CanMessage message) {
            if (mCanLog.findItem(message) != null) {
                mmAppliedMessages.add(message);
                return true;
            }
            else {
                return false;
            }
		}

		@Override public void start() {
			mmAppliedMessages = Sets.newHashSet();
		}

		@Override public void stop() {
			for (Iterator<? extends CanLog.Item> i = mCanLog.iterator(); i.hasNext();) {
				CanLog.Item item = i.next(); 
				
				boolean matches = false;
				for (CanMessage message: mmAppliedMessages) {
					if (item.matches(message)) {
						matches = true;
						break;
					}
				}
				
				if (!matches) {
					i.remove();
				}
			}
		}		
	}

    /* start logging after first item in the current log was triggered */
	private class FollowTrigger implements Operation { 
		private CanLog.Item mmTrigger;
		private boolean mmTriggerApplied;

		@Override public boolean applyMessage(CanMessage message) {
			if (mmTrigger == null) return true;
			
			mmTriggerApplied = mmTriggerApplied || mmTrigger.matches(message);  
			if (mmTriggerApplied) {
				mCanLog.add(message);
			}
			return mmTriggerApplied;
		}

		@Override public void start() {
			if (mCanLog.size() == 0) {
				mmTrigger = null;
			} else {
				mmTrigger = mCanLog.getItems().get(0);
			}
			mCanLog.clear();
		}

		@Override public void stop() {
		}		
	}

    private class ValueCompare implements Operation {
        private final int mmComparisonResult;
        private Set<CanMessage> mmAppliedMessages;

        private ValueCompare(int mComparisonResult) {
            mmComparisonResult = mComparisonResult;
        }

        @Override public boolean applyMessage(CanMessage message) {
            CanLog.Item item = mCanLog.findItemByDestination(message.getDestination());
            if (item != null
                && item.getCanMessage().getDataBigInteger().compareTo(message.getDataBigInteger()) == mmComparisonResult) {
                mmAppliedMessages.add(message);
                return true;
            } else {
                return false;
            }
        }

        @Override public void start() {
            mmAppliedMessages = Sets.newHashSet();
        }

        @Override public void stop() {
            for (Iterator<? extends CanLog.Item> i = mCanLog.iterator(); i.hasNext();) {
                CanLog.Item item = i.next();

                boolean matches = false;
                boolean matchesDestination = false;
                for (CanMessage message: mmAppliedMessages) {
                    if (item.matches(message)) {
                        matches = true;
                        break;
                    }
                    if (item.matchesDestination(message.getDestination())) {
                        matchesDestination = true;
                    }
                }

                if (!matches && !matchesDestination) {
                    i.remove();
                } else if (matchesDestination) {
                    mCanLog.setItemDestinationMatcher(item);
                }
            }
        }
    }
}
