package com.discover.fsfinder.util;

public class StringUtil {

    public static boolean isNullOrEmpty(String str) {
    	return (null == str || str.isEmpty());
    }
    
    public static boolean isNotNullOrEmpty(String str) {
    	return !isNullOrEmpty(str);
    }

}
