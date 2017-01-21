package com.discover.fsfinder.path.hint;

import java.util.ArrayList;
import java.util.List;

import com.discover.fsfinder.ExecutionContext;
import com.discover.fsfinder.UserCmdLineInput;
import com.discover.fsfinder.path.FSPathProvider;

public class UserInputHintPathProvider implements FSPathProvider {

	List<String> inputHintPaths = new ArrayList<String>();
	
	public List<String> getPaths() {
		List<String> pathList = new ArrayList<String>();		
		pathList.add(ExecutionContext.getInstance().getUserInput().getTargetDir());
		
		return pathList;
	}
	
}
