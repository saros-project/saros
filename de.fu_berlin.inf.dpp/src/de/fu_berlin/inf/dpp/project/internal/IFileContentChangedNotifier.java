package de.fu_berlin.inf.dpp.project.internal;

public interface IFileContentChangedNotifier {

    public void addFileContentChangedListener(
        IFileContentChangedListener listener);

    public void removeFileContentChangedListener(
        IFileContentChangedListener listener);
}
