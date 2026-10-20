(ns dev
  (:require [io.pedestal.connector :as conn]
            [io.pedestal.http.http-kit :as hk]
            [tetris.tetris-server.connector :refer [connector-map]]
            [tetris.tetris-server.db :as db]))

(defonce connector nil)

(defn init
  "Constructs the current Pedestal development connector."
  []
  (alter-var-root #'connector
    (fn [_]
        (-> (connector-map {:dev-mode? true
                            ;; You will want to disable this as it generates huges amounts of output.
                            ;; However, it is very useful when tracking down which interceptor put
                            ;; the :response, or other key, into the context.
                            :host "0.0.0.0"
                            :trace? true})
            (hk/create-connector nil)))))

(defn start!
  "Starts the current development connector."
  []
  (alter-var-root #'connector conn/start!)
  (db/init!))

(defn stop!
  "Shuts down the current development connector."
  []
  (alter-var-root #'connector
    (fn [connector]
      (when connector
        (conn/stop! connector))
        nil)))

(defn go!
  "Initializes the current development connector and starts it running."
  []
  (init)
  (start!))

(comment
  ;; For REPL development, this can be evaluated:

  (go!)
  (start!)
  (stop!)

  ;;
)

