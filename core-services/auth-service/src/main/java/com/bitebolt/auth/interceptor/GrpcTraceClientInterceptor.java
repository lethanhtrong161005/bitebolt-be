package com.bitebolt.auth.interceptor;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor;
import org.slf4j.MDC;

import java.util.UUID;

@Slf4j
@GrpcGlobalClientInterceptor
public class GrpcTraceClientInterceptor implements ClientInterceptor {

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions,
            Channel next) {

        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                String traceId = MDC.get(GrpcTraceConstants.MDC_TRACE_ID_KEY);
                if (traceId == null || traceId.isEmpty()) {
                    traceId = UUID.randomUUID().toString();
                    MDC.put(GrpcTraceConstants.MDC_TRACE_ID_KEY, traceId);
                }
                log.info("Propagating Trace ID: {} to gRPC server", traceId);
                headers.put(GrpcTraceConstants.TRACE_ID_METADATA_KEY, traceId);
                super.start(responseListener, headers);
            }
        };
    }
}
