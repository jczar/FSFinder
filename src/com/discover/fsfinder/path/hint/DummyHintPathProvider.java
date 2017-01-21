package com.discover.fsfinder.path.hint;

import com.discover.fsfinder.path.FSPathProvider;
import java.util.ArrayList;
import java.util.List;

public class DummyHintPathProvider implements FSPathProvider {

	public List<String> getPaths() {
        return new ArrayList<String>() {
            {
                add("/u01/app/fa");
            }
        };
    }
}
