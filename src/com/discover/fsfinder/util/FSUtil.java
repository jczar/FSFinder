package com.discover.fsfinder.util;

import java.util.LinkedList;
import java.util.List;

import com.discover.fsfinder.MultiThreadFSContext;

public class FSUtil {

    public static String[] getPathElements(String path) {
        String pathElements[] = null;
        
        if (path.indexOf("/") >= 0) {
            path = path.replaceAll("//", "/");
            
            if (path.startsWith("/"))
                path = path.substring(1);
            
            pathElements = path.split("/");
        } else {
            pathElements = (new String[] {
                path
            });
        }
        
        return pathElements;
    }
    
    public static String formatBottomUpPath(String path) {
    	String[] pathElements = getPathElements(path);
    	List<String> formattedPathElms = new LinkedList<String>();
    	
    	if (pathElements == null || pathElements.length <= 0) {
    		return path;
    	}        	
    	
    	boolean previousWildCardElm = false;    	
    	int firstNonWildCardElm = -1;
    			
    	for (int i = pathElements.length - 1; i >= 0; i--) {
    		
    		if (pathElements[i].indexOf(MultiThreadFSContext.SINGLE_LEVEL_WILDCARD) < 0) {
    			firstNonWildCardElm = i;
    			break;
    		}
    	}
    	
    	// If no non-wildcard element was found (e.g. path is composed of wildcards only)
    	// then return a multi level wildcard
    	if (firstNonWildCardElm < 0) {
    		return "/" + MultiThreadFSContext.MULTI_LEVEL_WILDCARD + "/";
    	}
    	
    	for (int i = 0; i <= firstNonWildCardElm; i++) {
    		if (pathElements[i].indexOf(MultiThreadFSContext.SINGLE_LEVEL_WILDCARD) >= 0) {
    			if (!previousWildCardElm) {
    				formattedPathElms.add(pathElements[i]);
    				previousWildCardElm = !previousWildCardElm;
    			} else {
        			formattedPathElms.set(formattedPathElms.size() - 1, MultiThreadFSContext.MULTI_LEVEL_WILDCARD);    				
    			}
    		} else {
    			formattedPathElms.add(pathElements[i]);
    			previousWildCardElm = false;
    		}
    	}    		
    	
    	StringBuilder sbr = new StringBuilder();
    	for (String pathElm: formattedPathElms) {
    		sbr.append("/");
    		sbr.append(pathElm);    		
    	}
    	
    	return sbr.toString();
    }
    
}
