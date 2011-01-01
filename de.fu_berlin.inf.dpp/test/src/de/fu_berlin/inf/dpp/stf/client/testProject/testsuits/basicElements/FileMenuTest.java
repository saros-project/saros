package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.basicElements;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * File menu tests from GUI-Use-Cases-List Excel file.
 * 
 * @author Milosz Mazurkiewicz
 */
public class FileMenuTest {

    /**
     * @see SWTWorkbenchBot
     */
    private static SWTWorkbenchBot _bot;

    /**
     * Message if assertion fails
     */
    private String _assertionFailedMessage;

    /**
     * @see BeforeClass
     * @throws Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        _bot = new SWTWorkbenchBot();
    }

    /**
     * @see AfterClass
     * @throws Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        _bot = null;
    }

    /**
     * @see Before
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {

    }

    /**
     * @see After
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        cleanup();
    }

    /**
     * Create a new Pegasus project: File > New > Pegasus Project.
     */
    @Test
    public void createPegasusProjectTest() {
        try {
            createPegasusProject("MyTestProject1");
        } catch (WidgetNotFoundException e) {
            closeDialogWindow();
            throw e;
        }
    }

    /**
     * Create a new Pegasus project using wizard 1: File > New > Other... >
     * Pegasus > Pegasus Project.
     */
    @Test
    public void createNewPegasusProjectUsingWizard1Test() {
        try {
            _bot.menu("File").menu("New").menu("Project...").click();
            _assertionFailedMessage = "Window 'New Project' is not active";
            assertTrue(_assertionFailedMessage, _bot.shell("New Project")
                .isActive());
            _bot.tree().expandNode("Pegasus").select("Pegasus Project");
            _bot.button("Next >").click();
            _bot.textWithLabel("Project name:").setText("MyTestProject2_1");
            _bot.button("Finish").click();
            waitUntilShellCloses("New Pegasus Project");
        } catch (WidgetNotFoundException e) {
            closeDialogWindow();
            throw e;
        }
    }

    /**
     * Create a new Pegasus project using wizard 2: File > New > Project... >
     * Pegasus > Pegasus Project.
     */
    @Test
    public void createNewPegasusProjectUsingWizard2Test() {
        try {
            _bot.menu("File").menu("New").menu("Project...").click();
            _assertionFailedMessage = "Window 'New Project' is not active";
            assertTrue(_assertionFailedMessage, _bot.shell("New Project")
                .isActive());
            _bot.tree().expandNode("Pegasus").select("Pegasus Project");
            _bot.button("Next >").click();
            _bot.textWithLabel("Project name:").setText("MyTestProject2_1");
            _bot.button("Finish").click();
            waitUntilShellCloses("New Pegasus Project");
        } catch (WidgetNotFoundException e) {
            closeDialogWindow();
            throw e;
        }
    }

    /**
     * Create a new Pegasus project using tool bar button 'New'
     */
    @Test
    public void createNewPegasusProjectUsingToolBarButtonNewTest() {
        try {
            _bot.toolbarButtonWithTooltip("New").click();
            _assertionFailedMessage = "Window 'New' is not active";
            assertTrue(_assertionFailedMessage, _bot.shell("New").isActive());
            _bot.tree().expandNode("Pegasus").select("Pegasus Project").click();
            _bot.button("Next >").click();
            _bot.textWithLabel("Project name:").setText("MyTestProject2_2");
            _bot.button("Finish").click();
            waitUntilShellCloses("New Pegasus Project");
        } catch (WidgetNotFoundException e) {
            closeDialogWindow();
            throw e;
        }
    }

    /**
     * Creates a new general project: File > New > Other... > General > Project.
     */
    @Test
    public void createNewGeneralProjectTest() {
        try {
            _bot.menu("File").menu("New").menu("Project...").click();
            _assertionFailedMessage = "Window 'New Project' is not active";
            assertTrue(_assertionFailedMessage, _bot.shell("New Project")
                .isActive());
            _bot.tree().expandNode("General").select("Project");
            _bot.button("Next >").click();
            _bot.textWithLabel("Project name:").setText("MyTestProject3_1");
            _bot.button("Finish").click();
            waitUntilShellCloses("New Project");
        } catch (WidgetNotFoundException e) {
            closeDialogWindow();
            throw e;
        }
    }

    /**
     * Creates a new general project using tool bar button:<br />
     * tool bar button New > General > Project.
     */
    @Test
    public void createNewGeneralProjectUsingToolBarButtonNewTest() {
        try {
            _bot.toolbarButtonWithTooltip("New").click();
            _assertionFailedMessage = "Window 'New' is not active";
            assertTrue(_bot.shell("New").isActive());
            _bot.tree().expandNode("General").select("Project");
            _bot.button("Next >").click();
            _bot.textWithLabel("Project name:").setText("MyTestProject3_2");
            _bot.button("Finish").click();
            waitUntilShellCloses("New Project");
        } catch (WidgetNotFoundException e) {
            closeDialogWindow();
            throw e;
        }
    }

    /**
     * Create a new test hierarchy: File > New > Other... > Pegasus > Test
     * Hierarchy
     */
    @Test
    public void createNewTestHierarchyUsingWizzardTest() {
        try {
            createPegasusProject("MyTestProject4");

            createFileNewOtherWizzard("Pegasus", "Test Hierarchy",
                "MyTestProject4", "File Name", "MyTestHierarchy4_1.xml");
            assertTestHierarchyWasCreated("MyTestHierarchy4_1.xml");
        } catch (WidgetNotFoundException e) {
            closeDialogWindow();
            throw e;
        }
    }

    /**
     * Create a new test hierarchy: File > New > Test Hierarchy
     */
    @Test
    public void createNewTestHierarchyTest() {
        try {
            createPegasusProject("MyTestProject4");

            createFileNewWizzard("Test Hierarchy", "New Test Hierarchy",
                "MyTestProject4", "File Name", "MyTestHierarchy4_2.xml");
            assertTestHierarchyWasCreated("MyTestHierarchy4_2.xml");
        } catch (WidgetNotFoundException e) {
            closeDialogWindow();
            throw e;
        }
    }

    /**
     * Create a new test hierarchy: File > New Testhierarchy
     */
    @Test
    public void createNewTestHierarchyQuickTest() {
        try {
            createPegasusProject("MyTestProject4");

            _bot.menu("File").menu("New Testhierarchy").click();
            _assertionFailedMessage = "There is no editor tab 'New_Test'";
            assertTrue(_assertionFailedMessage, _bot.editorByTitle("New_Test")
                .getTitle().equals("New_Test"));
            _bot.menu("File").menu("Save As...").click();
            _assertionFailedMessage = "Window 'Save As...' is not active";
            assertTrue(_assertionFailedMessage, _bot.shell("Save As...")
                .isActive());
            _bot.tree().expandNode("MyTestProject4").select();
            _bot.textWithLabel("File name:").setText("MyTestHierarchy4_3.xml");
            _bot.button("Finish").click();
            assertTestHierarchyWasCreated("MyTestHierarchy4_3.xml");
        } catch (WidgetNotFoundException e) {
            closeDialogWindow();
            throw e;
        }
    }

