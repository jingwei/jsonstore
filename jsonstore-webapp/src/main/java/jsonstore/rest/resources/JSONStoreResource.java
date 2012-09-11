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

package jsonstore.rest.resources;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sun.jersey.api.core.InjectParam;
import com.sun.jersey.api.spring.Autowire;

import jsonstore.JSONObjectStore;
import jsonstore.JSONRepository;
import jsonstore.JSONStoreStatus;

/**
 * JSONStoreResource
 * 
 * @author jwu
 * @since 04/20, 2012
 */
@Autowire
@Path("/")
public class JSONStoreResource {
    final static Logger logger = Logger.getLogger(JSONStoreResource.class);
    
    @InjectParam
    JSONRepository repository;
    
    @Context
    UriInfo uriInfo;
    
    @GET
    @Path("/{source}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getStore(@PathParam("source")String source) {
        List<String> list = uriInfo.getQueryParameters().get("keys");
        
        if(list == null || list.size() == 0) {
            try {
                if(repository.has(source)) {
                    String schemaStr = repository.getSchema(source);
                    if(schemaStr != null) {
                        return Response.status(Status.OK).entity(schemaStr).build();
                    } else {
                        JSONObject status = JSONStoreStatus.FOUND.build(source);
                        return Response.status(Status.OK).entity(status).build();
                    }
                } else {
                    JSONObject status = JSONStoreStatus.NOT_FOUND.build(source);
                    return Response.status(Status.OK).entity(status).build();
                }
            } catch (Exception e) {
                e.printStackTrace();
                JSONObject status = JSONStoreStatus.FAILED.build(source, e.getMessage());
                return Response.status(Status.INTERNAL_SERVER_ERROR).entity(status).build();
            }
        } else {
            JSONObjectStore<String> jsonStore = repository.get(source);
            Map<String, JSONObject> kvMap = new HashMap<String, JSONObject>(); 
            
            for(String param : list) {
                String[] keys = param.split(",");
                for(String key : keys) {
                    key = key.trim();
                    if(key.length() > 0) {
                        try {
                            JSONObject value = jsonStore.get(key);
                            kvMap.put(key, value);
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            
            try {
                JSONObject json = new JSONObject().put(source, kvMap);
                return Response.status(Status.OK).entity(json).build();
            } catch (JSONException e) {
                e.printStackTrace();
                JSONObject status = JSONStoreStatus.FAILED.build(source, e.getMessage());
                return Response.status(Status.INTERNAL_SERVER_ERROR).entity(status).build();
            }
        }
    }
    
    @PUT
    @Path("/{source}")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response putStore(@PathParam("source")String source, JSONObject schema) {
        try {
            if(!repository.has(source)) {
                repository.create(source);
            }
            
            String schemaStr = schema.toString(2);
            repository.putSchema(source, schemaStr);
            logger.info(source + " schema: " + schemaStr);
            
            JSONObject status = JSONStoreStatus.UPDATED.build(source, "schema added");
            return Response.status(Status.OK).entity(status).build();
        } catch (Exception e) {
            e.printStackTrace();
            JSONObject status = JSONStoreStatus.FAILED.build(source, e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(status).build();
        }
    }
    
    @POST
    @Path("/{source}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response postStore(@PathParam("source")String source, JSONObject config) {
        try {
            // Save the JSON configuration
            if(!repository.knows(source)) {
                repository.putConfig(source, config.toString(2));
            }
            
            // Creates the JSON store
            if(!repository.has(source)) {
                repository.create(source);
                
                JSONObject status = JSONStoreStatus.CREATED.build(source);
                return Response.status(Status.OK).entity(status).build();
            } else {
                JSONObject status = JSONStoreStatus.FOUND.build(source);
                return Response.status(Status.OK).entity(status).build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            JSONObject status = JSONStoreStatus.FAILED.build(source, e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(status).build();
        }
    }
    
    @DELETE
    @Path("/{source}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response deleteStore(@PathParam("source")String source) {
        try {
            if(repository.knows(source)) {
                repository.remove(source);
                
                JSONObject status = JSONStoreStatus.DELETED.build(source);
                return Response.status(Status.OK).entity(status).build();
            } else {
                
                JSONObject status = JSONStoreStatus.NOT_FOUND.build(source);
                return Response.status(Status.OK).entity(status).build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            JSONObject status = JSONStoreStatus.FAILED.build(source, e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(status).build();
        }
    }
    
    @GET
    @Path("/{source}/{key}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response doStoreGet(@PathParam("source")String source, @PathParam("key")String key) {
        try {
            JSONObjectStore<String> jsonStore = repository.get(source);
            JSONObject value = jsonStore.get(key);
            return Response.status(Status.OK).entity(value).build();
        } catch (Exception e) {
            JSONObject status = JSONStoreStatus.FAILED.build(source, e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(status).build();
        }
    }
    
    @PUT
    @Path("/{source}/{key}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response doStorePut(@PathParam("source")String source, @PathParam("key")String key, JSONObject value) {
        try {
            JSONObjectStore<String> jsonStore = repository.get(source);
            JSONObject old = jsonStore.get(key);
            jsonStore.put(key, value);
            return Response.status(Status.OK).entity(old).build();
        } catch (Exception e) {
        	e.printStackTrace();
            JSONObject status = JSONStoreStatus.FAILED.build(source, e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(status).build();
        }
    }
    
    @POST
    @Path("/{source}/{key}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response doStorePost(@PathParam("source")String source, @PathParam("key")String key, JSONObject value) {
        try {
            JSONObjectStore<String> jsonStore = repository.get(source);
            jsonStore.put(key, value);
            
            JSONObject status = JSONStoreStatus.UPDATED.build(source);
            return Response.status(Status.OK).entity(status).build();
        } catch (Exception e) {
        	e.printStackTrace();
            JSONObject status = JSONStoreStatus.FAILED.build(source, e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(status).build();
        }
    }
    
    @DELETE
    @Path("/{source}/{key}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response doStoreDelete(@PathParam("source")String source, @PathParam("key")String key) {
        try {
            JSONObjectStore<String> jsonStore = repository.get(source);
            JSONObject deleted = jsonStore.get(key);
            jsonStore.delete(key);
            return Response.status(Status.OK).entity(deleted).build();
        } catch (Exception e) {
            e.printStackTrace();
            JSONObject status = JSONStoreStatus.FAILED.build(source, e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(status).build();
        }
    }
    
    @POST
    @Path("/{source}/flush")
    @Produces({MediaType.APPLICATION_JSON})
    public Response flush(@PathParam("source")String source) {
        try {
            JSONObjectStore<String> jsonStore = repository.get(source);
            if(jsonStore != null) {
                jsonStore.persist();
                
                JSONObject status = JSONStoreStatus.FLUSHED.build(source, "persist");
                return Response.status(Status.OK).entity(status).build();
            } else {
                JSONObject status = JSONStoreStatus.NOT_FOUND.build(source);
                return Response.status(Status.OK).entity(status).build();
            }
        } catch (Exception e) {
            JSONObject status = JSONStoreStatus.FAILED.build(source, e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(status).build();
        }
    }
    
    @POST
    @Path("/{source}/sync")
    @Produces({MediaType.APPLICATION_JSON})
    public Response sync(@PathParam("source")String source) {
        try {
            JSONObjectStore<String> jsonStore = repository.get(source);
            if(jsonStore != null) {
                jsonStore.sync();
                
                JSONObject status = JSONStoreStatus.FLUSHED.build(source, "sync");
                return Response.status(Status.OK).entity(status).build();
            } else {
                JSONObject status = JSONStoreStatus.NOT_FOUND.build(source);
                return Response.status(Status.OK).entity(status).build();
            }
        } catch (Exception e) {
            JSONObject status = JSONStoreStatus.FAILED.build(source, e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(status).build();
        }
    }
}
