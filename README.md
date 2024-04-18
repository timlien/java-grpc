# GPRC in Java

## Introduction

This is a simple example of how to use gRPC in Java. The example is a simple client-server application where the client sends a request to the server and the server responds with a message.

## Communication Pattern

gRPC supports four types of communication patterns:

### Unary

The client sends a single request to the server and gets a single response back.

### Server Streaming

The client sends a single request to the server and gets a stream of responses back.

### Client Streaming

The client sends a stream of requests to the server and gets a single response back.

### Bidirectional Streaming

The client sends a stream of requests to the server and gets a stream of responses back.