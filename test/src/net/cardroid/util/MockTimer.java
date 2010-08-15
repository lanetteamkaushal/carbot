package net.cardroid.util;

import com.google.common.collect.Lists;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Date: Apr 23, 2010
 * Time: 1:06:09 AM
 *
 * @author Lex Nikitin
 */
public class MockTimer extends Timer {
    public List<MockTimerTask> timerTasks = Lists.newArrayList();

    public MockTimer() {
        super();
    }

    public MockTimer(boolean isDaemon) {
        super(isDaemon);
    }

    public MockTimer(String name) {
        super(name);
    }

    public MockTimer(String name, boolean isDaemon) {
        super(name, isDaemon);
    }

    @Override public void schedule(TimerTask task, long delay) {
        MockTimerTask mockTask = new MockTimerTask(task);
        mockTask.sheduledDelay = delay;
        timerTasks.add(mockTask);
    }

    @Override public void schedule(TimerTask task, Date time) {
        MockTimerTask mockTask = new MockTimerTask(task);
        mockTask.sheduledTime = time.getTime();
        timerTasks.add(mockTask);
    }

    @Override public void schedule(TimerTask task, long delay, long period) {
        MockTimerTask mockTask = new MockTimerTask(task);
        mockTask.sheduledDelay = delay;
        mockTask.sheduledRate = period;
        timerTasks.add(mockTask);
    }

    @Override public void schedule(TimerTask task, Date firstTime, long period) {
        MockTimerTask mockTask = new MockTimerTask(task);
        mockTask.sheduledTime = firstTime.getTime();
        mockTask.sheduledRate = period;
        timerTasks.add(mockTask);
    }

    @Override public void scheduleAtFixedRate(TimerTask task, long delay, long period) {
        MockTimerTask mockTask = new MockTimerTask(task);
        mockTask.sheduledDelay = delay;
        mockTask.sheduledRate = period;
        timerTasks.add(mockTask);
    }

    @Override public void scheduleAtFixedRate(TimerTask task, Date firstTime, long period) {
        MockTimerTask mockTask = new MockTimerTask(task);
        mockTask.sheduledTime = firstTime.getTime();
        mockTask.sheduledRate = period;
        timerTasks.add(mockTask);
    }

    @Override public void cancel() {
        super.cancel();
    }

    @Override public int purge() {
        return super.purge();
    }

    public static class MockTimerTask extends TimerTask {
        public final TimerTask timerTask;
        public long sheduledDelay;
        public long sheduledTime;
        public long sheduledRate;

        public MockTimerTask(TimerTask timerTask) {
            this.timerTask = timerTask;
        }

        @Override public void run() {
            timerTask.run();
        }

        public boolean isCancelled() {
            try {
                Field field = TimerTask.class.getDeclaredField("cancelled");
                field.setAccessible(true);
                return field.getBoolean(timerTask);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
