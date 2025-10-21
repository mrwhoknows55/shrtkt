package xyz.avdt.plugins

import io.ktor.http.*
import io.ktor.http.HttpHeaders.UserAgent
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter
import io.opentelemetry.instrumentation.ktor.v3_0.KtorServerTelemetry
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor
import io.opentelemetry.semconv.ServiceAttributes

fun Application.configureMonitoring() {
    System.setProperty("otel.metrics.exporter", "none")
    System.setProperty("otel.logs.exporter", "none")
    System.setProperty("otel.traces.exporter", "none")
    val spanExporter = OtlpGrpcSpanExporter.builder().setEndpoint("https://shrtkt.onrender.com").build()

    val openTelemetry = AutoConfiguredOpenTelemetrySdk.builder().addTracerProviderCustomizer { old, _ ->
        old.addSpanProcessor(BatchSpanProcessor.builder(spanExporter).build())
    }.addResourceCustomizer { oldResource, _ ->
        oldResource.toBuilder().putAll(oldResource.attributes).put(ServiceAttributes.SERVICE_NAME, "shrtkt").build()
    }.build().openTelemetrySdk

    install(KtorServerTelemetry) {
        setOpenTelemetry(openTelemetry)
        capturedRequestHeaders(UserAgent, HEADER_SERVER_TIMING)
        knownMethods(HttpMethod.DefaultMethods)


        spanKindExtractor {
            if (httpMethod == HttpMethod.Post) {
                SpanKind.PRODUCER
            } else {
                SpanKind.CLIENT
            }
        }
        attributesExtractor {
            onStart {
                attributes.put("start-time", System.currentTimeMillis())
            }
            onEnd {
                attributes.put("end-time", System.currentTimeMillis())
            }
        }
    }
}