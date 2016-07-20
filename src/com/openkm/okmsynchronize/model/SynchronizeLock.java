package com.openkm.okmsynchronize.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author abujosa
 */
public class SynchronizeLock {
    
    private List<Path> lockedPath;
    
    private static SynchronizeLock lock = new SynchronizeLock();
    
    private SynchronizeLock() {
        super();
        
        lockedPath = new ArrayList<Path>();
    }
    
    public static SynchronizeLock getSynchronizeLock() {                        
        return lock;
    } 
    
    public void addPath(Path p) {
        if(!lockedPath.contains(p)) {
            lockedPath.add(p);
        }
    }
    
    public void deletePath(Path p) {
        if(lockedPath.contains(p)) {
            lockedPath.remove(p);
        }
    }
    
    public boolean isLock(Path p) {
        boolean find = false;
        for(Path pa : lockedPath) {
            find = p.startsWith(pa);
            break;
        }
        
        return find;
    }
    
    public boolean haveBlockedPath() {
        return !lockedPath.isEmpty();
    }
    
}
