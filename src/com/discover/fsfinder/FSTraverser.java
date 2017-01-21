package com.discover.fsfinder;

import java.util.List;

import com.discover.fsfinder.exception.FSFinderException;
import com.discover.fsfinder.target.FSTarget;

public abstract class FSTraverser {
    protected boolean regexExpand;
    
    // Default traverse direction set to Top Down, as long as no wildcards are used.
    // Otherwise, it will change to Bottom up approach
    protected TRAVERSE_DIRECTION traverseDirection = TRAVERSE_DIRECTION.TOP_DOWN;

    public FSTraverser(boolean regexExpand) {
    	this.regexExpand = regexExpand;
    }
    
    public List<String> traverse(List<FSTarget> targets, List<String> startPaths, List<String> ignorePaths) throws FSFinderException {
        setIgnorePaths(ignorePaths);
        return traverse(targets, startPaths);
    }
    
    protected abstract void validateStartPaths(List<String> startPaths) throws FSFinderException;
    
    protected void setTraverseDirection(TRAVERSE_DIRECTION traverseDirection) {
    	this.traverseDirection = traverseDirection;
    }
    
    protected TRAVERSE_DIRECTION getTraverseDirection() {
    	return traverseDirection;
    }
    
    public abstract List<String> traverse(List<FSTarget> targets, List<String> startPaths) throws FSFinderException;
    public abstract List<String> getTraverseFailedPaths();

    protected abstract void setIgnorePaths(List<String> list);
        
    public enum TRAVERSE_DIRECTION {
    	TOP_DOWN,
    	BOTTOM_UP
    }
}
