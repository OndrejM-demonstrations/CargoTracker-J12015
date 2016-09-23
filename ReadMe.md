Reactive improvements of Cargo Tracker
================================

This project provides an example how to enhance an existing Java EE application 
written in a traditional approach in a reactive way to improve its responsiveness.

This project is based on the original [cargo tracker](https://cargotracker.java.net/) 
application which is already modified to separate a pathfinder service into a separate 
microservice accessible by REST.

Starting with the monolith and one extracted microservice, it takes one particular usecase 
of searching for routes for delivering cargo and improves its responsiveness using asynchronous approach in multiple steps.
Later, even the communication between the monolith and microservice is enhanced in asynchrnous way. REST communication is exchanged for a solution based on easy to use message-passing, which is based on distributed CDI event bus provided by Payara Micro runtime. 

Enhancement is demonstrated in several steps, each happens to be in a separate branch:

- `master` - contains the final solution
- `payara-micro` - changes to the original cargo tracker monolith to make it run on Payara Micro (disable JMS, and minor workarounds)
- `j1_01_async_rest_client` - enhancement of the REST client accessing the pathfinder microservice - uses async API, but the request still waits for results to update GUI. CompletableFuture is used to chain executions when computation is completed asynchronously
- `j1_02_async_rest_endpoint` - enhancement of REST endpoint in pathfinder with async API - the computation can be executed in a separate thread or external resource, without blocking the initial request thread, which may be released ASAP. 
- `j1_04_websocket` - added web sockets to also update the UI asynchronously with blocking the initial thread. Web page is loaded ASAP and data is pushed later -> page is lot more responsive. We reverted CompletableFuture to make it possible to send multiple chunks of data ono-by-one using the same future - future is finished immediately and callbacks are chained asynchronously for each piece of response. For more complex usecases, we recommend using [RxJava](https://github.com/ReactiveX/RxJava) observers or reactive streams. But we still have some blocking calls in the pipeline, therefore page still takes unnecessary time to load initially.
- `j1_05_async_cargo_repository` - one more blocking call on DB is made asynchronous. No async JPA calls, therefore we retrieve the data in a background thread and finish the request immediately. When background thread retrieves the data, continues with async computation and data is pushed to the browser via web socket.
- `j1_07_cdi_event_bus` - finally redesigned the communication between monolith and pathfinder microservice to exchange blocking REST call for two-way message passing of CDI events. An event is passed from monolith to pathfinder under a generated id to initiate the computation, it is stored in a map in the monolith. When computation is finished, pathfinder sends another event to monolith, which tries to match it with the initial event and complete the future to continue processing.

To run the demo, execute both monolith and pathfinder using payara-micro runtime, a described below for PathFinder Microservice. For CargoTracker, you may want to use a static port so that you can connect to the web application always with the same URL. So recommended commands to execute:

To run monolith (run before you start pathfinder):
```shell
java -jar payara-micro.jar --deploy cargo-tracker.war --port 8080
```

```shell
java -jar payara-micro.jar --deploy pathfinder-1.0-SNAPSHOT.war --autoBindHttp
```

You can even scale up pathfinder microservice by running the 2nd command mutiple times. The page will deliver results more quickly as both will receive the CDI event to tart the computation, with any further configuration. Pathfinder microservice automatically registers itself with the monolith and the rest of the cluster.

PathFinder Microservice
-----------------------

The Pathfinder micro service was originally embedded into the core Cargo tracker
application. In this project it is separated out as an individual RESTful service
with its own maven project *pathfinder*.

The Pathfinder application can then be run on Payara Micro using the command;

```shell
java -jar payara-micro.jar --deploy pathfinder-1.0-SNAPSHOT.war --autoBindHttp
```

This can also be run multiple times to "scale out" the micro service. 

Load balancing configuration snippets for Nginx and HAProxy are provided at the 
root of the project to show how you would configure these tools to integrate
the cargo tracker application and the pathfinder micro service into a functional
service.

CargoTracker Monolith
---------------------

The *cargo-monolith* maven project is the original Cargo Tracker application with
the path finder functionality extracted and removed. 

To ensure the application knows the URL of the path finder microservice the URL 
must be specified. There are two options for this;
* You must edit the ejb-jar.xml in the WEB-INF directory and set below to the correct URL
```xml
            <env-entry>
                <env-entry-name>graphTraversalUrl</env-entry-name>
                <env-entry-type>java.lang.String</env-entry-type>
                <env-entry-value>http://127.0.0.1/pathfinder/rest/graph-traversal/shortest-path</env-entry-value>
            </env-entry>
```

Alternatively the ExternalRoutingService EJB can also retrieve the URL directly from JNDI.
If you bind the URL to JNDI at /graphTraversalUrlJNDI

This can be done in GlassFish using the asadmin command
```shell
create-custom-resource --restype java.lang.String --factoryclass org.glassfish.resources.custom.factory.PrimitivesAndStringFactory --property value="http\:\/\/127.0.0.1\/pathfinder\/rest\/graph-traversal\/shortest-path" graphTraversalUrlJNDI
 ```

Once this has been configured the cargo-tracker.war file can be deployed to GlassFish

Running the Application
-----------------------

To run the application you must run GlassFish or Payara Server and deploy the 
configured cargo-tracker application as described above. You must also run one or
more Payara Micro instances. Payara Micro automatically binds to the next available
http port starting from 8080 when using the command line above.

Once you have the application deployed and Payara Micro running cargo tracker can
 be accessed via http://127.0.0.1/cargo-tracker/.
To see the Pathfinder micro service in action navigate to;
* Administration Interface
* Book
* run through the booking wizard.

In your Payara Micro log you will see a message like below when the service is called;
```shell
[2015-09-30T15:30:55.051+0100] [Payara 4.1] [INFO] [] [net.java.pathfinder.api.GraphTraversalService] [tid: _ThreadID=23 _ThreadName=http-listener(5)] [timeMillis: 1443623455051] [levelValue: 800] Path Finder Service called for USNYC to USDAL
```
If you are using the nginx snippet or haproxy snippet you should see load balancing between
the Payara Micro instances. Demonstrating how easy it is to scale out the microservice.

