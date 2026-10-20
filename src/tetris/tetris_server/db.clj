(ns tetris.tetris-server.db
  (:require [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [next.jdbc.connection :as conn]))

(defonce ds nil)

(defn create-datasource! []
  (alter-var-root
    #'ds
    (fn [_]
      (jdbc/with-options
        (conn/->pool
          com.zaxxer.hikari.HikariDataSource
          {:dbtype "postgres" :dbname "tetris" :username "postgres" :password "dev12345"
           :dataSourceProperties {:socketTimeout 30}})
        {:builder-fn rs/as-unqualified-maps}))))

(defn init! []
  (create-datasource!))
