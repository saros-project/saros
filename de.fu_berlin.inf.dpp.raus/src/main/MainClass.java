package main;

import org.eclipse.swt.widgets.Display;

import ui.RAUSApp;

public class MainClass {

    private static Display display;

    public static void main(String[] a) {

        display = new Display();
        new RAUSApp(display);
        display.dispose();
    }

}
