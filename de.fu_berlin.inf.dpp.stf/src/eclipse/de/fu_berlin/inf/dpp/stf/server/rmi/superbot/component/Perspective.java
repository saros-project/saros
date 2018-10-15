package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component;

public class Perspective {

    public final static PersType WHICH_PERSPECTIVE = PersType.JAVA;
    public static ExplorerType WHICH_EXPLORER = ExplorerType.PACKAGE;

    public enum PersType {
        JAVA, RESOURCE, DEBUG
    }

    public enum ExplorerType {
        PACKAGE, RESOURCE
    }

    static {
        switch (WHICH_PERSPECTIVE) {
        case JAVA:
            WHICH_EXPLORER = ExplorerType.PACKAGE;
            break;
        case RESOURCE:
            WHICH_EXPLORER = ExplorerType.RESOURCE;
            break;
        default:
            WHICH_EXPLORER = ExplorerType.PACKAGE;
            break;
        }
    }

}
