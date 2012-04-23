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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.codehaus.jettison.json.JSONObject;

import krati.io.SerializationException;
import krati.io.Serializer;

/**
 * JSONObjectSerializer
 * 
 * @author jwu
 * @since 04/20, 2012
 */
public class JSONObjectSerializer implements Serializer<JSONObject> {
    
    public JSONObjectSerializer() {}
    
    @Override
    public JSONObject deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null) {
            return null;
        }
        
        try {
            bytes = decompress(bytes);
            String jsonStr = new String(bytes);
            return new JSONObject(jsonStr);
        } catch (Exception e) {
            throw new SerializationException("Failed to deserialize", e);
        }
    }
    
    @Override
    public byte[] serialize(JSONObject json) throws SerializationException {
        if (json == null) {
            return null;
        }
        
        try {
            byte[] bytes = json.toString().getBytes();
            return compress(bytes);
        } catch (Exception e) {
            throw new SerializationException("Failed to serialize", e);
        }
    }

    public static byte[] decompress(byte[] bytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        GZIPInputStream gzis = new GZIPInputStream(bais);
        List<byte[]> list = new LinkedList<byte[]>();
        byte[] buf = new byte[2048];
        
        int len = gzis.read(buf, 0, 2048);
        int total = 0;
        while (len > 0) {
            byte[] b1 = new byte[len];
            System.arraycopy(buf, 0, b1, 0, len);
            list.add(b1);
            total += len;
            len = gzis.read(buf, 0, 2048);
        }
        gzis.close();
        
        byte[] whole = new byte[total];
        int start = 0;
        for (byte[] part : list) {
            System.arraycopy(part, 0, whole, start, part.length);
            start += part.length;
        }
        
        return whole;
    }
    
    public static byte[] compress(byte[] bytes) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPOutputStream gzos = new GZIPOutputStream(baos);
        gzos.write(bytes);
        gzos.close();
        return baos.toByteArray();
    }
}
