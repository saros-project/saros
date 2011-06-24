package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.internal.impl;

import java.lang.reflect.Field;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.internal.IInternal;
import de.fu_berlin.inf.dpp.util.VersionManager;

public class InternalImpl extends StfRemoteObject implements IInternal {

    private static final Logger log = Logger.getLogger(InternalImpl.class);

    private static final InternalImpl INSTANCE = new InternalImpl();

    private Field versionManagerBundleField;

    Bundle sarosBundle;

    public static IInternal getInstance() {
        return INSTANCE;
    }

    private InternalImpl() {
        try {
            versionManagerBundleField = VersionManager.class
                .getDeclaredField("bundle");
            versionManagerBundleField.setAccessible(true);
        } catch (SecurityException e) {
            log.error("reflection failed", e);
        } catch (NoSuchFieldException e) {
            log.error("reflection failed", e);
        }
    }

    public void changeSarosVersion(String version) throws RemoteException {

        log.trace("attempting to change saros version to: " + version);
        Version v = Version.parseVersion(version);

        if (versionManagerBundleField == null)
            throw new IllegalStateException(
                "unable to change version, reflection failed");

        try {

            if (sarosBundle == null)
                sarosBundle = (Bundle) versionManagerBundleField
                    .get(getVersionManager());

            versionManagerBundleField.set(getVersionManager(),
                new BundleFake(v));

        } catch (IllegalArgumentException e) {
            log.debug("unable to change saros version, reflection failed ", e);
        } catch (IllegalAccessException e) {
            log.debug("unable to change saros version, reflection failed", e);
        }

    }

    public void resetSarosVersion() throws RemoteException {

        log.trace("attempting to reset saros version");

        if (sarosBundle == null)
            return;

        try {
            versionManagerBundleField.set(getVersionManager(), sarosBundle);
        } catch (IllegalArgumentException e) {
            log.debug("unable to reset saros version, reflection failed", e);
        } catch (IllegalAccessException e) {
            log.debug("unable to reset saros version, reflection failed", e);
        }

        sarosBundle = null;

    }
}
