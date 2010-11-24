package de.fu_berlin.inf.dpp.vcs;

import static de.fu_berlin.inf.dpp.test.util.SarosTestUtils.replay;
import static org.easymock.classextension.EasyMock.createMock;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.junit.Test;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.business.VCSActivity;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISarosSession;

public class TestVCSActivity {
    @Test
    public void testIncludes() {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

        JID jid = new JID("");
        User source = new User(replay(createMock(ISarosSession.class)), jid, 0);
        IProject p = root.getProject("p");
        SPath p_a = new SPath(p, new Path("a"));
        VCSActivity switch_p_a = new VCSActivity(VCSActivity.Type.Switch,
            source, p_a, "", "", "");
        assertFalse(switch_p_a.includes(switch_p_a));

        SPath p_a_b = new SPath(p, new Path("a/b"));
        VCSActivity switch_p_a_b = new VCSActivity(VCSActivity.Type.Switch,
            source, p_a_b, "", "", "");
        assertFalse(switch_p_a_b.includes(switch_p_a));
        assertTrue(switch_p_a.includes(switch_p_a_b));

        VCSActivity update_p_a_b = new VCSActivity(VCSActivity.Type.Update,
            source, p_a_b, "", "", "");
        assertTrue(switch_p_a.includes(update_p_a_b));

        VCSActivity update_p_a = new VCSActivity(VCSActivity.Type.Update,
            source, p_a, "", "", "");
        assertFalse(update_p_a.includes(switch_p_a_b));
        assertFalse(switch_p_a_b.includes(update_p_a));
    }

}
