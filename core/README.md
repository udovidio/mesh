# Gentics Mesh - Core

The core contains the actual main business logic implementation.

## Class RestAPIVerticle

The class [RestAPIVerticle](src/main/java/com/gentics/mesh/rest/RestAPIVerticle.java) is used to provide all REST API Endpoints,
which are implemented in subclasses of [AbstractInternalEndpoint](../common/src/main/java/com/gentics/mesh/router/route/AbstractInternalEndpoint.java).

## Package com.gentics.mesh.core.endpoint

This package contains classes that implement REST API Endpoints.

Classes like [UserEndpoint](src/main/java/com/gentics/mesh/core/endpoint/user/UserEndpoint.java) create the routes
and delegate handling to handler classes like [UserCrudHandler](src/main/java/com/gentics/mesh/core/endpoint/user/UserCrudHandler.java).

