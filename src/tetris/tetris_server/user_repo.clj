(ns tetris.tetris-server.user-repo 
  (:require
    [next.jdbc.sql :as sql]
    [tetris.tetris-server.db :refer [ds]])
  (:import java.sql.Timestamp
           java.time.Instant))

(defn find-users [keys]
  (first (sql/find-by-keys ds :users keys)))

(defn save-user [user]
  (sql/insert! ds :users {:username (:username user)
                          :password (:password user)
                          :user_type (:user-type user)}))

(defn update-user [user]
  (sql/update! ds :users {:password (:password user)
                          :update_at (Timestamp/from (Instant/now))}
               {:id (:id user)}))
