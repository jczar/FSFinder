package com.discover.fsfinder;

import com.discover.fsfinder.exception.FSFinderException;
import com.discover.fsfinder.util.LoggingUtil;

import java.io.PrintStream;

public class MultiThreadTimedBarrier extends Thread
{
    private static int _timeout = 0;
    private static MultiThreadTimedBarrier timedBarrier = new MultiThreadTimedBarrier();
    private static boolean isDone = false;	

    public static MultiThreadTimedBarrier getInstance() {
        return timedBarrier;
    }

    public static void startTimer(int timeout) {
        if (timeout <= 0) {
            throw new FSFinderException("Cannot run the timer with 0 timeout");
        } else {
            _timeout = timeout * 1000;
            
            timedBarrier.start();
            return;
        }
    }

    public static void resetTimer() {
        timedBarrier.interrupt();
    }

    public void run() {
        if (_timeout <= 0)
            return;
        
        while (!isDone) { 
            synchronized (this) {
                try {
                    wait(_timeout);
                    isDone = true;
                    
                    LoggingUtil.printlnDebugMessage("No worker reset timer. We are DONE!");
                }
                catch (InterruptedException interruptedexception) { }
            }
        }
    }

    public static boolean isDone() {
        return isDone;
    }
}