    /**
     * Create a new system configuration file using wizard: File > New >
     * Other... > Pegasus > System Configuration
     */
    @Test
    public void createNewSystemConfigurationUsingWizzardTest() {
        try {
            createPegasusProject("MyTestProject5");

            _bot.menu("File").menu("New").menu("Other...").click();
            _assertionFailedMessage = "Window 'New' is not active";
            assertTrue(_assertionFailedMessage, _bot.shell("New").isActive());
            _bot.tree().expandNode("Pegasus").select("System Configuration");
            _bot.button("Next >").click();
            _assertionFailedMessage = "Window 'New System Configuration' is not active";
            assertTrue(_assertionFailedMessage,
                _bot.shell("New System Configuration").isActive());
            _bot.tree().expandNode("MyTestProject5").select();
            SWTBotText text = _bot.textWithLabel("File name:");
            text.setText("MySystemConfiguration5_1.xml");
            _bot.button("Finish").click();
            waitUntilShellCloses("New System Configuration");
            assertSystemConfigurationWasCreated("MySystemConfiguration5_1.xml");
        } catch (WidgetNotFoundException e) {
            closeDialogWindow();
            throw e;
        }
    }

    /**
     * Create a new system configuration file: File > New > System Configuration
     */
    @Test
    public void createNewSystemConfigurationTest() {
        try {
            createPegasusProject("MyTestProject5");

            createSystemConfiguration("MyTestProject5",
                "MySystemConfiguration5_2.xml");
        } catch (WidgetNotFoundException e) {
            closeDialogWindow();
            throw e;
        }
    }

    /**
     * Create a new folder using wizard: File > New > Other... > General >
     * Folder
     */
    @Test
    public void createNewFolderUsingWizardTest() {
        try {
            createPegasusProject("MyTestProject6");

            createFileNewOtherWizzard("General", "Folder", "MyTestProject6",
                "Folder name:", "MyTestFolder6_2");
            assertFolderWasCreated("MyTestFolder6_2");
        } catch (WidgetNotFoundException e) {
            closeDialogWindow();
            throw e;
        }
    }

    /**
     * Create a new folder: File > New > Folder
     */
    @Test
    public void createNewFolderTest() {
        try {
            createPegasusProject("MyTestProject6");

            createFileNewWizzard("Folder", "New Folder", "MyTestProject6",
                "Folder name:", "MyTestFolder6_1");
            assertFolderWasCreated("MyTestFolder6_1");
        } catch (WidgetNotFoundException e) {
            closeDialogWindow();
            throw e;
        }
    }

    /**
     * Create New File: File > New > Other... > General > File
     */
    @Test
    public void createNewFileTest() {
        try {
            createPegasusProject("MyTestProject7");

            createFileNewOtherWizzard("General", "File", "MyTestProject7",
                "File name:", "MyTestFile7.xml");
            _assertionFailedMessage = "There is no editor tab 'MyTestFile7.xml'";
            assertTrue(
                _assertionFailedMessage,
                _bot.editorByTitle("MyTestFile7.xml").getTitle()
                    .equals("MyTestFile7.xml"));
            _bot.editorByTitle("MyTestFile7.xml").toTextEditor()
                .setText("MyTestFile7.xml");
            _assertionFailedMessage = "'MyTestFile7.xml' is not an active editor";
            assertTrue(_assertionFailedMessage,
                _bot.editorByTitle("MyTestFile7.xml").isActive());
            _bot.editorByTitle("MyTestFile7.xml").saveAndClose();
        } catch (WidgetNotFoundException e) {
            closeDialogWindow();
            throw e;
        }
    }

    /**
     * Create New Untitled Text File: File > New > Other... > General > Untitled
     * Text File
     */
    @Test
    public void createNewUntitledTextFileTest() {
        try {
            createPegasusProject("MyTestProject7");

            _bot.menu("File").menu("New").menu("Other...").click();
            _assertionFailedMessage = "Window 'New' is not active";
            assertTrue(_assertionFailedMessage, _bot.shell("New").isActive());
            _bot.tree().expandNode("General").select("Untitled Text File");
            _bot.button("Finish").click();
            /* Write something to untitled text file, save and close it */
            _assertionFailedMessage = "There is no editor tab 'Untitled 1'";
            assertTrue(_assertionFailedMessage, _bot
                .editorByTitle("Untitled 1").getTitle().equals("Untitled 1"));
            _bot.editorByTitle("Untitled 1").toTextEditor()
                .setText("Untitled 1");
            _bot.menu("File").menu("Save").click();
            _assertionFailedMessage = "Window 'Save As' is not active";
            assertTrue(_assertionFailedMessage, _bot.shell("Save As")
                .isActive());
            _bot.tree().expandNode("MyTestProject7").select();
            _bot.textWithLabel("File name:").setText("MyUntitledFile7.txt");
            _bot.button("OK").click();
            _bot.editorByTitle("MyUntitledFile7.txt").close();
        } catch (WidgetNotFoundException e) {
            closeDialogWindow();
            throw e;
        }
    }

    /**
     * Closing tabs in editor using 'File > Close' and 'File > Close All'
     */
    @Test
    public void closeAndCloseAllTest() {
        try {
            createPegasusProject("MyTestProject8");

            /* Create 10 test hierarchy files */
            for (int i = 0; i < 10; i++) {
                createFileNewOtherWizzard("Pegasus", "Test Hierarchy",
                    "MyTestProject8", "File Name", "MyTestHierarchy8_" + i
                        + ".xml");
            }
            _assertionFailedMessage = "Number of editors should be 10";
            assertEquals(_assertionFailedMessage, 10, _bot.editors().size());

            /* Close last tab */
            _bot.menu("File").menu("Close").click();
            _assertionFailedMessage = "Number of editors should be 9";
            assertEquals(_assertionFailedMessage, 9, _bot.editors().size());

            /* Close first tab */
            _bot.editorByTitle("MyTestHierarchy8_0.xml").show();
            _bot.menu("File").menu("Close").click();
            _assertionFailedMessage = "Number of editors should be 8";
            assertEquals(8, _bot.editors().size());

            /* Close all tabs */
            _bot.menu("File").menu("Close All").click();
            _assertionFailedMessage = "All editors should be closed";
            assertEquals(_assertionFailedMessage, 0, _bot.editors().size());
        } catch (WidgetNotFoundException e) {
            closeDialogWindow();
            throw e;
        }
    }

    /**
     * Save active window in test editor: File > Save
     */
    @Test
    public void saveTest() {
        try {
            createPegasusProject("MyTestProject9");

            createSystemConfiguration("MyTestProject9",
                "MySystemConfiguration9.xml");

            _assertionFailedMessage = "No editor tab named 'MySystemConfiguration9.xml'";
            assertTrue(_assertionFailedMessage,
                _bot.editorByTitle("MySystemConfiguration9.xml").getTitle()
                    .equals("MySystemConfiguration9.xml"));
            assertSaveDisabled();

            _bot.editorByTitle("MySystemConfiguration9.xml").toTextEditor()
                .setText("Make file dirty");
            assertSaveEnabled();
            _bot.menu("File").menu("Save").click();
            assertSaveDisabled();
        } catch (WidgetNotFoundException e) {
            closeDialogWindow();
            throw e;
        }
    }

