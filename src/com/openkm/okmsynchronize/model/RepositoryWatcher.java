/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openkm.okmsynchronize.model;

import com.openkm.okmsynchronize.service.SynchronizeService;
import com.openkm.okmsynchronize.utils.SynchronizeException;
import com.openkm.okmsynchronize.utils.SynchronizeLog;
import com.openkm.okmsynchronize.utils.Utils;
import com.openkm.okmsynchronize.ws.OpenKMWS;
import com.openkm.okmsynchronize.ws.OpenKMWSFactory;
import com.openkm.okmsynchronize.ws.OpenKMWSVersions;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author andreu
 */
public class RepositoryWatcher {

    private final static String KEY_BUNDLE = RepositoryWatcher.class.getName();

    private  FileSystem fs;
    private  WatchService ws;
    private  Map<WatchKey, Path> keys;    
    private  ExecutorService service;
    private  SynchronizeMonitorThread monitor;
    private  ConfigurationModel con;
    private  SynchronizeLog log; 
    private  SynchronizedRepository repository;
    private  OpenKMWS webservice;
    
    private long previousCode = -1l;
    private WatchEvent.Kind<Path> previousKind = null;

    public RepositoryWatcher(ConfigurationModel c, SynchronizeLog log, String... directories) {

        try {        
            
            this.con = c;
            this.log = log;
            
            // Initialize
            service = Executors.newCachedThreadPool();
            fs = FileSystems.getDefault();
            ws = fs.newWatchService();           
            keys = new ConcurrentHashMap<WatchKey, Path>();   
            monitor = SynchronizeMonitorThread.getSynchronizeMonitorThread();
            
            ServerCredentials credentials = new ServerCredentials(con.getKeyValue(ConfigurationModel.KEY_USER) 
                                                                , con.getKeyValue(ConfigurationModel.KEY_PASSWORD)
                                                                , con.getKeyValue(ConfigurationModel.KEY_HOST)
                                                                , !Utils.isEmpty(con.getKeyValue(ConfigurationModel.KEY_SDK_VERSION))? OpenKMWSVersions.valueOf(con.getKeyValue(ConfigurationModel.KEY_SDK_VERSION)) : null);            
            try {
                webservice = OpenKMWSFactory.instance(credentials);
            } catch (SynchronizeException e) {
                log.error(KEY_BUNDLE, e);
            }

            for (String dir : directories) {
                reg(fs.getPath(dir), keys, ws);
            }

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public void setRepository(SynchronizedRepository repository) {
        this.repository = repository;
    }
    
    public void addDirectory(String dir) throws IOException {
        Path path = fs.getPath(dir);  
        WatchKey key = getKeyByValue(keys, path);
        if(key == null || !key.isValid()) {           
                reg(path, keys, ws);
        }
    }
    
    public void removeDirectory(String dir) throws IOException {
        Path path = fs.getPath(dir);

        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult postVisitDirectory(Path path, IOException exc) throws IOException {
                WatchKey key = getKeyByValue(keys, path);
                if (key != null) {
                    key.cancel();
                    key.pollEvents();
                    log.info(KEY_BUNDLE + " Removed directory from watcher: " + path);  
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }
 
    public void stop() throws IOException {
        for (WatchKey key : keys.keySet()) {
            log.info(KEY_BUNDLE + " Stopping watcher from:" + keys.get(key));  
            key.cancel();
            key.pollEvents();
        }

        // close WatcherService
        ws.close();
        
        // Stoping monitor
        monitor.shutdown();

        // Stoping service executor
        service.shutdownNow();
    }

    public void start() throws IOException {
        // Starting Watch Service
        service.submit(new Runnable() {
            @Override
            public void run() {
                log.info(KEY_BUNDLE + " Start watcher service ...");            
                while (Thread.interrupted() == false) {
                    WatchKey key;
                    try {
                        key = ws.poll(10, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        break;
                    } catch (ClosedWatchServiceException e) {
                        break;
                    }
                    if (key != null) {
                        Path path = keys.get(key);
                        Map<WatchEvent.Kind<Path>, Entry<Path, Path>> kinds = new HashMap<WatchEvent.Kind<Path>, Entry<Path, Path>>();                       
                        for (WatchEvent<?> i : key.pollEvents()) {
                            WatchEvent<Path> event = cast(i);
                            WatchEvent.Kind<Path> kind = event.kind();
                            Path name = event.context();
                            Path child = path.resolve(name);
                            
                            // get king and path Si no és fitxer de control
                            if(!child.toString().endsWith(".odesktop")) {
                                kinds.put(kind, new AbstractMap.SimpleEntry<Path, Path>(path, child));
                            }                            
                            if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                                if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
                                    try {
                                        walk(child, keys, ws);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                        
                        // Process events    
                        if (!kinds.isEmpty())
                            eventProcessing(kinds);
                        
                        if (key.reset() == false) {
                            log.info(KEY_BUNDLE + " Removed directory from watcher: " + keys.get(key));        
                            keys.remove(key);
                            if (keys.isEmpty()) {
                                break;
                            }
                        }
                    }
                }     
                log.info(KEY_BUNDLE + " Stop watcher service ...");        
            }
        });
    }
    
    private void reg(Path dir, final Map<WatchKey, Path> keys, final WatchService ws) throws IOException {
        if (Files.isDirectory(dir, LinkOption.NOFOLLOW_LINKS)) {
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    WatchKey key = dir.register(ws, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                    keys.put(key, dir);
                    log.info(KEY_BUNDLE + " Added directory to watcher: " + dir);      
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    @SuppressWarnings("unchecked")
    private <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    private void walk(Path root, final Map<WatchKey, Path> keys,
            final WatchService ws) throws IOException {
        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir,
                    BasicFileAttributes attrs) throws IOException {
                reg(dir, keys, ws);
                return super.preVisitDirectory(dir, attrs);
            }
        });
    }
    
    private void eventProcessing(Map<WatchEvent.Kind<Path>, Entry<Path, Path>> kinds) {
        if (kinds.keySet().size() == 1) {
            simpleEventProcessing(kinds.keySet().iterator().next(), kinds.values().iterator().next().getKey(), kinds.values().iterator().next().getValue());
        } else if (kinds.keySet().size() == 2) {
            Iterator<WatchEvent.Kind<Path>> itw = kinds.keySet().iterator();
            Iterator<Entry<Path, Path>> ite = kinds.values().iterator();
            
            WatchEvent.Kind<Path> k1 = itw.next();
            WatchEvent.Kind<Path> k2 = itw.next();
            Path s = ite.next().getValue();            
            Path d = ite.next().getValue(); 
            complexEventProcessing(k1, k2, s, d);            
        } else {
            log.info(KEY_BUNDLE + " Uncontrolled Type of event.");      
        }
    }
    
    private void simpleEventProcessing(WatchEvent.Kind<Path> kind, Path path, Path child) {
        
        if(child.toFile().isFile() && ENTRY_CREATE.equals(kind)) {
            //System.out.printf("NOU FITXER: %s%n", child);
        } else if(child.toFile().isFile() && ENTRY_DELETE.equals(kind)) {
            //System.out.printf("FITXER ELMINAT: %s%n", child);
        } else if(child.toFile().isFile() && ENTRY_MODIFY.equals(kind)) {
            //System.out.printf("FITXER MODIFICAT: %s%n", child);
        } else if(!child.toFile().isFile() && ENTRY_DELETE.equals(kind)) {
            //System.out.printf("%CARPETA ELMINADA: %s%n", child);
        }
    }
    
    private void complexEventProcessing(WatchEvent.Kind<Path> kind1, WatchEvent.Kind<Path> kind2, Path path1, Path path2) {
        if ((ENTRY_DELETE.equals(kind1) && ENTRY_CREATE.equals(kind2)) || (ENTRY_CREATE.equals(kind1) && ENTRY_DELETE.equals(kind2))) {
            // Rename event
            String source = null;
            String destination = null;
            if (!path1.toFile().isHidden() && !path2.toFile().isHidden()) {
                if (path1.toFile().exists()) {
                    source = path2.toString();
                    destination = path1.toString();
                } else {
                    source = path1.toString();
                    destination = path2.toString();
                }

                SynchronizedObject sobj = null;
                // Buscamos si el objeto renombrado ya está bajo el control del sincronizador
                for (SynchronizedObject o : repository.getAllSynchronizeObjects()) {
                    if (source.equals(o.getLocalPath())) {
                        sobj = o;
                    }
                }
                // Buscamos si el objeto renombrado ya está bajo el control del sincronizador de documentos
                if (sobj == null) {
                    for (SynchronizedObject o : repository.getSyncronizedDocuments()) {
                        if (source.equals(o.getLocalPath())) {
                            sobj = o;
                        }
                    }
                }
                if (sobj != null) {
                    Task t = new Task(sobj, FileSystems.getDefault().getPath(destination), repository);
                    t.run();
                } else {                    
                    // Rename de un objeto que aun no está bajo control del sincronizador  
                    // no hacemos nada
                }
            }
        }
    }
    
    private class Task implements Runnable {
        SynchronizedObject o;
        Path destination;
        SynchronizedRepository repository;

        public Task(SynchronizedObject o, Path destination, SynchronizedRepository repository) {
            this.o = o;
            this.destination = destination;
            this.repository = repository;
        }
                        

		@Override
		public void run() {

			// lock new path
			SynchronizeLock lock = SynchronizeLock.getSynchronizeLock();
			log.debug(KEY_BUNDLE + " lock path:" + destination.toString());
			lock.addPath(destination);

			SynchronizeService service = SynchronizeService.getInstance(webservice, log);
			service.renameSynchronizedObject(o, destination, repository);

			// Unlock path
			log.debug(KEY_BUNDLE + " Unlock path:" + destination.toString());
			lock.deletePath(destination);
		}

	}
    
    public <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Entry<T, E> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

}
