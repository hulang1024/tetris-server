(ns tetris-server.handlers.user-test 
  (:require
    [clojure.test :refer [deftest is testing use-fixtures]]
    [matcher-combinators.test]
    [tetris-server.handlers.api-test-util :refer [post]]
    [io.pedestal.connector :as connector]
    [tetris-server.main :as main]))

(defonce test-server (atom nil))

(def base-url "http://localhost:8080/api")

(defn- start-test-server []
  (reset! test-server (main/start-server true)))

(defn- stop-test-server []
  (when-let [server @test-server]
    (connector/stop! server)
    (reset! test-server nil)))

(use-fixtures :once
              (fn [f]
                (start-test-server)
                (try (f) (finally (stop-test-server)))))

(deftest register
  (testing "用户注册已存在的账号直接返回特定状态码"
    (let [resp (post (str base-url "/register")
                     {:username "alice" :password "secret"})]
      (is (match? {:body {"code" "username-exists"}}
                  resp)))))

(deftest login
  (testing "用户登录返回cookie"
    (let [resp (post (str base-url "/login")
                     {:username "alice" :password "secret"})]
      (is (match? {:body {"code" "ok" "data" {"user" some?}}
                   :cookies {"ring-session" {:value some?}}}
                  resp))))

  (testing "用户登录密码错误"
    (let [resp (post (str base-url "/login")
                     {:username "alice" :password "wrong-password"})]
      (is (match? {:body {"code" "failed"}} resp))
      (is (not (contains? resp :cookies)))))

  (testing "用户登录用户名不存在"
    (let [resp (post (str base-url "/login")
                     {:username "nobody" :password "secret"})]
      (is (match? {:body {"code" "failed"}} resp))
      (is (not (contains? resp :cookies))))))
