package de.fu_berlin.inf.dpp.intellij.project.filesystem;

import com.intellij.mock.MockLocalFileSystem;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.easymock.EasyMock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.powermock.api.easymock.PowerMock;

import java.io.File;
import java.io.IOException;

public class AbstractResourceTest {
    public static final String TESTFILE_NAME = "testfile.txt";
    public static final String NEW_FILE_NAME = "newCreateFile.txt";
    public static final String TEST_PROJECT_NAME = "project";
    public static final String RELATIVE_TEST_RESOURCE_PATH =
        TEST_PROJECT_NAME + "/" + TESTFILE_NAME;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    protected void createTestProjectFolder() throws IOException {
        folder.create();
        folder.newFolder(TEST_PROJECT_NAME);
    }

    protected void mockFileSystem() {
        PowerMock.mockStatic(LocalFileSystem.class);

        LocalFileSystem fs = new MockLocalFileSystem() {
            @Nullable
            @Override
            public VirtualFile refreshAndFindFileByIoFile(
                @NotNull
                File file) {
                return null;
            }
        };

        EasyMock.expect(LocalFileSystem.getInstance()).andReturn(fs);
        PowerMock.replay(LocalFileSystem.class);
    }

    protected IntelliJProjectImpl getMockProject() {
        Project project = EasyMock.createNiceMock(Project.class);

        EasyMock.expect(project.getBasePath())
            .andReturn(folder.getRoot().getAbsolutePath());
        EasyMock.replay(project);
        return new IntelliJProjectImpl(project, TEST_PROJECT_NAME);
    }
}
