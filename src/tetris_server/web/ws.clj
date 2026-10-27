(ns tetris-server.web.ws
  (:require [cheshire.core :as json]
            [io.pedestal.service.websocket :as ws]))

(defonce ^:private ws-channels (atom {}))

(defn- add-ws-channel! [uid ch]
  (swap! ws-channels assoc uid ch))

(defn- del-ws-channel! [ch-to-del]
  (swap! ws-channels (fn [xs]
                       (into {} (remove (fn [[_ ch]] (= ch ch-to-del)) xs)))))

(defn close! [uid]
  (when-let [ch (get @ws-channels uid)]
    (ws/close! ch)))

(defn broadcast-event! [event uids]
  (doseq [ws-ch (map #(get @ws-channels %) uids)]
    (ws/send-text! ws-ch (json/generate-string event))))

(def ws-interceptor
  (ws/websocket-interceptor
    ::echo
    {:on-open  (fn [channel request]                        
                 (assert (some? (:session request)))
                 (add-ws-channel! (-> request :session :uid) channel)
                 nil)
     :on-text  (fn [channel _proc text]
                 (when (= text "stat-online-users")
                   (ws/send-text! channel (str (count @ws-channels)))))
     :on-close (fn [channel _proc _reason]                    
                 (del-ws-channel! channel))}))
