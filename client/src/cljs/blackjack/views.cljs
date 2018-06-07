(ns blackjack.views
  (:require
   [re-frame.core :as re-frame]
   [blackjack.subs :as subs]))

(def hidden-card
  [:div { :class ["card" "hidden"] } "?"])

(defn card [ [ suit rank ]]
  (let [ color (if (some #{suit} ["♦" "♥"]) "red" "black") ]
    [:div { :class "card" :style { :color color } } (str suit rank)]))

(defn hand [hand hide-rest]
  (let [ get-color (fn [suit] (if (some #{suit} ["♦" "♥"]) "red" "black")) ]
    [:div { :class "hand" }
      (map-indexed (fn [ idx x ] (if (and hide-rest (> idx 0)) hidden-card (card x))) hand)
    ]))

(defn game-display [state]
  [:div
    [:div
      [:label { :class "label" } "Current Bet"]
      (str "$" (:current-bet state))
    ]
    [:div
      [:label { :class "label" } "Player Hand"]
      (hand (:player-hand state) false)
    ]
    [:div
      [:label { :class "label" } "Dealer Hand"]
      (hand (:dealer-hand state) (not (#{:dealer-done :winner-determined :round-over} (:status state))))
    ]
  ])

(def make-bet-form
  [:div
    [:form { :onSubmit (fn [ce] (.preventDefault ce) (re-frame/dispatch [ :make-bet ]))}
      [:label "Enter bet: "]
      [:input { :type "text" :onChange (fn [ce] (re-frame/dispatch [ :change-bet-text ce.target.value ])) }]
      [:input { :type "submit" :value "Bet" }]
    ]
  ])

(def player-turn-panel
  [:form
    [:button { :onClick (fn [ce] (.preventDefault ce) (re-frame/dispatch [ :player-hit ])) } "HIT" ]
    [:button { :onClick (fn [ce] (.preventDefault ce) (re-frame/dispatch [ :player-stand ])) } "STAND" ]
  ])

(def dealer-turn-panel
  [:div "Dealer playing..."])

(def ready-to-deal-panel
  [:div "Dealing..."])

(def round-over-form
  [:div
    [:div (str "Round Over!")]
    [:button { :onClick (fn [ce] (.preventDefault ce) (re-frame/dispatch [ :start-new-round ])) } "Play again" ]
  ])

(defn result-panel [[result reason]]
  (let [result-msg (case result :player-won "You won" :tied "Game tied" :player-lost "You lost")
        reason-msg (case reason nil "" :player-busted "because you busted" :higher-score "because of higher score" :dealer-busted "because dealer busted")]
    [:div
      (str result-msg " " reason-msg)
      [:button { :onClick (fn [ce] (.preventDefault ce) (re-frame/dispatch [ :settle-money ])) } "OK" ]
    ]))

(defn main-panel []
  (let [state @(re-frame/subscribe [::subs/state])]
    (if (= (:status state) :initializing)
      [:p "Initializing..."]
      [:div
        (case (:server-status state)
          :connected "Connected"
          :disconneted "Disconnected"
          "")
        [:h1 "Blackjack"]
        [:div (str "Your bankroll: $" (:player-money state))]
        (if (:server-msg state) [:div.msg (:server-msg state)])
        (if (not (#{:round-over :waiting-for-bet } (:status state))) (game-display state))
        (case (:status state)
            :waiting-for-bet make-bet-form
            :ready-to-deal ready-to-deal-panel
            :player-turn player-turn-panel
            :dealer-turn dealer-turn-panel
            :winner-determined (result-panel (:round-result state))
            :round-over round-over-form)])))
