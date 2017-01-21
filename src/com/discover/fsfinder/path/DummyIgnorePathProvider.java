package com.discover.fsfinder.path;

import java.util.ArrayList;
import java.util.List;

public class DummyIgnorePathProvider
    implements FSPathProvider {

    public List<String> getPaths() {
        return new ArrayList<String>() {
            {
                add("/bin");
                add("/boot");
                add("/dev");
                add("/lib");
                add("/lib64");
                add("/lost+found");
                add("/mnt");
                add("/root");
                add("/sbin");
                add("/sys");
            }
        };
    }
}
