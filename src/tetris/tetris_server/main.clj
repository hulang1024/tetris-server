(ns tetris.tetris-server.main
  "Runs the tetris/tetris-server service."
  (:require [io.pedestal.connector :as connector]
            [io.pedestal.http.http-kit :as hk]
            [io.pedestal.log :as log]
            [tetris.tetris-server.connector :refer [connector-map]]
            [tetris.tetris-server.db :as db]))

(defn- log-startup
  [connector-map]
  (log/info :msg "Service tetris/tetris-server startup"
            :port (:port connector-map))
  connector-map)

(defn start-service
  [_]
  (db/init!)
  (-> (connector-map {:join? true
                      :host "0.0.0.0"})
      log-startup
      (hk/create-connector nil)
      connector/start!))

