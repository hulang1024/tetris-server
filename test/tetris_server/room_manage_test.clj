(ns tetris-server.room-manage-test
  (:require [clojure.test :refer [deftest is testing]]
            [matcher-combinators.test :refer [match?]]
            [tetris-server.room-manage :refer
             [add-room join-room leave-room toggle-ready start-game]]))

(def one-room {:id "1"
               :host 1
               :players-min 2
               :players-max 99})
(def ready-room (assoc one-room :state :ready))

(deftest add-room
  (testing "可重复"
    (is (match? {"1" (assoc ready-room :players [{:id 1 :readied false}])}
                (-> {}
                    (add-room one-room)
                    (add-room one-room))))))

(deftest join-room
  (testing "处理不存在的/可重复/最大人数"
    (let [one-room (assoc one-room :players-max 2)]
      (is (match? {"1" (assoc one-room
                              :state :ready
                              :players [{:id 1 :readied false}
                                        {:id 2 :readied false}])}
                  (-> (add-room {} one-room)
                      (join-room "non-existent" 3)
                      (join-room "1" 2)
                      (join-room "1" 2)
                      (join-room "1" 3)))))))

(deftest leave-room
  (testing "处理不存在的/可重复"
    (is (match? {"1" (assoc ready-room :players [{:id 1 :readied false}])}
                (-> (add-room {} one-room)
                    (join-room "1" 2)
                    (leave-room "non-existent" 3)
                    (leave-room "1" 2)
                    (leave-room "1" 2)))))

  (testing "房主离开后自动更新房主"
    (is (match? {"1" (assoc ready-room
                            :host 2
                            :players [{:id 2 :readied false}])}
                (-> (add-room {} one-room)
                    (join-room "1" 2)
                    (leave-room "1" 1)))))

  (testing "玩家全部离开后空房间自动删除"
    (is (= {}
           (-> (add-room {} one-room)
               (join-room "1" 2)
               (leave-room "1" 1)
               (leave-room "1" 2))))))

(deftest toggle-ready
  (testing "处理不存在的房间"
    (is (match? {"1" (assoc ready-room :players [{:id 1 :readied false}])}
                (-> (add-room {} one-room)
                    (toggle-ready "non-existent" 3)
                    (toggle-ready "1" 1)
                    (toggle-ready "1" 1))))))

(deftest start-game
  (testing "不存在房间/人数小于设置最小人数则失败"
    (let [one-room (assoc one-room :players-min 2)]
      (is (match? {"1" (assoc one-room
                              :state :ready
                              :players-min 2)}
                  (-> (add-room {} one-room)
                      (start-game "non-existent" false)
                      (start-game "1" false)
                      (start-game "1" true))))))

  (testing "非强制开始时如非全部已准备则失败"
    (is (match? {"1" ready-room}
                (-> (add-room {} one-room)
                    (join-room "1" 2)
                    (start-game "1" false)))))

  (testing "强制开始忽略准备状态"
    (is (match? {"1" (assoc ready-room :state :inprogress)}
                (-> (add-room {} one-room)
                    (join-room "1" 2)
                    (start-game "1" true)))))

  (testing "普通情况/可重复开始"
    (is (match? {"1" (assoc ready-room :state :inprogress)}
                (-> (add-room {} one-room)
                    (join-room "1" 2)
                    (toggle-ready "1" 1)
                    (toggle-ready "1" 2)
                    (start-game "1" true)
                    (start-game "1" true))))))
