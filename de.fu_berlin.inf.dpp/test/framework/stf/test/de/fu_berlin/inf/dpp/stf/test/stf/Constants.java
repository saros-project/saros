package de.fu_berlin.inf.dpp.stf.test.stf;

import de.fu_berlin.inf.dpp.SarosConstants;
import de.fu_berlin.inf.dpp.net.xmpp.JID;

public interface Constants {

  /**
   * ********************************************
   *
   * <p>test data
   *
   * <p>********************************************
   */
  public static final JID TEST_JID =
      new JID("edna_stf@saros-con.imp.fu-berlin.de/" + SarosConstants.RESOURCE);

  public static final JID TEST_JID2 =
      new JID("dave_stf@saros-con.imp.fu-berlin.de/" + SarosConstants.RESOURCE);
  /* test data for modifying account */
  public static final String SERVER = "saros-con.imp.fu-berlin.de";
  public static final String NEW_XMPP_JABBER_ID = "new_alice_stf@" + SERVER;
  public static final String REGISTERED_USER_NAME = "bob_stf";
  public static final String INVALID_SERVER_NAME = "saros-con";
  // need to change, if you want to test creatAccount
  public static JID JID_TO_CREATE =
      new JID(("test3@saros-con.imp.fu-berlin.de/" + SarosConstants.RESOURCE));
  public static final JID JID_TO_ADD =
      new JID(("bob_stf@" + SERVER + "/" + SarosConstants.RESOURCE));
  public static final JID JID_TO_CHANGE =
      new JID((NEW_XMPP_JABBER_ID + "/" + SarosConstants.RESOURCE));
  public static String PASSWORD = "dddfffggg";
  public static String NO_MATCHED_REPEAT_PASSWORD = "dddfffggggg";
  /* Project name */
  public static final String PROJECT1 = "Foo1_Saros";
  public static final String PROJECT1_COPY = "Foo1_Saros-copy";

  public static final String PROJECT1_NEXT = "Foo1_1_Saros";
  public static final String PROJECT2 = "Foo2_Saros";
  public static final String PROJECT3 = "Foo3_Saros";
  /* Folder name */
  public static final String FOLDER1 = "MyFolder";
  public static final String FOLDER2 = "MyFolder2";
  /* File */
  public static final String FILE1 = "MyFile.xml";
  public static final String FILE2 = "MyFile2.xml";
  public static final String FILE3 = "file.txt";

  /* Package name */
  public static final String PKG1 = "my.pkg";
  public static final String PKG2 = "my.pkg2";
  public static final String PKG3 = "my.pkg3";
  /* class name */
  public static final String CLS1 = "MyClass";
  public static final String CLS2 = "MyClass2";
  public static final String CLS3 = "MyClass3";
  /* class name with suffix */
  public static final String CLS1_SUFFIX = "MyClass.java";
  public static final String CLS2_SUFFIX = "MyClass2.java";
  public static final String CLS3_SUFFIX = "MyClass3.java";
  /* content path */
  public static final String CP1 = "test/resources/stf/" + CLS1 + ".java.txt";
  public static final String CP2 = "test/resources/stf/" + CLS2 + ".java.txt";
  public static final String CP3 = "test/resources/stf/" + CLS3 + ".java.txt";
  public static final String CP1_CHANGE = "test/resources/stf/" + CLS1 + "Change.java.txt";
  public static final String CP2_CHANGE = "test/resources/stf/" + CLS2 + "Change.java.txt";
}
