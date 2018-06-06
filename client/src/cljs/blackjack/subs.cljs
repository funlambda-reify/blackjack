(ns blackjack.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::state
 (fn [db] db))
