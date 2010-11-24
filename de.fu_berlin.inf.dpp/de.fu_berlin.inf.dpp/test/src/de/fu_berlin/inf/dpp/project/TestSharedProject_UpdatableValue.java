package de.fu_berlin.inf.dpp.project;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.fu_berlin.inf.dpp.project.SharedProject.UpdatableValue;

public class TestSharedProject_UpdatableValue {
    public @Test void update() {
        UpdatableValue<String> x = new UpdatableValue<String>(null);
        assertFalse(x.update(null));

        assertTrue(x.update("hello"));
        assertFalse(x.update("hello"));

        assertTrue(x.update("kitty"));
        assertFalse(x.update("kitty"));

        assertTrue(x.update(null));
        assertFalse(x.update(null));
    }

    public @Test void getValue() {
        UpdatableValue<String> x = new UpdatableValue<String>(null);
        assertEquals(null, x.getValue());
        x.update("hello");
        assertEquals("hello", x.getValue());
        x.update("kitty");
        assertEquals("kitty", x.getValue());
        x.update(null);
        assertEquals(null, x.getValue());
    }

}
