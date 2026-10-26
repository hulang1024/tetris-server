(ns tetris-server.routes
  (:require [io.pedestal.http.ring-middlewares :as middlewares]
            [io.pedestal.http.route.definition.table :as table]
            [io.pedestal.interceptor :refer [interceptor]]
            [malli.core :as m]
            [malli.error :as me]
            [ring.middleware.session.memory :as memory]
            [tetris-server.api :refer [api-response]]
            [tetris-server.handlers.room :as room]
            [tetris-server.handlers.user :as user]))

(defonce session-store (memory/memory-store))

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

(defonce session-interceptor
  (middlewares/session {:store session-store}))

(defn api-route [verb path handler params-schema & [opts]]
  [(str "/api" path)
   verb (cond-> []
          true (into [extract-session-id session-interceptor])
          (not= (:auth-required? opts) false)
          (conj auth)
          (some? params-schema) (conj (validation-params params-schema))
          true (conj handler))
   :route-name (or (:route-name opts)
                   (keyword (subs path 1)))])

(defn routes []
  (table/table-routes
    [(api-route :post "/register" user/register user/RegisterRequest {:auth-required? false})
     (api-route :post "/login" user/login user/LoginRequest {:auth-required? false})
     (api-route :post "/logout" user/logout nil)
     (api-route :get "/whoami" user/whoami nil)

     (api-route :post "/room/search-rooms" room/search-rooms room/SearchRoomRequest)
     (api-route :post "/room/create-room" room/create-room room/CreateRoomRequest)
     (api-route :post "/room/join-room" room/join-room room/JoinRoomRequest)
     (api-route :post "/room/leave-room" room/leave-room nil)
     (api-route :post "/room/toggle-ready" room/toogle-ready nil)
     (api-route :post "/room/start-game" room/start-game nil)]))
