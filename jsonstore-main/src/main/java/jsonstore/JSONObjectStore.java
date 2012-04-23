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

import org.codehaus.jettison.json.JSONObject;

import krati.io.Serializer;
import krati.store.DataStore;
import krati.store.SerializableObjectStore;

/**
 * JSONObjectStore
 * 
 * @author jwu
 * @since 04/20, 2012
 */
public class JSONObjectStore extends SerializableObjectStore<Long, JSONObject> {
    
    /**
     * Constructs a new instance of JSONObjectStore.
     * 
     * @param store - the underlying {@link DataStore}.
     * @param keySerializer - the key serializer of JSONObjectStore
     * @param valueSerializer - the value serializer of JSONObjectStore
     */
    public JSONObjectStore(DataStore<byte[], byte[]> store,
                           Serializer<Long> keySerializer,
                           Serializer<JSONObject> valueSerializer) {
        super(store, keySerializer, valueSerializer);
    }
}
