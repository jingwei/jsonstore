# JSONStore 

JSONStore is for storing and retrieving JSON objects via the REST API.
It is built on Jackson, Jersey, Jetty and [Krati](http://sna-projects.com/krati/).

### Homepage

Find out more about JSONStore at [https://github.com/jingwei/jsonstore](https://github.com/jingwei/jsonstore).

### License

Apache Public License (APL) 2.0

### Artifact

    jsonstore.jar
    jsonstore-webapp.war

### Build the jar

    mvn clean package

### Maven Coordinate

    groupId: jsonstore
    artifactId: jsonstore
    version: 0.1

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

    MAVEN_OPTS="-Xms1g -Xmx1g" mvn clean jetty:run -Djsonstore.instance.home=repository -Djsonstore.instance.initial.capacity=100000

The parameter <code>jsonstore.instance.initial.capacity</code> cannot be modified once the JSONRepository instance is up and running.
So this parameter must be set properly based on the estimated size of data sets.

Depending on the size of data sets, you may have to modify the JVM heap size accordingly via MAVEN_OPTS.
For example, you can increase the JVM heap size to 8g using <code>MAVEN_OPTS="-Xms8g -Xmx8g"</code>.

For more information on REST operations, check out [jsonstore/jsonstore-webapp/README.md](https://github.com/jingwei/jsonstore/blob/master/jsonstore-webapp/README.md).
