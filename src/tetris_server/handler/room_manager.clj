(ns tetris-server.handler.room-manager
  (:require [tetris-server.handler.room :as room :refer [Room RoomId]]))

(def Rooms [:map-of RoomId Room])

(defn put-room [rooms room] (assoc rooms (:id room) room))

(defn del-room [rooms room-id]
  (dissoc rooms room-id))

(defn new-room-manager [] {:rooms (atom {})})

(defn get-rooms [rm]
  (vals @(:rooms rm)))

(defn get-room [rm room-id]
  (get @(:rooms rm) room-id))

(defn add-room [rooms room]
  (put-room rooms (room/make-room room)))

(defn add-room!
  {:malli/schema [:=> [:cat Rooms Room] Rooms]}
  [rm room]
  (swap! (:rooms rm) add-room (assoc room
                                     :id (str (java.util.UUID/randomUUID))
                                     :created-at (System/currentTimeMillis))))

(defn join-room [rm room-id uid]
  (when-let [room (get-room rm room-id)]
    (swap! (:rooms rm) put-room (room/join room uid))))

(defn leave-room [rm room-id uid]
  (when-let [room (get-room rm room-id)]
    (let [left-room (room/leave room uid)]
      (swap! (:rooms rm)
             (fn [rooms]
               (if (some? left-room)
                 (put-room rooms left-room)
                 (del-room rooms room)))))))

(defn toggle-ready [rm room-id uid]
  (when-let [room (get-room rm room-id)]
    (swap! (:rooms rm) put-room (room/toggle-ready room uid))))

(defn start-game [rm room-id force?]
  (when-let [room (get-room rm room-id)]
    (swap! (:rooms rm) put-room (room/start-game room force?))))

(defn get-joined-room [rm uid]
  (first (filter #(room/joined? % uid) (vals @(:rooms rm)))))
