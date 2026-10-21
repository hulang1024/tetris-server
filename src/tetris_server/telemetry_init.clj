(ns tetris-server.telemetry-init
  (:require [io.pedestal.metrics.otel :as otel])
  (:import (io.opentelemetry.api GlobalOpenTelemetry)))

(defn metric-source
  "Wraps the meter obtained from GlobalOpenTelemetry, with an
  instrumentation scope name of tetris-server.metrics."
  []
  (-> (GlobalOpenTelemetry/getMeter "tetris-server.metrics")
      (otel/wrap-meter)))

(defn tracing-source
  "Returns the tracer obtained from GlobalOpenTelementry, with an
  instrumentation scope name of tetris-server.tracing."
  []
  (GlobalOpenTelemetry/getTracer "tetris-server.tracing"))
