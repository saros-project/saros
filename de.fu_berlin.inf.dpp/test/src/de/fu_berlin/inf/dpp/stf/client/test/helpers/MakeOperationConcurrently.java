package de.fu_berlin.inf.dpp.stf.client.test.helpers;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.stf.client.Musician;

public class MakeOperationConcurrently {
    private final static Logger log = Logger
        .getLogger(MakeOperationConcurrently.class);

    public static <T> List<T> workAll(List<Callable<T>> tasks) throws InterruptedException {
//        if(System.getProperty("").equals("MAC"))
//            return workAll(tasks, 1);
//        else
            return workAll(tasks,tasks.size());
    }
    
    public static <T> List<T> workAll(List<Callable<T>> tasks,
        int numberOfThreads) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(numberOfThreads);
        final List<Future<T>> futureResult;
        futureResult = pool.invokeAll(tasks);

        List<T> result = new ArrayList<T>();
        for (Future<T> future : futureResult) {
            final T value;
            try {
                value = future.get();
            } catch (ExecutionException e) {
                log.error("Couldn't execute task", e);
                continue;
            }
            result.add(value);
        }
        return result;
    }

    public static List<Boolean> leaveSessionConcurrently(Musician... invitees)
        throws RemoteException, InterruptedException {
        List<Musician> peers = new LinkedList<Musician>();
        for (Musician invitee : invitees) {
            peers.add(invitee);
        }
        List<Callable<Boolean>> leaveTasks = new ArrayList<Callable<Boolean>>();
        for (int i = 0; i < peers.size(); i++) {
            final Musician musician = peers.get(i);
            leaveTasks.add(new Callable<Boolean>() {
                public Boolean call() throws Exception {
                    // Need to check for isDriver before leaving.
                    musician.sessionV.leaveTheSessionByPeer();
                    return musician.sessionV.isParticipant(musician.jid);
                }
            });
        }
        log.trace("workAll(leaveTasks)");
        final List<Boolean> workAll = workAll(leaveTasks);

        return workAll;
    }
}
