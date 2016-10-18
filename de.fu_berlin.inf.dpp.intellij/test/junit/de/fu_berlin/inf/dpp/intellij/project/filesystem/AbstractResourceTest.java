package de.fu_berlin.inf.dpp.intellij.project.filesystem;

import com.intellij.mock.MockLocalFileSystem;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBusFactory;
import org.easymock.IAnswer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.getCurrentArguments;
import static org.easymock.EasyMock.isA;
import static org.junit.Assert.fail;
import static org.powermock.api.easymock.PowerMock.createNiceMock;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.mockStaticPartial;
import static org.powermock.api.easymock.PowerMock.replay;

public class AbstractResourceTest {
    public static final String TEST_FILE_NAME = "testfile.txt";
    public static final String NEW_FILE_NAME = "newCreateFile.txt";
    public static final String TEST_PROJECT_NAME = "project";
    public static final String RELATIVE_TEST_RESOURCE_PATH =
        TEST_PROJECT_NAME + "/" + TEST_FILE_NAME;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void before() throws IOException {
        folder.create();
        folder.newFolder(TEST_PROJECT_NAME);
    }

    protected IntelliJProjectImpl getMockProject() {
        Project project = createMock(Project.class);
        expect(project.getName()).andReturn(TEST_PROJECT_NAME);
        expect(project.getBasePath()).andReturn(
            folder.getRoot().getAbsolutePath() + File.separator
                + TEST_PROJECT_NAME);

        replay(project);

        return new IntelliJProjectImpl(project);
    }

    protected void mockFileSystem() {
        mockStatic(LocalFileSystem.class);

        expect(LocalFileSystem.getInstance())
            .andReturn(new MockLocalFileSystem() {
                @Nullable
                @Override
                public VirtualFile refreshAndFindFileByIoFile(
                    @NotNull
                    File file) {
                    return new VirtualFileMock(file);
                }

                @Nullable
                @Override
                public VirtualFile findFileByPath(
                    @NotNull
                    String path) {
                    return new VirtualFileMock(new File(path));
                }
            }).anyTimes();

        replay(LocalFileSystem.class);
    }

    protected void mockApplicationManager() {
        mockStaticPartial(ApplicationManager.class, "getApplication");

        expect(ApplicationManager.getApplication()).andReturn(mockApplication())
            .anyTimes();

        replay(ApplicationManager.class);
    }

    private Application mockApplication() {
        Application mock = createNiceMock(Application.class);

        mock.invokeAndWait(isA(Runnable.class), isA(ModalityState.class));
        expectLastCall().andAnswer(new IAnswer<Void>() {
            @Override
            public Void answer() {
                Runnable runnable = (Runnable) getCurrentArguments()[0];
                runnable.run();
                return null;
            }
        });

        expect(mock.getMessageBus())
            .andReturn(MessageBusFactory.newMessageBus(this)).anyTimes();

        expect(mock.getDefaultModalityState()).andReturn(new ModalityState() {
            @Override
            public boolean dominates(
                @NotNull
                ModalityState modalityState) {
                return true;
            }

            @Override
            public String toString() {
                return "mock modality state";
            }
        }).anyTimes();

        try {
            mock.runWriteAction(isA(ThrowableComputable.class));
            expectLastCall().andAnswer(new IAnswer<Void>() {
                @Override
                public Void answer() throws IOException {
                    ThrowableComputable<Void, IOException> computable = (ThrowableComputable<Void, IOException>) getCurrentArguments()[0];
                    computable.compute();
                    return null;
                }
            });
        } catch (Throwable t) {
            fail("Exception thrown while building mocks");
        }

        replay(mock);

        return mock;
    }

    protected void createTestProjectFolder() throws IOException {
        folder.create();
        folder.newFolder(TEST_PROJECT_NAME);
    }
}
