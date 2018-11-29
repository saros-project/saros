package de.fu_berlin.inf.dpp.misc.xstream;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.easymock.EasyMock;
import org.junit.Test;

public class ReplaceableConverterTest {

  private static class Dummy {
    //
  }

  @Test
  public void reset() {
    /* Mocks */
    Converter c1 = EasyMock.createMock(Converter.class);
    expect(c1.canConvert(Dummy.class)).andStubReturn(true);

    c1.marshal(
        isA(Object.class), isA(HierarchicalStreamWriter.class), isA(MarshallingContext.class));
    EasyMock.expectLastCall().once();

    EasyMock.replay(c1);

    /* XStream config */
    XStream xstream = new XStream(new DomDriver());
    ReplaceableConverter resetable = new ReplaceableConverter(c1);
    xstream.registerConverter(resetable);

    /* Test it */
    assertFalse("ReplaceableConverter was not properly set up", resetable.isReset());

    assertNotNull("Converter cannot convert", xstream.toXML(new Dummy()));

    resetable.reset();
    assertTrue("ReplaceableConverter was not properly reset", resetable.isReset());

    /*
     * This call should not reach the converter.
     */
    xstream.toXML(new Dummy());

    /*
     * Verify that the converter was used exactly once, i.e. it was not
     * called while it was inactive.
     */
    EasyMock.verify(c1);
  }

  @Test
  public void replace() {
    /* Mocks */
    Converter c1 = EasyMock.createMock(Converter.class);
    expect(c1.canConvert(Dummy.class)).andStubReturn(true);

    Converter c2 = EasyMock.createMock(Converter.class);
    expect(c2.canConvert(Dummy.class)).andStubReturn(true);

    c2.marshal(
        isA(Object.class), isA(HierarchicalStreamWriter.class), isA(MarshallingContext.class));
    EasyMock.expectLastCall().once();

    EasyMock.replay(c1, c2);

    /* XStream config */
    XStream xstream = new XStream(new DomDriver());
    ReplaceableConverter resetable = new ReplaceableConverter(c1);
    xstream.registerConverter(resetable);

    /* Test it */
    resetable.reset();
    assertTrue("ReplaceableConverter was not properly reset", resetable.isReset());

    /*
     * This call should not reach any of the converters.
     */
    xstream.toXML(new Dummy());

    resetable.replace(c2);
    assertNotNull("Converter was not reactivated", xstream.toXML(new Dummy()));

    /*
     * Verify that the first converter is not called, and the second exactly
     * once
     */
    EasyMock.verify(c1, c2);
  }
}
