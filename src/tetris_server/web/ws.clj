(ns tetris-server.web.ws
  (:require [io.pedestal.service.websocket :as ws]))

(def channels (atom {}))

(def ws-interceptor
  (ws/websocket-interceptor
    ::echo
    {:on-open  (fn [channel request]                        
                 (assert (some? (:session request)))
                 (swap! channels assoc (-> request :session :uid) channel)
                 nil)
     :on-text  (fn [channel _proc text]
                 (if (= text "online")
                   (ws/send-text! channel (str (keys @channels)))
                   (ws/send-text! channel (str "echo: " text))))
     :on-close (fn [_channel _proc reason]                    
                 (println "WebSocket connection closed:" reason))}))
