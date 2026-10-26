(ns user 
  (:require
    [clj-reload.core :refer [reload]]
    [malli.instrument :as mi]
    [clj-commons.pretty.repl :as repl]
    [tetris-server.main :refer [start-server]]))

(println "Dev mode started.")

(repl/install-pretty-exceptions)

(mi/collect!)
(mi/instrument!)

(start-server true)
