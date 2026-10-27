(ns user 
  (:require
    [clj-reload.core :refer [reload]]
    [malli.instrument :as mi]
    [clj-commons.pretty.repl :as repl]
    [tetris-server.server :as server]))

(println "Dev mode started.")

(repl/install-pretty-exceptions)

(mi/collect!)
(mi/instrument!)

(defonce _server (server/start true))
