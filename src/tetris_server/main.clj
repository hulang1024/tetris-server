(ns tetris-server.main
  "Runs the tetris-server service."
  (:require [io.pedestal.connector :as connector]
            [io.pedestal.http.http-kit :as hk]
            [io.pedestal.log :as log]
            [tetris-server.connector :refer [connector-map]]
            [tetris-server.db :as db]))

(defn- log-startup
  [connector-map]
  (log/info :msg "Service tetris-server startup"
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

