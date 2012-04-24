package jsonstore;

import krati.io.SerializationException;
import krati.io.Serializer;
import krati.io.serializer.IntSerializer;

/**
 * PathKeyIntSerializer
 * 
 * @author jwu
 * @since 04/23, 2012
 */
public class PathKeyIntSerializer implements Serializer<String> {
    private final Serializer<Integer> serializer;
    
    /**
     * Constructs a new instance of PathKeyIntSerializer.
     */
    public PathKeyIntSerializer() {
        this.serializer = new IntSerializer();
    }
    
    @Override
    public String deserialize(byte[] keyBytes) throws SerializationException {
        return serializer.deserialize(keyBytes).toString();
    }
    
    @Override
    public byte[] serialize(String key) throws SerializationException {
        try {
            Integer obj = new Integer(key);
            return serializer.serialize(obj);
        } catch(Exception e) {
            throw new SerializationException("Failed to serialize: " + key, e);
        }
    }
}
