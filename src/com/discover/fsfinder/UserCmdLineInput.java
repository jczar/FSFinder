package com.discover.fsfinder;

import java.util.ArrayList;
import java.util.List;

import gnu.getopt.Getopt;

public class UserCmdLineInput {

	private List<String> processPatterns = new ArrayList<String>();
	private String targetDir;
	private String targetFile;
	// Setting default number of occurrences to 1. 
	private int maxOccurrences = 1;	
	private boolean isVerbose = false;
	private boolean isDebug = false;
	private boolean isBacktrace = false;
	private boolean isRegex = false;
	private boolean isProcessBased = false;
	private boolean searchAll = true;
	
	private static UserCmdLineInput userInput = null;
	
	private UserCmdLineInput() {	
	}
	
	public List<String> getProcessPatterns() {
		return processPatterns;
	}
	
	public String getTargetDir() {
		return targetDir;
	}
	
	public String getTargetFile() {
		return targetFile;
	}
	
	public int getMaxOccurrences() {
		return maxOccurrences;
	}
	
	public boolean isProcessBased() {
		return isProcessBased;
	}
	
	public boolean isVerbose() {
		return isVerbose;
	}
	
	public boolean isDebug() {
		return isDebug;
	}
	
	public boolean isBacktrace() {
		return isBacktrace;
	}
	
	public boolean isRegex() {
		return isRegex;
	}
	
	public boolean isSearchAll() {
		return searchAll;
	}
	
	@Override
	public String toString() {
		StringBuilder sbr = new StringBuilder();
		
		sbr.append("================================");
		sbr.append("\nUser command line parameters: \n\n");
		sbr.append("Process patterns: ");
		for (String pattern: processPatterns) {
			sbr.append(pattern).append(",");
		}
		sbr.append("\nTarget file: ").append(targetFile);
		sbr.append("\nTarget directory: ").append(targetDir);
		sbr.append("\nSearch all: ").append(searchAll).append("\n");
		sbr.append("================================");
		
		return sbr.toString();
	}
	
	public static UserCmdLineInput createUserCmdLineInput(String[] args) { // Receive some getOpt object to parse
		 if (userInput != null) { 
			 return userInput;
		 }
		
		 userInput = new UserCmdLineInput();
		 
		 Getopt g = new Getopt("FSFinder", args, "t:s:p:n:vdbr");

		 int c;
		 while ((c = g.getopt()) != -1) {
		     switch (c) {		     
		          case 't':
		        	  userInput.targetFile = g.getOptarg();
		        	  break;
		        	  
		          case 's':
		        	  userInput.targetDir = g.getOptarg();
		              break;
		              
		          case 'p':
		              userInput.processPatterns.add(g.getOptarg());
		              userInput.isProcessBased = true;
		              userInput.isBacktrace = true;
		              
	  	              break;
	  	              
		          case 'v':
		        	  userInput.isVerbose = true;
		        	  break;
		        	  
		          case 'd': 
		        	  userInput.isDebug = true;
		        	  break;
		        	  
		          case 'b':
		        	  userInput.isBacktrace = true;
		        	  break;
		        	  
		          case 'r': 
		        	  userInput.isRegex = true;
		        	  break;
		        	  
		          case 'n':
		        	  try {
		        	      userInput.maxOccurrences = Integer.parseInt(g.getOptarg());
		        	      userInput.searchAll = false;
		        	      
		        	  } catch (Exception e) {
		        		  return null;
		        	  }
		        	  break;
	  	              
		          case '?':
		              break; 
		              
		          default:
		              System.out.print("getopt() returned " + c + "\n");
		       }
		   }
		
		return userInput;
	}
	
    public static void printCmdLineUsage() {
    	System.out.println("Usage: java -jar fsfinder.jar -t <target_file> -s <start_dir> [options]");
    	System.out.println("\nWhere options may be one of the following:");
    	
    	System.out.println("-s Specifies start point for the search");    	
    	System.out.println("\n-p <process_pattern>\t\tIndicates the pattern to use in a process based search. Wildcards \"*\" and \"**\" may be used");
    	System.out.println("-n <number_of_occurrences>\tSpecifies the maximum number of occurrences to find");
    	System.out.println("-d Runs the finder in debug mode");
    	System.out.println("-d Runs the finder in debug mode");
    	System.out.println("-b Specifies backtrace search (top-bottom or right to left search)");
    	System.out.println("-r Indicates that the target given should be considered a regex expression");
    	System.out.println("-v\t\t\t\tRuns the finder in verbose mode");    	
    }
}
