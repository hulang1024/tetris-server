(ns tetris-server.handlers.room 
  (:require
    [tetris-server.api :refer [api-response]]
    [tetris-server.room-manage-service :as room-manager]))

(def CreateRoomRequest
  [:map
   [:public :boolean]
   [:game-settings [:map]]])

(def JoinRoomRequest
  [:map
   [:room-id :string]])

(def SearchRoomRequest [:map])

(defn search-rooms [{:keys [room-manager]}]
  (api-response :ok (room-manager/get-rooms room-manager)))

(defn create-room [{:keys [room-manager]
                    {:keys [public game-settings]} :json-params
                    {:keys [uid]} :session}]
  (let [create-room {:host uid
                     :public public
                     :game-settings game-settings}
        room (room-manager/add-room room-manager create-room)]
    (api-response :ok {:room room})))

(defn join-room [{:keys [room-manager]
                  {:keys [room-id]} :json-params
                  {:keys [uid]} :session}]
  (let [room (room-manager/get-room room-manager room-id)]
    (cond
      (nil? room) (api-response :not-found)
      (= (:state room) :inprogress) (api-response :inprogress)
      :else (do 
              (room-manager/join-room room-manager (:id room) uid)
              (api-response :ok {:room room})))))

(defn leave-room [{:keys [room-manager]
                   {:keys [uid]} :session}]
  (let [room (room-manager/get-joined-room room-manager uid)]
    (cond
      (nil? room) (api-response :not-found)
      :else (do 
              (room-manager/leave-room room-manager (:id room) uid)
              (api-response :ok)))))

(defn toogle-ready [{:keys [room-manager]
                     {:keys [uid]} :session}]
  (let [room (room-manager/get-joined-room room-manager uid)]
    (cond
      (nil? room) (api-response :not-found)
      :else (do 
              (room-manager/toggle-ready room-manager room uid)
              (api-response :ok)))))

(defn start-game [{:keys [room-manager]
                   {:keys [uid]} :session}]
  (let [room (room-manager/get-joined-room room-manager uid)]
    (cond
      (nil? room) (api-response :not-found)
      (not= (:host room) uid) (api-response :failed)
      :else (do 
              (room-manager/start-game room-manager room force)
              (api-response :ok)))))

