package net.cardroid.testing;

import com.google.common.collect.Lists;
import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Date: Apr 10, 2010
 * Time: 10:17:25 PM
 *
 * @author Lex Nikitin
 */
public class MultithreadedTestCase extends TestCase {
    protected List<Throwable> mThrowables = Lists.newArrayList();

    @Override protected void tearDown() throws Exception {
        if (!mThrowables.isEmpty()) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(baos);

            for (Throwable throwable : mThrowables) {
                throwable.printStackTrace(printStream);
                printStream.println();
            }

            fail(baos.toString());
        }        
    }
    
    // lame but easy way to wait until things settle.
    protected void waitForAsyncProcess() {
    try {
        Thread.sleep(Constants.ASYNC_WAIT_TIME);
    } catch (InterruptedException e) {
        throw new RuntimeException(e);
    }
}

    protected void waitForThread(final Thread thread) throws InterruptedException {
        try {
            thread.join(Constants.MAX_WAIT_TIME_MILLIS);
        } catch (InterruptedException e) {
            mThrowables.add(e);
        }

        if(thread.isAlive()) {
            fail("Thread is still alive");
        }
    }

    protected void waitForSemaphore(Semaphore semaphoreOut) {
        try {
            if(!semaphoreOut.tryAcquire(Constants.MAX_WAIT_TIME_MILLIS, TimeUnit.MILLISECONDS)) {
            	fail("Timed out waiting for semaphore");
            }
        } catch (InterruptedException e) {
            mThrowables.add(e);
        }
    }
}
