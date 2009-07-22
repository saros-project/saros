package de.fu_berlin.inf.dpp.util;

import org.apache.commons.lang.time.StopWatch;

/**
 * Apache StopWatch with Fluid Interface Style.
 * 
 * All times are returned in ms.
 * 
 * All calls are delegated to the {@link #watch}.
 */
public class StoppWatch {

    protected StopWatch watch = new StopWatch();

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        return watch.equals(obj);
    }

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
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return watch.hashCode();
    }

    /**
     * @see org.apache.commons.lang.time.StopWatch#reset()
     */
    public StoppWatch reset() {
        watch.reset();
        return this;
    }

    /**
     * @see org.apache.commons.lang.time.StopWatch#resume()
     */
    public StoppWatch resume() {
        watch.resume();
        return this;
    }

    /**
     * @see org.apache.commons.lang.time.StopWatch#split()
     */
    public StoppWatch split() {
        watch.split();
        return this;
    }

    /**
     * @see org.apache.commons.lang.time.StopWatch#start()
     */
    public StoppWatch start() {
        watch.start();
        return this;
    }

    /**
     * @see org.apache.commons.lang.time.StopWatch#stop()
     */
    public StoppWatch stop() {
        watch.stop();
        return this;
    }

    /**
     * @see org.apache.commons.lang.time.StopWatch#suspend()
     */
    public StoppWatch suspend() {
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
    public StoppWatch unsplit() {
        watch.unsplit();
        return this;
    }

    /**
     * Convenience function returning a string representing of the throughput
     * achieved when sending =length= number of bytes in getTime() ms.
     */
    public String throughput(long length) {
        return Util.throughput(length, getTime());
    }

}
