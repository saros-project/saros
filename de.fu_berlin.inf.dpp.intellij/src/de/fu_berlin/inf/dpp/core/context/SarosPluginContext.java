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

package de.fu_berlin.inf.dpp.core.context;

import de.fu_berlin.inf.dpp.core.Saros;

/**
 * Provides the possibility to initialize a component with the components hold
 * in the given {@link SarosContext}. You can set the
 * context via
 * {@link SarosPluginContext#setSarosContext(SarosContext)}
 * .
 * <p/>
 * Typically this is the context created by {@link de.fu_berlin.inf.dpp.core.Saros}
 * while it's initialization.
 *
 * @author philipp.cordes
 */
public class SarosPluginContext {

    private static SarosContext sarosContext;

    public static void setSarosContext(SarosContext sarosContext) {
        SarosPluginContext.sarosContext = sarosContext;
    }

    /**
     * Use this only in eclipse-specific components like actions or views. If
     * you want to initialize a saros-specific component which is located in our
     * 'business-logic' you should use
     * {@link SarosContext#initComponent(Object)} directly
     * from context of the current Saros.
     *
     * @param toInjectInto component which should to be initialized.
     */
    public static void initComponent(Object toInjectInto) {
        Saros.checkInitialized();
        sarosContext.initComponent(toInjectInto);
    }

    /**
     * Use this only in eclipse-specific components like actions or views. If
     * you want to reinject a saros-specific component which is located in our
     * 'business-logic' you should use
     * {@link SarosContext#reinject(Object)} directly from
     * context of the current Saros.
     *
     * @param toReinject component which should to be reinjected.
     */
    public static void reinject(Object toReinject) {
        Saros.checkInitialized();
        sarosContext.reinject(toReinject);
    }
}

