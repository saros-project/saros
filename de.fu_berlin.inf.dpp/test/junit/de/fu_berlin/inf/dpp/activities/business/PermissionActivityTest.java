package de.fu_berlin.inf.dpp.activities.business;

import java.util.List;

import org.junit.Test;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.Permission;

public class PermissionActivityTest extends AbstractActivityTest {

    @Test
    @Override
    public void testConversion() {
        List<Permission> permissions = toListPlusNull(Permission.values());

        for (User target : targets) {
            for (Permission permission : permissions) {
                PermissionActivity pa;
                try {
                    pa = new PermissionActivity(source, target, permission);
                } catch (IllegalArgumentException e) {
                    continue;
                }

                testConversionAndBack(pa);
            }
        }
    }

}
