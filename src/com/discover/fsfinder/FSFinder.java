package com.discover.fsfinder;

import java.util.ArrayList;
import java.util.List;

import com.discover.fsfinder.exception.FSFinderException;
import com.discover.fsfinder.path.FSPathProvider;
import com.discover.fsfinder.target.FSTarget;

public abstract class FSFinder {
    protected List<String> hintPaths = new ArrayList<String>();
    protected List<String> ignorePaths = new ArrayList<String>();
    
    protected FSPathProvider hintProvider, ignorePathProvider;
    protected FSTraverser traverser;
    
    protected List<FSTarget> fsTargets;
	
    protected FSFinder() {
    }

    protected FSFinder(FSTraverser traverser, FSPathProvider hintProvider, FSPathProvider ignorePathProvider)
        throws FSFinderException {
        if (hintProvider == null) {
            throw new FSFinderException("HintProvider object provided is invalid");
        }
        
        if (traverser == null) {
            throw new FSFinderException("Traverser object provided is invalid");
        } 

        this.hintProvider = hintProvider;
        this.ignorePathProvider = ignorePathProvider;
        this.traverser = traverser;
    }

    protected void loadHintPaths()
        throws FSFinderException {
        hintPaths = hintProvider.getPaths();
    }

    protected void loadIgnorePaths()
        throws FSFinderException {
        if (null != ignorePathProvider) {
            ignorePaths = ignorePathProvider.getPaths();
        }
    }

    public abstract void defineTargets();
    public abstract List<String> postFindProcess(List<String> list)
        throws FSFinderException;

    public List<String> findTargets()
        throws FSFinderException {
        loadHintPaths();
        loadIgnorePaths();
        
        if (null == hintPaths || hintPaths.size() <= 0) {
            return null;
        } 
        
        List<String> targetsFound = traverser.traverse(fsTargets, hintPaths, ignorePaths);
        
        return postFindProcess(targetsFound);
    }

    public List<String> getTraverseFailedPaths() {
        if (null != traverser)
            return traverser.getTraverseFailedPaths();
        else
            throw new FSFinderException("A traverser has not been defined for this finder");
    }

}
