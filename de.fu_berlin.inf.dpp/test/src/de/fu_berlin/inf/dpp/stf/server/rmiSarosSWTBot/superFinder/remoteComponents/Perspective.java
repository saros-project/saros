package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents;

public class Perspective {

    public final static PersType WHICH_PERSPECTIVE = PersType.JAVA;
    public static explorerType WHICH_EXPLORER = explorerType.PACKAGE;

    public enum PersType {
        JAVA, RESOURCE, DEBUG
    }

    public enum explorerType {
        PACKAGE, RESOURCE
    }

    static {
        switch (WHICH_PERSPECTIVE) {
        case JAVA:
            WHICH_EXPLORER = explorerType.PACKAGE;
            break;
        case RESOURCE:
            WHICH_EXPLORER = explorerType.RESOURCE;

            break;
        default:
            WHICH_EXPLORER = explorerType.PACKAGE;
            break;
        }
    }

}
