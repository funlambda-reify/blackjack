(ns blackjack.db
  (:require [blackjack.util :as util]
            [clojure.spec.alpha :as s]))

(def starting-money 1000)

(s/def ::non-negative-int (s/and integer? #(>= % 0)))

(s/def ::player-money ::non-negative-int)
(s/def ::status #{ :waiting-for-bet :ready-to-deal :player-turn :dealer-turn 
                   :dealer-done :winner-determined :round-over })
(s/def ::db (s/keys :req-un [ ::status ::player-money ]))

(def default-db
  { :status :waiting-for-bet
    :round-result nil
    :player-money starting-money
    :bet-text ""
    :current-bet nil
    :deck (util/new-deck)
    :player-hand nil
    :player-poss-scores nil
    :dealer-hand nil
    :dealer-poss-scores nil })
