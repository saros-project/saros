package de.fu_berlin.inf.dpp.project;

import static de.fu_berlin.inf.dpp.test.util.SarosTestUtils.replay;
import static org.easymock.classextension.EasyMock.createMock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.business.FolderActivity;
import de.fu_berlin.inf.dpp.activities.business.IResourceActivity;
import de.fu_berlin.inf.dpp.activities.business.VCSActivity;
import de.fu_berlin.inf.dpp.net.JID;

public class TestResourceActivityFilter {
    private ResourceActivityFilter filter;
    private VCSActivity switch_p;
    private VCSActivity switch_p_a;
    private VCSActivity switch_p_b;
    private IResourceActivity add_p_a;

    // Setup helpers
    private VCSActivity switch_(User source, SPath p) {
        final VCSActivity result = new VCSActivity(VCSActivity.Type.Switch,
            source, p, "", "", "");
        return result;
    }

    private SPath newSPath(String fullPathString) {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IPath path = new Path(fullPathString);
        IProject project = root.getProject(path.segment(0));
        final SPath result = new SPath(project, path.removeFirstSegments(1));
        return result;
    }

    public @Before
    void setUp() throws Exception {
        JID jid = new JID("kitty@hello.com");
        User source = new User(replay(createMock(ISarosSession.class)), jid, 0);

        add_p_a = new FolderActivity(source, FolderActivity.Type.Created,
            newSPath("/p/a"));

        switch_p = switch_(source, newSPath("/p"));
        switch_p_a = switch_(source, newSPath("/p/a"));
        switch_p_b = switch_(source, newSPath("/p/b"));

        filter = new ResourceActivityFilter();
    }

    public @After
    void tearDown() throws Exception {
        //
    }

    public @Test
    void init() throws Exception {
        final List<IResourceActivity> collected = filter.retrieveAll();
        assertTrue(collected != null);
        assertTrue(collected != null && collected.isEmpty());
    }

    public @Test
    void addRemove() throws Exception {
        filter.enter(add_p_a);
        final List<IResourceActivity> collected = filter.retrieveAll();
        assertEquals(1, collected.size());
        // Don't use equals() here.
        assertTrue(add_p_a == collected.get(0));
        assertTrue(filter.retrieveAll().isEmpty());
    }

    public @Test
    void filterIncludedActivity() throws Exception {
        assertTrue(switch_p.includes(add_p_a));

        filter.enter(add_p_a);
        filter.enter(switch_p);

        final List<IResourceActivity> collected = filter.retrieveAll();
        assertEquals(1, collected.size());
        // Don't use equals() here.
        assertTrue(switch_p == collected.get(0));
    }

    public @Test
    void dontFilterNotIncludedActivities() throws Exception {
        assertFalse(switch_p_b.includes(switch_p_a));
        assertFalse(switch_p_a.includes(switch_p_b));

        filter.enter(switch_p_a);
        filter.enter(switch_p_b);

        final List<IResourceActivity> collected = filter.retrieveAll();
        assertEquals(2, collected.size());
        IResourceActivity c0 = collected.get(0);
        IResourceActivity c1 = collected.get(1);
        // Could use collected.contains(), but it uses equals().
        assertTrue((c0 == switch_p_a && c1 == switch_p_b)
            || (c1 == switch_p_a && c0 == switch_p_b));
    }
}
