(ns tetris-server.handlers.api-test-util 
  (:require
    [cheshire.core :as json]
    [clj-http.client :as http]))

(defn post [url data]
  (let [resp (http/post url
                        {:content-type :json
                         :body (json/generate-string data)
                         :throw-exceptions false})]
    (assoc resp :body (json/parse-string (:body resp)))))
