package org.limewire.util;

import java.io.File;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

/** Utility test-case class that others can extend for easier testing. */ 
public abstract class BaseTestCase extends AssertComparisons {
    
    protected volatile static Class _testClass;
    private   final static Timer _testKillerTimer = new Timer(true);
    protected volatile static String _currentTestName;
    protected Thread _testThread;
    protected TestResult _testResult;
    protected TimerTask _testKiller;
    protected long _startTimeForTest;
    protected Class<? extends Throwable> expectedException;

    /**
     * bug 6435126
     */
    private static final Thread INTERRUPT_FIXER = new Thread() {
    	@Override
        public void run() {
    		try {
    			Thread.sleep(Integer.MAX_VALUE);
    		} catch (InterruptedException ignore){}
    	}
    };
    
    static {
        INTERRUPT_FIXER.setDaemon(true);
        INTERRUPT_FIXER.start();
        DeadlockMonitor.startDeadlockMonitoring();
    }
    
    /**
     * The base constructor.
     * Nothing should ever be initialized in the constructor.
     * This is because of the way JUnit sets up tests --
     * It first builds a new instance of the class for every possible test,
     * then it runs through those instances, calling the appropriate test.
     * All pre & post initializations that are necessary for every test
     * should be in the new 'preSetUp' and 'postTearDown' methods.
     */    
    public BaseTestCase(String name) {
        super(name);
        _testClass = getClass();
    }
    
    /**
     * Build a test suite containing all of the test methods in the given class
     * @param cls The test class (must be subclassed from TestCase)
     * @return <tt>TestSuite</tt> object that can be returned by suite method
     */
    @SuppressWarnings("unchecked")
    protected static TestSuite buildSingleTestSuite(Class cls) {
        _testClass = cls;
        
        String method = System.getProperty("junit.test.method");
        if(method != null) {
            method = method.trim();
            if(!"".equals(method) && !"${method}".equals(method)) {
                
                StringTokenizer st = new StringTokenizer(method, ",");
                List l = new LinkedList();
                while(st.hasMoreTokens())
                    l.add(st.nextToken());
                
                String[] tests = (String[])l.toArray(new String[l.size()]);
                return buildTestSuite(cls, tests);
            }
        }
        return new LimeTestSuite(cls);
    }
    
    @SuppressWarnings("unchecked")
    protected static TestSuite buildTestSuite(Class cls) {
        TestSuite suite = buildSingleTestSuite(cls);
        String timesP = System.getProperty("junit.test.times","1");
        int times = 1 ;
        try {
            times = Integer.parseInt(timesP);
        } catch (NumberFormatException ignored ){}
        
        List tests = new LinkedList();
        for (Enumeration e = suite.tests();e.hasMoreElements();) 
            tests.add(e.nextElement());
        
        while (times-- > 1) {
            for (Iterator iter = tests.iterator(); iter.hasNext();) 
                suite.addTest((Test) iter.next());
        }
        
        // add a warning if we are running individual tests
        if (!System.getProperty("junit.test.method","${method}").equals("${method}"))
            suite.addTest(warning("Warning - Full test suite has not been run."));
        
        return suite;
    }
    
    /**
     * Build a test suite containing a single test from a specificed test class
     * @param cls The test class (must be subclassed from TestCase)
     * @param test The name of the test method in cls to be run
     * @return <tt>TestSuite</tt> object that can be returned by suite method
     */
    protected static TestSuite buildTestSuite(Class cls, String test) {
        _testClass = cls;
        return buildTestSuite(cls, new String[]{test});
    }
    
    /**
     * Build a test suite containing a set of tests from a specificed test class
     * @param cls The test class (must be subclassed from TestCase)
     * @param test Array containing the names of the test methods in cls to be 
     * run
     * @return <tt>TestSuite</tt> object that can be returned by suite method
     */
    protected static TestSuite buildTestSuite(Class cls, String[] tests) {
        _testClass = cls;
        TestSuite suite = new LimeTestSuite(false, cls);
        for (int ii = 0; ii < tests.length; ii++) {
            if (!tests[ii].startsWith("test"))
                tests[ii]="test"+tests[ii];
            suite.addTest(TestSuite.createTest(cls, tests[ii]));
        }
        return suite;
    }
    
    /**
     * Recursively delete a directory.
     */
    protected static void cleanFiles(File dir, boolean deleteDirs) {
        if (dir == null)
            return;

        File[] files = dir.listFiles();
        for (int i = 0; files != null && i < files.length; i++) {
            if (files[i].isDirectory()) {
                cleanFiles(files[i], deleteDirs);
            } else {
                files[i].delete();
            }
        }
        if (deleteDirs)
            dir.delete();
    }

    /**
     * Used in conjunction with <code>BaseTestCase</code>'s implementation
     * of <code>ErrorService</code>.  Allows a test to configure an expected
     * <code>Exception</code>.
     * @see #error(Throwable, String)
     * @param t the expected Exception type
     */
    protected void setExpectedException(Class<? extends Throwable> t) {
        this.expectedException = t;
    }
    
