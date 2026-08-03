package com.bitebolt.user.interceptor;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.slf4j.MDC;

import java.util.UUID;

@Slf4j
@GrpcGlobalServerInterceptor
public class GrpcTraceServerInterceptor implements ServerInterceptor {

  @Override
  public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
      ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {

    String tempTraceId = headers.get(GrpcTraceConstants.TRACE_ID_METADATA_KEY);
    if (tempTraceId == null || tempTraceId.isEmpty()) {
      tempTraceId = UUID.randomUUID().toString();
    }
    final String traceId = tempTraceId;

    MDC.put(GrpcTraceConstants.MDC_TRACE_ID_KEY, traceId);
    log.info(
        "Extracted Trace ID: {} from gRPC metadata for method: {}",
        traceId,
        call.getMethodDescriptor().getFullMethodName());

    try {
      ServerCall.Listener<ReqT> delegate = next.startCall(call, headers);
      return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(delegate) {
        @Override
        public void onMessage(ReqT message) {
          MDC.put(GrpcTraceConstants.MDC_TRACE_ID_KEY, traceId);
          super.onMessage(message);
        }

        @Override
        public void onHalfClose() {
          MDC.put(GrpcTraceConstants.MDC_TRACE_ID_KEY, traceId);
          super.onHalfClose();
        }

        @Override
        public void onCancel() {
          try {
            super.onCancel();
          } finally {
            MDC.remove(GrpcTraceConstants.MDC_TRACE_ID_KEY);
          }
        }

        @Override
        public void onComplete() {
          try {
            super.onComplete();
          } finally {
            MDC.remove(GrpcTraceConstants.MDC_TRACE_ID_KEY);
          }
        }
      };
    } catch (Exception e) {
      MDC.remove(GrpcTraceConstants.MDC_TRACE_ID_KEY);
      throw e;
    }
  }
}
