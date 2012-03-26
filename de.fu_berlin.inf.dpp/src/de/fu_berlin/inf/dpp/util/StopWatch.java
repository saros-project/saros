package de.fu_berlin.inf.dpp.util;

/**
 * Apache StopWatch with Fluid Interface Style.
 * 
 * All times are returned in ms.
 * 
 * All calls are delegated to the {@link #watch}.
 * 
 * @see org.apache.commons.lang.time.StopWatch
 */
public class StopWatch {

    protected org.apache.commons.lang.time.StopWatch watch = new org.apache.commons.lang.time.StopWatch();

    /**
     * @see org.apache.commons.lang.time.StopWatch#getSplitTime()
     */
    public long getSplitTime() {
        return watch.getSplitTime();
    }

    /**
     * @see org.apache.commons.lang.time.StopWatch#getStartTime()
     */
    public long getStartTime() {
        return watch.getStartTime();
    }

    /**
     * @see org.apache.commons.lang.time.StopWatch#getTime()
     */
    public long getTime() {
        return watch.getTime();
    }

    /**
     * @see org.apache.commons.lang.time.StopWatch#reset()
     */
    public StopWatch reset() {
        watch.reset();
        return this;
    }

    /**
     * @see org.apache.commons.lang.time.StopWatch#resume()
     */
    public StopWatch resume() {
        watch.resume();
        return this;
    }

    /**
     * @see org.apache.commons.lang.time.StopWatch#split()
     */
    public StopWatch split() {
        watch.split();
        return this;
    }

    /**
     * @see org.apache.commons.lang.time.StopWatch#start()
     */
    public StopWatch start() {
        watch.start();
        return this;
    }

    /**
     * @see org.apache.commons.lang.time.StopWatch#stop()
     */
    public StopWatch stop() {
        watch.stop();
        return this;
    }

    /**
     * @see org.apache.commons.lang.time.StopWatch#suspend()
     */
    public StopWatch suspend() {
        watch.suspend();
        return this;
    }

    /**
     * @see org.apache.commons.lang.time.StopWatch#toSplitString()
     */
    public String toSplitString() {
        return watch.toSplitString();
    }

    /**
     * @see org.apache.commons.lang.time.StopWatch#toString()
     */
    @Override
    public String toString() {
        return watch.toString();
    }

    /**
     * @see org.apache.commons.lang.time.StopWatch#unsplit()
     */
    public StopWatch unsplit() {
        watch.unsplit();
        return this;
    }

    /**
     * Convenience function returning a string representing of the throughput
     * achieved when sending =length= number of bytes in getTime() ms.
     */
    public String throughput(long length) {
        return Utils.throughput(length, getTime());
    }

}
