/*
 * Copyright (c) 2012 Jingwei Wu
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package jsonstore;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.jettison.json.JSONObject;

import krati.core.StoreConfig;
import krati.core.StoreFactory;
import krati.core.segment.WriteBufferSegmentFactory;
import krati.io.Serializer;
import krati.io.serializer.LongSerializer;
import krati.store.DataStore;

/**
 * JSONRepository is configurable via the following parameters:  
 * 
 * <pre>
 *   -Djsonstore.instance.home=&lt;homeDir&gt;
 *   -Djsonstore.instance.initial.capacity=&lt;initialCapacity&gt;
 * </pre>
 * 
 * <p>
 * The parameter <code>jsonstore.instance.initial.capacity</code> cannot be modified
 * once the JSONRepository instance is up and running. So this parameter must be set
 * properly based on the estimated size of data sets.
 * </p>
 * 
 * @author jwu
 * @since 04/20, 2012
 */
public final class JSONRepository {
    /**
     * The home directory of repository.
     */
    private File homeDir;
    
    /**
     * The multi-JSONObjectStore repository.
     */
    private final Map<String, JSONObjectStore> repository =
        new ConcurrentHashMap<String, JSONObjectStore>();
    
    /**
     * Constructs a new instance of JSONRepository.
     * 
     * @throws Exception if the repository cannot be instantiated.
     */
    public JSONRepository() throws Exception {
        File[] files = getHomeDir().listFiles();
        for(File file : files) {
            if(file.isDirectory()) {
                String source = file.getName();
                JSONObjectStore jsonStore = create(source);
                repository.put(source, jsonStore);
            }
        }
        
        // Adds shutdown hook to sync store changes
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public synchronized void run() {
                for(String source : repository.keySet()) {
                    try {
                        repository.get(source).sync();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    
    /**
     * Checks if there exists an active store for the specified <code>source</code>.
     * 
     * @param source - the source (i.e., store name)
     * @return <code>true</code> if the specified <code>source</code> is present and the associated store is open.
     * Otherwise, <code>false</code>.
     */
    public boolean has(String source) {
        return repository.containsKey(source);
    }
    
    /**
     * Checks if there exists a store for the specified <code>source</code>, regardless the associated store is open or not.
     * 
     * @param source - the source (i.e., store name)
     * @return <code>true</code> if the specified <code>source</code> is present.
     * Otherwise, <code>false</code>.
     */
    public boolean knows(String source) {
        if(repository.containsKey(source)) {
            return true;
        }
        
        try {
            return getStoreDir(source, false).exists();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Gets the store associated with the specified <code>source</code>.
     * 
     * @param source - the source (i.e., store name)
     */
    public JSONObjectStore get(String source) {
        return repository.get(source);
    }
    
    /**
     * Associates the specified <code>jsonStore</code> with the specified <code>source</code> in this repository.
     * 
     * @param source - the source (i.e., store name)
     * @param jsonStore - the JSON store
     */
    public void put(String source, JSONObjectStore jsonStore) {
        repository.put(source, jsonStore);
    }
    
    /**
     * Opens the specified <code>source</code> if it is present..
     * 
     * @param source - the source (i.e., store name)
     * @return <code>true</code> if the specified <code>source</code> is present.
     * Otherwise, <code>false</code>.
     */
    public boolean open(String source) throws Exception {
        if(repository.containsKey(source)) {
            repository.get(source).open();
            return true;
        } else {
            File storeDir = getStoreDir(source, false);
            if(storeDir.exists()) {
                create(source);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Closes the specified <code>source</code> if it is present..
     * 
     * @param source - the source (i.e., store name)
     * @return <code>true</code> if the specified <code>source</code> is present and the associated store is closed successfully.
     * Otherwise, <code>false</code>.
     * @throws Exception
     */
    public boolean close(String source) throws Exception {
        JSONObjectStore store = repository.remove(source);
        if(store != null) {
            store.close();
            return true;
        } else {
            return getStoreDir(source, false).exists();
        }
    }
    
    /**
     * Removes the specified <code>source</code> from this repository if it is present.
     * The JSON store associated with the specified <code>source</code> will be removed
     * permanently.
     * 
     * @param source - the source (i.e., store name)
     */
    public void remove(String source) {
        JSONObjectStore store = repository.remove(source);
        try {
            if(store != null) {
                store.close();
            }
            
            File storeDir = getStoreDir(source, false);
            if(storeDir.exists()) {
                deleteDirectory(storeDir);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Gets the home directory of this JSONRepository.
     * 
     * @throws Exception
     */
    public File getHomeDir() throws IOException {
        if(homeDir == null) {
            String path = System.getProperty("jsonstore.instance.home");
            homeDir = new File(path);
            if(!homeDir.exists()) {
                homeDir.mkdirs();
            }
        }
        
        return homeDir;
    }
    
    /**
     * Gets the JSON store directory.
     * 
     * @param source - the source (i.e., store name)
     * @param create - whether create the store directory
     * @return the JSON store directory
     * @throws IOException
     */
    protected File getStoreDir(String source, boolean create) throws IOException {
        File storeDir = new File(getHomeDir(), source);
        if(create && !storeDir.exists()) {
            storeDir.mkdirs();
        }
        
        return storeDir;
    }
    
    /**
     * Create a new JSON store for the specified <code>source</code>.
     * 
     * @param source - the source (i.e., store name)
     * @return the created JSON store
     * @throws Exception if the JSON store cannot be created for any reasons.
     */
    public synchronized JSONObjectStore create(String source) throws Exception {
        if(has(source)) {
            return get(source);
        }
        
        // Create the JSON store directory
        File storeDir = getStoreDir(source, true);
        
        int initialCapacity = 1000000;
        String value = System.getProperty("jsonstore.instance.initial.capacity");
        if(value != null) {
            initialCapacity = Integer.parseInt(value);
        }
        
        StoreConfig config = new StoreConfig(storeDir, initialCapacity); // Customize
        config.setSegmentFactory(new WriteBufferSegmentFactory());       // Customize
        config.setSegmentFileSizeMB(128);                                // Customize
        config.setBatchSize(1000);                                       // Customize
        config.setNumSyncBatches(10);                                    // Customize
        
        Serializer<Long> keySerializer = new LongSerializer();           // Customize
        Serializer<JSONObject> valueSerializer = new JSONObjectSerializer();
        DataStore<byte[], byte[]> store = StoreFactory.createIndexedDataStore(config);
        JSONObjectStore jsonStore = new JSONObjectStore(store, keySerializer, valueSerializer);
        repository.put(source, jsonStore);
        return jsonStore; 
    }
    
    /**
     * Gets the JSON schema associated with the specified <code>source</code>.
     * 
     * @param source - the source (i.e., store name)
     * @return the JSON schema string
     */
    public synchronized String getSchema(String source) {
        try {
            File storeDir = getStoreDir(source, false);
            File schemaFile = new File(storeDir, "schema.json");
            if(schemaFile.exists()) {
                int length = 0;
                char[] buffer = new char[2048];
                StringBuilder builder = new StringBuilder();
                
                Reader in = new InputStreamReader(new FileInputStream(schemaFile), "UTF-8");
                while((length = in.read(buffer, 0, buffer.length)) > 0) {
                    builder.append(buffer, 0, length);
                }
                in.close();
                
                return builder.toString();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Associates the specified JSON schema with the specified <code>source</code>.
     * 
     * @param source - the source (i.e., store name)
     * @param schemaStr - the JSON schema string
     * @throws IOException
     */
    public synchronized void putSchema(String source, String schemaStr) throws IOException {
        File storeDir = getStoreDir(source, true);
        File schemaFile = new File(storeDir, "schema.json");
        PrintWriter writer = new PrintWriter(schemaFile, "UTF-8");
        writer.println(schemaStr);
        writer.close();
    }
    
    /**
     * Removes the JSON schema associated with the specified <code>source</code>.
     * 
     * @param source - the source (i.e., store name)
     * @return the JSON schema string if the JSON schema is present
     */
    public synchronized String removeSchema(String source) {
        String schemaStr = null;
        try {
            File storeDir = getStoreDir(source, false);
            File schemaFile = new File(storeDir, "schema.json");
            if(schemaFile.exists()) {
                schemaStr = getSchema(source);
                schemaFile.delete();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        return schemaStr;
    }
    
    /**
     * Deletes the specified file directory.
     * 
     * @param dir - the directory to delete
     * @throws IOException
     */
    static void deleteDirectory(File dir) throws IOException {
        File[] files = dir.listFiles();
        
        for (File f : files) {
            if (f.isDirectory()) {
                deleteDirectory(f);
            } else {
                boolean deleted = f.delete();
                if (!deleted) {
                    throw new IOException(f.getAbsolutePath() + " not deleted");
                }
            }
        }
        
        boolean deleted = dir.delete();
        if (!deleted) {
            throw new IOException(dir.getAbsolutePath() + " not deleted");
        }
    }
}
