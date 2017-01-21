package com.discover.fsfinder.path.hint;

import java.util.ArrayList;
import java.util.List;

import com.discover.fsfinder.path.FSPathProvider;

public abstract class FSBasePathProvider implements FSPathProvider {

	protected FSPathProvider fsPathProvider;
    protected List<String> processPatterns = new ArrayList<String>();
	
	public FSBasePathProvider(FSPathProvider fsPathProvider) {
		this.fsPathProvider = fsPathProvider;
	}
		
	@Override
	public List<String> getPaths() {
		if (null != fsPathProvider) {
			return fsPathProvider.getPaths();
		}
		
		return new ArrayList<String>();
	}
}
