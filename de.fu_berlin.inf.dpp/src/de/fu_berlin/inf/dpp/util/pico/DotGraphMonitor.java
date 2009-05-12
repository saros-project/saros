package de.fu_berlin.inf.dpp.util.pico;

/*****************************************************************************
 * Copyright (C) PicoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Paul Hammant                                             *
 *****************************************************************************/

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.Injector;
import org.picocontainer.PicoContainer;
import org.picocontainer.monitors.AbstractComponentMonitor;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.concurrent.IJupiterActivityManager;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * Component monitor which can be used to create a dependency graph of
 * components created and re-injected via PicoContainer (the latter is the
 * reason why this is an adapted implementation).
 * 
 * Based on DotDependencyGraphComponentMonitor
 * 
 * Run like this:
 * 
 * neato -Tsvg -Goverlap=false file.dot > diagram.svg
 * 
 * @author oezbek
 */
public final class DotGraphMonitor extends AbstractComponentMonitor implements
    ComponentMonitor {

    private static final Logger log = Logger.getLogger(DotGraphMonitor.class
        .getName());

    protected IdentityHashMap<Object, Instantiation> allInstantiated = new IdentityHashMap<Object, Instantiation>();

    public DotGraphMonitor(final ComponentMonitor delegate) {
        super(delegate);
    }

    public void save(File file) throws IOException {

        try {
            String output = "digraph G {\n" + "  node [shape=box];\n"
                + "  rank=source;\n" + "  rankdir=LR;\n"
                + getClassDependencyGraph() + "\n" + "}";

            FileUtils.writeStringToFile(file, output);
        } catch (Exception e) {
            log.error("Internal error: ", e);
        }
    }

    public DotGraphMonitor() {
        // do nothing
    }

    @Override
    public <T> void instantiated(final PicoContainer container,
        final ComponentAdapter<T> componentAdapter,
        final Constructor<T> constructor, final Object instantiated,
        final Object[] injected, final long duration) {

        this.allInstantiated.put(instantiated, new Instantiation(constructor,
            instantiated, injected));

        super.instantiated(container, componentAdapter, constructor,
            instantiated, injected, duration);
    }

    @Override
    public void invoking(PicoContainer container,
        ComponentAdapter<?> componentAdapter, Member member, Object instance) {

        if (!(componentAdapter instanceof Injector<?>))
            return;

        Instantiation i = allInstantiated.get(instance);
        if (i == null) {
            i = new Instantiation(null, instance, new Object[] {});
            allInstantiated.put(instance, i);
        }

        if (member instanceof Field) {
            Field f = (Field) member;
            i.fieldInjected.add(f.getType());
        }
        super.invoking(container, componentAdapter, member, instance);
    }

    public String getClassDependencyGraph() {

        Set<String> lines = new HashSet<String>();

        for (Instantiation instantiation : allInstantiated.values()) {

            Object instantiated = instantiation.getInstantiated();

            for (int j = 0; j < instantiation.getConstructorInjected().length; j++) {
                Object injected = instantiation.getConstructorInjected()[j];
                lines.add("  '" + instantiated.getClass().getSimpleName()
                    + "' -> '" + injected.getClass().getSimpleName() + "';\n");
            }

            for (Class<?> invocationInjected : instantiation
                .getInvocationInjected()) {
                lines.add("  '" + instantiated.getClass().getSimpleName()
                    + "' -> '" + invocationInjected.getSimpleName() + "';\n");
            }
        }

        HashMap<String, String> colors = new HashMap<String, String>();
        colors.put(Saros.class.getPackage().getName(), "red");
        colors.put(JID.class.getPackage().getName(), "blue");
        colors.put(Util.class.getPackage().getName(), "grey");
        colors.put(ISharedProject.class.getPackage().getName(), "green");
        colors.put(IJupiterActivityManager.class.getPackage().getName(),
            "yellow");
        colors.put(EditorManager.class.getPackage().getName(), "green");

        StringBuilder sb = new StringBuilder();
        atO: for (Object o : allInstantiated.keySet()) {

            String name = o.getClass().getPackage().getName();

            while (name != null && name.length() > 0) {

                String color = colors.get(name);
                if (color != null) {
                    sb.append("  \"" + o.getClass().getSimpleName()
                        + "\" [color=" + color + "];\n");
                    continue atO;
                }

                int index = name.lastIndexOf('.');
                if (index == -1)
                    break;

                name = name.substring(0, index);
            }
        }

        return sb.toString() + sortLines(lines);
    }

    private String sortLines(final Set<String> lines) {
        List<String> list = new ArrayList<String>(lines);
        Collections.sort(list);

        String dependencies = "";
        for (Object aList : list) {
            String dep = (String) aList;
            dependencies = dependencies + dep;
        }
        return dependencies.replaceAll("'", "\"");
    }

    public String getInterfaceDependencyGraph() {
        Set<String> lines = new HashSet<String>();

        for (Instantiation instantiation : allInstantiated.values()) {

            for (int j = 0; j < instantiation.getConstructorInjected().length; j++) {
                Object injected = instantiation.getConstructorInjected()[j];
                Class<?> injectedType = instantiation.getConstructor()
                    .getParameterTypes()[j];
                Object instantiated = instantiation.getInstantiated();
                if (injected.getClass() != injectedType) {
                    lines.add("  '" + instantiated.getClass().getName()
                        + "' -> '" + injectedType.getName()
                        + "' [style=dotted,label='needs'];\n");
                    lines.add("  '" + injected.getClass().getName() + "' -> '"
                        + injectedType.getName()
                        + "' [style=dotted, color=red,label='isA'];\n");
                    lines.add("  '" + injectedType.getName()
                        + "' [shape=box, label=" + printClassName(injectedType)
                        + "];\n");
                } else {
                    lines.add("  '" + instantiated.getClass().getName()
                        + "' -> '" + injected.getClass().getName()
                        + "' [label='needs'];\n");
                }
                lines.add("  '" + instantiated.getClass().getName()
                    + "' [label=" + printClassName(instantiated.getClass())
                    + "];\n");
            }
        }

        return sortLines(lines);
    }

    private String printClassName(final Class<?> clazz) {
        String className = clazz.getName();
        return "'" + className.substring(className.lastIndexOf(".") + 1)
            + "\\n" + clazz.getPackage().getName() + "'";

    }

    private static final class Instantiation {

        public List<Class<?>> fieldInjected = new ArrayList<Class<?>>();
        final Constructor<?> constructor;
        final Object instantiated;
        final Object[] injected;

        public Instantiation(final Constructor<?> constructor,
            final Object instantiated, final Object[] injected) {
            this.constructor = constructor;
            this.instantiated = instantiated;
            this.injected = injected;
        }

        public List<Class<?>> getInvocationInjected() {
            return fieldInjected;
        }

        public Constructor<?> getConstructor() {
            return constructor;
        }

        public Object getInstantiated() {
            return instantiated;
        }

        public Object[] getConstructorInjected() {
            return injected;
        }
    }
}
