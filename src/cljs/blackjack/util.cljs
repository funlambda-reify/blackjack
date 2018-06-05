(ns blackjack.util
    (:require [clojure.string :as str]
              [clojure.set :as set]))

(defn new-deck
    []
    (apply list
      (shuffle
        (for [ suit [ "♠" "♥" "♦" "♣" ]
               rank [ "A" 2 3 4 5 6 7 8 9 10 "J" "K" "Q" ] ]
          [ suit rank ]))))

; TODO: Deal with empty deck scenario better
(defn deal-cards
  "Deals n cards from the input deck. Returns dealt cards and resulting deck."
  ([ n deck ] (deal-cards n deck []))
  ([ n deck dealt-cards ]
    (if (= n 0)
      [ dealt-cards deck ]
      (let [next-card (first deck)]
        (if next-card
          (recur (dec n) (rest deck) (conj dealt-cards next-card))
          (throw (Exception. "No cards left in deck!")))))))

(defn possible-card-scores
  "Returns the possible scores for a card. Number cards of rank n return [ n ],
    face cards return [ 10 ], and aces return [ 1 11 ]."
  [ [ suit rank ] ] 
  (if (= rank "A") [ 1 11 ]
    (if (contains? #{ "J" "Q" "K" } rank) [ 10 ] ; review this
      [ rank ])))

(defn possible-hand-scores
  "Returns the possible scores for a collection of cards (a hand)."
  ([hand] (possible-hand-scores hand [0]))
  ([hand acc]
    (if (empty? hand) 
      acc
      (let [next-card-possible-scores (possible-card-scores (first hand))]
        (recur (rest hand) (reduce concat (map (fn [ps1] (map (fn [ps2] (+ ps1 ps2)) next-card-possible-scores)) acc)))))))

(defn get-valid-scores
  "Filters a collection of blackjack scores down to valid (non-busting) scores (<= 21)"
  [scores]
  (filter #(<= % 21) scores))

(defn hand->string
  [hand]
  (str/join ", " (map (fn [ [ suit rank ] ] (str suit rank)) hand)))

(defn tryParseInt
  [str]
  (let [ res (js/parseInt str) ]
    (if (js/isNaN res) nil res)))
