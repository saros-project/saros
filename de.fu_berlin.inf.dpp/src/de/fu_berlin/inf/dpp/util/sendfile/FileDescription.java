package de.fu_berlin.inf.dpp.util.sendfile;

import java.io.File;
import java.io.Serializable;

public class FileDescription implements Serializable {
    private static final long serialVersionUID = -1385642437497697528L;
    String name;
    long size;

    protected static FileDescription fromFile(File file) {
        FileDescription self = new FileDescription();
        self.name = file.getName();
        self.size = file.length();
        return self;
    }
}