package de.fu_berlin.inf.dpp.stf.client.util;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.net.JID;

public interface Constants {

    /**********************************************
     * 
     * test data
     * 
     **********************************************/
    public final static JID TEST_JID = new JID(
        "edna_stf@saros-con.imp.fu-berlin.de/" + Saros.RESOURCE);
    public final static JID TEST_JID2 = new JID(
        "dave_stf@saros-con.imp.fu-berlin.de/" + Saros.RESOURCE);
    /* test data for modifying account */
    public final static String SERVER = "saros-con.imp.fu-berlin.de";
    public final static String NEW_XMPP_JABBER_ID = "new_alice_stf@" + SERVER;
    public final static String REGISTERED_USER_NAME = "bob_stf";
    public static final String INVALID_SERVER_NAME = "saros-con";
    // need to change, if you want to test creatAccount
    public static JID JID_TO_CREATE = new JID(
        ("test3@saros-con.imp.fu-berlin.de/" + Saros.RESOURCE));
    public final static JID JID_TO_ADD = new JID(
        ("bob_stf@" + SERVER + "/" + Saros.RESOURCE));
    public final static JID JID_TO_CHANGE = new JID(
        (NEW_XMPP_JABBER_ID + "/" + Saros.RESOURCE));
    public static String PASSWORD = "dddfffggg";
    public static String NO_MATCHED_REPEAT_PASSWORD = "dddfffggggg";
    /* Project name */
    public static final String PROJECT1 = "Foo_Saros1";
    public static final String PROJECT1_COPY = "copy_of_FOO_Saros1";

    public static final String PROJECT1_NEXT = "Foo_Saros 1";
    public static final String PROJECT2 = "Foo_Saros2";
    public static final String PROJECT3 = "Foo_Saros3";
    /* Folder name */
    public static final String FOLDER1 = "MyFolder";
    public static final String FOLDER2 = "MyFolder2";
    /* File */
    public static final String FILE1 = "MyFile.xml";
    public static final String FILE2 = "MyFile2.xml";
    public static final String FILE3 = "file.txt";
    public static final String[] PATH = { PROJECT1, FILE3 };
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
    public static final String CP1 = "test/resources/stf/" + CLS1 + ".java";
    public static final String CP2 = "test/resources/stf/" + CLS2 + ".java";
    public static final String CP3 = "test/resources/stf/" + CLS3 + ".java";
    public static final String CP1_CHANGE = "test/resources/stf/" + CLS1
        + "Change.java";
    public static final String CP2_CHANGE = "test/resources/stf/" + CLS2
        + "Change.java";
    /** modified in stf_test_project_copy */
    public static final String SVN_CLS1_REV4 = "2767";
    /** modified in stf_test_project_copy */
    public static final String SVN_CLS1_REV3 = "2737";
    /** copy from stf_test_project to stf_test_project_copy */
    public static final String SVN_CLS1_REV2 = "2736";
    /** Initial commit in stf_test_project. */
    public static final String SVN_CLS1_REV1 = "2735";
    public static final String SVN_CLS1_SWITCHED_URL = "http://saros-build.imp.fu-berlin.de/svn/saros/stf_tests/stf_test_project_copy/src/pkg/Test.java";
    public static final String SVN_CLS1_FULL_PATH = "/stf_test_project/src/pkg/Test.java";
    public static final String SVN_CLS1_SUFFIX = Constants.SVN_CLS1 + ".java";
    public static final String SVN_CLS1 = "Test";
    public static final String SVN_PKG = "pkg";
    public static final String SVN_PROJECT_URL_SWITCHED = Constants.SVN_REPOSITORY_URL
        + "/stf_tests/stf_test_project_copy";
    /* SVN infos */
    public static final String SVN_REPOSITORY_URL = "http://saros-build.imp.fu-berlin.de/svn/saros";
    public static final String SVN_PROJECT = "stf_test_project";
    public static final String SVN_PROJECT_COPY = "copy_of_stf_test_project";
    public static final String SVN_SUFFIX = "[stf_test/stf_test_project]";
    public static String SVN_PROJECT_PATH = System.getProperty("os.name")
        .matches("Mac OS X.*") ? "stf_tests/stf_test_project"
        : "/stf_tests/stf_test_project";
}
