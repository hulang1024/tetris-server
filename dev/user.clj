(ns user 
  (:require
    [clj-reload.core :refer [reload]]
    [clj-commons.pretty.repl :as repl]
    [tetris-server.main :refer [start-service]]))

(println "Dev mode started.")

(repl/install-pretty-exceptions)

(start-service)
