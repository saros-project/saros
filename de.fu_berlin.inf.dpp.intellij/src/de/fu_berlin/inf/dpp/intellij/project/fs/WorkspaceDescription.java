/*
 *
 *  DPP - Serious Distributed Pair Programming
 *  (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2010
 *  (c) NFQ (www.nfq.com) - 2014
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 1, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * /
 */

package de.fu_berlin.inf.dpp.intellij.project.fs;

import de.fu_berlin.inf.dpp.core.workspace.IWorkspaceDescription;

public class WorkspaceDescription implements IWorkspaceDescription {
    private boolean autoBuilding;
    private String[] buildOrder;
    private long fileStateLongevity = 10000;
    private int maxBuildIterations = 10;
    private int maxFileStates = 100;
    private long maxFileStateSize = 100000000L;
    private boolean applyFileStatePolicy = false;
    private long snapshotInterval = 10000;

    @Override
    public String[] getBuildOrder() {
        return buildOrder;
    }

    @Override
    public void setBuildOrder(String[] arg0) {
        this.buildOrder = arg0;
    }

    @Override
    public long getFileStateLongevity() {
        return fileStateLongevity;
    }

    @Override
    public void setFileStateLongevity(long arg0) {
        this.fileStateLongevity = arg0;
    }

    @Override
    public int getMaxBuildIterations() {
        return maxBuildIterations;
    }

    @Override
    public void setMaxBuildIterations(int arg0) {
        this.maxBuildIterations = arg0;
    }

    @Override
    public int getMaxFileStates() {
        return maxFileStates;
    }

    @Override
    public void setMaxFileStates(int arg0) {
        this.maxFileStateSize = arg0;
    }

    @Override
    public long getMaxFileStateSize() {
        return maxFileStateSize;
    }

    @Override
    public void setMaxFileStateSize(long arg0) {
        this.maxFileStateSize = arg0;
    }

    @Override
    public boolean isApplyFileStatePolicy() {
        return applyFileStatePolicy;
    }

    @Override
    public void setApplyFileStatePolicy(boolean arg0) {
        this.applyFileStatePolicy = arg0;
    }

    @Override
    public long getSnapshotInterval() {
        return snapshotInterval;
    }

    @Override
    public void setSnapshotInterval(long arg0) {
        this.snapshotInterval = arg0;
    }

    @Override
    public boolean isAutoBuilding() {
        return autoBuilding;
    }

    @Override
    public void setAutoBuilding(boolean arg0) {
        this.autoBuilding = arg0;
    }
}