    /**
     * Save active window in test editor: tool bar button Save
     */
    @Test
    public void saveUsingToolBarButtonSaveTest() {
        try {
            createPegasusProject("MyTestProject9");

            createSystemConfiguration("MyTestProject9",
                "MySystemConfiguration9.xml");

            _assertionFailedMessage = "No editor tab named 'MySystemConfiguration9.xml'";
            assertTrue(_assertionFailedMessage,
                _bot.editorByTitle("MySystemConfiguration9.xml").getTitle()
                    .equals("MySystemConfiguration9.xml"));
            assertSaveDisabled();

            _bot.editorByTitle("MySystemConfiguration9.xml").toTextEditor()
                .setText("Make file dirty");
            assertSaveEnabled();
            _bot.toolbarButtonWithTooltip("Save (Ctrl+S)").click();
            assertSaveDisabled();
        } catch (WidgetNotFoundException e) {
            closeDialogWindow();
            throw e;
        }
    }

    /**
     * Saving all windows in editor with File > Save All. Creates 5 dirty system
     * configuration files, performs File > Save All, and checks if all files in
     * editor were saved.
     */
    @Test
    public void saveAllTest() {
        try {
            createPegasusProject("MyTestProject11");

            for (int i = 0; i < 5; i++) {
                createSystemConfiguration("MyTestProject11",
                    "MySystemConfiguration11_" + i + ".xml");
                _bot.editorByTitle("MySystemConfiguration11_" + i + ".xml")
                    .toTextEditor().setText("Make file dirty");
                assertSaveEnabled();
            }

            _bot.menu("File").menu("Save All").click();

            for (int i = 0; i < 5; i++) {
                _bot.editorByTitle("MySystemConfiguration11_" + i + ".xml")
                    .setFocus();
                assertSaveDisabled();
            }
            _bot.menu("File").menu("Close All").click();
        } catch (WidgetNotFoundException e) {
            closeDialogWindow();
            throw e;
        }
    }

    /**
     * Saving all windows in editor with tool bar button 'Save All'. Creates 5
     * dirty system configuration files, clicks 'Save All' tool bar button, and
     * checks if all files in editor were saved.
     */
    @Test
    public void saveAllWithToolBarButtonSaveAllTest() {
        try {
            createPegasusProject("MyTestProject11");

            for (int i = 0; i < 5; i++) {
                createSystemConfiguration("MyTestProject11",
                    "MySystemConfiguration11_" + i + ".xml");
                _bot.editorByTitle("MySystemConfiguration11_" + i + ".xml")
                    .toTextEditor().setText("Make file dirty");
                assertSaveEnabled();
            }

            _bot.toolbarButtonWithTooltip("Save All (Ctrl+Shift+S)").click();

            for (int i = 0; i < 5; i++) {
                _bot.editorByTitle("MySystemConfiguration11_" + i + ".xml")
                    .setFocus();
                assertSaveDisabled();
            }
            _bot.menu("File").menu("Close All").click();
        } catch (WidgetNotFoundException e) {
            closeDialogWindow();
            throw e;
        }
    }

    /**
     * Save active window in editor with another name using File > Save As...
     */
    @Test
    public void saveAsTest() {
        try {
            createPegasusProject("MyTestProject12");

            createSystemConfiguration("MyTestProject12",
                "MySystemConfiguration12");

            /* Modify and save file using File > Save As... */
            _bot.editorByTitle("MySystemConfiguration12.xml")
                .toTextEditor()
                .setText(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                        + "<systemConfiguration xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
                        + "  <systemUnderTests/>\n" + "  <testEquipments/>\n"
                        + "  <physicalLinks/>\n" + "  <properties>\n"
                        + "    <property name=\"runCounter\" value=\"1\"/>\n"
                        + "  </properties>\n" + "  </systemConfiguration>");
            _bot.menu("File").menu("Save As...").click();
            _assertionFailedMessage = "Window 'Save As' is not active";
            assertTrue(_bot.shell("Save As").isActive());
            _bot.tree().select("MyTestProject12");
            _bot.textWithLabel("File name:").setText(
                "MySystemConfiguration12_1");
            _bot.button("OK").click();
            assertSaveDisabled();

            /*
             * Check if saved file MySystemConfiguration12_1 has the right
             * contents
             */
            _assertionFailedMessage = "'MySystemConfiguration12_1.xml' has different contents than expected";
            assertEquals(
                _assertionFailedMessage,
                _bot.editorByTitle("MySystemConfiguration12_1.xml")
                    .toTextEditor().getText(),
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    + "<systemConfiguration xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
                    + "  <systemUnderTests/>\n" + "  <testEquipments/>\n"
                    + "  <physicalLinks/>\n" + "  <properties>\n"
                    + "    <property name=\"runCounter\" value=\"1\"/>\n"
                    + "  </properties>\n" + "  </systemConfiguration>");
        } catch (WidgetNotFoundException e) {
            closeDialogWindow();
            throw e;
        }
    }

    /**
     * Save active window in test Editor with another name using tool bar button
     * Save As...
     */
    @Test
    public void saveAsWithToolBarButtonSaveAsTest() {
        try {
            createPegasusProject("MyTestProject12");

            createSystemConfiguration("MyTestProject12",
                "MySystemConfiguration12");

            /* Modify and save to another file using tool bar button Save As... */
            _bot.editorByTitle("MySystemConfiguration12.xml")
                .toTextEditor()
                .setText(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                        + "<systemConfiguration xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
                        + "  <systemUnderTests/>\n" + "  <testEquipments/>\n"
                        + "  <physicalLinks/>\n" + "  <properties>\n"
                        + "    <property name=\"runCounter\" value=\"2\"/>\n"
                        + "  </properties>\n" + "  </systemConfiguration>");
            _bot.menu("File").menu("Save As...").click();
            _assertionFailedMessage = "Window 'Save As' is not active";
            assertTrue(_bot.shell("Save As").isActive());
            _bot.tree().select("MyTestProject12");
            _bot.textWithLabel("File name:").setText(
                "MySystemConfiguration12_2");
            _bot.button("OK").click();
            assertSaveDisabled();

            /*
             * Check if saved file MySystemConfiguration12_3 has the right
             * contents
             */
            _assertionFailedMessage = "'MySystemConfiguration12_2.xml' has different contents than expected";
            assertEquals(
                _assertionFailedMessage,
                _bot.editorByTitle("MySystemConfiguration12_2.xml")
                    .toTextEditor().getText(),
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    + "<systemConfiguration xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
                    + "  <systemUnderTests/>\n" + "  <testEquipments/>\n"
                    + "  <physicalLinks/>\n" + "  <properties>\n"
                    + "    <property name=\"runCounter\" value=\"2\"/>\n"
                    + "  </properties>\n" + "  </systemConfiguration>");
        } catch (WidgetNotFoundException e) {
            closeDialogWindow();
            throw e;
        }
    }

