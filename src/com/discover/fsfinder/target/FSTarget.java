package com.discover.fsfinder.target;

public class FSTarget {	
    private String name;
    private boolean preceding;
    private TYPE type;
    private FSTargetValidator validator;
	
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
}
