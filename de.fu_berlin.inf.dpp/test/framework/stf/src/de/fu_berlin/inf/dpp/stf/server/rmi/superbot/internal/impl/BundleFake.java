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

    public int getState() {
        return 0;
    }

    public void start(int options) throws BundleException {
        //
    }

    public void start() throws BundleException {
        //
    }

    public void stop(int options) throws BundleException {
        //
    }

    public void stop() throws BundleException {
        //
    }

    public void update(InputStream input) throws BundleException {
        //
    }

    public void update() throws BundleException {
        //
    }

    public void uninstall() throws BundleException {
        //
    }

    @SuppressWarnings("rawtypes")
    public Dictionary getHeaders() {
        return null;
    }

    public long getBundleId() {
        return 0;
    }

    public String getLocation() {
        return null;
    }

    public ServiceReference[] getRegisteredServices() {
        return null;
    }

    public ServiceReference[] getServicesInUse() {
        return null;
    }

    public boolean hasPermission(Object permission) {
        return false;
    }

    public URL getResource(String name) {
        return null;
    }

    @SuppressWarnings("rawtypes")
    public Dictionary getHeaders(String locale) {
        return null;
    }

    public String getSymbolicName() {
        return null;
    }

    @SuppressWarnings("rawtypes")
    public Class loadClass(String name) throws ClassNotFoundException {
        return null;
    }

    @SuppressWarnings("rawtypes")
    public Enumeration getResources(String name) throws IOException {
        return null;
    }

    @SuppressWarnings("rawtypes")
    public Enumeration getEntryPaths(String path) {
        return null;
    }

    public URL getEntry(String path) {
        return null;
    }

    public long getLastModified() {
        return 0;
    }

    @SuppressWarnings("rawtypes")
    public Enumeration findEntries(String path, String filePattern,
        boolean recurse) {
        return null;
    }

    public BundleContext getBundleContext() {
        return null;
    }

    @SuppressWarnings("rawtypes")
    public Map getSignerCertificates(int signersType) {
        return null;
    }

    public Version getVersion() {
        return version;
    }

    public int compareTo(Bundle bundle) {
        return 0;
    }

    public <A> A adapt(Class<A> clazz) {
        return null;
    }

    public File getDataFile(String path) {
        return null;
    }

}
