(ns blackjack.db
  (:require [blackjack.util :as util]))

(def starting-money 1000)

(def initial-db
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
