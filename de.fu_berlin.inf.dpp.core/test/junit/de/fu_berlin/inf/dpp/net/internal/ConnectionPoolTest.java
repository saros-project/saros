package de.fu_berlin.inf.dpp.net.internal;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

public class ConnectionPoolTest {

  private ConnectionPool pool;

  @Before
  public void setUp() {
    pool = new ConnectionPool();
  }

  @Test
  public void testAddAndRemoveOnClosedPool() {

    final IByteStreamConnection connection = EasyMock.createNiceMock(IByteStreamConnection.class);

    EasyMock.replay(connection);

    assertSame("pool accepted connection when closed", connection, pool.add("foo", connection));

    assertNull("pool accepted connection when closed", pool.remove("foo"));
  }

  @Test
  public void testAddAndRemoveOnOpenedPool() {

    final IByteStreamConnection connection0 = EasyMock.createNiceMock(IByteStreamConnection.class);

    final IByteStreamConnection connection1 = EasyMock.createNiceMock(IByteStreamConnection.class);

    EasyMock.replay(connection0, connection1);

    pool.open();

    pool.add("foo", connection0);

    assertSame(
        "pool did not return the correct previous connection on add()",
        connection0,
        pool.add("foo", connection1));

    assertSame(
        "pool did not return the correct previous connection on remove()",
        connection1,
        pool.remove("foo"));
  }

  @Test
  public void testGet() {

    final IByteStreamConnection connection = EasyMock.createNiceMock(IByteStreamConnection.class);

    EasyMock.replay(connection);

    pool.open();

    pool.add("foo", connection);
    assertSame("pool does not contain the added connection", connection, pool.get("foo"));

    pool.remove("foo");
    assertNull("pool does contain the removed connection", pool.get("foo"));

    pool.add("foo", connection);
    pool.close();

    assertNull("pool does contain the added connection afer close", pool.get("foo"));
  }

  @Test
  public void testPoolClose() {

    final IByteStreamConnection connection0 = EasyMock.createNiceMock(IByteStreamConnection.class);

    final IByteStreamConnection connection1 = EasyMock.createNiceMock(IByteStreamConnection.class);

    connection0.close();
    EasyMock.expectLastCall().once();

    connection1.close();
    EasyMock.expectLastCall().once();

    EasyMock.replay(connection0, connection1);

    pool.open();

    pool.add("foo0", connection0);
    pool.add("foo1", connection1);

    pool.close();

    assertNull("connection was not removed on close", pool.remove("foo0"));
    assertNull("connection was not removed on close", pool.remove("foo1"));

    // close twice to ensure close is only called once
    pool.close();

    EasyMock.verify(connection0, connection1);
  }
}
