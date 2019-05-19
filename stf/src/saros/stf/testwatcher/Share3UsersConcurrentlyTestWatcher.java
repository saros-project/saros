package saros.stf.testwatcher;

import java.util.ArrayList;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class Share3UsersConcurrentlyTestWatcher extends TestWatcher {
    public static ArrayList<String> list = new ArrayList<String>();

    @Override
    public void succeeded(Description description) {

        list.add("Succeeded");

    }

    @Override
    public void failed(Throwable e, Description description) {
        list.add("Failed");

    }

    public static ArrayList<String> getList() {
        return list;
    }

    public static boolean checkIfAllSucceeded() {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals("Failed")) {
                return false;
            }
        }
        return true;
    }

}
