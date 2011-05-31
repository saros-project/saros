package de.fu_berlin.inf.dpp.util;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * 
 * @author Lindner, Andreas und Marcus
 * 
 */

public class NamedThreadFactoryTest {
    @Test
    public void nullNameShouldExceptWhenCreated() {
        NamedThreadFactory factory = new NamedThreadFactory(null);
        
        Thread t1 = factory.newThread(new Runnable() { public void run() { /**/ }});
        Thread t2 = factory.newThread(new Runnable() { public void run() { /**/ }});

        assertTrue(t1.getName().equals("null0"));
        assertTrue(t2.getName().equals("null1"));
        
        // TODO: check some other things? maybe unwanted behavior, class has to be changed!?
    }

    @Test
    public void emptyNameShouldWork() {
        NamedThreadFactory factory = new NamedThreadFactory("");
        
        Thread t1 = factory.newThread(new Runnable() { public void run() { /**/ }});
        Thread t2 = factory.newThread(new Runnable() { public void run() { /**/ }});
        
        assertTrue(t1.getName().equals("0"));
        assertTrue(t2.getName().equals("1"));
    }
    
    @Test
    public void anyOtherNameShouldWork() {
        String name = "anyname";
        NamedThreadFactory factory = new NamedThreadFactory(name);
        
        Thread t1 = factory.newThread(new Runnable() { public void run() { /**/ }});
        Thread t2 = factory.newThread(new Runnable() { public void run() { /**/ }});

        assertTrue(t1.getName().equals(name + "0"));
        assertTrue(t2.getName().equals(name + "1"));
    }
}
