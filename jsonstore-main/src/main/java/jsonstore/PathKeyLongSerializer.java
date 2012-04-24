package jsonstore;

import krati.io.SerializationException;
import krati.io.Serializer;
import krati.io.serializer.LongSerializer;

/**
 * PathKeyLongSerializer
 * 
 * @author jwu
 * @since 04/23, 2012
 */
public class PathKeyLongSerializer implements Serializer<String> {
    private final Serializer<Long> serializer;
    
    /**
     * Constructs a new instance of PathKeyLongSerializer.
     */
    public PathKeyLongSerializer() {
        this.serializer = new LongSerializer();
    }
    
    @Override
    public String deserialize(byte[] keyBytes) throws SerializationException {
        return serializer.deserialize(keyBytes).toString();
    }
    
    @Override
    public byte[] serialize(String key) throws SerializationException {
        try {
            Long obj = new Long(key);
            return serializer.serialize(obj);
        } catch(Exception e) {
            throw new SerializationException("Failed to serialize: " + key, e);
        }
    }
}
