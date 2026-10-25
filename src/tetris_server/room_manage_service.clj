(ns tetris-server.room-manage-service
  (:require [tetris-server.room-manage :as manage]))

(defn new-room-manager [] {:rooms (atom {})})

(defn get-rooms [rm]
  (vals @(:rooms rm)))

(defn get-room [rm room-id]
  (manage/get-room @(:rooms rm) room-id))

(defn add-room [rm room]
  (swap! (:rooms rm) manage/add-room
         (assoc room :id (str (java.util.UUID/randomUUID)))))

(defn join-room [rm room-id uid]
  (swap! (:rooms rm) manage/join-room room-id uid))

(defn leave-room [rm room-id uid]
  (swap! (:rooms rm) manage/leave-room room-id uid))

(defn toggle-ready [rm room-id uid]
  (swap! (:rooms rm) manage/toggle-ready room-id uid))

(defn start-game [rm room-id force?]
  (swap! (:rooms rm) manage/start-game room-id force?))

(defn get-joined-room [rm uid]
  (manage/get-joined-room @(:rooms rm) uid))
