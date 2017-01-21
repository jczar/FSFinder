package com.discover.fsfinder;

import java.util.List;

public interface MultiThreadFSHandler {
    public abstract void queuePath(String path, String[] args);

    public abstract void queuePaths(List<String> paths, String[] args);
    
    public abstract void processRejectedPaths();
    
}
