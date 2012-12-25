package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.internal.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;

class BundleFake implements Bundle {

    private Version version;

    public BundleFake(Version version) {
        this.version = version;
    }

    @Override
    public int getState() {
        return 0;
    }

    @Override
    public void start(int options) throws BundleException {
        //
    }

    @Override
    public void start() throws BundleException {
        //
    }

    @Override
    public void stop(int options) throws BundleException {
        //
    }

    @Override
    public void stop() throws BundleException {
        //
    }

    @Override
    public void update(InputStream input) throws BundleException {
        //
    }

    @Override
    public void update() throws BundleException {
        //
    }

    @Override
    public void uninstall() throws BundleException {
        //
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Dictionary getHeaders() {
        return null;
    }

    @Override
    public long getBundleId() {
        return 0;
    }

    @Override
    public String getLocation() {
        return null;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public ServiceReference[] getRegisteredServices() {
        return null;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public ServiceReference[] getServicesInUse() {
        return null;
    }

    @Override
    public boolean hasPermission(Object permission) {
        return false;
    }

    @Override
    public URL getResource(String name) {
        return null;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Dictionary getHeaders(String locale) {
        return null;
    }

    @Override
    public String getSymbolicName() {
        return null;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Class loadClass(String name) throws ClassNotFoundException {
        return null;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Enumeration getResources(String name) throws IOException {
        return null;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Enumeration getEntryPaths(String path) {
        return null;
    }

    @Override
    public URL getEntry(String path) {
        return null;
    }

    @Override
    public long getLastModified() {
        return 0;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Enumeration findEntries(String path, String filePattern,
        boolean recurse) {
        return null;
    }

    @Override
    public BundleContext getBundleContext() {
        return null;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Map getSignerCertificates(int signersType) {
        return null;
    }

    @Override
    public Version getVersion() {
        return version;
    }

    // ignore warning here, so the code will compile against Eclipse 3.6 and 3.7
    public int compareTo(Bundle bundle) {
        return 0;
    }

    // ignore warning here, so the code will compile against Eclipse 3.6 and 3.7
    public <A> A adapt(Class<A> clazz) {
        return null;
    }

    // ignore warning here, so the code will compile against Eclipse 3.6 and 3.7
    public File getDataFile(String path) {
        return null;
    }

}
