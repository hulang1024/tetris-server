(ns tetris-server.handler.api-test-util 
  (:require
    [cheshire.core :as json]
    [clj-http.client :as http]
    [clojure.test :refer [use-fixtures]]
    [tetris-server.server :as server]))

(defonce test-server (atom nil))

(defn- start-test-server []
  (reset! test-server (server/start true)))

(defn- stop-test-server []
  (when-let [server @test-server]
    (server/stop server)
    (reset! test-server nil)))

(defn setup-test-server []
  (use-fixtures :once
                (fn [f]
                  (start-test-server)
                  (try (f) (finally (stop-test-server))))))

(defn request [method-fn url opts]
  (let [resp (method-fn (str "http://localhost:8080/api" url)
                        (merge {:content-type :json
                                :throw-exceptions false}
                               opts))]
    (assoc resp :body (json/parse-string (:body resp)))))

(defn post [url body & [opts]]
  (request http/post url (merge {:body (json/generate-string body)} opts)))

(defn get [url query-params & [opts]]
  (request http/get url (merge {:query-params query-params} opts)))
