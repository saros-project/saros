package de.fu_berlin.inf.dpp.versioning;

import java.util.Comparator;

/**
 * Enumeration to describe whether a local version is compatible with a
 * remote one.
 */
public enum Compatibility {

    /**
     * Versions are (probably) compatible
     */
    OK {
        @Override
        public Compatibility invert() {
            return OK;
        }
    },
    /**
     * The local version is (probably) too old to work with the remote
     * version.
     * 
     * The user should be told to upgrade
     */
    TOO_OLD {
        @Override
        public Compatibility invert() {
            return TOO_NEW;
        }
    },
    /**
     * The local version is (probably) too new to work with the remote
     * version.
     * 
     * The user should be told to tell the peer to update.
     */
    TOO_NEW {
        @Override
        public Compatibility invert() {
            return TOO_OLD;
        }
    };

    /**
     * 
     * @return <code>TOO_OLD</code> if the initial compatibility was
     *         <code>TOO_NEW</code>, <code>TOO_NEW</code> if the initial
     *         compatibility was <code>TOO_OLD</code>, <code>OK</code>
     *         otherwise
     */
    public abstract Compatibility invert();

    /**
     * Given a result from {@link Comparator#compare(Object, Object)} will
     * return the associated Compatibility object
     */
    public static Compatibility valueOf(int comparison) {
        switch (Integer.signum(comparison)) {
        case -1:
            return TOO_OLD;
        case 0:
            return OK;
        case 1:
        default:
            return TOO_NEW;
        }
    }
}