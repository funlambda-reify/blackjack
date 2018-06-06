(ns server.core
  (:require [ring.middleware.params :as mw]
            [ring.util.response :as ring]
            [ring.adapter.jetty :as jetty])
  (:gen-class))

(def bankroll (atom 1000))

(defn update-bankroll [new-value]
  (reset! bankroll new-value))

(def invalid-request
  {:status 400 :headers {} :body "Invalid request"})
(defn success [msg]
  {:status 200 :headers {} :body msg})

(defn handler [request]
  (case (:request-method request)
     :get (success (str @bankroll))
     :post (let [{bankroll "bankroll"} (:form-params request)]
              (if bankroll
                (do (update-bankroll bankroll)
                    (success "Bankroll Updated!"))
                invalid-request))
     invalid-request))

(def app (mw/wrap-params handler))

(defn -main [& args]
  (jetty/run-jetty app {:port 3000}))
