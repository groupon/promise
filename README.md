Promise
=======

<a href="https://raw.githubusercontent.com/groupon/promise/master/LICENSE">
    <img src="https://img.shields.io/hexpm/l/plug.svg"
         alt="License: Apache 2">
</a>
<a href="http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.groupon.promise%22%20a%3A%22promise%22">
    <img src="https://img.shields.io/maven-central/v/com.groupon.promise/promise.svg"
         alt="Maven Artifact">
</a>

The Promise library is a Java based implementation of the [Promise A+](http://promisesaplus.com) specification.  As part of the implementation we added the ability to unpack list results for chaining and track overall status of the chain as a whole.  Additionally, there is a deduplication feature which detects equivalent calls on promise functions and short circuits the response.

Usage
-----

Basic use with concrete class:

    Promise<String> promise = new PromiseImpl<String>();
    promise.then(new FixedValueFunction<>("didSomething"));
    promise.fulfill("value");

Basic use with lambda:

    Promise<String> promise = new PromiseImpl<String>();
    promise.thenSync(value -> "didSomething");
    promise.fulfill("value");

Basic use with async lambda:

    Promise<String> promise = new PromiseImpl<String>();
    promise.thenAsync(value -> new DefaultPromiseFuture("didSomething"));
    promise.fulfill("value");

Basic use for lists:

    PromiseList<String> promise = new PromiseListImpl<String>();
    promise.map().then(new FixedValueFunction<>("didSomethingOncePerItemInList"));
    promise.fulfill(Arrays.asList("one", "two"));

Perform operation after chain completes:

    Promise<String> promise = new PromiseImpl<String>();
    promise.then(new FixedValueFunction<>("didSomething"));
    promise.after().then(new FixedValueFunction<>("success"), new FixedValueFuncion<>("failure"));

Mark promise optional (default is false):

    Promise<String> promise = new PromiseImpl<String>();
    future.thenSync(value -> { throw new Exception("failed"); }).optional(true);
    promise.fulfill("value");
    promise.after().then(new FixedValueFunction<>("success"), new FixedValueFuncion<>("failure"));

Prevent duplicate work (default is true):

    Promise<String> promise = new PromiseImpl<String>().nonduplicating(true);
    promise.then(new FixedValueFunction<>("didSomething"));
    promise.then(new FixedValueFunction<>("didSomething"));
    promise.fulfill("value");

Building
--------

Prerequisites:
* [JDK8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* [Maven 3.3.3+](http://maven.apache.org/download.cgi)

Building:

    promise> mvn verify

To use the local version you must first install it locally:

    promise> mvn install

You can determine the version of the local build from the pom file.  Using the local version is intended only for testing or development.


License
-------

Published under Apache Software License 2.0, see LICENSE

&copy; Groupon Inc., 2015
