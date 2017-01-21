package com.discover.fsfinder;

import com.discover.fsfinder.exception.FSFinderException;
import java.util.*;

public class IterableOracleInventoryFinder extends FSFinder
    implements IterableFinder {
    private Collection<FSFinder> finders;
    private Set<String> traverseFailedPaths = new LinkedHashSet<String>();

    public IterableOracleInventoryFinder() {
    }

    public void setFinderCollection(Collection<FSFinder> finders) {
        this.finders = finders;
    }

    @Override
    public List<String> findTargets()
        throws FSFinderException {
        Set<String> oracleInvLocSet = new LinkedHashSet<String>();
        for (Iterator<FSFinder> finderIterator = finders.iterator(); finderIterator.hasNext();) {
            FSFinder finder = finderIterator.next();
            List<String> finderInvLocResult = finder.findTargets();
            List<String> finderFailedPaths = finder.getTraverseFailedPaths();
            
            if (null != finderInvLocResult) {
                oracleInvLocSet.addAll(finderInvLocResult);
            }
            
            if (null != finderFailedPaths) {
                traverseFailedPaths.addAll(finderFailedPaths);
            }
        }

        return new ArrayList<String>(oracleInvLocSet);
    }

    @Override
    public List<String> postFindProcess(List<String> targetsFound) {
        return targetsFound;
    }

    @Override
    public List<String> getTraverseFailedPaths() {
        return new ArrayList<String>(traverseFailedPaths);
    }

    public void defineTargets() {
    }

}