    /*
     * This is modified to run 'preSetUp' and 'postTearDown' as methods
     * which all tests will run, regardless of their implementation
     * (or lack of) of setUp and tearDown.
     *
     * It is also modified so that if setUp throws something, tearDown
     * will still be run.
     *
	 */
	@Override
    public void runBare() throws Throwable {
        _currentTestName = getName();
        if(System.getProperty("junit.test.hidetestname", "${hidetestname}").equals("${hidetestname}")) {
            System.out.println("Running test: " + _currentTestName);
            //LogFactory.getLog(this.getClass()).info("Running test: " + _currentTestName);
        }
        assertNotNull(_currentTestName);
        Throwable thrown = null;
        try {
            preSetUp();
            setUp();
            runTest();
        } catch (Throwable t) {
            thrown = t;
            throw thrown;
        } finally {
            try {
                tearDown();
            } catch (Throwable tearDown){
                // don't let throwables during tearDown
                // overwrite throwables from the test
                if (thrown == null)
                    throw tearDown;
            } finally {
                postTearDown();
            }
        }
    }
    
    /**
     * Intercepted to allow us to get a handle to the test result, so we can 
     * add errors from the ErrorService callback (giving us errors that were
     * triggered from outside of the test thread).
     */
    @Override
    public void run(TestResult result) {
        _testResult = result;
        super.run(result);
    }
    
    /**
     * Called before each test's setUp.
     * Used to determine which thread the test is running in,
     * set up the testing directories, and possibly print
     * debugging information (such as the current test being run)
     * This must also set the ErrorService's callback, so it
     * associates with the correct test object.
     */
    protected void preSetUp() throws Exception {
        _testThread = Thread.currentThread();
        ErrorUtils.setCallback(this);
        setupTestTimer();
    }
    
    /**
     * Called after each test's tearDown.
     * Used to remove directories and possibly other things.
     */
    protected void postTearDown() {
        stopTestTimer();
    }
    
    /** After all tearDown/postTearDown/globalTearDown teardowns. */
    public static void afterAllTestsTearDown() throws Throwable {
        System.gc();
    }
    
    /**
     * Sets up the TimerTask to kill the running test after a certian amount of time.
     */
    private final void setupTestTimer() {
        _startTimeForTest = System.currentTimeMillis();
        _testKiller = new TimerTask() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                error(new RuntimeException("Stalled!  Took " +
                                    (now - _startTimeForTest) + " ms."),
                      "Test Took Too Long");
            }
        };
        // kill in a bit.
        _testKillerTimer.schedule(_testKiller, 7 * 60 * 1000);
    }
    
    /**
     * Stops the test timer since the test finished.
     */
    private final void stopTestTimer() {
        _testKiller.cancel();
        _testKiller = null;
    }

    /**
     * Fails the test with an AssertionFailedError and another
     * error as the root cause.
     */
    public static void fail(Throwable e) {
        fail(null, e);
    }
    
    /**
     * Fails the test with an AssertionFailedError and another
     * error as the root cause, with a message.
     */
    public static void fail(String message, Throwable e) {
        throw new UnexpectedExceptionError(message, e);
    }
            
    
    /**
     * Stub for error(Throwable, String)
     */
    public void error(Throwable ex) {
        error(ex, null);
    }
    
    /** 
     * This is the callback from ErrorService, and why we implement
     * ErrorCallback.
     *
     * It is used to catch errors that may or may not be inside of the
     * test thread.  If it is in the thread, we can just rethrow the
     * error, and the test will fail as normal. If it is outside of the
     * thread, we want the test results to remember the error, but we 
     * must allow the test to continue as normal, possibly succeeding, 
     * failing or erroring.
     * 
     * If an <code>expectedException</code> has been set, then
     * this method will check to see if the incoming exception 
     * is of the correct class.  If it is, then the call to this method is essentially ignored.
     *
     * Note that while the XML formatter can easily handle the case of
     * multiple failures/errors in a single test, the XML->HTML converter
     * doesn't do that good of a job.  It correctly lists the amount of
     * errors/failures, but it will only write the last one as the status
     * of the test, and will also only write the last one as the
     * message/stacktrace.
     */
    public void error(Throwable ex, String detail) {
        if(expectedException != null && ex != null) {
            if(expectedException.isInstance(ex)) {
                return;
            }
        }
        ex = new UnexpectedExceptionError(detail, ex); // remember the detail & stack trace of the ErrorService.
        if ( _testThread != Thread.currentThread() ) {
            // the Eclipse JUnit plug-in does not report multiple errors per test case: 
            // https://bugs.eclipse.org/bugs/show_bug.cgi?id=125296
            // print out stack trace to make debugging of uncauhgt exceptions easier
            for (StackTraceElement item : _testThread.getStackTrace()) {
                if (item.getClassName().contains("org.eclipse")) {
                    ex.printStackTrace();
                    break;
                }
            }
            _testResult.addError(this, ex);
            _testThread.interrupt();
        } else {
            fail("ErrorService callback error", ex);
        }
    }
    
    /**
     * Returns a test which will fail and log a warning message.
     * Copied from JUnit's TestSuite.java
     * Note that it does not have to extend BaseTestCase, just TestCase.
     * BaseTestCase would add needless complexity to an otherwise
     * simple failure message.
     */
    private static Test warning(final String message) {
    	return new TestCase("warning") {
    		@Override
            protected void runTest() {
    			fail(message);
    		}
    	};
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        setExpectedException(null);
    }
}       

