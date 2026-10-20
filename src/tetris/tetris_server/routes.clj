(ns tetris.tetris-server.routes
  (:require [io.pedestal.http.route.definition.table :as table]
            [tetris.tetris-server.user-repo :as user-repo]))

(defn login [request]
  (let [{:keys [username password]} (:json-params request)
        user (first (user-repo/find-users {:username username}))]
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body {:user user}}))

(defn routes
  []
  (table/table-routes
    [["/login" :post [login]]]))
