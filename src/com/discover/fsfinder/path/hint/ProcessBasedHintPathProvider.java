package com.discover.fsfinder.path.hint;

import com.discover.fsfinder.ExecutionContext;
import com.discover.fsfinder.path.FSPathProvider;
import com.discover.fsfinder.util.LoggingUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ProcessBasedHintPathProvider
    extends FSBasePathProvider {

	public ProcessBasedHintPathProvider(FSPathProvider fsPathProvider) {
		super(fsPathProvider);
	}
    
    //{
    //	    	
        /*processPatterns.add("java");
        processPatterns.add("opmn");
        processPatterns.add("weblogic");
        processPatterns.add("wlserver");
        processPatterns.add("ohs");
        processPatterns.add("essbase");
        processPatterns.add("sawserver");
        processPatterns.add("idm"); */    
    //}
    
    public List<String> getPaths() {
        List<String> pathList = super.getPaths();
        
        pathList = (null == pathList ? new ArrayList<String>() : pathList);
        
        processPatterns = ExecutionContext.getInstance().getUserInput().getProcessPatterns();
        
        if (null == processPatterns || processPatterns.isEmpty()) {
        	return pathList;
        }
        
        String cmdLineScript[] = formCommandLineScript();
        
        try {
            Process p = Runtime.getRuntime().exec(cmdLineScript);
            
            BufferedReader brInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader brError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        
            LoggingUtil.printlnVerboseMessage("\n========================================\n");
            LoggingUtil.printlnVerboseMessage("Obtained the following path hints: ");
            
            String line;
            while ((line = brInput.readLine()) != null) { 
                if (line.trim().startsWith("/")) {
                    pathList.add(line);
                    LoggingUtil.printlnVerboseMessage("[" + line + "]");
                }
            }
            
            brInput.close();
            LoggingUtil.printlnVerboseMessage("\nGot the following error: ");
            
            while (null != (line = brError.readLine())) 
                System.out.println(line);
            
            p.waitFor();
            
            LoggingUtil.printlnVerboseMessage("Hint provider execution finished");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        return pathList;
    }

    private String[] formCommandLineScript() {
        String _cmdLineScript[] = {
            "/bin/sh", "-c", ""
        };
        
        String _cmdLineStr = "ps -ef | egrep -i \"(";
        for (int i = 0; i < processPatterns.size(); i++) {
            if (i != 0)
                _cmdLineStr = (new StringBuilder(String.valueOf(_cmdLineStr))).append("|").toString();
            
            _cmdLineStr = (new StringBuilder(String.valueOf(_cmdLineStr))).append((String) processPatterns.get(i)).toString();
        }

        _cmdLineStr = (new StringBuilder(String.valueOf(_cmdLineStr))).append(")\" | awk '{ print $8 }' | sort | uniq").toString();
        _cmdLineScript[2] = _cmdLineStr;
        
        return _cmdLineScript;
    }


}
