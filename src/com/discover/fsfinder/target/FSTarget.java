package com.discover.fsfinder.target;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;

import com.discover.fsfinder.ExecutionContext;

public class FSTarget {	
    private String name;
    private boolean preceding;
    private TYPE type;
    private EXPR_TYPE targetExprType = EXPR_TYPE.REGULAR;
    private FSTargetValidator validator;
    
    private ExpressionValidator expressionValidator = new ExpressionValidator(); 
	
    public FSTarget(String name, boolean preceding, TYPE type) {
        this(name, preceding, type, null);
    }

    public FSTarget(String name, boolean preceding, TYPE type, FSTargetValidator validator) {
        this.preceding = false;
        this.name = name;
        this.preceding = preceding;
        this.type = type;
        this.validator = validator;        
    }
    
    public boolean isValid(String absoluteFilePath) {
        return validator == null ? true : validator.isValid(absoluteFilePath);
    }

    public String getName() {
        return name;
    }

    public boolean isPreceding() {
        return preceding;
    }

    public TYPE getType() {
        return type;
    }

    public EXPR_TYPE getTargetExprType() {
    	return targetExprType;
    }
    
    public ExpressionValidator getExprValidator() {
        return expressionValidator;
    }

    public FSTargetValidator getTargetValidator() {
        return validator;
    }

    public void setTargetValidator(FSTargetValidator validator) {
        this.validator = validator;
    }
    
    public enum TYPE {	
    	DIRECTORY,
    	FILE
    }
    
    public enum EXPR_TYPE {
    	REGULAR("regular"),
    	GLOB("glob"),
    	REGEX("regex");
    
    	private String value;
    	
    	private EXPR_TYPE(String value) {
    		this.value = value;
    	}
    	public String getValue() {
    		return this.value;
    	}
    }
    
    class ExpressionValidator {
    	static final String globChars = ".*[\\*\\?\\[\\]\\{\\}].*";
        
    	// PathMatcher can be used if JDK is 1.7+
        // PathMatcher targetMatcher = null;
    	Matcher targetMatcher = null; 

        public boolean isGlobTarget(String targetName) {        	
        	return (targetName.matches(globChars)); 
        }
                
        public void loadTargetRegex(String targetName, boolean isRegexRequired) {
        	if (ExecutionContext.getInstance().getUserInput().isRegex()) {
        		targetExprType = EXPR_TYPE.REGEX;
        		
        	    // A JDK version check can be added. If JDK is 1.7+, we can use FileSystem from NIO pkg
        		// Otherwise, we can use a regular Matcher
         	    //targetMatcher = FileSystems.getDefault().getPathMatcher(targetExprType.getValue() + ":" + targetName);
        		Pattern p = Pattern.compile(targetName);
        		targetMatcher = p.matcher("");
        	}
        		
        	if (isGlobTarget(targetName)) { 
        		targetExprType = EXPR_TYPE.GLOB;
        	} else {
        		targetExprType = EXPR_TYPE.REGULAR;
        	}

        	if (targetExprType != EXPR_TYPE.REGULAR) { 

        	}
        }
        public boolean doesFilenameMatch(String fileName) {
        	return ((targetExprType == EXPR_TYPE.REGULAR && name.equals(fileName)) 
        			|| (targetExprType == EXPR_TYPE.GLOB && FilenameUtils.wildcardMatch(fileName, name))
        			|| (targetExprType == EXPR_TYPE.REGEX && (null != targetMatcher && targetMatcher.reset(fileName).matches())));
        	        	
        	/*if (targetExprType == EXPR_TYPE.REGULAR && name.equals(fileName)) {
        		
        	}
        	if (targetExprType == EXPR_TYPE.GLOB) {
        		return FilenameUtils.wildcardMatch(fileName, name);
        	}
        	if (targetExprType == EXPR_TYPE.REGEX) {
        		return (null != targetMatcher && targetMatcher.reset(fileName).matches());
        	} */
        	
        	// This check can be used for glob/regex if JDK is 1.7+
        	//return (null != targetMatcher && targetMatcher.matches(FileSystems.getDefault().getPath(fileName)));
        }
    }
}
