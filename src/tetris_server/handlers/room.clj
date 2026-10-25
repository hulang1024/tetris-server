(ns tetris-server.handlers.room 
  (:require
    [tetris-server.api :refer [api-response]]
    [tetris-server.room-manage-service :as room-manager]))

(def CreateRoomRequest
  [:map
   [:public :boolean]
   [:mode :string]])

(def SearchRoomRequest [:map])

(defn search-rooms [request]
  (let [room-manager (get-in request [:services :room-manager])]
    (api-response :ok (room-manager/get-rooms room-manager))))

(defn create-room [request]
  (let [room-manager (get-in request [:services :room-manager])
        {:keys [uid]} (:session request)
        req (:json-params request)
        new-room {:host uid
                  :public (:public req)
                  :mode (keyword (:mode req))}
        room (room-manager/add-room room-manager new-room)]
    (api-response :ok {:room room})))

(defn join-room [request]
  (let [room-manager (get-in request [:services :room-manager])
        {:keys [uid]} (:session request)
        req (:json-params request)
        room (room-manager/get-room room-manager (:room_id req))]
    (cond
      (nil? room) (api-response :not-found)
      (= (:state room) :inprogress) (api-response :inprogress)
      :else (do 
              (room-manager/join-room room-manager (:id room) uid)
              (api-response :ok {:room room})))))

(defn leave-room [request]
  (let [room-manager (get-in request [:services :room-manager])
        {:keys [uid]} (:session request)
        joined-room (room-manager/get-joined-room room-manager uid)]
    (cond
      (nil? joined-room) (api-response :not-found)
      :else (do 
              (room-manager/leave-room room-manager (:id joined-room) uid)
              (api-response :ok)))))

(defn toogle-ready [request]
  (let [room-manager (get-in request [:services :room-manager])
        {:keys [uid]} (:session request)
        req (:json-params request)
        room (room-manager/get-joined-room room-manager (:room_id req))]
    (cond
      (nil? room) (api-response :not-found)
      :else (do 
              (room-manager/toggle-ready room-manager room uid)
              (api-response :ok)))))

(defn start-game [request]
  (let [room-manager (get-in request [:services :room-manager])
        {:keys [uid]} (:session request)
        req (:json-params request)
        room (room-manager/get-joined-room room-manager (:room_id req))]
    (cond
      (nil? room) (api-response :not-found)
      (not= (:host room) uid) (api-response :failed)
      :else (do 
              (room-manager/start-game room-manager room false)
              (api-response :ok)))))

