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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.Injector;
import org.picocontainer.PicoContainer;
import org.picocontainer.monitors.AbstractComponentMonitor;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.util.Function;
import de.fu_berlin.inf.dpp.util.Pair;

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

    private static final long serialVersionUID = 7368290879876948459L;

    private static final Logger log = Logger.getLogger(DotGraphMonitor.class
        .getName());

    protected IdentityHashMap<Object, Instantiation> allInstantiated = new IdentityHashMap<Object, Instantiation>();

    public static boolean cluster = false;

    public DotGraphMonitor(final ComponentMonitor delegate) {
        super(delegate);
    }

    public void save(File file) {
        try {
            String output = "digraph G {\n" + "  node [shape=box];\n"
                + "  rank=source;\n" + "  rankdir=LR;\n  node[penwidth=2.0];\n"
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

        Set<Class<?>> allComponentClasses = new HashSet<Class<?>>();

        for (Instantiation instantiation : allInstantiated.values()) {

            Object instantiated = instantiation.getInstantiated();
            allComponentClasses.add(instantiated.getClass());

            Object[] injects = instantiation.getConstructorInjected();
            for (int j = 0; j < injects.length; j++) {
                Object injected = injects[j];
                allComponentClasses.add(injected.getClass());
                lines.add("  '" + instantiated.getClass().getSimpleName()
                    + "' -> '" + injected.getClass().getSimpleName() + "';\n");
            }

            for (Class<?> invocationInjected : instantiation
                .getInvocationInjected()) {
                allComponentClasses.add(invocationInjected);
                lines.add("  '" + instantiated.getClass().getSimpleName()
                    + "' -> '" + invocationInjected.getSimpleName() + "';\n");
            }
        }

        HashMap<String, String> colors = new HashMap<String, String>();
        colors.put("core", "red");
        colors.put("net", "blue");
        colors.put("util", "aquamarine");
        colors.put("misc", "aquamarine");
        colors.put("observables", "darkolivegreen1");
        colors.put("logging", "yellow");
        colors.put("consistency", "green");
        colors.put("ui", "gold1");
        colors.put("undo", "gold1");
        colors.put("action", "gold1");
        colors.put("prefs", "blueviolet");
        colors.put("integration", "deeppink");
        colors.put("feedback", "blueviolet");
        colors.put("pico", "black");

        int i = 0;

        StringBuilder sb = new StringBuilder();

        if (!cluster) {
            sb.append("subgraph cluster").append(i++).append("{\n");
            sb.append("  style=rounded;  style=filled;"
                + " bgcolor=white; fontsize=40;\n");
            sb.append("  label=\"legend\";\n");
            for (Entry<String, String> entry : colors.entrySet()) {
                sb.append("\"").append(entry.getKey()).append("\" [color=")
                    .append(entry.getValue()).append("];\n");
            }
            sb.append("}\n");
        }

        for (Pair<String, List<Class<?>>> p : Pair.partition(
            allComponentClasses, new ModuleFunction())) {

            if (cluster) {
                sb.append("subgraph cluster").append(i++).append("{\n");
                sb
                    .append("  style=rounded;  style=filled; color=gray92; fontsize=40;\n");
                sb.append("  label=\"").append(p.p).append("\";\n");
            }

            String color = colors.get(p.p);
            if (color == null) {
                log.warn("No color found for Module " + p.p);
                color = "black";
            }

            for (Class<?> clazz : p.v) {
                sb.append("  \"" + clazz.getSimpleName() + "\"");
                sb.append(" [color=" + color + "]");
                sb.append(";\n");
            }

            if (cluster)
                sb.append("}\n");
        }

        return sb.toString() + sortLines(lines);
    }

    public static String getColorOld(Class<?> clazz,
        HashMap<String, String> colors) {
        String name;
        Package myPackage = clazz.getPackage();
        if (myPackage == null) {
            name = "misc";
        } else {
            name = myPackage.getName();
        }

        while (name != null && name.length() > 0) {

            String color = colors.get(name);
            if (color != null) {
                return "  \"" + clazz.getSimpleName() + "\" [color=" + color
                    + "];\n";
            }

            int index = name.lastIndexOf('.');
            if (index == -1)
                break;

            name = name.substring(0, index);
        }

        return null;
    }

    private String sortLines(final Set<String> lines) {
        List<String> list = new ArrayList<String>(lines);
        Collections.sort(list);

        StringBuilder dependencies = new StringBuilder();
        for (Object aList : list) {
            dependencies.append(aList);
        }
        return dependencies.toString().replaceAll("'", "\"");
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

    public static final class ModuleFunction implements
        Function<Class<?>, String> {
        public String apply(Class<?> u) {
            if (u.isArray()) {
                u = u.getComponentType();
            }
            Component c = u.getAnnotation(Component.class);
            if (c == null) {
                log.debug("Injected component with no @Component annotation: "
                    + u.getSimpleName());
                return Component.DEFAULT_MODULE;
            } else {
                return c.module();
            }
        }
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