    /**
     * Moving resources to selected destination in workspace using File >
     * Move...
     */
    @Test
    public void moveTest() {
        try {
            createPegasusProject("MyTestProject13_1");
            createSystemConfiguration("MyTestProject13_1",
                "MySystemConfiguration13_1.xml");
            createPegasusProject("MyTestProject13_2");

            /*
             * Move is enabled only when focus is on Navigator view with some
             * items selected
             */
            _bot.viewByTitle("Parameter").setFocus();
            _assertionFailedMessage = "'File > Move...' option should be disabled";
            assertFalse(_assertionFailedMessage,
                _bot.menu("File").menu("Move...").isEnabled());
            _bot.viewByTitle("Navigator").setFocus();
            _assertionFailedMessage = "'File > Move...' option should be enabled";
            assertTrue(_assertionFailedMessage,
                _bot.menu("File").menu("Move...").isEnabled());

            moveResource("MySystemConfiguration13_1.xml", "MyTestProject13_1",
                "MyTestProject13_2");
        } catch (WidgetNotFoundException e) {
            closeDialogWindow();
            throw e;
        }
    }

    /**
     * Rename resources (project, system configuration file, test hierarchy
     * file, folder) using: File > Rename...
     */
    @Test
    public void renameTest() {
        try {
            createPegasusProject("MyTestProject14");
            createSystemConfiguration("MyTestProject14",
                "MySystemConfiguration.xml");
            createFileNewWizzard("Test Hierarchy", "New Test Hierarchy",
                "MyTestProject14", "File Name", "MyTestHierarchy.xml");
            createFileNewWizzard("Folder", "New Folder", "MyTestProject14",
                "Folder name:", "MyFolder");

            renameProject("MyTestProject14", "MyTestProject14_Renamed1");
            renameResource("MyTestProject14_Renamed1",
                "MySystemConfiguration.xml",
                "MySystemConfiguration_Renamed1.xml");
            renameResource("MyTestProject14_Renamed1", "MyTestHierarchy.xml",
                "MyTestHierarchy_Renamed1.xml");
            renameResource("MyTestProject14_Renamed1", "MyFolder",
                "MyFolder_Renamed1");

            /* You cannot rename to already existing file name */
            _bot.tree(1).expandNode("MyTestProject14_Renamed1")
                .getNode("MySystemConfiguration_Renamed1.xml")
                .contextMenu("Rename...").click();
            _assertionFailedMessage = "Window 'Rename Resource' is not active";
            assertTrue(_assertionFailedMessage, _bot.shell("Rename Resource")
                .isActive());
            _bot.textWithLabel("New name:").setText(
                "MyTestHierarchy_Renamed1.xml");
            _assertionFailedMessage = "'OK' button should be disabled";
            assertFalse(_bot.button("OK").isEnabled());
            _bot.button("Cancel").click();
        } catch (WidgetNotFoundException e) {
            closeDialogWindow();
            throw e;
        }
    }

    /**
     * Check if Refresh button works: File > Refresh.
     * 
     * @throws IOException
     */
    @Test
    public void refreshTest() throws IOException {
        try {
            createPegasusProject("MyTestProject15");
            createSystemConfiguration("MyTestProject15",
                "MySystemConfiguration15.xml");

            /*
             * modify existing file, refresh and check if editor contents
             * changed
             */
            String directory = getWorkspacePath() + "MyTestProject15";
            writeToFile(
                directory,
                "MySystemConfiguration15.xml",
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    + "<systemConfiguration xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
                    + "  <systemUnderTests/>\n" + "  <testEquipments/>\n"
                    + "  <physicalLinks/>\n" + "  <properties>\n"
                    + "    <property name=\"runCounter\" value=\"2\"/>\n"
                    + "  </properties>\n" + "  </systemConfiguration>");
            _bot.menu("File").menu("Refresh").click();
            _assertionFailedMessage = "'MySystemConfiguration15.xml' has different contents than expected";
            assertEquals(
                _assertionFailedMessage,
                _bot.editorByTitle("MySystemConfiguration15.xml")
                    .toTextEditor().getText(),
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    + "<systemConfiguration xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
                    + "  <systemUnderTests/>\n" + "  <testEquipments/>\n"
                    + "  <physicalLinks/>\n" + "  <properties>\n"
                    + "    <property name=\"runCounter\" value=\"2\"/>\n"
                    + "  </properties>\n" + "  </systemConfiguration>");

            /*
             * create new file, refresh and check if it appears in Navigator
             * view
             */
            createFile(directory, "MySystemConfiguration15_1.xml");
            writeToFile(
                directory,
                "MySystemConfiguration15_1.xml",
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    + "<systemConfiguration xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
                    + "  <systemUnderTests/>\n" + "  <testEquipments/>\n"
                    + "  <physicalLinks/>\n" + "  <properties>\n"
                    + "    <property name=\"runCounter\" value=\"3\"/>\n"
                    + "  </properties>\n" + "  </systemConfiguration>");
            _bot.viewByTitle("Navigator").setFocus();
            _bot.tree(1).select("MyTestProject15");
            _bot.menu("File").menu("Refresh").click();
            _assertionFailedMessage = "'MySystemConfiguration15_1.xml' was expected in Navigator";
            assertTrue(
                _assertionFailedMessage,
                _bot.tree(1).expandNode("MyTestProject15")
                    .select("MySystemConfiguration15_1.xml") != null);
        } catch (WidgetNotFoundException e) {
            closeDialogWindow();
            throw e;
        }
    }

    /**
     * Exports project to archive file using: File > Export... > General >
     * Archive File.
     * 
     * @throws IOException
     */
    @Test
    public void exportToArchiveFileTest() throws IOException {
        try {
            String directory = getWorkspacePath();

            createPegasusProject("MyTestProject16");
            createSystemConfiguration("MyTestProject16",
                "MySystemConfiguration16.xml");

            _bot.menu("File").menu("Export...").click();
            _assertionFailedMessage = "Window 'Export' should be active";
            assertEquals(_assertionFailedMessage, _bot.activeShell().getText(),
                "Export");
            _bot.tree().expandNode("General").select("Archive File");
            _bot.button("Next >").click();
            _assertionFailedMessage = "'Finish' button should be disabled";
            assertFalse(_assertionFailedMessage, _bot.button("Finish")
                .isEnabled());
            _bot.button("Select All").click();
            _bot.comboBoxWithLabel("To archive file:").setText(
                directory + "ExportedFile");
            _bot.button("Finish").click();
            waitUntilShellCloses("Export");
            _assertionFailedMessage = "File 'ExportedFile.zip' does not exist";
            assertTrue(isFileCreated(directory, "ExportedFile.zip"));
        } catch (WidgetNotFoundException e) {
            closeDialogWindow();
            throw e;
        }
    }

