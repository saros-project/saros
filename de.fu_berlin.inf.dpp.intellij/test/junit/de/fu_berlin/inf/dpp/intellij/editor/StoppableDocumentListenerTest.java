package de.fu_berlin.inf.dpp.intellij.editor;

import com.intellij.mock.MockEditorEventMulticaster;
import com.intellij.mock.MockEditorFactory;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.event.EditorEventMulticaster;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.easymock.EasyMock.expect;
import static org.fest.assertions.Assertions.assertThat;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replay;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ EditorFactory.class })
public class StoppableDocumentListenerTest {

    private StoppableDocumentListener listener;
    boolean listening;

    @Before
    public void before() {
        mockEditorFactory();
        listener = new StoppableDocumentListener(dummyEditorManager(), null);
        listening = false;
    }

    @Test
    public void testStart() {
        listener.startListening();

        assertListening();
    }

    @Test
    public void testStop() {
        listener.startListening();
        listener.stopListening();

        assertNotListening();
    }

    @Test
    public void testEnable() {
        listener.setEnabled(true);

        assertListening();
    }

    @Test
    public void testDisable() {
        listener.startListening();
        listener.setEnabled(false);

        assertNotListening();
    }

    private void assertListening() {
        assertThat(listening).as("Is listening").isTrue();
        assertThat(listener.enabled).as("Is enabled").isTrue();
    }

    private void assertNotListening() {
        assertThat(listening).as("Is not listening").isFalse();
        assertThat(listener.enabled).as("Is disabled").isFalse();
    }

    private void mockEditorFactory() {
        mockStatic(EditorFactory.class);

        final MockEditorEventMulticaster multicaster = new MockEditorEventMulticaster() {

            @Override
            public void addDocumentListener(
                @NotNull
                DocumentListener listener) {
                listening = true;
            }

            @Override
            public void removeDocumentListener(
                @NotNull
                DocumentListener listener) {
                listening = false;
            }
        };

        expect(EditorFactory.getInstance()).andReturn(new MockEditorFactory() {

            @NotNull
            @Override
            public EditorEventMulticaster getEventMulticaster() {
                return multicaster;
            }
        }).atLeastOnce();

        replay(EditorFactory.class);
    }

    private EditorManager dummyEditorManager() {
        return null;
    }
}