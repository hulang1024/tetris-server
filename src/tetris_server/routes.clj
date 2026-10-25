(ns tetris-server.routes
  (:require [cheshire.core :as json]
            [io.pedestal.http.ring-middlewares :as middlewares]
            [io.pedestal.http.route.definition.table :as table]
            [io.pedestal.interceptor :refer [interceptor]]
            [malli.core :as m]
            [malli.error :as me]
            [ring.middleware.session.memory :as memory]
            [tetris-server.user-db :as user-db]))

(def session-store (memory/memory-store))

(defn api-response
  ([code] (api-response code nil))
  ([code data]
   {:status 200
    :headers {"Content-Type" "application/json"}
    :body  (json/generate-string (cond-> {:code code}
                                   data (assoc :data data)))}))

(defn user-info [user]
  (dissoc user :password))

(defn get-user-info [m]
  (dissoc (first (user-db/find-users m))
          :password))

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
      (assoc (api-response :ok {:user (user-info user)
                                :sid sid})
             :session/key sid
             :session {:uid (:id user)})
      (api-response :failed))))

(defn logout [_request]
  (assoc (api-response :ok)
         :session nil))

(defn whoami [request]
  (let [{:keys [uid]} (:session request)]
    (api-response :ok (get-user-info {:id uid}))))

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

(defn validation-params [schema]
  (interceptor
    {:name ::validate-params
     :enter (fn [ctx]
              (let [body (get-in ctx [:request :json-params])
                    err  (m/explain schema body)]
                (if err
                  (assoc ctx :response
                         (api-response :error {:errors (me/humanize err)}))
                  ctx)))}))

(def auth
  (interceptor
    {:name ::auth
     :enter (fn [ctx]
              (let [{:keys [uid]} (get-in ctx [:request :session])]
                (if uid
                  ctx
                  (assoc ctx :response {:status 401}))))}))

(def extract-session-id
  (interceptor
    {:name :extract-session-id 
     :enter (fn [ctx]
              (if (get-in ctx [:request :cookies "ring-session"])
                ctx
                (if-let [sid (get-in ctx [:request :headers "x-session-id"])]
                  (-> ctx
                      (assoc-in [:request :cookies "ring-session"] {:value sid})
                      (assoc-in [:request :session/key] sid))
                  ctx)))}))

(def session-interceptor
  (middlewares/session {:store session-store}))

(defn api-route [verb path handler params-schema & [opts]]
  [(str "/api/" path)
   verb (cond-> []
          true (into [extract-session-id session-interceptor])
          (not= (:auth-required? opts) false)
          (conj auth)
          (some? params-schema) (conj (validation-params params-schema))
          true (conj handler))
   :route-name (or (:route-name opts)
                   (keyword (subs path 1)))])

(defn routes
  []
  (table/table-routes
    [(api-route :post "/register" register RegisterRequest {:auth-required? false})
     (api-route :post "/login" login LoginRequest {:auth-required? false})
     (api-route :post "/logout" logout nil)
     (api-route :get "/whoami" whoami nil)]))
