(ns blackjack.db
  (:require [blackjack.util :as util]
            [clojure.spec.alpha :as s]))

(s/def ::non-negative-int (s/and integer? #(>= % 0)))

; (s/def ::player-money (s/or ::non-negative-int nil?))
(s/def ::status #{ :initializing :waiting-for-bet :ready-to-deal :player-turn 
                   :dealer-turn :dealer-done :winner-determined :round-over })
(s/def ::db (s/keys :req-un [ ::status  ]))

(def default-db
  { :status :initializing
    :saving-bankroll nil
    :round-result nil
    :player-money nil
    :server-msg nil
    :server-status nil
    :bet-text ""
    :current-bet nil
    :deck (util/new-deck)
    :player-hand nil
    :player-poss-scores nil
    :dealer-hand nil
    :dealer-poss-scores nil })
