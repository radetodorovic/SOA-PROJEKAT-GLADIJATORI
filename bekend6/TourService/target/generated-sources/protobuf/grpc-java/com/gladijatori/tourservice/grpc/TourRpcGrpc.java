package com.gladijatori.tourservice.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.63.0)",
    comments = "Source: tour_rpc.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class TourRpcGrpc {

  private TourRpcGrpc() {}

  public static final java.lang.String SERVICE_NAME = "tourrpc.TourRpc";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.gladijatori.tourservice.grpc.GetPublishedToursRequest,
      com.gladijatori.tourservice.grpc.TourListReply> getGetPublishedToursMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetPublishedTours",
      requestType = com.gladijatori.tourservice.grpc.GetPublishedToursRequest.class,
      responseType = com.gladijatori.tourservice.grpc.TourListReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.gladijatori.tourservice.grpc.GetPublishedToursRequest,
      com.gladijatori.tourservice.grpc.TourListReply> getGetPublishedToursMethod() {
    io.grpc.MethodDescriptor<com.gladijatori.tourservice.grpc.GetPublishedToursRequest, com.gladijatori.tourservice.grpc.TourListReply> getGetPublishedToursMethod;
    if ((getGetPublishedToursMethod = TourRpcGrpc.getGetPublishedToursMethod) == null) {
      synchronized (TourRpcGrpc.class) {
        if ((getGetPublishedToursMethod = TourRpcGrpc.getGetPublishedToursMethod) == null) {
          TourRpcGrpc.getGetPublishedToursMethod = getGetPublishedToursMethod =
              io.grpc.MethodDescriptor.<com.gladijatori.tourservice.grpc.GetPublishedToursRequest, com.gladijatori.tourservice.grpc.TourListReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetPublishedTours"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.gladijatori.tourservice.grpc.GetPublishedToursRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.gladijatori.tourservice.grpc.TourListReply.getDefaultInstance()))
              .setSchemaDescriptor(new TourRpcMethodDescriptorSupplier("GetPublishedTours"))
              .build();
        }
      }
    }
    return getGetPublishedToursMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.gladijatori.tourservice.grpc.GetTourByIdRequest,
      com.gladijatori.tourservice.grpc.TourReply> getGetTourByIdMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetTourById",
      requestType = com.gladijatori.tourservice.grpc.GetTourByIdRequest.class,
      responseType = com.gladijatori.tourservice.grpc.TourReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.gladijatori.tourservice.grpc.GetTourByIdRequest,
      com.gladijatori.tourservice.grpc.TourReply> getGetTourByIdMethod() {
    io.grpc.MethodDescriptor<com.gladijatori.tourservice.grpc.GetTourByIdRequest, com.gladijatori.tourservice.grpc.TourReply> getGetTourByIdMethod;
    if ((getGetTourByIdMethod = TourRpcGrpc.getGetTourByIdMethod) == null) {
      synchronized (TourRpcGrpc.class) {
        if ((getGetTourByIdMethod = TourRpcGrpc.getGetTourByIdMethod) == null) {
          TourRpcGrpc.getGetTourByIdMethod = getGetTourByIdMethod =
              io.grpc.MethodDescriptor.<com.gladijatori.tourservice.grpc.GetTourByIdRequest, com.gladijatori.tourservice.grpc.TourReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetTourById"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.gladijatori.tourservice.grpc.GetTourByIdRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.gladijatori.tourservice.grpc.TourReply.getDefaultInstance()))
              .setSchemaDescriptor(new TourRpcMethodDescriptorSupplier("GetTourById"))
              .build();
        }
      }
    }
    return getGetTourByIdMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.gladijatori.tourservice.grpc.CreateTourRequest,
      com.gladijatori.tourservice.grpc.TourReply> getCreateTourMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CreateTour",
      requestType = com.gladijatori.tourservice.grpc.CreateTourRequest.class,
      responseType = com.gladijatori.tourservice.grpc.TourReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.gladijatori.tourservice.grpc.CreateTourRequest,
      com.gladijatori.tourservice.grpc.TourReply> getCreateTourMethod() {
    io.grpc.MethodDescriptor<com.gladijatori.tourservice.grpc.CreateTourRequest, com.gladijatori.tourservice.grpc.TourReply> getCreateTourMethod;
    if ((getCreateTourMethod = TourRpcGrpc.getCreateTourMethod) == null) {
      synchronized (TourRpcGrpc.class) {
        if ((getCreateTourMethod = TourRpcGrpc.getCreateTourMethod) == null) {
          TourRpcGrpc.getCreateTourMethod = getCreateTourMethod =
              io.grpc.MethodDescriptor.<com.gladijatori.tourservice.grpc.CreateTourRequest, com.gladijatori.tourservice.grpc.TourReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "CreateTour"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.gladijatori.tourservice.grpc.CreateTourRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.gladijatori.tourservice.grpc.TourReply.getDefaultInstance()))
              .setSchemaDescriptor(new TourRpcMethodDescriptorSupplier("CreateTour"))
              .build();
        }
      }
    }
    return getCreateTourMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.gladijatori.tourservice.grpc.UpdateTourRequest,
      com.gladijatori.tourservice.grpc.TourReply> getUpdateTourMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "UpdateTour",
      requestType = com.gladijatori.tourservice.grpc.UpdateTourRequest.class,
      responseType = com.gladijatori.tourservice.grpc.TourReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.gladijatori.tourservice.grpc.UpdateTourRequest,
      com.gladijatori.tourservice.grpc.TourReply> getUpdateTourMethod() {
    io.grpc.MethodDescriptor<com.gladijatori.tourservice.grpc.UpdateTourRequest, com.gladijatori.tourservice.grpc.TourReply> getUpdateTourMethod;
    if ((getUpdateTourMethod = TourRpcGrpc.getUpdateTourMethod) == null) {
      synchronized (TourRpcGrpc.class) {
        if ((getUpdateTourMethod = TourRpcGrpc.getUpdateTourMethod) == null) {
          TourRpcGrpc.getUpdateTourMethod = getUpdateTourMethod =
              io.grpc.MethodDescriptor.<com.gladijatori.tourservice.grpc.UpdateTourRequest, com.gladijatori.tourservice.grpc.TourReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "UpdateTour"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.gladijatori.tourservice.grpc.UpdateTourRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.gladijatori.tourservice.grpc.TourReply.getDefaultInstance()))
              .setSchemaDescriptor(new TourRpcMethodDescriptorSupplier("UpdateTour"))
              .build();
        }
      }
    }
    return getUpdateTourMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.gladijatori.tourservice.grpc.AddKeyPointRequest,
      com.gladijatori.tourservice.grpc.TourReply> getAddKeyPointMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "AddKeyPoint",
      requestType = com.gladijatori.tourservice.grpc.AddKeyPointRequest.class,
      responseType = com.gladijatori.tourservice.grpc.TourReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.gladijatori.tourservice.grpc.AddKeyPointRequest,
      com.gladijatori.tourservice.grpc.TourReply> getAddKeyPointMethod() {
    io.grpc.MethodDescriptor<com.gladijatori.tourservice.grpc.AddKeyPointRequest, com.gladijatori.tourservice.grpc.TourReply> getAddKeyPointMethod;
    if ((getAddKeyPointMethod = TourRpcGrpc.getAddKeyPointMethod) == null) {
      synchronized (TourRpcGrpc.class) {
        if ((getAddKeyPointMethod = TourRpcGrpc.getAddKeyPointMethod) == null) {
          TourRpcGrpc.getAddKeyPointMethod = getAddKeyPointMethod =
              io.grpc.MethodDescriptor.<com.gladijatori.tourservice.grpc.AddKeyPointRequest, com.gladijatori.tourservice.grpc.TourReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "AddKeyPoint"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.gladijatori.tourservice.grpc.AddKeyPointRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.gladijatori.tourservice.grpc.TourReply.getDefaultInstance()))
              .setSchemaDescriptor(new TourRpcMethodDescriptorSupplier("AddKeyPoint"))
              .build();
        }
      }
    }
    return getAddKeyPointMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.gladijatori.tourservice.grpc.AddToCartRequest,
      com.gladijatori.tourservice.grpc.ShoppingCartReply> getAddToCartMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "AddToCart",
      requestType = com.gladijatori.tourservice.grpc.AddToCartRequest.class,
      responseType = com.gladijatori.tourservice.grpc.ShoppingCartReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.gladijatori.tourservice.grpc.AddToCartRequest,
      com.gladijatori.tourservice.grpc.ShoppingCartReply> getAddToCartMethod() {
    io.grpc.MethodDescriptor<com.gladijatori.tourservice.grpc.AddToCartRequest, com.gladijatori.tourservice.grpc.ShoppingCartReply> getAddToCartMethod;
    if ((getAddToCartMethod = TourRpcGrpc.getAddToCartMethod) == null) {
      synchronized (TourRpcGrpc.class) {
        if ((getAddToCartMethod = TourRpcGrpc.getAddToCartMethod) == null) {
          TourRpcGrpc.getAddToCartMethod = getAddToCartMethod =
              io.grpc.MethodDescriptor.<com.gladijatori.tourservice.grpc.AddToCartRequest, com.gladijatori.tourservice.grpc.ShoppingCartReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "AddToCart"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.gladijatori.tourservice.grpc.AddToCartRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.gladijatori.tourservice.grpc.ShoppingCartReply.getDefaultInstance()))
              .setSchemaDescriptor(new TourRpcMethodDescriptorSupplier("AddToCart"))
              .build();
        }
      }
    }
    return getAddToCartMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.gladijatori.tourservice.grpc.CheckoutRequest,
      com.gladijatori.tourservice.grpc.PurchaseTokenListReply> getCheckoutMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Checkout",
      requestType = com.gladijatori.tourservice.grpc.CheckoutRequest.class,
      responseType = com.gladijatori.tourservice.grpc.PurchaseTokenListReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.gladijatori.tourservice.grpc.CheckoutRequest,
      com.gladijatori.tourservice.grpc.PurchaseTokenListReply> getCheckoutMethod() {
    io.grpc.MethodDescriptor<com.gladijatori.tourservice.grpc.CheckoutRequest, com.gladijatori.tourservice.grpc.PurchaseTokenListReply> getCheckoutMethod;
    if ((getCheckoutMethod = TourRpcGrpc.getCheckoutMethod) == null) {
      synchronized (TourRpcGrpc.class) {
        if ((getCheckoutMethod = TourRpcGrpc.getCheckoutMethod) == null) {
          TourRpcGrpc.getCheckoutMethod = getCheckoutMethod =
              io.grpc.MethodDescriptor.<com.gladijatori.tourservice.grpc.CheckoutRequest, com.gladijatori.tourservice.grpc.PurchaseTokenListReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Checkout"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.gladijatori.tourservice.grpc.CheckoutRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.gladijatori.tourservice.grpc.PurchaseTokenListReply.getDefaultInstance()))
              .setSchemaDescriptor(new TourRpcMethodDescriptorSupplier("Checkout"))
              .build();
        }
      }
    }
    return getCheckoutMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.gladijatori.tourservice.grpc.TourExecutionActionRequest,
      com.gladijatori.tourservice.grpc.TourExecutionReply> getStartTourExecutionMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "StartTourExecution",
      requestType = com.gladijatori.tourservice.grpc.TourExecutionActionRequest.class,
      responseType = com.gladijatori.tourservice.grpc.TourExecutionReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.gladijatori.tourservice.grpc.TourExecutionActionRequest,
      com.gladijatori.tourservice.grpc.TourExecutionReply> getStartTourExecutionMethod() {
    io.grpc.MethodDescriptor<com.gladijatori.tourservice.grpc.TourExecutionActionRequest, com.gladijatori.tourservice.grpc.TourExecutionReply> getStartTourExecutionMethod;
    if ((getStartTourExecutionMethod = TourRpcGrpc.getStartTourExecutionMethod) == null) {
      synchronized (TourRpcGrpc.class) {
        if ((getStartTourExecutionMethod = TourRpcGrpc.getStartTourExecutionMethod) == null) {
          TourRpcGrpc.getStartTourExecutionMethod = getStartTourExecutionMethod =
              io.grpc.MethodDescriptor.<com.gladijatori.tourservice.grpc.TourExecutionActionRequest, com.gladijatori.tourservice.grpc.TourExecutionReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "StartTourExecution"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.gladijatori.tourservice.grpc.TourExecutionActionRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.gladijatori.tourservice.grpc.TourExecutionReply.getDefaultInstance()))
              .setSchemaDescriptor(new TourRpcMethodDescriptorSupplier("StartTourExecution"))
              .build();
        }
      }
    }
    return getStartTourExecutionMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.gladijatori.tourservice.grpc.TourExecutionActionRequest,
      com.gladijatori.tourservice.grpc.TourExecutionReply> getCheckExecutionProgressMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CheckExecutionProgress",
      requestType = com.gladijatori.tourservice.grpc.TourExecutionActionRequest.class,
      responseType = com.gladijatori.tourservice.grpc.TourExecutionReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.gladijatori.tourservice.grpc.TourExecutionActionRequest,
      com.gladijatori.tourservice.grpc.TourExecutionReply> getCheckExecutionProgressMethod() {
    io.grpc.MethodDescriptor<com.gladijatori.tourservice.grpc.TourExecutionActionRequest, com.gladijatori.tourservice.grpc.TourExecutionReply> getCheckExecutionProgressMethod;
    if ((getCheckExecutionProgressMethod = TourRpcGrpc.getCheckExecutionProgressMethod) == null) {
      synchronized (TourRpcGrpc.class) {
        if ((getCheckExecutionProgressMethod = TourRpcGrpc.getCheckExecutionProgressMethod) == null) {
          TourRpcGrpc.getCheckExecutionProgressMethod = getCheckExecutionProgressMethod =
              io.grpc.MethodDescriptor.<com.gladijatori.tourservice.grpc.TourExecutionActionRequest, com.gladijatori.tourservice.grpc.TourExecutionReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "CheckExecutionProgress"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.gladijatori.tourservice.grpc.TourExecutionActionRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.gladijatori.tourservice.grpc.TourExecutionReply.getDefaultInstance()))
              .setSchemaDescriptor(new TourRpcMethodDescriptorSupplier("CheckExecutionProgress"))
              .build();
        }
      }
    }
    return getCheckExecutionProgressMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.gladijatori.tourservice.grpc.TourExecutionActionRequest,
      com.gladijatori.tourservice.grpc.TourExecutionReply> getCompleteExecutionMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CompleteExecution",
      requestType = com.gladijatori.tourservice.grpc.TourExecutionActionRequest.class,
      responseType = com.gladijatori.tourservice.grpc.TourExecutionReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.gladijatori.tourservice.grpc.TourExecutionActionRequest,
      com.gladijatori.tourservice.grpc.TourExecutionReply> getCompleteExecutionMethod() {
    io.grpc.MethodDescriptor<com.gladijatori.tourservice.grpc.TourExecutionActionRequest, com.gladijatori.tourservice.grpc.TourExecutionReply> getCompleteExecutionMethod;
    if ((getCompleteExecutionMethod = TourRpcGrpc.getCompleteExecutionMethod) == null) {
      synchronized (TourRpcGrpc.class) {
        if ((getCompleteExecutionMethod = TourRpcGrpc.getCompleteExecutionMethod) == null) {
          TourRpcGrpc.getCompleteExecutionMethod = getCompleteExecutionMethod =
              io.grpc.MethodDescriptor.<com.gladijatori.tourservice.grpc.TourExecutionActionRequest, com.gladijatori.tourservice.grpc.TourExecutionReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "CompleteExecution"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.gladijatori.tourservice.grpc.TourExecutionActionRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.gladijatori.tourservice.grpc.TourExecutionReply.getDefaultInstance()))
              .setSchemaDescriptor(new TourRpcMethodDescriptorSupplier("CompleteExecution"))
              .build();
        }
      }
    }
    return getCompleteExecutionMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.gladijatori.tourservice.grpc.TourExecutionActionRequest,
      com.gladijatori.tourservice.grpc.TourExecutionReply> getAbandonExecutionMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "AbandonExecution",
      requestType = com.gladijatori.tourservice.grpc.TourExecutionActionRequest.class,
      responseType = com.gladijatori.tourservice.grpc.TourExecutionReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.gladijatori.tourservice.grpc.TourExecutionActionRequest,
      com.gladijatori.tourservice.grpc.TourExecutionReply> getAbandonExecutionMethod() {
    io.grpc.MethodDescriptor<com.gladijatori.tourservice.grpc.TourExecutionActionRequest, com.gladijatori.tourservice.grpc.TourExecutionReply> getAbandonExecutionMethod;
    if ((getAbandonExecutionMethod = TourRpcGrpc.getAbandonExecutionMethod) == null) {
      synchronized (TourRpcGrpc.class) {
        if ((getAbandonExecutionMethod = TourRpcGrpc.getAbandonExecutionMethod) == null) {
          TourRpcGrpc.getAbandonExecutionMethod = getAbandonExecutionMethod =
              io.grpc.MethodDescriptor.<com.gladijatori.tourservice.grpc.TourExecutionActionRequest, com.gladijatori.tourservice.grpc.TourExecutionReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "AbandonExecution"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.gladijatori.tourservice.grpc.TourExecutionActionRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.gladijatori.tourservice.grpc.TourExecutionReply.getDefaultInstance()))
              .setSchemaDescriptor(new TourRpcMethodDescriptorSupplier("AbandonExecution"))
              .build();
        }
      }
    }
    return getAbandonExecutionMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static TourRpcStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<TourRpcStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<TourRpcStub>() {
        @java.lang.Override
        public TourRpcStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new TourRpcStub(channel, callOptions);
        }
      };
    return TourRpcStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static TourRpcBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<TourRpcBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<TourRpcBlockingStub>() {
        @java.lang.Override
        public TourRpcBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new TourRpcBlockingStub(channel, callOptions);
        }
      };
    return TourRpcBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static TourRpcFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<TourRpcFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<TourRpcFutureStub>() {
        @java.lang.Override
        public TourRpcFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new TourRpcFutureStub(channel, callOptions);
        }
      };
    return TourRpcFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void getPublishedTours(com.gladijatori.tourservice.grpc.GetPublishedToursRequest request,
        io.grpc.stub.StreamObserver<com.gladijatori.tourservice.grpc.TourListReply> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetPublishedToursMethod(), responseObserver);
    }

    /**
     */
    default void getTourById(com.gladijatori.tourservice.grpc.GetTourByIdRequest request,
        io.grpc.stub.StreamObserver<com.gladijatori.tourservice.grpc.TourReply> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetTourByIdMethod(), responseObserver);
    }

    /**
     */
    default void createTour(com.gladijatori.tourservice.grpc.CreateTourRequest request,
        io.grpc.stub.StreamObserver<com.gladijatori.tourservice.grpc.TourReply> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getCreateTourMethod(), responseObserver);
    }

    /**
     */
    default void updateTour(com.gladijatori.tourservice.grpc.UpdateTourRequest request,
        io.grpc.stub.StreamObserver<com.gladijatori.tourservice.grpc.TourReply> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getUpdateTourMethod(), responseObserver);
    }

    /**
     */
    default void addKeyPoint(com.gladijatori.tourservice.grpc.AddKeyPointRequest request,
        io.grpc.stub.StreamObserver<com.gladijatori.tourservice.grpc.TourReply> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getAddKeyPointMethod(), responseObserver);
    }

    /**
     */
    default void addToCart(com.gladijatori.tourservice.grpc.AddToCartRequest request,
        io.grpc.stub.StreamObserver<com.gladijatori.tourservice.grpc.ShoppingCartReply> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getAddToCartMethod(), responseObserver);
    }

    /**
     */
    default void checkout(com.gladijatori.tourservice.grpc.CheckoutRequest request,
        io.grpc.stub.StreamObserver<com.gladijatori.tourservice.grpc.PurchaseTokenListReply> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getCheckoutMethod(), responseObserver);
    }

    /**
     */
    default void startTourExecution(com.gladijatori.tourservice.grpc.TourExecutionActionRequest request,
        io.grpc.stub.StreamObserver<com.gladijatori.tourservice.grpc.TourExecutionReply> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getStartTourExecutionMethod(), responseObserver);
    }

    /**
     */
    default void checkExecutionProgress(com.gladijatori.tourservice.grpc.TourExecutionActionRequest request,
        io.grpc.stub.StreamObserver<com.gladijatori.tourservice.grpc.TourExecutionReply> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getCheckExecutionProgressMethod(), responseObserver);
    }

    /**
     */
    default void completeExecution(com.gladijatori.tourservice.grpc.TourExecutionActionRequest request,
        io.grpc.stub.StreamObserver<com.gladijatori.tourservice.grpc.TourExecutionReply> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getCompleteExecutionMethod(), responseObserver);
    }

    /**
     */
    default void abandonExecution(com.gladijatori.tourservice.grpc.TourExecutionActionRequest request,
        io.grpc.stub.StreamObserver<com.gladijatori.tourservice.grpc.TourExecutionReply> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getAbandonExecutionMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service TourRpc.
   */
  public static abstract class TourRpcImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return TourRpcGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service TourRpc.
   */
  public static final class TourRpcStub
      extends io.grpc.stub.AbstractAsyncStub<TourRpcStub> {
    private TourRpcStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected TourRpcStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new TourRpcStub(channel, callOptions);
    }

    /**
     */
    public void getPublishedTours(com.gladijatori.tourservice.grpc.GetPublishedToursRequest request,
        io.grpc.stub.StreamObserver<com.gladijatori.tourservice.grpc.TourListReply> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetPublishedToursMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getTourById(com.gladijatori.tourservice.grpc.GetTourByIdRequest request,
        io.grpc.stub.StreamObserver<com.gladijatori.tourservice.grpc.TourReply> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetTourByIdMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void createTour(com.gladijatori.tourservice.grpc.CreateTourRequest request,
        io.grpc.stub.StreamObserver<com.gladijatori.tourservice.grpc.TourReply> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getCreateTourMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void updateTour(com.gladijatori.tourservice.grpc.UpdateTourRequest request,
        io.grpc.stub.StreamObserver<com.gladijatori.tourservice.grpc.TourReply> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getUpdateTourMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void addKeyPoint(com.gladijatori.tourservice.grpc.AddKeyPointRequest request,
        io.grpc.stub.StreamObserver<com.gladijatori.tourservice.grpc.TourReply> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getAddKeyPointMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void addToCart(com.gladijatori.tourservice.grpc.AddToCartRequest request,
        io.grpc.stub.StreamObserver<com.gladijatori.tourservice.grpc.ShoppingCartReply> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getAddToCartMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void checkout(com.gladijatori.tourservice.grpc.CheckoutRequest request,
        io.grpc.stub.StreamObserver<com.gladijatori.tourservice.grpc.PurchaseTokenListReply> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getCheckoutMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void startTourExecution(com.gladijatori.tourservice.grpc.TourExecutionActionRequest request,
        io.grpc.stub.StreamObserver<com.gladijatori.tourservice.grpc.TourExecutionReply> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getStartTourExecutionMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void checkExecutionProgress(com.gladijatori.tourservice.grpc.TourExecutionActionRequest request,
        io.grpc.stub.StreamObserver<com.gladijatori.tourservice.grpc.TourExecutionReply> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getCheckExecutionProgressMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void completeExecution(com.gladijatori.tourservice.grpc.TourExecutionActionRequest request,
        io.grpc.stub.StreamObserver<com.gladijatori.tourservice.grpc.TourExecutionReply> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getCompleteExecutionMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void abandonExecution(com.gladijatori.tourservice.grpc.TourExecutionActionRequest request,
        io.grpc.stub.StreamObserver<com.gladijatori.tourservice.grpc.TourExecutionReply> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getAbandonExecutionMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service TourRpc.
   */
  public static final class TourRpcBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<TourRpcBlockingStub> {
    private TourRpcBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected TourRpcBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new TourRpcBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.gladijatori.tourservice.grpc.TourListReply getPublishedTours(com.gladijatori.tourservice.grpc.GetPublishedToursRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetPublishedToursMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.gladijatori.tourservice.grpc.TourReply getTourById(com.gladijatori.tourservice.grpc.GetTourByIdRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetTourByIdMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.gladijatori.tourservice.grpc.TourReply createTour(com.gladijatori.tourservice.grpc.CreateTourRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getCreateTourMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.gladijatori.tourservice.grpc.TourReply updateTour(com.gladijatori.tourservice.grpc.UpdateTourRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getUpdateTourMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.gladijatori.tourservice.grpc.TourReply addKeyPoint(com.gladijatori.tourservice.grpc.AddKeyPointRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getAddKeyPointMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.gladijatori.tourservice.grpc.ShoppingCartReply addToCart(com.gladijatori.tourservice.grpc.AddToCartRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getAddToCartMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.gladijatori.tourservice.grpc.PurchaseTokenListReply checkout(com.gladijatori.tourservice.grpc.CheckoutRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getCheckoutMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.gladijatori.tourservice.grpc.TourExecutionReply startTourExecution(com.gladijatori.tourservice.grpc.TourExecutionActionRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getStartTourExecutionMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.gladijatori.tourservice.grpc.TourExecutionReply checkExecutionProgress(com.gladijatori.tourservice.grpc.TourExecutionActionRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getCheckExecutionProgressMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.gladijatori.tourservice.grpc.TourExecutionReply completeExecution(com.gladijatori.tourservice.grpc.TourExecutionActionRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getCompleteExecutionMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.gladijatori.tourservice.grpc.TourExecutionReply abandonExecution(com.gladijatori.tourservice.grpc.TourExecutionActionRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getAbandonExecutionMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service TourRpc.
   */
  public static final class TourRpcFutureStub
      extends io.grpc.stub.AbstractFutureStub<TourRpcFutureStub> {
    private TourRpcFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected TourRpcFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new TourRpcFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.gladijatori.tourservice.grpc.TourListReply> getPublishedTours(
        com.gladijatori.tourservice.grpc.GetPublishedToursRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetPublishedToursMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.gladijatori.tourservice.grpc.TourReply> getTourById(
        com.gladijatori.tourservice.grpc.GetTourByIdRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetTourByIdMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.gladijatori.tourservice.grpc.TourReply> createTour(
        com.gladijatori.tourservice.grpc.CreateTourRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getCreateTourMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.gladijatori.tourservice.grpc.TourReply> updateTour(
        com.gladijatori.tourservice.grpc.UpdateTourRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getUpdateTourMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.gladijatori.tourservice.grpc.TourReply> addKeyPoint(
        com.gladijatori.tourservice.grpc.AddKeyPointRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getAddKeyPointMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.gladijatori.tourservice.grpc.ShoppingCartReply> addToCart(
        com.gladijatori.tourservice.grpc.AddToCartRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getAddToCartMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.gladijatori.tourservice.grpc.PurchaseTokenListReply> checkout(
        com.gladijatori.tourservice.grpc.CheckoutRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getCheckoutMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.gladijatori.tourservice.grpc.TourExecutionReply> startTourExecution(
        com.gladijatori.tourservice.grpc.TourExecutionActionRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getStartTourExecutionMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.gladijatori.tourservice.grpc.TourExecutionReply> checkExecutionProgress(
        com.gladijatori.tourservice.grpc.TourExecutionActionRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getCheckExecutionProgressMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.gladijatori.tourservice.grpc.TourExecutionReply> completeExecution(
        com.gladijatori.tourservice.grpc.TourExecutionActionRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getCompleteExecutionMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.gladijatori.tourservice.grpc.TourExecutionReply> abandonExecution(
        com.gladijatori.tourservice.grpc.TourExecutionActionRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getAbandonExecutionMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_GET_PUBLISHED_TOURS = 0;
  private static final int METHODID_GET_TOUR_BY_ID = 1;
  private static final int METHODID_CREATE_TOUR = 2;
  private static final int METHODID_UPDATE_TOUR = 3;
  private static final int METHODID_ADD_KEY_POINT = 4;
  private static final int METHODID_ADD_TO_CART = 5;
  private static final int METHODID_CHECKOUT = 6;
  private static final int METHODID_START_TOUR_EXECUTION = 7;
  private static final int METHODID_CHECK_EXECUTION_PROGRESS = 8;
  private static final int METHODID_COMPLETE_EXECUTION = 9;
  private static final int METHODID_ABANDON_EXECUTION = 10;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GET_PUBLISHED_TOURS:
          serviceImpl.getPublishedTours((com.gladijatori.tourservice.grpc.GetPublishedToursRequest) request,
              (io.grpc.stub.StreamObserver<com.gladijatori.tourservice.grpc.TourListReply>) responseObserver);
          break;
        case METHODID_GET_TOUR_BY_ID:
          serviceImpl.getTourById((com.gladijatori.tourservice.grpc.GetTourByIdRequest) request,
              (io.grpc.stub.StreamObserver<com.gladijatori.tourservice.grpc.TourReply>) responseObserver);
          break;
        case METHODID_CREATE_TOUR:
          serviceImpl.createTour((com.gladijatori.tourservice.grpc.CreateTourRequest) request,
              (io.grpc.stub.StreamObserver<com.gladijatori.tourservice.grpc.TourReply>) responseObserver);
          break;
        case METHODID_UPDATE_TOUR:
          serviceImpl.updateTour((com.gladijatori.tourservice.grpc.UpdateTourRequest) request,
              (io.grpc.stub.StreamObserver<com.gladijatori.tourservice.grpc.TourReply>) responseObserver);
          break;
        case METHODID_ADD_KEY_POINT:
          serviceImpl.addKeyPoint((com.gladijatori.tourservice.grpc.AddKeyPointRequest) request,
              (io.grpc.stub.StreamObserver<com.gladijatori.tourservice.grpc.TourReply>) responseObserver);
          break;
        case METHODID_ADD_TO_CART:
          serviceImpl.addToCart((com.gladijatori.tourservice.grpc.AddToCartRequest) request,
              (io.grpc.stub.StreamObserver<com.gladijatori.tourservice.grpc.ShoppingCartReply>) responseObserver);
          break;
        case METHODID_CHECKOUT:
          serviceImpl.checkout((com.gladijatori.tourservice.grpc.CheckoutRequest) request,
              (io.grpc.stub.StreamObserver<com.gladijatori.tourservice.grpc.PurchaseTokenListReply>) responseObserver);
          break;
        case METHODID_START_TOUR_EXECUTION:
          serviceImpl.startTourExecution((com.gladijatori.tourservice.grpc.TourExecutionActionRequest) request,
              (io.grpc.stub.StreamObserver<com.gladijatori.tourservice.grpc.TourExecutionReply>) responseObserver);
          break;
        case METHODID_CHECK_EXECUTION_PROGRESS:
          serviceImpl.checkExecutionProgress((com.gladijatori.tourservice.grpc.TourExecutionActionRequest) request,
              (io.grpc.stub.StreamObserver<com.gladijatori.tourservice.grpc.TourExecutionReply>) responseObserver);
          break;
        case METHODID_COMPLETE_EXECUTION:
          serviceImpl.completeExecution((com.gladijatori.tourservice.grpc.TourExecutionActionRequest) request,
              (io.grpc.stub.StreamObserver<com.gladijatori.tourservice.grpc.TourExecutionReply>) responseObserver);
          break;
        case METHODID_ABANDON_EXECUTION:
          serviceImpl.abandonExecution((com.gladijatori.tourservice.grpc.TourExecutionActionRequest) request,
              (io.grpc.stub.StreamObserver<com.gladijatori.tourservice.grpc.TourExecutionReply>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getGetPublishedToursMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.gladijatori.tourservice.grpc.GetPublishedToursRequest,
              com.gladijatori.tourservice.grpc.TourListReply>(
                service, METHODID_GET_PUBLISHED_TOURS)))
        .addMethod(
          getGetTourByIdMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.gladijatori.tourservice.grpc.GetTourByIdRequest,
              com.gladijatori.tourservice.grpc.TourReply>(
                service, METHODID_GET_TOUR_BY_ID)))
        .addMethod(
          getCreateTourMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.gladijatori.tourservice.grpc.CreateTourRequest,
              com.gladijatori.tourservice.grpc.TourReply>(
                service, METHODID_CREATE_TOUR)))
        .addMethod(
          getUpdateTourMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.gladijatori.tourservice.grpc.UpdateTourRequest,
              com.gladijatori.tourservice.grpc.TourReply>(
                service, METHODID_UPDATE_TOUR)))
        .addMethod(
          getAddKeyPointMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.gladijatori.tourservice.grpc.AddKeyPointRequest,
              com.gladijatori.tourservice.grpc.TourReply>(
                service, METHODID_ADD_KEY_POINT)))
        .addMethod(
          getAddToCartMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.gladijatori.tourservice.grpc.AddToCartRequest,
              com.gladijatori.tourservice.grpc.ShoppingCartReply>(
                service, METHODID_ADD_TO_CART)))
        .addMethod(
          getCheckoutMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.gladijatori.tourservice.grpc.CheckoutRequest,
              com.gladijatori.tourservice.grpc.PurchaseTokenListReply>(
                service, METHODID_CHECKOUT)))
        .addMethod(
          getStartTourExecutionMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.gladijatori.tourservice.grpc.TourExecutionActionRequest,
              com.gladijatori.tourservice.grpc.TourExecutionReply>(
                service, METHODID_START_TOUR_EXECUTION)))
        .addMethod(
          getCheckExecutionProgressMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.gladijatori.tourservice.grpc.TourExecutionActionRequest,
              com.gladijatori.tourservice.grpc.TourExecutionReply>(
                service, METHODID_CHECK_EXECUTION_PROGRESS)))
        .addMethod(
          getCompleteExecutionMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.gladijatori.tourservice.grpc.TourExecutionActionRequest,
              com.gladijatori.tourservice.grpc.TourExecutionReply>(
                service, METHODID_COMPLETE_EXECUTION)))
        .addMethod(
          getAbandonExecutionMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.gladijatori.tourservice.grpc.TourExecutionActionRequest,
              com.gladijatori.tourservice.grpc.TourExecutionReply>(
                service, METHODID_ABANDON_EXECUTION)))
        .build();
  }

  private static abstract class TourRpcBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    TourRpcBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.gladijatori.tourservice.grpc.TourRpcProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("TourRpc");
    }
  }

  private static final class TourRpcFileDescriptorSupplier
      extends TourRpcBaseDescriptorSupplier {
    TourRpcFileDescriptorSupplier() {}
  }

  private static final class TourRpcMethodDescriptorSupplier
      extends TourRpcBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    TourRpcMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (TourRpcGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new TourRpcFileDescriptorSupplier())
              .addMethod(getGetPublishedToursMethod())
              .addMethod(getGetTourByIdMethod())
              .addMethod(getCreateTourMethod())
              .addMethod(getUpdateTourMethod())
              .addMethod(getAddKeyPointMethod())
              .addMethod(getAddToCartMethod())
              .addMethod(getCheckoutMethod())
              .addMethod(getStartTourExecutionMethod())
              .addMethod(getCheckExecutionProgressMethod())
              .addMethod(getCompleteExecutionMethod())
              .addMethod(getAbandonExecutionMethod())
              .build();
        }
      }
    }
    return result;
  }
}
