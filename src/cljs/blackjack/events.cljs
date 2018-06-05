(ns blackjack.events
  (:require
   [re-frame.core :as re-frame]
   [blackjack.db :as db]
   [blackjack.util :as util]))

(defn require-status
  [db expected-status]
  (if (not= (:status db) expected-status) 
    (throw (str "Expected status " expected-status 
            " but in status " (:status db)))))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _] db/initial-db))

(re-frame/reg-event-db 
  :start-new-round
  (fn [db [_]]
    (require-status db :round-over)
    (assoc db 
      :status :waiting-for-bet
      :deck (util/new-deck)
      :player-hand nil
      :player-poss-scores nil
      :dealer-hand nil
      :dealer-poss-scores nil)))

(re-frame/reg-event-db 
  :change-bet-text
  (fn [db [_ bet-text]]
    (require-status db :waiting-for-bet)
    (assoc db :bet-text bet-text)))

(re-frame/reg-event-fx 
  :make-bet
  (fn [{:keys [db]} [_]]
    (require-status db :waiting-for-bet)
    (if-let [ bet-amount (util/tryParseInt (:bet-text db)) ]
      { :db (assoc db 
              :status :ready-to-deal 
              :current-bet bet-amount
              :bet-text ""
              :player-money (- (:player-money db) bet-amount))
        :dispatch-later [{ :ms 1000 :dispatch [:deal-hands] }] }
      { })))

(re-frame/reg-event-db 
  :deal-hands
  (fn [db [_]]
    (require-status db :ready-to-deal)
    (let [[player-hand deck] (util/deal-cards 2 (:deck db))
          [dealer-hand deck] (util/deal-cards 2 deck) ]
      (assoc db
        :status :player-turn
        :deck deck
        :player-hand player-hand
        :player-poss-scores (util/get-valid-scores (util/possible-hand-scores player-hand))
        :dealer-hand dealer-hand
        :dealer-poss-scores (util/get-valid-scores (util/possible-hand-scores dealer-hand))))))

(re-frame/reg-event-fx 
  :player-hit
  (fn [{:keys [db]} [_]]
    (require-status db :player-turn)
    (let [[ player-new-cards deck] (util/deal-cards 1 (:deck db))
            player-hand (concat (:player-hand db) player-new-cards)
            poss-hand-scores (util/get-valid-scores (util/possible-hand-scores player-hand))
            player-busted (empty? poss-hand-scores) ]
      { :db (assoc db
              :status (if player-busted :winner-determined :player-turn)
              :round-result (if player-busted [ :player-lost :player-busted ] nil)
              :deck deck
              :player-hand player-hand
              :player-poss-scores poss-hand-scores) 
        ; :dispatch [ (if player-busted :settle-money nil) ] 
      })))

(re-frame/reg-event-fx 
  :player-stand
  (fn [{:keys [db]} [_]]
    (require-status db :player-turn)
    {:db (assoc db :status :dealer-turn)
     :dispatch-later [ { :ms 1000 :dispatch [ :dealer-play ] } ] }))

(re-frame/reg-event-fx
  :dealer-play
  (fn [{:keys [db]} [_]]
    (require-status db :dealer-turn)
      (loop [db db] ; Take cards until bust or (highest poss.) dealer score is >= 17
        (if (or (empty? (:dealer-poss-scores db)) 
                (>= (last (:dealer-poss-scores db)) 17))
            (let [ dealer-busted (empty? (util/get-valid-scores (util/possible-hand-scores (:dealer-hand db)))) ]
              {
                :db (assoc db 
                      :status (if dealer-busted :winner-determined :dealer-done)
                      :round-result (if dealer-busted [ :player-won :dealer-busted ] nil))
                :dispatch [ (if dealer-busted nil :compare-scores) ]
              })
            (let [ [dealer-new-cards deck] (util/deal-cards 1 (:deck db))
                    dealer-hand (concat (:dealer-hand db) dealer-new-cards)
                    poss-hand-scores (util/get-valid-scores (util/possible-hand-scores dealer-hand)) ]
              (recur
                (assoc db
                  :deck deck
                  :dealer-hand dealer-hand
                  :dealer-poss-scores poss-hand-scores)))))))

(re-frame/reg-event-fx
  :compare-scores
  (fn [{:keys [db]} [_]]
    (require-status db :dealer-done)
    (let [player-score (last (:player-poss-scores db))
          dealer-score (last (:dealer-poss-scores db))]
      { :db (assoc db
              :status :winner-determined
              :round-result (cond (> player-score dealer-score) [ :player-won :higher-score ]
                                  (< player-score dealer-score) [ :player-lost :higher-score ]
                                  :else [ :tied ]))})))

(re-frame/reg-event-db
  :settle-money
  (fn [db [_]]
    (require-status db :winner-determined)
    (assoc db
      :status :round-over
      :current-bet 0
      :player-money (case (first (:round-result db))
                        :player-won (+ (:player-money db) (* 2 (:current-bet db)))
                        :tied (+ (:player-money db) (:current-bet db))
                        :player-lost (:player-money db)))))
