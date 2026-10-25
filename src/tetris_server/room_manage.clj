(ns tetris-server.room-manage)

(defn- make-player [uid] {:id uid :readied false})

(defn get-room [rooms id] (get rooms id))

(defn- put-room [rooms room] (assoc rooms (:id room) room))

(defn add-room [rooms room]
  (put-room rooms (assoc room
                         :players [(make-player (:host room))]
                         :state :ready)))

(defn- del-room [rooms room-id]
  (dissoc rooms room-id))

(defn get-joined-room [rooms uid]
  (first (filter (fn [room] (some #(= (:id %) uid) (:players room)))
                 (vals rooms))))

(defn join-room [rooms room-id uid]
  (if-let [room (get-room rooms room-id)]
    (if-not (get-joined-room rooms uid)
      (put-room rooms (update room :players conj (make-player uid)))
      rooms)
    rooms))

(defn leave-room [rooms room-id uid]
  (if-let [room (get-room rooms room-id)]
    (let [room (update room :players
                       (fn [players]
                         (filterv #(not= (:id %) uid) players)))]
      (if (empty? (:players room))
        (del-room rooms room-id)
        (put-room rooms
                  (if (= uid (:host room))
                    (assoc room :host (-> room :players first :id))
                    room))))
    rooms))

(defn toggle-ready [rooms room-id uid]
  (if-let [room (get-room rooms room-id)]
    (put-room rooms
              (update room :players
                      (fn [players]
                        (mapv #(if (= (:id %) uid) (update % :readied not) %)
                              players))))
    rooms))

(defn start-game [rooms room-id force?]
  (if-let [room (get-room rooms room-id)]
    (if (and (> (count (:players room)) 1)
             (or force? (every? :readied (:players room))))
      (put-room rooms (assoc room :state :inprogress))
      rooms)
    rooms))

