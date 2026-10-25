(ns tetris-server.room-manage-test
  (:require [clojure.test :refer [deftest is testing]]
            [matcher-combinators.test :refer [match?]]
            [tetris-server.room-manage :as room-manage]))

(def one-room {:id "1"
               :host 1
               :public true
               :mode "classic"})
(def ready-room (assoc one-room :state :ready))
(def rooms (room-manage/add-room {} one-room))

(deftest add-room
  (testing "可重复"
    (is (match? {"1" (assoc ready-room :players [{:id 1 :readied false}])}
                (-> {}
                    (room-manage/add-room one-room)
                    (room-manage/add-room one-room))))))

(deftest join-room
  (testing "处理不存在的/可重复"
    (is (match? {"1" (assoc ready-room :players [{:id 1 :readied false}
                                                 {:id 2 :readied false}])}
                (-> rooms
                    (room-manage/join-room "non-existent" 3)
                    (room-manage/join-room "1" 2)
                    (room-manage/join-room "1" 2))))))

(deftest leave-room
  (testing "处理不存在的/可重复"
    (is (match? {"1" (assoc ready-room :players [{:id 1 :readied false}])}
                (-> rooms
                    (room-manage/join-room "1" 2)
                    (room-manage/leave-room "non-existent" 3)
                    (room-manage/leave-room "1" 2)
                    (room-manage/leave-room "1" 2)))))

  (testing "房主离开后自动更新房主"
    (is (match? {"1" (assoc ready-room
                            :host 2
                            :players [{:id 2 :readied false}])}
                (-> rooms
                    (room-manage/join-room "1" 2)
                    (room-manage/leave-room "1" 1)))))

  (testing "玩家全部离开后空房间自动删除"
    (is (= {}
           (-> rooms
               (room-manage/join-room "1" 2)
               (room-manage/leave-room "1" 1)
               (room-manage/leave-room "1" 2))))))

(deftest toggle-ready
  (testing "处理不存在的房间"
    (is (match? {"1" (assoc ready-room :players [{:id 1 :readied false}])}
                (-> rooms
                    (room-manage/toggle-ready "non-existent" 3)
                    (room-manage/toggle-ready "1" 1)
                    (room-manage/toggle-ready "1" 1))))))

(deftest start-game
  (testing "不存在房间/人数小于2则失败"
    (is (match? {"1" ready-room}
                (-> rooms
                    (room-manage/start-game "non-existent" false)
                    (room-manage/start-game "1" false)
                    (room-manage/start-game "1" true)))))

  (testing "非强制开始时如非全部已准备则失败"
    (is (match? {"1" ready-room}
                (-> rooms
                    (room-manage/join-room "1" 2)
                    (room-manage/start-game "1" false)))))

  (testing "强制开始忽略准备状态"
    (is (match? {"1" (assoc ready-room :state :inprogress)}
                (-> rooms
                    (room-manage/join-room "1" 2)
                    (room-manage/start-game "1" true)))))

  (testing "普通情况/可重复开始"
    (is (match? {"1" (assoc ready-room :state :inprogress)}
                (-> rooms
                    (room-manage/join-room "1" 2)
                    (room-manage/toggle-ready "1" 1)
                    (room-manage/toggle-ready "1" 2)
                    (room-manage/start-game "1" true)
                    (room-manage/start-game "1" true))))))