    /**
     * Exports project to file system using File > Export... > General > File
     * System
     * 
     * @throws IOException
     */
    @Test
    public void exportToFileSystemTest() throws IOException {
        try {
            String directory = getWorkspacePath();

            createPegasusProject("MyTestProject18");
            createSystemConfiguration("MyTestProject18",
                "MySystemConfiguration18.xml");

            _bot.menu("File").menu("Export...").click();
            _assertionFailedMessage = "Window 'Export' should be active";
            assertEquals(_assertionFailedMessage, _bot.activeShell().getText(),
                "Export");
            _bot.tree().expandNode("General").select("File System");
            _bot.button("Next >").click();
            _assertionFailedMessage = "'Finish' button should be disabled";
            assertFalse(_assertionFailedMessage, _bot.button("Finish")
                .isEnabled());
            _bot.button("Select All").click();
            _bot.comboBoxWithLabel("To directory:").setText(
                directory + "ExportedToFileSystem");
            _bot.button("Finish").click();
            if (_bot.activeShell().getText().equals("Question"))
                _bot.button("Yes").click();
            waitUntilShellCloses("Export");
            directory = directory + "ExportedToFileSystem"
                + System.getProperty("file.separator") + "MyTestProject18";
            _assertionFailedMessage = "File '.project' does not exist";
            assertTrue(_assertionFailedMessage,
                isFileCreated(directory, ".project"));
            _assertionFailedMessage = "File 'MySystemConfiguration18.xml' does not exist";
            assertTrue(_assertionFailedMessage,
                isFileCreated(directory, "MySystemConfiguration18.xml"));
        } catch (WidgetNotFoundException e) {
            closeDialogWindow();
            throw e;
        }
    }

    /**
     * Exports test hierarchy file to PDF and CSV using File > Export... >
     * Pegasus > Pegasus Test Material Elements
     * 
     * @throws IOException
     */
    @Test
    public void exportPegasusTestMaterialElementsTest() throws IOException {
        try {
            String directory = getWorkspacePath();

            createPegasusProject("MyTestProject19");
            createFileNewWizzard("Test Hierarchy", "New Test Hierarchy",
                "MyTestProject19", "File Name", "MyTestHierarchy19.xml");
            assertTestHierarchyWasCreated("MyTestHierarchy19.xml");

            /* Create test hierarchy with TpPing test procedure */
            _bot.menu("File").menu("Close").click();
            _bot.tree(1).expandNode("MyTestProject19")
                .expandNode("MyTestHierarchy19.xml").contextMenu("Text Editor")
                .click();
            _bot.editorByTitle("MyTestHierarchy19.xml")
                .toTextEditor()
                .setText(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                        + "<TestHierarchy xmlns=\"testConfiguration2.xml.pegasus\" format=\"10.0.0.20090917113828R\" lastChanged=\"2009-09-30T11:30:10.204+02:00\">"
                        + "<Group>"
                        + "<General loglevel=\"LOG\" name=\"New_Group\" id=\"0\" shortdescr=\"\" detaildescr=\"\"/>"
                        + "<Procedure class=\"pegasus.testProcedure.TpPing\">"
                        + "<General loglevel=\"LOG\" name=\"TpPing\" id=\"0\" shortdescr=\"TpPing - checks whether a host is reachable\" detaildescr=\"Instance of TpPing&#xA;Sending a ping() via the runtime platform.&#xA;The system's output can be saved in a separate file (recommended).&#xA;For Windows systems the output is parsed for string &quot;timed out&quot; (time-out message on&#xA;English platform) and &quot;Zeit???berschreitung&quot; (German platform) per default.&#xA;If one of these is contained, the TP is set to failed.&#xA;Another search string can be added to the string parameter  &quot;_timeOutString&quot;.&#xA;If any of these strings is found in the return string of the ping() command,&#xA;the verdict &quot;failed&quot; is set.Other strings, that are parsed for are &quot;could not find host&quot; and &quot;Bad value for option&quot;.&#xA;For these the TP terminates.&#xA;This type of strings can be extended, by adding a string to to the string parameter &quot;_terminationString&quot;&#xA;&#xA;The output from other operating systems is not parsed, because currently no testing is possible.&#xA;&#xA;CheckTp uses the checking mechanism of the Java class InetAddress,&#xA;which in case of a hostname, checks if the DNS service returns a IP Adress,&#xA;or in case of a given IP adress, if it has a valid form.&#xA;&#xA;The following platforms are recognized: Windows, Unix/Linux, SunOS\"><ParameterList><Parameter value=\"localhost\" description=\"Host IP resp. name, to which the ping should be sent.&#xA;\" readonly=\"false\" dynamic=\"false\" type=\"String\" name=\"_host\" modtype=\"ADD_NOTHING\"/><Parameter value=\"1\" description=\"Number of echo requests to be sent.&#xA;Must be larger than zero and less than 2000000001.&#xA;Currently only used for Windows operating system (Range: 1 ... 2000000000)\" readonly=\"false\" dynamic=\"false\" type=\"IntegerRange\" name=\"_echoes\" modtype=\"ADD_NOTHING\"/><Parameter value=\"10\" description=\"The time-out is used as option for the platform ping and it sets a time after which an unsuccessful try is stopped). No Java Timer is used for it.&#xA;ON WINDOWS: Time-out in  milliseconds to wait for each reply.&#xA;ON LINUX  : Deadline - a timeout in seconds, before ping exits.&#xA;Negative values are treated as, if not set.&#xA;Default value 10 milliseconds for Windows and 10 seconds for Linux!\" readonly=\"false\" dynamic=\"false\" type=\"Integer\" name=\"_timeout\" modtype=\"ADD_NOTHING\"/><Parameter value=\"false\" description=\"If set to &quot;true&quot; the output of the ping is saved in an extra file,&#xA;in the log directory. If set to &quot;false&quot;,&#xA;the output is written to the log.\" readonly=\"false\" dynamic=\"false\" type=\"Boolean\" name=\"_saveInFile\" modtype=\"ADD_NOTHING\"/><Parameter value=\"\" description=\"Additional string that causes a termination of the test procedure if found in return string (case sensitive).\" readonly=\"false\" dynamic=\"false\" type=\"String\" name=\"_terminationString\" modtype=\"ADD_NOTHING\"/><Parameter value=\"\" description=\"Additional string that causes a verdict &quot;failed&quot; of the test procedure if found in return string (case sensitive).\" readonly=\"false\" dynamic=\"false\" type=\"String\" name=\"_timeOutString\" modtype=\"ADD_NOTHING\"/>"
                        + "</ParameterList></General></Procedure>"
                        + "</Group></TestHierarchy>");
            _bot.menu("File").menu("Save").click();
            _bot.menu("File").menu("Close").click();
            /* switch back Test Editor as default editor */
            _bot.tree(1).expandNode("MyTestProject19")
                .expandNode("MyTestHierarchy19.xml").contextMenu("Test Editor")
                .click();
            _bot.menu("File").menu("Close").click();

            /* Export test hierarchy to CSV */
            _bot.menu("File").menu("Export...").click();
            _assertionFailedMessage = "Window 'Export' should be active";
            assertEquals(_assertionFailedMessage, _bot.activeShell().getText(),
                "Export");
            _bot.tree().expandNode("Pegasus")
                .select("Pegasus Test Material Elements");
            _bot.button("Next >").click();
            _assertionFailedMessage = "Window 'Export Pegasus Resources' should be active";
            assertTrue(_assertionFailedMessage, _bot.activeShell().getText()
                .equals("Export Pegasus Resources"));
            _assertionFailedMessage = "'Finish' button should be disabled";
            assertFalse(_assertionFailedMessage, _bot.button("Finish")
                .isEnabled());
            _bot.comboBoxWithLabel("To Directory: ").setText(
                directory + "PegasusTestMaterialElements");
            _bot.button("Next >").click();
            _bot.radio("CSV").click();
            _bot.button("Finish").click();
            waitUntilShellCloses("Export Pegasus Resources");
            _assertionFailedMessage = "File 'MyTestHierarchy19.csv' was not created";
            assertTrue(
                _assertionFailedMessage,
                isFileCreated(directory + "PegasusTestMaterialElements",
                    "MyTestHierarchy19.csv"));

            /* Export test hierarchy to PDF */
            _bot.menu("File").menu("Export...").click();
            _assertionFailedMessage = "Window 'Export' should be active";
            assertEquals(_assertionFailedMessage, _bot.activeShell().getText(),
                "Export");
            _bot.tree().expandNode("Pegasus")
                .select("Pegasus Test Material Elements");
            _bot.button("Next >").click();
            _assertionFailedMessage = "Window 'Export Pegasus Resources' should be active";
            assertTrue(_assertionFailedMessage, _bot.activeShell().getText()
                .equals("Export Pegasus Resources"));
            _assertionFailedMessage = "'Finish' button should be disabled";
            assertFalse(_assertionFailedMessage, _bot.button("Finish")
                .isEnabled());
            _bot.comboBoxWithLabel("To Directory: ").setText(
                directory + "PegasusTestMaterialElements");
            _bot.button("Next >").click();
            _bot.radio("PDF").click();
            _bot.button("Finish").click();
            waitUntilShellCloses("Export Pegasus Resources");
            _assertionFailedMessage = "File 'MyTestHierarchy19.csv' was not created";
            assertTrue(
                _assertionFailedMessage,
                isFileCreated(directory + "PegasusTestMaterialElements",
                    "MyTestHierarchy19.pdf"));
        } catch (WidgetNotFoundException e) {
            closeDialogWindow();
            throw e;
        }
    }

