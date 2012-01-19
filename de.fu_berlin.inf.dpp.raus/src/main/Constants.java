package main;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class Constants {

    public static URL getRepoRoot() {
        try {
            return new URL("https://dpp.svn.sourceforge.net/svnroot/dpp");
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public static File getWorkingCopy() {
        return new File(System.getProperty("user.dir"));
    }

    public static URL getRepoPath(String path) {
        try {
            return new URL(getRepoRoot(), path);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public static File getChangelogFile() {
        return new File(getWorkingCopy(), "CHANGELOG");
    }

}
