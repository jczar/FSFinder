package com.discover.fsfinder.util;

import com.discover.fsfinder.ExecutionContext;

public class LoggingUtil {

	public static void printlnVerboseMessage(String message) {
		if (ExecutionContext.getInstance().getUserInput().isVerbose()) {
		    printlnMessage(message);
		}
	}

	public static void printVerboseMessage(String message) {
		if (ExecutionContext.getInstance().getUserInput().isVerbose()) {
		    printMessage(message);
		}
	}

	public static void printlnDebugMessage(String message) {
		if (ExecutionContext.getInstance().getUserInput().isDebug()) {
		    printlnMessage(message);
		}
	}

	public static void printDebugMessage(String message) {
		if (ExecutionContext.getInstance().getUserInput().isDebug()) {
		    printMessage(message);
		}
	}
	
	public static void printlnMessage(String message) {
		System.out.println(message);
	}
	
	public static void printMessage(String message) {
		System.out.print(message);
	}
	
}