    /**
     * Imports project from file system using File > Import > General > Existing
     * Projects into Workspace
     * 
     * @throws IOException
     */
    @Test
    public void importProjectTest() throws IOException {
        try {
            String directory = getWorkspacePath();

            createPegasusProject("MyTestProject20");
            createSystemConfiguration("MyTestProject20",
                "MySystemConfiguration20.xml");

            /* delete project without removing it from file system */
            _bot.tree(1).getAllItems()[0].contextMenu("Delete").click();
            if (_bot.shell("Delete Resources").isActive()) {
                _bot.checkBox().deselect();
                _bot.button("OK").click();
                waitUntilShellCloses("Delete Resources");
            }

            /* import project from workspace */
            _bot.menu("File").menu("Import...").click();
            _assertionFailedMessage = "Window 'Import' should be active";
            assertEquals(_assertionFailedMessage, _bot.activeShell().getText(),
                "Import");
            _bot.tree().expandNode("General")
                .select("Existing Projects into Workspace");
            _bot.button("Next >").click();
            assertTrue(_assertionFailedMessage, _bot.activeShell().getText()
                .equals("Import"));
            _assertionFailedMessage = "'Finish' button should be disabled";
            assertFalse(_assertionFailedMessage, _bot.button("Finish")
                .isEnabled());
            _bot.radio(0).click();
            _bot.text(0).setText(directory + "MyTestProject20");
            _bot.button("Refresh").click();
            _bot.button("Finish").click();
            waitUntilShellCloses("Import");
            _assertionFailedMessage = "'MyTestProject20' should be visible in navigator";
            assertTrue(_bot.tree(1).expandNode("MyTestProject20").isVisible());
        } catch (WidgetNotFoundException e) {
            closeDialogWindow();
            throw e;
        }
    }

    /**
     * Imports zipped project from file system using File > Import... > General
     * > Archive File
     * 
     * @throws IOException
     */
    @Test
    public void importZippedProjectTest() throws IOException {
        try {
            String directory = getWorkspacePath();

            createPegasusProject("MyTestProject20");
            createSystemConfiguration("MyTestProject20",
                "MySystemConfiguration20.xml");

            /* export project to zip file */
            _bot.menu("File").menu("Export...").click();
            _assertionFailedMessage = "Window 'Export' should be active";
            assertEquals(_assertionFailedMessage, _bot.activeShell().getText(),
                "Export");
            _bot.tree().expandNode("General").select("Archive File");
            _bot.button("Next >").click();
            _assertionFailedMessage = "'Finish' button should be disabled";
            assertFalse(_assertionFailedMessage, _bot.button("Finish")
                .isEnabled());
            _bot.button("Select All").click();
            _bot.comboBoxWithLabel("To archive file:").setText(
                directory + "ZippedMyTestProject20");
            _bot.button("Finish").click();
            waitUntilShellCloses("Export");
            _assertionFailedMessage = "File 'ZippedMyTestProject20.zip' does not exist";
            assertTrue(isFileCreated(directory, "ZippedMyTestProject20.zip"));

            /* delete project from Navigator view and file system */
            deleteAllFromNavigator();

            /* import project from workspace */
            _bot.menu("File").menu("Import...").click();
            _assertionFailedMessage = "Window 'Import' should be active";
            assertEquals(_assertionFailedMessage, _bot.activeShell().getText(),
                "Import");
            _bot.tree().expandNode("General")
                .select("Existing Projects into Workspace");
            _bot.button("Next >").click();
            assertTrue(_assertionFailedMessage, _bot.activeShell().getText()
                .equals("Import"));
            _assertionFailedMessage = "'Finish' button should be disabled";
            assertFalse(_assertionFailedMessage, _bot.button("Finish")
                .isEnabled());
            _bot.radio(1).click();
            _bot.text(1).setText(directory + "ZippedMyTestProject20.zip");
            _bot.button("Refresh").click();
            _bot.sleep(1000);
            _bot.button("Finish").click();
            waitUntilShellCloses("Import");
            _bot.viewByTitle("Navigator").setFocus();
            _assertionFailedMessage = "'MyTestProject20' should be visible in navigator";
            assertTrue(_bot.tree(1).expandNode("MyTestProject20").isVisible());
        } catch (WidgetNotFoundException e) {
            closeDialogWindow();
            throw e;
        }
    }

    /**
     * Create workspace path. Works only when 'Pegasus RCP' is an active window.
     * 
     * @return
     */
    private String getWorkspacePath() {
        String workspacePath;
        waitUntilShellIsActive("Pegasus RCP");
        _bot.menu("File").menu("Switch Workspace").menu("Other...").click();
        _assertionFailedMessage = "Window 'Workspace Launcher' should be active";
        assertTrue(_assertionFailedMessage, _bot.activeShell().getText()
            .equals("Workspace Launcher"));
        workspacePath = _bot.comboBoxWithLabel("Workspace:").getText();
        workspacePath = workspacePath + System.getProperty("file.separator");
        _bot.button("Cancel").click();
        return workspacePath;
    }

