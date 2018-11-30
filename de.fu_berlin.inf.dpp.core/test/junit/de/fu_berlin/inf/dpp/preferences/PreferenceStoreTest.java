package de.fu_berlin.inf.dpp.preferences;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class PreferenceStoreTest {

  private PreferenceStore store;

  @Before
  public void setup() {
    store = new PreferenceStore();
  }

  @Test
  public void testGetDefaultDefaultValues() {

    assertEquals(PreferenceStore.DEFAULT_BOOLEAN, store.getDefaultBoolean("foo"));

    assertEquals(PreferenceStore.DEFAULT_STRING, store.getDefaultString("foo"));

    assertEquals(PreferenceStore.DEFAULT_INT, store.getDefaultInt("foo"));

    assertEquals(PreferenceStore.DEFAULT_LONG, store.getDefaultLong("foo"));
  }

  @Test
  public void testGetDefaultValuesWithDefaultValuesSet() {

    store.setDefault("foo.boolean", true);
    store.setDefault("foo.integer", Integer.MAX_VALUE);
    store.setDefault("foo.long", Long.MAX_VALUE);
    store.setDefault("foo.string", "foo");

    assertEquals(true, store.getDefaultBoolean("foo.boolean"));
    assertEquals(Integer.MAX_VALUE, store.getDefaultInt("foo.integer"));
    assertEquals(Long.MAX_VALUE, store.getDefaultLong("foo.long"));
    assertEquals("foo", store.getDefaultString("foo.string"));

    assertEquals(true, store.getBoolean("foo.boolean"));
    assertEquals(Integer.MAX_VALUE, store.getInt("foo.integer"));
    assertEquals(Long.MAX_VALUE, store.getLong("foo.long"));
    assertEquals("foo", store.getString("foo.string"));
  }

  @Test
  public void testGetValuesWithDefaultValuesAndValuesSet() {

    store.setDefault("foo.boolean", true);
    store.setDefault("foo.integer", Integer.MAX_VALUE);
    store.setDefault("foo.long", Long.MAX_VALUE);
    store.setDefault("foo.string", "foo");

    store.setValue("foo.boolean", false);
    store.setValue("foo.integer", Integer.MIN_VALUE);
    store.setValue("foo.long", Long.MIN_VALUE);
    store.setValue("foo.string", "bar");

    assertEquals(true, store.getDefaultBoolean("foo.boolean"));
    assertEquals(Integer.MAX_VALUE, store.getDefaultInt("foo.integer"));
    assertEquals(Long.MAX_VALUE, store.getDefaultLong("foo.long"));
    assertEquals("foo", store.getDefaultString("foo.string"));

    assertEquals(false, store.getBoolean("foo.boolean"));
    assertEquals(Integer.MIN_VALUE, store.getInt("foo.integer"));
    assertEquals(Long.MIN_VALUE, store.getLong("foo.long"));
    assertEquals("bar", store.getString("foo.string"));
  }

  @Test
  public void testGetValuesWithNoValuesSet() {

    assertEquals(PreferenceStore.DEFAULT_BOOLEAN, store.getBoolean("foo"));

    assertEquals(PreferenceStore.DEFAULT_STRING, store.getString("foo"));

    assertEquals(PreferenceStore.DEFAULT_INT, store.getInt("foo"));

    assertEquals(PreferenceStore.DEFAULT_LONG, store.getLong("foo"));
  }

  @Test
  public void testGetValuesWithValuesSet() {

    store.setValue("foo.boolean", "true");
    store.setValue("foo.integer", Integer.MAX_VALUE);
    store.setValue("foo.long", Long.MAX_VALUE);
    store.setValue("foo.string", "foo");

    assertEquals(true, store.getBoolean("foo.boolean"));
    assertEquals(Integer.MAX_VALUE, store.getInt("foo.integer"));
    assertEquals(Long.MAX_VALUE, store.getLong("foo.long"));
    assertEquals("foo", store.getString("foo.string"));
  }

  @Test
  public void testAssertProperDefaultValuesOnInvalidValueData() {

    store.setValue("foo.boolean", "FILE_NOT_FOUND");
    store.setValue("foo.integer", "###0xABCDEF###");
    store.setValue("foo.long", "###0xABCDEF###");

    assertEquals(false, store.getBoolean("foo.boolean"));

    assertEquals(PreferenceStore.DEFAULT_INT, store.getInt("foo.integer"));

    assertEquals(PreferenceStore.DEFAULT_LONG, store.getLong("foo.long"));
  }

  @Test
  public void testSetValueNonASCIICharacters() {
    final String data = new String("<&ハローキティ&>");

    store.setValue("hello.kitty.with.xml.control.characters", data);

    assertEquals(data, store.getString("hello.kitty.with.xml.control.characters"));
  }

  @Test
  public void testPreferenceChange() {

    final List<PreferenceChangeEvent> changeEvents = new ArrayList<PreferenceChangeEvent>();

    final IPreferenceChangeListener listener =
        new IPreferenceChangeListener() {

          @Override
          public void preferenceChange(PreferenceChangeEvent event) {
            changeEvents.add(event);
          }
        };

    store.addPreferenceChangeListener(listener);

    store.setDefault("bool", true);
    store.setDefault("int", 4711);
    store.setDefault("long", 4711L);
    store.setDefault("string", "string");

    assertEquals(
        "changing default values must not result in change events", 0, changeEvents.size());

    store.setValue("bool", true);
    store.setValue("int", 4711);
    store.setValue("long", 4711L);
    store.setValue("foo", "bar");

    assertEquals("at least one preference change was not broadcasted", 4, changeEvents.size());

    assertEquals(false, changeEvents.get(0).getOldValue());
    assertEquals(true, changeEvents.get(0).getNewValue());
    assertEquals("bool", changeEvents.get(0).getPreferenceName());

    assertEquals(0, changeEvents.get(1).getOldValue());
    assertEquals(4711, changeEvents.get(1).getNewValue());
    assertEquals("int", changeEvents.get(1).getPreferenceName());

    assertEquals(0L, changeEvents.get(2).getOldValue());
    assertEquals(4711L, changeEvents.get(2).getNewValue());
    assertEquals("long", changeEvents.get(2).getPreferenceName());

    assertEquals("", changeEvents.get(3).getOldValue());
    assertEquals("bar", changeEvents.get(3).getNewValue());
    assertEquals("foo", changeEvents.get(3).getPreferenceName());

    store.setValue("bool", true);
    store.setValue("int", 4711);
    store.setValue("long", 4711L);
    store.setValue("foo", "bar");

    assertEquals(
        "at least one preference change was broadcasted although it should not",
        4,
        changeEvents.size());
  }
}
