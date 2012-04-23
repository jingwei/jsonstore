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
