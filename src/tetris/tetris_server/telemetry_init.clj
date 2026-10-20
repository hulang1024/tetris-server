(ns tetris.tetris-server.telemetry-init
  (:require [io.pedestal.metrics.otel :as otel])
  (:import (io.opentelemetry.api GlobalOpenTelemetry)))

(defn metric-source
  "Wraps the meter obtained from GlobalOpenTelemetry, with an
  instrumentation scope name of tetris.tetris-server.metrics."
  []
  (-> (GlobalOpenTelemetry/getMeter "tetris.tetris-server.metrics")
      (otel/wrap-meter)))

(defn tracing-source
  "Returns the tracer obtained from GlobalOpenTelementry, with an
  instrumentation scope name of tetris.tetris-server.tracing."
  []
  (GlobalOpenTelemetry/getTracer "tetris.tetris-server.tracing"))