    /**
     * Create file in the file system.
     * 
     * @param directory
     *            destination directory
     * @param fileName
     *            name of the file to be created
     * @throws IOException
     */
    private void createFile(String directory, String fileName)
        throws IOException {
        File f = new File(directory, fileName);
        if (!f.exists()) {
            f.createNewFile();
        }
    }

    /**
     * Verifies if the file was created in the file system.
     * 
     * @param directory
     *            directory of the file
     * @param fileName
     *            name of the file
     * @return true if file exists, false if file not found
     * @throws IOException
     */
    private boolean isFileCreated(String directory, String fileName)
        throws IOException {
        File f = new File(directory, fileName);
        return (f.exists()) ? true : false;
    }

    /**
     * Write contents to previously created file.
     * 
     * @param directory
     *            destination directory
     * @param fileName
     *            name of the file
     * @param fileContents
     *            contents of the file
     * @throws IOException
     */
    private void writeToFile(String directory, String fileName,
        String fileContents) throws IOException {
        File f = new File(directory, fileName);
        FileOutputStream fop = new FileOutputStream(f);

        if (f.exists()) {
            String str = fileContents;
            fop.write(str.getBytes());
            fop.flush();
            fop.close();
        }
    }

    /**
     * Closes dialog window if there is any (when 'Pegasus RCP' is not currently
     * active shell).
     */
    private void closeDialogWindow() {
        String activeShellName;
        if ((activeShellName = _bot.activeShell().getText()) != "Pegasus RCP") {
            _bot.shell(activeShellName).close();
        }
    }

    /**
     * Moves resource resourceName from projectName1 to projectName2. Those
     * resources must already exist while calling this method. Using File ->
     * Move...
     * 
     * @param resourceName
     * @param projectName1
     * @param projectName2
     */
    private void moveResource(String resourceName, String projectName1,
        String projectName2) {
        _bot.tree(1).expandNode(projectName1).getNode(resourceName).select();
        _bot.menu("File").menu("Move...").click();

        _assertionFailedMessage = "Window 'Move Resources' is not active";
        assertTrue(_assertionFailedMessage, _bot.shell("Move Resources")
            .isActive());
        _bot.tree().expandNode(projectName2).select();
        _bot.button("OK").click();
        waitUntilShellCloses("Rename Resource");
        _assertionFailedMessage = "Project" + projectName1
            + " should not contain file '" + resourceName + "'";
        assertFalse(_assertionFailedMessage,
            (_bot.tree(1).expandNode(projectName1).getNodes())
                .contains(resourceName));
        _assertionFailedMessage = "Project " + projectName2
            + " does not contain " + resourceName;
        assertNotNull(_assertionFailedMessage,
            _bot.tree(1).expandNode(projectName2).getNode(resourceName));
    }

    /**
     * Creates Pegasus project with given name. Simulates: File > New > Pegasus
     * Project
     * 
     * @param projectName
     */
    private void createPegasusProject(String projectName) {
        _bot.menu("File").menu("New").menu("Pegasus Project").click();
        _assertionFailedMessage = "Window 'New Pegasus Project' is not active";
        assertTrue(_assertionFailedMessage, _bot.shell("New Pegasus Project")
            .isActive());
        _bot.textWithLabel("Project name:").setText(projectName);
        _bot.button("Finish").click();
        waitUntilShellCloses("New Pegasus Project");
    }

    /**
     * Creates system configuration file 'fileName' inside project
     * 'projectName'. Simulates: File > New > System Configuration.
     * 
     * @param projectName
     * @param fileName
     */
    private void createSystemConfiguration(String projectName, String fileName) {
        if (!fileName.contains(".xml"))
            fileName = fileName.concat(".xml");

        createFileNewWizzard("System Configuration",
            "New System Configuration", projectName, "File name:", fileName);
        assertSystemConfigurationWasCreated(fileName);
    }

    /**
     * Verifies that test hierarchy file with a given name was created. Checks
     * if proper tab was opened. Confirms that there is no possibility to create
     * file with the same name.
     * 
     * @param testHierarchyName
     */
    private void assertTestHierarchyWasCreated(String testHierarchyName) {
        _assertionFailedMessage = "There is no editor tab '"
            + testHierarchyName + "'";
        assertTrue(_assertionFailedMessage, testHierarchyName.equals(_bot
            .editorByTitle(testHierarchyName).getTitle()));

        _bot.menu("File").menu("New").menu("Test Hierarchy").click();
        _assertionFailedMessage = "Window 'New Test Hierarchy' is not active";
        assertTrue(_assertionFailedMessage, _bot.shell("New Test Hierarchy")
            .isActive());
        _bot.textWithLabel("File Name").setText(testHierarchyName);
        _assertionFailedMessage = "'Finish' button should be disabled";
        assertFalse(_assertionFailedMessage, _bot.button("Finish").isEnabled());
        _bot.button("Cancel").click();
    }

    /**
     * Verifies that system configuration file with a given name was created. It
     * checks if proper tab was opened in editor, if the default file contents
     * are as expected and if you can open a file with the same name.
     * 
     * @param systemConfigName
     */
    private void assertSystemConfigurationWasCreated(String systemConfigName) {
        _assertionFailedMessage = "There is no editor tab '" + systemConfigName
            + "'";
        assertEquals(_assertionFailedMessage,
            _bot.editorByTitle(systemConfigName).getTitle(), systemConfigName);

        _assertionFailedMessage = "The contents of '" + systemConfigName
            + "' is different than expected";
        assertEquals(
            _assertionFailedMessage,
            _bot.editorByTitle(systemConfigName).toTextEditor().getText(),
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<systemConfiguration xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
                + "  <systemUnderTests/>\n" + "  <testEquipments/>\n"
                + "  <physicalLinks/>\n" + "  <properties/>\n"
                + "  </systemConfiguration>");

        _bot.menu("File").menu("New").menu("System Configuration").click();
        _assertionFailedMessage = "Window 'New System Configuration' is not active";
        assertTrue(_assertionFailedMessage,
            _bot.shell("New System Configuration").isActive());
        _bot.textWithLabel("File name:").setText(systemConfigName);
        _assertionFailedMessage = "Finish button should be disabled";
        assertFalse(_assertionFailedMessage, _bot.button("Finish").isEnabled());
        _bot.button("Cancel").click();
    }

    /**
     * Verifies that currently selected folder with a given name was created.
     * 
     * @param folderName
     */
    private void assertFolderWasCreated(String folderName) {
        _bot.viewByTitle("Navigator").setFocus();
        _bot.menu("Edit").menu("Delete").click();
        _assertionFailedMessage = "No dialog confirming file '" + folderName
            + "' removal";
        assertEquals(
            _assertionFailedMessage,
            _bot.label(
                "Are you sure you want to delete '" + folderName
                    + "' from the file system?").getText(),
            "Are you sure you want to delete '" + folderName
                + "' from the file system?");
        _bot.button("Cancel").click();
        waitUntilShellIsActive("Eclipse RCP");
    }

