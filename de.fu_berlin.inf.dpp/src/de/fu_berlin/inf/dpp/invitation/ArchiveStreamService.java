package de.fu_berlin.inf.dpp.invitation;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.net.internal.StreamService;
import de.fu_berlin.inf.dpp.net.internal.StreamSession;

public class ArchiveStreamService extends StreamService {
    private static Logger log = Logger
        .getLogger(IncomingProjectNegotiation.class);

    protected IProject sharedProject;
    protected int numOfFiles;

    protected StreamSession streamSession;

    protected Lock startLock = new ReentrantLock();
    protected Condition sessionReceived = startLock.newCondition();

    @Override
    public String getServiceName() {
        return "ArchiveProject";
    }

    @Override
    public int getStreamsPerSession() {
        return 1;
    }

    @Override
    public int[] getChunkSize() {
        return new int[] { 1024 * 1024 };
    }

    public int getFileNum() {
        return numOfFiles;
    }

    public void setProject(IProject sp) {
        this.sharedProject = sp;
    }

    @Override
    public boolean sessionRequest(User from, Object initial) {
        log.info(from + " wants to stream the project archive.");

        numOfFiles = ((Integer) initial).intValue();

        return true;
    }

    @Override
    public void startSession(final StreamSession newSession) {

        this.startLock.lock();
        this.streamSession = newSession;
        this.sessionReceived.signal();
        this.startLock.unlock();

    }

}
