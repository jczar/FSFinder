package com.discover.fsfinder;

import com.discover.fsfinder.util.FSUtil;
import java.util.HashMap;
import java.util.Map;

public class FSFinderCache
{
    private FSFinderCacheNode root;
    
    public FSFinderCache() {
        root = new FSFinderCacheNode("/", null);
    }

    public FSFinderCacheNode getRoot() {
        return root;
    }
    
    public void addPath(String path) {
        if(getRoot() == null) {
            root = new FSFinderCacheNode(path, null);
            return;
        }
        
        FSFinderCacheNode node = getRoot();
        String pathElements[] = FSUtil.getPathElements(path);
        
        for(int idx = 0; idx < pathElements.length; idx++)
            if(node.contains(pathElements[idx]))
                node = node.getChild(pathElements[idx]);
            else
                node = node.addSubsequentPath(pathElements[idx]);

    }	
    
    /*
     * A given path is considered to be "contained" in the cache if:
     *
     * The cache contains a path in which EACH AND EVERY one of its elements or components
     * is part of the argument path, with the exact same sequence.
     *
     *  E.g.
     *
     *  Argument path (passed as the method parameter value)
     *  /u01/app/oracle/product
     *
     *  Cached path:
     *  /u01/app
     *
     *  In this case, each and every component of the cached path (/u01/app)
     *  is part of the argument path. Therefore, the path passed as argument
     *  should be "contained" in the cached path.
     *
     *  If ANY of the components of the cached path (say the last one: /u01/ora)
     *  is different, it cannot be stated that the argument path is contained there.
     */
    public boolean containsPath(String path) {
        FSFinderCacheNode node = getRoot();
        
        String pathElements[] = FSUtil.getPathElements(path);
        
        boolean isContained = true;
        for(int idx = 0; isContained && idx < pathElements.length;)
            if(node.contains(pathElements[idx]))
            {
                if((node = node.getChild(pathElements[idx])) == null)
                    break;
                idx++;
            } else
            {
                isContained = false;
            }

        return isContained;
    }

    /*
     * A given path is considered to be "implicitly contained" if:
     *
     * The root node of the cache contains the first element or component
     * of the argument path, which means that all subsequent directories
     * should have been traversed already.
     *
     * In this case, it does not matter if the rest of the cached path is longer
     * than the argument path or if it has no further elements/components
     * matching the same components in the argument.
     *
     * E.g.
     * Argument path: /u01/app/fa/config
     * Cached path: /u01/foo/bar/baz/tetris/houze
     *
     * If the cached path has been FULLY traversed, all directories
     * under /u01 should have been traversed indirectly/implicitly,
     * meaning that argument path is "implicitly" contained in the
     * cached path.
     *
     * Note: At some point, the cache could store what depth
     * of a given cached path has been traversed by an ongoing running
     * traverser. In that case, this method would need to check that as well.
     */
    public boolean containsImplicitPath(String path) {
        String pathElements[] = FSUtil.getPathElements(path);
        if(null == pathElements|| pathElements.length <= 0)
        {
            return false;
        }
        
        FSFinderCacheNode node = getRoot();
        return node.contains(pathElements[0]);
    }
    
    
    class FSFinderCacheNode {
        String pathElement;
        FSFinderCacheNode parent;
        Map<String, FSFinderCacheNode> children = null;
        
        FSFinderCacheNode(String pathElement, FSFinderCacheNode parent) {
            this.pathElement = pathElement;
            this.parent = parent;
        }        
    	
        public FSFinderCacheNode addSubsequentPath(String path) {
            if(children == null)
                children = new HashMap<String, FSFinderCacheNode>();
            
            if(!children.containsKey(path)) {
                FSFinderCacheNode fsCacheNode = new FSFinderCacheNode(path, this);
                children.put(path, fsCacheNode);
                
                return fsCacheNode;
            } 
            
            return null;

        }

        public boolean contains(String path) {
            return (children != null && children.containsKey(path));
        }

        public FSFinderCacheNode getChild(String path) {
            if(children != null)
                return (FSFinderCacheNode)children.get(path);
            
            return null;
        }
    }
}
