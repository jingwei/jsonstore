# JSONStore Webapp

JSONStore Webapp provides a RESTful interface for storing and retrieving JSON objects.
It is built on Jackson, Jersey, Jetty and [Krati](http://sna-projects.com/krati/).

### Homepage

Find out more about JSONStore at [https://github.com/jingwei/jsonstore](https://github.com/jingwei/jsonstore).

### License

Apache Public License (APL) 2.0

### Artifact

    jsonstore-webapp.war

### Build the war

    mvn clean package

### Maven Coordinate

    groupId: jsonstore
    artifactId: jsonstore-webapp
    version: 0.1

### Setup Eclipse

Setup Eclipse by executing the command below:

    mvn eclipse:eclipse

Inside Eclipse, select Preferences > Java > Build Path > Classpath Variables. Define a new classpath variable M2_REPO and assign maven repository.

For more information, check out http://maven.apache.org/guides/mini/guide-ide-eclipse.html

### Launch JSONStore Webapp 

Launch the jsonstore-webapp application using the next command from the jsonstore/jsonstore-webapp folder:

    MAVEN_OPTS="-Xms1g -Xmx1g" mvn clean jetty:run -Djsonstore.instance.home=repository

Depending on the size of data sets, you may have to modify the JVM heap size accordingly via MAVEN_OPTS.
For example, you can increase the JVM heap size to 8g using <code>MAVEN_OPTS="-Xms8g -Xmx8g"</code>.

### Perform REST Operations

Creates a JSON store for "News".

    curl -X POST -H "Content-type: application/json" http://localhost:9010/jsonstore/News -d '
    {
      "initialCapacity": 1000000
    }
    '

The parameter <code>initialCapacity</code> cannot be modified once the JSON store instance is up and running.
So this parameter must be set properly based on the estimated size of data sets.

Creates a customized JSON store using the JSON schema below.

    {
      "name" : "config",
      "type" : "object",
      "properties" :
      {
        "initialCapacity": { "type": "number" },
        "batchSize": { "type": "number" },
        "numSyncBatches": { "type": "number" },
        "segmentFileSizeMB": { "type": "number" },
        "segmentFactoryClass": { "type": "string" },
        "keySerializerClass": { "type": "string" },
        "valueSerializerClass": { "type": "string" }
      }
    }
    
    curl -X POST -H "Content-type: application/json" http://localhost:9010/jsonstore/News -d '
    {
      "initialCapacity": 1000000,
      "batchSize": 1000,
      "numSyncBatches": 10,
      "segmentFileSizeMB": 128,
      "segmentFactoryClass": "krati.core.segment.WriteBufferSegmentFactory",
      "keySerializerClass": "jsonstore.PathKeyLongSerializer",
      "valueSerializerClass": "jsonstore.JSONObjectSerializer"
    }
    '
    
Puts the JSON schema of the specified JSON store.

    curl -X PUT -H "Content-type: application/json" http://localhost:9010/jsonstore/News -d '
    {
      "name" : "News",
      "type" : "object",
      "properties" : {
        "id": { "type": "number", "required": true },
        "title": { "type" : "string", "required": true },
        "summary": { "type" : "string" },
        "timestamp": { "type" : "number", "required": true }
      }
    }
    '

Gets the JSON schema of the specified JSON store.

    curl -X GET -H "Accept: application/json" http://localhost:9010/jsonstore/News

Removes the specified JSON store permanently.

    curl -X DELETE http://localhost:9010/jsonstore/News

Gets a JSON object.

    curl -X GET -H "Accept: application/json" http://localhost:9010/jsonstore/News/12345

Bulk-Gets a number of JSON objects.

    curl -X GET -H "Accept: application/json" http://localhost:9010/jsonstore/News?keys=12345,100029

Deletes a JSON object.

    curl -X DELETE -H "Accept: application/json" http://localhost:9010/jsonstore/News/12345

Puts/Updates a JSON object.

    curl -X PUT -H "Content-type: application/json" http://localhost:9010/jsonstore/News/12345 -d '
    {"id":12345,"timestamp":1334210734541,"title":"Facebook clarifies changes to its terms of use"}
    '

Posts/Inserts a JSON object.

    curl -X POST -H "Content-type: application/json" http://localhost:9010/jsonstore/News/12345 -d '
    {"id":12345,"timestamp":1334210734541,"title":"Facebook clarifies changes to its terms of use"}
    '

Syncs/Flushes a JSON store

    curl -X POST http://localhost:9010/jsonstore/News/sync
    curl -X POST http://localhost:9010/jsonstore/News/flush

