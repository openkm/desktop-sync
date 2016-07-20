/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openkm.okmsynchronize.model;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author andreu
 */
public class SynchronizeMonitorThread {
   
    //RejectedExecutionHandler implementation
    private RejectedExecutionHandler rejectionHandler;
    //Get the ThreadFactory implementation to use
    private ThreadFactory threadFactory;
    //creating the ThreadPoolExecutor
    private ThreadPoolExecutor executorPool;
    
    private Integer corePoolSize = 2;
    private Integer maximumPoolSize = 4;
    private Integer keepAliveTime = 10;
    
    /** El objeto que almacena la instancia de la clase según el patrón singleton. */
    private static SynchronizeMonitorThread singleton = new SynchronizeMonitorThread();

    /** Construye un objeto de la clase. Este método es privado para forzar el patrón singleton. */
    private SynchronizeMonitorThread() {
        super();
        
        // Initialize monitor
        threadFactory = Executors.defaultThreadFactory();        
        rejectionHandler = new RejectedExecutionHandlerImpl();        
        executorPool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(corePoolSize), threadFactory, rejectionHandler);
    }
    
    
    /**
     * Recupera el objeto singleton de la clase.
     *
     * @return el objeto singleton de la clase SynchronizeMonitorThread.
     */
    public static SynchronizeMonitorThread getSynchronizeMonitorThread() {
        return singleton;
    }
    
    public void execute(Runnable t) {
        executorPool.execute(t);
    }
    
    public ThreadPoolExecutor getExecutor() {
        return executorPool;
    }
    
    public void shutdown() {
        executorPool.shutdown();
    }
    
    private class RejectedExecutionHandlerImpl implements RejectedExecutionHandler {

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            System.out.println("eeeeeeeeeeeeeeeee");
        }        
    }
    

    
}
