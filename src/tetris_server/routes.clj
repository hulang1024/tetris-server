(ns tetris-server.routes
  (:require [cheshire.core :as json]
            [io.pedestal.http.route.definition.table :as table]
            [tetris-server.user-repo :as user-repo]))

(defn api-response
  ([code] (api-response code nil))
  ([code data]
   {:status 200
    :headers {"Content-Type" "application/json"}
    :body  (json/generate-string (cond-> {:code code}
                                   data (assoc :data data)))}))

(defn generate-api-token [user]
  (str "user-" (:id user)))

(defn register [request]
  (let [user (:json-params request)]
    (if (seq (user-repo/find-users (select-keys user [:username])))
      (api-response :username-exists)
      (let [user (assoc user :user-type 1)]
        (user-repo/save-user! user)
        (api-response :ok)))))

(defn login [request]
  (let [{:keys [username password]} (:json-params request)
        user (first (user-repo/find-users {:username username}))]
    (if (and user (= (:password user) password))
      (api-response :ok {:user (dissoc user :password)
                         :token (generate-api-token user)})
      (api-response :failed))))

(defn routes
  []
  (table/table-routes
    [["/api/register" :post [register]]
     ["/api/login" :post [login]]]))
