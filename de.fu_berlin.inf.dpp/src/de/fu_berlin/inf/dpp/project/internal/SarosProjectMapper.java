package de.fu_berlin.inf.dpp.project.internal;

import java.util.Collections;
import java.util.Set;

import org.eclipse.core.resources.IProject;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * Class responsible for mapping global project IDs to local IProjects.
 * 
 * These IDs are used to identify shared projects across the network, even when
 * the local names of a shared project are different. Since only the host is
 * able to share projects, the ID is determined by the host.
 */
class SarosProjectMapper {

    BiMap<String, IProject> mapping = HashBiMap.create();

    public synchronized void addMapping(String id, IProject localProject) {
        mapping.put(id, localProject);
    }

    public synchronized void removeMapping(String id) {
        mapping.remove(id);
    }

    public synchronized void removeMapping(IProject localProject) {
        mapping.inverse().remove(localProject);
    }

    public synchronized String getID(IProject localProject) {
        return mapping.inverse().get(localProject);
    }

    public synchronized IProject getProject(String id) {
        return mapping.get(id);
    }

    public synchronized boolean isShared(IProject localProject) {
        return mapping.containsValue(localProject);
    }

    public synchronized Set<IProject> getProjects() {
        return Collections.unmodifiableSet(mapping.values());
    }

    public synchronized int size() {
        return mapping.size();
    }
}
