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
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.sun.jersey.api.spring.Autowire;
import com.sun.jersey.spi.inject.Inject;

import jsonstore.JSONObjectStore;
import jsonstore.JSONRepository;

/**
 * JSONStoreResource
 * 
 * @author jwu
 * @since 04/20, 2012
 */
@Autowire
@Component
@Scope("request")
@Path("/")
public class JSONStoreResource {
    final static Logger logger = Logger.getLogger(JSONStoreResource.class);
    
    @Inject
    JSONRepository repository;
    
    @Context
    UriInfo uriInfo;
    
    @Context
    Request request;
    
    @GET
    @Path("/{source}")
    @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    public Response getStore(@PathParam("source")String source) {
        List<String> list = uriInfo.getQueryParameters().get("keys");
        
        if(list == null || list.size() == 0) {
            try {
                if(repository.has(source)) {
                    String schemaStr = repository.getSchema(source);
                    if(schemaStr != null) {
                        return Response.status(Status.OK).entity(schemaStr).build();
                    } else {
                        return Response.status(Status.OK).entity(source + " found").build();
                    }
                } else {
                    return Response.status(Status.OK).entity(source + " not found").build();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
            }
        } else {
            JSONObjectStore jsonStore = repository.get(source);
            Map<Long, JSONObject> kvMap = new HashMap<Long, JSONObject>(); 
            
            for(String param : list) {
                String[] parts = param.split(",");
                for(String s : parts) {
                    s = s.trim();
                    if(s.length() > 0) {
                        try {
                            long key = Long.parseLong(s);
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
                return Response.status(Status.INTERNAL_SERVER_ERROR).build();
            }
        }
    }
    
    @PUT
    @Path("/{source}")
    @Produces({MediaType.TEXT_PLAIN})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response putStore(@PathParam("source")String source, JSONObject schema) {
        try {
            if(!repository.has(source)) {
                repository.create(source);
            }
            
            String schemaStr = schema.toString(2);
            repository.putSchema(source, schemaStr);
            logger.info(source + " schema: " + schemaStr);
            
            return Response.status(Status.OK).entity(source + " schema added").build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    @POST
    @Path("/{source}")
    @Produces({MediaType.TEXT_PLAIN})
    public Response createStore(@PathParam("source")String source) {
        try {
            if(!repository.has(source)) {
                repository.create(source);
                return Response.status(Status.OK).entity(source + " created").build();
            } else {
                return Response.status(Status.OK).entity(source + " found").build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    @DELETE
    @Path("/{source}")
    @Produces({MediaType.TEXT_PLAIN})
    public Response removeStore(@PathParam("source")String source) {
        try {
            if(repository.knows(source)) {
                repository.remove(source);
                return Response.status(Status.OK).entity(source + " removed").build();
            } else {
                return Response.status(Status.OK).entity(source + " not found").build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    @GET
    @Path("/{source}/{key}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response doStoreGet(@PathParam("source")String source, @PathParam("key")long key) {
        try {
            JSONObjectStore jsonStore = repository.get(source);
            JSONObject value = jsonStore.get(key);
            return Response.status(Status.OK).entity(value).build();
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PUT
    @Path("/{source}/{key}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response doStorePut(@PathParam("source")String source, @PathParam("key")long key, JSONObject value) {
        try {
            JSONObjectStore jsonStore = repository.get(source);
            JSONObject old = jsonStore.get(key);
            jsonStore.put(key, value);
            return Response.status(Status.OK).entity(old).build();
        } catch (Exception e) {
        	e.printStackTrace();
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @POST
    @Path("/{source}/{key}")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response doStorePost(@PathParam("source")String source, @PathParam("key")long key, JSONObject value) {
        try {
            JSONObjectStore jsonStore = repository.get(source);
            jsonStore.put(key, value);
            return Response.status(Status.OK).build();
        } catch (Exception e) {
        	e.printStackTrace();
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @DELETE
    @Path("/{source}/{key}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response doStoreDelete(@PathParam("source")String source, @PathParam("key")long key) {
        try {
            JSONObjectStore jsonStore = repository.get(source);
            JSONObject deleted = jsonStore.get(key);
            jsonStore.delete(key);
            return Response.status(Status.OK).entity(deleted).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @POST
    @Path("/{source}/flush")
    @Produces({MediaType.TEXT_PLAIN})
    public Response flush(@PathParam("source")String source) {
        try {
            JSONObjectStore jsonStore = repository.get(source);
            if(jsonStore != null) {
                jsonStore.persist();
                return Response.status(Status.OK).entity("OK").build();
            } else {
                return Response.status(Status.OK).entity(source + " not found").build();
            }
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    @POST
    @Path("/{source}/sync")
    @Produces({MediaType.TEXT_PLAIN})
    public Response sync(@PathParam("source")String source) {
        try {
            JSONObjectStore jsonStore = repository.get(source);
            if(jsonStore != null) {
                jsonStore.sync();
                return Response.status(Status.OK).entity("OK").build();
            } else {
                return Response.status(Status.OK).entity(source + " not found").build();
            }
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}