    /**
     * Creates file of type menuItem from: File > New > menuItem.
     * 
     * @param menuItem
     *            item in File > New > menuItem
     * @param windowTitle
     *            window title that shows up after clicking menuItem in File >
     *            New > menuItem
     * @param projectName
     *            name of the project where file should be created
     * @param textFileLabel
     *            label name near the text field where fileName should be
     *            entered (e.g. "File name:")
     * @param fileName
     *            name of the file of type menuItem
     */
    private void createFileNewWizzard(String menuItem, String windowTitle,
        String projectName, String textFileLabel, String fileName) {
        _bot.menu("File").menu("New").menu(menuItem).click();
        _assertionFailedMessage = "Window '" + windowTitle + "' is not active";
        assertTrue(_assertionFailedMessage, _bot.shell(windowTitle).isActive());
        _bot.tree().select(projectName);
        _bot.textWithLabel(textFileLabel).setText(fileName);
        _bot.button("Finish").click();
        waitUntilShellIsActive("Pegasus RCP");
    }

    /**
     * Creates file of type treeItem from: File > New > Other... wizzard.
     * 
     * @param treeRoot
     *            folder name in 'New' wizzard window
     * @param treeItem
     *            tree item in folder treeRoot
     * @param projectName
     *            name of the project where file should be created
     * @param textFieldLabel
     *            label name near the text field where fileName should be
     *            entered (e.g. "File name:")
     * @param fileName
     *            name of the file of type menuItem
     */
    private void createFileNewOtherWizzard(String treeRoot, String treeItem,
        String projectName, String textFieldLabel, String fileName) {

        _bot.menu("File").menu("New").menu("Other...").click();
        _assertionFailedMessage = "Window 'New' is not active";
        assertTrue(_assertionFailedMessage, _bot.shell("New").isActive());
        _bot.tree().expandNode(treeRoot).select(treeItem);
        _bot.button("Next >").click();
        _bot.tree().select(projectName);
        _bot.textWithLabel(textFieldLabel).setText(fileName);
        _bot.button("Finish").click();
        waitUntilShellIsActive("Eclipse RCP");
    }

    /**
     * Verifies that Save option is enabled (File menu option and tool bar
     * button)
     */
    private void assertSaveEnabled() {
        _assertionFailedMessage = "Tool bar button 'Save (Ctrl+S) should be enabled";
        assertTrue(_assertionFailedMessage,
            _bot.toolbarButtonWithTooltip("Save (Ctrl+S)").isEnabled());
        _assertionFailedMessage = "'File > Save' option should be enabled";
        assertTrue(_assertionFailedMessage, _bot.menu("File").menu("Save")
            .isEnabled());
    }

    /**
     * Verifies that Save option is disabled (File menu option and tool bar
     * button)
     */
    private void assertSaveDisabled() {
        _assertionFailedMessage = "Tool bar button 'Save (Ctrl+S) should be disabled";
        assertFalse(_assertionFailedMessage,
            _bot.toolbarButtonWithTooltip("Save (Ctrl+S)").isEnabled());
        _assertionFailedMessage = "'File > Save' option should be disabled";
        assertFalse(_assertionFailedMessage, _bot.menu("File").menu("Save")
            .isEnabled());
    }

    /**
     * Deletes all resources in Navigator view.
     */
    private void deleteAllFromNavigator() {
        int rootFoldersNumber = _bot.tree(1).getAllItems().length;

        for (int i = 0; i < rootFoldersNumber; i++) {
            _bot.tree(1).getAllItems()[0].contextMenu("Delete").click();
            if (_bot.shell("Delete Resources").isActive()) {
                _bot.checkBox().select();
                _bot.button("OK").click();
            }
        }
        waitUntilShellCloses("Delete Resources");
    }

    /**
     * Renames a resource inside project using: File -> Rename...
     * 
     * @param projectName
     * @param oldResourceName
     * @param newResourceName
     */
    private void renameResource(String projectName, String oldResourceName,
        String newResourceName) {
        _bot.tree(1).expandNode(projectName).getNode(oldResourceName).select();
        _bot.menu("File").menu("Rename...").click();
        _assertionFailedMessage = "Window 'Rename Resource' is not active";
        assertTrue(_assertionFailedMessage, _bot.shell("Rename Resource")
            .isActive());
        _bot.textWithLabel("New name:").setText(newResourceName);
        _bot.button("OK").click();
        waitUntilShellCloses("Rename Resource");
        _assertionFailedMessage = "Node '" + newResourceName
            + "' is not visible in Navigator view";
        assertTrue(_assertionFailedMessage, _bot.tree(1)
            .expandNode(projectName).getNode(newResourceName).isVisible());
    }

    /**
     * Rename project using: File -> Rename...
     * 
     * @param projectName
     * @param newProjectName
     */
    private void renameProject(String projectName, String newProjectName) {
        _bot.tree(1).expandNode(projectName).select();
        _bot.menu("File").menu("Rename...").click();
        _assertionFailedMessage = "Window 'Rename Resource' is not active";
        assertTrue(_assertionFailedMessage, _bot.shell("Rename Resource")
            .isActive());
        _bot.textWithLabel("New name:").setText(newProjectName);
        _bot.button("OK").click();
        waitUntilShellCloses("Rename Resource");
        _assertionFailedMessage = "Project '" + newProjectName
            + "' is not visible in Navigator view";
        assertTrue(_assertionFailedMessage,
            _bot.tree(1).expandNode(newProjectName).isVisible());
    }

    /**
     * Wait until shell 'shellTitle' closes. Do nothing if it does not exist
     * anymore.
     * 
     * @param shellTilte
     */
    private void waitUntilShellCloses(String shellTilte) {
        try {
            _bot.waitUntil(Conditions.shellCloses(_bot.shell(shellTilte)));
        } catch (WidgetNotFoundException e) {
            /*
             * Method waitUntilShellCloses(String shellTitle) throws
             * WidgetNotFoundException if window 'shellTitle' shut down itself
             * after the operation has finished
             */
        }
    }

    /**
     * Wait until shell 'shellTitle' opens.
     * 
     * @param shellTitle
     */
    private void waitUntilShellIsActive(String shellTitle) {
        try {
            _bot.waitUntil(Conditions.shellIsActive(shellTitle));
        } catch (TimeoutException e) {
            /*
             * This is empty on purpose. It catches 'TimeoutException: The shell
             * Eclipse RCP did not activate' thrown unnecessarily by method
             * waitUntil
             */
        }
    }

    /**
     * Performs cleanup needed after tests: close all opened editors, reset
     * perspective, delete all projects from Navigator view.
     */
    private void cleanup() {
        if (_bot.menu("File").menu("Close All").isEnabled())
            _bot.menu("File").menu("Close All").click();

        _bot.menu("Window").menu("Reset Perspective...").click();

        if (_bot.shell("Reset Perspective").isActive())
            _bot.button("OK").click();

        deleteAllFromNavigator();
    }
}
