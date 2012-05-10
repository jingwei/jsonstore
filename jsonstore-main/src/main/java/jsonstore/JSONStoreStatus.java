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

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * JSONStoreStatus
 * 
 * @author jwu
 * @since 05/09, 2012
 */
public enum JSONStoreStatus {
    FOUND {
        @Override
        public String value() {
            return "found";
        }
    },
    NOT_FOUND {
        @Override
        public String value() {
            return "not found";
        }
    },
    CREATED {
        @Override
        public String value() {
            return "created";
        }
    },
    UPDATED {
        @Override
        public String value() {
            return "updated";
        }
    },
    DELETED {
        @Override
        public String value() {
            return "deleted";
        }
    },
    OPENED {
        @Override
        public String value() {
            return "opened";
        }
    },
    CLOSED {
        @Override
        public String value() {
            return "closed";
        }
    },
    FLUSHED {
        @Override
        public String value() {
            return "flushed";
        }
    },
    FAILED {
        @Override
        public String value() {
            return "failed";
        }
    };
    
    /**
     * @return the status value.
     */
    public abstract String value();
    
    /**
     * Builds the response status via {@link JSONObject}.
     *  
     * @param source - the source (i.e., store name)
     * @return the response status JSONObject
     * @throws JSONException
     */
    public JSONObject build(String source) {
        if(source == null) {
            throw new NullPointerException();
        }
        
        JSONObject json = new JSONObject();
        try {
            json.put("source", source);
            json.put("status", value());
        } catch(Exception e) {}
        
        return json;
    }
    
    /**
     * Builds the response status via {@link JSONObject}.
     *  
     * @param source - the source (i.e., store name)
     * @param message - the message text
     * @return the response status JSONObject
     * @throws JSONException
     */
    public JSONObject build(String source, String message) {
        if(source == null || message == null) {
            throw new NullPointerException();
        }
        
        JSONObject json = new JSONObject();
        try {
            json.put("source", source);
            json.put("status", value());
            json.put("message", message);
        } catch(Exception e) {}
        
        return json;
    }
}
