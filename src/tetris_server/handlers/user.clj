(ns tetris-server.handlers.user 
  (:require
    [tetris-server.api :refer [api-response]]
    [tetris-server.user-db :as user-db]))

(def Username [:string {:min 3}])
(def Password [:string {:min 6}])

(def RegisterRequest
  [:map
   [:username Username]
   [:password Password]])

(def LoginRequest
  [:map
   [:username Username]
   [:password Password]])

(defn user-info [user]
  (dissoc user :password))

(defn get-user-info [m]
  (user-info (first (user-db/find-users m))))

(defn register [request]
  (let [user (:json-params request)]
    (if (seq (user-db/find-users (select-keys user [:username])))
      (api-response :username-exists)
      (let [user (assoc user :user-type 1)]
        (user-db/save-user! user)
        (api-response :ok)))))

(defn login [request]
  (let [{:keys [username password]} (:json-params request)
        user (first (user-db/find-users {:username username}))
        sid (or (:session/key request)
                (str (java.util.UUID/randomUUID)))]
    (if (and user (= (:password user) password))
      (assoc (api-response :ok {:user (user-info user)})
             :session/key sid
             :session {:uid (:id user)})
      (api-response :failed))))

(defn logout [_request]
  (assoc (api-response :ok)
         :session nil))

(defn whoami [request]
  (let [{:keys [uid]} (:session request)]
    (api-response :ok (get-user-info {:id uid}))))

