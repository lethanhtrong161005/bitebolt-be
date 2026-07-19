package com.ecomove.user.interceptor;

import com.ecomove.common.constant.AppConstant;
import io.grpc.Metadata;

public class GrpcTraceConstants {
    public static final String MDC_TRACE_ID_KEY = AppConstant.TRACE_ID_KEY;
    public static final Metadata.Key<String> TRACE_ID_METADATA_KEY =
            Metadata.Key.of("x-trace-id", Metadata.ASCII_STRING_MARSHALLER);
}
