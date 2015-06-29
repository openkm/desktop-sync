/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.openkm.okmsynchronize.model;

/**
 *
 * @author abujosa
 */
public interface SynchronizeWorker  {
    
    public void stopWorker();   
    
    public void start();
    
    public Thread.State getState();
    
}
