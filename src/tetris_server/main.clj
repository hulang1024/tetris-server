(ns tetris-server.main
  "Runs the tetris-server service."
  (:require [io.pedestal.connector :as connector]
            [io.pedestal.connector.dev :as dev]
            [io.pedestal.environment :as env]
            [io.pedestal.http.http-kit :as hk]
            [io.pedestal.interceptor :refer [interceptor]]
            [io.pedestal.log :as log]
            [tetris-server.db :as db]
            [tetris-server.routes :as routes]
            [tetris-server.room-manage-service :as room-manager]))

(defn- log-startup [connector-map]
  (log/info :msg "Service tetris-server startup"
            :port (:port connector-map))
  connector-map)

(def services
  {:room-manager (room-manager/new-room-manager)})

(defn inject-services []
  (interceptor
    {:name :inject-services
     :enter (fn [ctx]
              (assoc-in ctx [:request :services] services))}))

(defn- connector-map
  "Creates a connector map for the tetris-server service."
  []
  (let [dev-mode? env/dev-mode?
        join? (not env/dev-mode?)
        trace? env/dev-mode?]
    (->  (connector/default-connector-map 8080)
        (assoc :host "0.0.0.0")
        (cond->
          join? (assoc :join? true)
          dev-mode? (connector/with-interceptors dev/dev-interceptors)
          trace? dev/with-interceptor-observer)
        connector/with-default-interceptors
        (connector/with-interceptor (inject-services))
        (connector/with-routes
          (routes/routes)
          #_(resources/resource-routes {:resource-root "public"})))))

(defn start-service []
  (db/init!)
  (-> (connector-map)
      log-startup
      (hk/create-connector nil)
      connector/start!))

