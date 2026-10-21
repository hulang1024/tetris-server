(ns tetris-server.routes
  (:require [cheshire.core :as json]
            [malli.core :as m]
            [malli.error :as me]
            [io.pedestal.http.route.definition.table :as table]
            [io.pedestal.interceptor :as interceptor]
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

(defn validate-body [schema]
  (interceptor/interceptor
    {:name ::validate-body
     :enter (fn [ctx]
              (let [body (get-in ctx [:request :json-params])
                    err  (m/explain schema body)]
                (if err
                  (assoc ctx :response
                         (api-response :error {:errors (me/humanize err)}))
                  ctx)))}))
(defn routes
  []
  (table/table-routes
    [["/api/register" :post [(validate-body RegisterRequest) register]]
     ["/api/login" :post [(validate-body LoginRequest) login]]]))
