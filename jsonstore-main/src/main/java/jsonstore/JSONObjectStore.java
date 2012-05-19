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

import java.io.IOException;
import java.util.Map.Entry;

import org.codehaus.jettison.json.JSONObject;

import krati.io.Serializer;
import krati.store.DataStore;
import krati.store.ObjectStore;
import krati.store.SerializableObjectStore;
import krati.util.IndexedIterator;

/**
 * JSONObjectStore
 * 
 * @author jwu
 * @since 04/20, 2012
 */
public class JSONObjectStore<K> implements ObjectStore<K, JSONObject> {
    /**
     * The internal base store.
     */
    private final SerializableObjectStore<K, JSONObject> baseStore;
    
    /**
     * Constructs a new instance of JSONObjectStore.
     * 
     * @param store - the underlying {@link DataStore}.
     * @param keySerializer - the key serializer of JSONObjectStore
     * @param valueSerializer - the value serializer of JSONObjectStore
     */
    public JSONObjectStore(DataStore<byte[], byte[]> store,
                           Serializer<K> keySerializer,
                           Serializer<JSONObject> valueSerializer) {
        baseStore = new SerializableObjectStore<K, JSONObject>(store, keySerializer, valueSerializer);
    }
    
    @Override
    public byte[] getBytes(K key) {
        return baseStore.getBytes(key);
    }
    
    @Override
    public byte[] getBytes(byte[] keyBytes) {
        return baseStore.getBytes(keyBytes);
    }
    
    @Override
    public int capacity() {
        return baseStore.capacity();
    }
    
    @Override
    public JSONObject get(K key) {
        return baseStore.get(key);
    }
    
    @Override
    public boolean put(K key, JSONObject value) throws Exception {
        return baseStore.put(key, value);
    }
    
    @Override
    public boolean delete(K key) throws Exception {
        return baseStore.delete(key);
    }
    
    @Override
    public void clear() throws IOException {
        baseStore.clear();
    }
    
    @Override
    public IndexedIterator<Entry<K, JSONObject>> iterator() {
        return baseStore.iterator();
    }
    
    @Override
    public IndexedIterator<K> keyIterator() {
        return baseStore.keyIterator();
    }
    
    @Override
    public void persist() throws IOException {
        baseStore.persist();
    }
    
    @Override
    public void sync() throws IOException {
        baseStore.sync();
    }
    
    @Override
    public boolean isOpen() {
        return baseStore.isOpen();
    }
    
    @Override
    public void open() throws IOException {
        baseStore.open();
    }
    
    @Override
    public void close() throws IOException {
        baseStore.close();
    }
}
