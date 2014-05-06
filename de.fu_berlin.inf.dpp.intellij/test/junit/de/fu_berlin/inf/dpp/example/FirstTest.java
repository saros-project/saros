package de.fu_berlin.inf.dpp.example;

import com.intellij.codeHighlighting.HighlightingPass;
import com.thoughtworks.xstream.XStream;
import de.fu_berlin.inf.dpp.SarosCoreTestAction;
import de.fu_berlin.inf.dpp.activities.business.TextEditActivity;
import org.junit.Assert;
import org.junit.Test;

public class FirstTest {

    @Test
    public void failsDeliberately() {
        Assert.fail("Expected to fail, used to configure build scripts.");
    }

    @Test
    public void canReferenceClassesAcrossDependencies() {
        Assert.assertNotNull(SarosCoreTestAction.class); // from saros-i main classpath
        Assert.assertNotNull(TextEditActivity.class); // from saros core classpath
        Assert.assertNotNull(XStream.class); // from saros core libs
        Assert.assertNotNull(HighlightingPass.class); // from IntelliJ dependencies
    }
}
