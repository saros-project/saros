/**
 * 
 */
package de.fu_berlin.inf.dpp.util;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.Test;

/**
 * 
 */
public class ThreadAccessRecorderTest extends ThreadAccessRecorder {

    private static Logger log = Logger
        .getLogger(ThreadAccessRecorderTest.class);

    /**
     * Test method for
     * {@link de.fu_berlin.inf.dpp.util.ThreadAccessRecorder#record()}.
     * 
     * @throws InterruptedException
     */
    @Test
    public void testRecord() throws InterruptedException {
        final ThreadAccessRecorder tAccessRec = new ThreadAccessRecorder();
        int amountThreads = 5;
        for (int i = 0; i < amountThreads; i++) {
            Thread t1 = new Thread(new Runnable() {

                public void run() {
                    try {
                        tAccessRec.record();
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        log.debug("", e);
                    }

                }
            });
            t1.start();
            t1.join();
        }

        assertTrue("Record does not record all threads.",
            tAccessRec.threads.size() == amountThreads);

    }

    /**
     * Test method for
     * {@link de.fu_berlin.inf.dpp.util.ThreadAccessRecorder#record()}.
     * 
     * @throws InterruptedException
     */
    @Test(expected = InterruptedException.class)
    public void testRecord2() throws InterruptedException {
        final ThreadAccessRecorder tAccessRec = new ThreadAccessRecorder();
        tAccessRec.interrupt();
        // this should throw an interrupted exception!
        tAccessRec.record();
    }

    /**
     * Test method for
     * {@link de.fu_berlin.inf.dpp.util.ThreadAccessRecorder#release()}.
     * 
     * @throws InterruptedException
     */
    @Test
    public void testRelease() throws InterruptedException {

        final ThreadAccessRecorder tAccessRec = new ThreadAccessRecorder();
        int amountThreads = 5;
        for (int i = 0; i < amountThreads; i++) {
            Thread t1 = new Thread(new Runnable() {

                public void run() {
                    try {
                        tAccessRec.record();
                        tAccessRec.release();
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        log.debug("", e);
                    }

                }
            });
            t1.start();
            t1.join();
        }
        assertTrue("Release does not release the thread.",
            tAccessRec.threads.size() == 0);

    }

    /**
     * Test method for
     * {@link de.fu_berlin.inf.dpp.util.ThreadAccessRecorder#release()}.
     * 
     * @throws InterruptedException
     */
    @Test(expected = InterruptedException.class)
    public void testRelease2() throws InterruptedException {
        final ThreadAccessRecorder tAccessRec = new ThreadAccessRecorder();
        tAccessRec.interrupt();
        // this should throw an interrupted exception!
        tAccessRec.release();
    }

    /**
     * Test method for
     * {@link de.fu_berlin.inf.dpp.util.ThreadAccessRecorder#release()}.
     * 
     * @throws InterruptedException
     */
    @Test
    public void testRelease3() throws InterruptedException {

        final ThreadAccessRecorder tAccessRec = new ThreadAccessRecorder();
        Thread t1 = new Thread(new Runnable() {

            public void run() {
                try {
                    tAccessRec.record();
                    Thread.currentThread().interrupt();
                    // tAccessRec.interrupt();
                    tAccessRec.release();
                    assertTrue("The thread wasn't interrupted.", Thread
                        .currentThread().isInterrupted() == true);
                } catch (InterruptedException e) {
                    System.out.println(e);
                }

            }
        });
        t1.start();
        t1.join();
        tAccessRec.release();

    }

    /**
     * Test method for
     * {@link de.fu_berlin.inf.dpp.util.ThreadAccessRecorder#interrupt()}.
     * 
     * @throws InterruptedException
     */
    @Test
    public void testInterrupt() throws InterruptedException {
        final ThreadAccessRecorder tAccessRec = new ThreadAccessRecorder();
        int amountThreads = 5;
        for (int i = 0; i < amountThreads; i++) {
            Thread t1 = new Thread(new Runnable() {

                public void run() {
                    try {
                        tAccessRec.record();
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        log.debug("", e);
                    }

                }
            });
            t1.start();
            t1.join();
        }
        tAccessRec.interrupt();

        assertTrue(" Interrupted wasn't changed correctly",
            tAccessRec.interrupted == true);
        assertTrue("The threads weren't cleared",
            tAccessRec.threads.size() == 0);
    }

    /**
     * Test method for
     * {@link de.fu_berlin.inf.dpp.util.ThreadAccessRecorder#interrupted()}.
     */
    @Test
    public void testInterrupted() {
        final ThreadAccessRecorder tAccessRec = new ThreadAccessRecorder();
        assertTrue("The attribut interrupted wasn't returned correctly",
            tAccessRec.interrupted() == false);
        tAccessRec.interrupt();
        assertTrue("The attribut interrupted wasn't returned correctly",
            tAccessRec.interrupted() == true);
    }

}
