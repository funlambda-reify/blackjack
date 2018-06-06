(defproject server "0.1.0-SNAPSHOT"
  :description "Server for blackjack app"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [ring/ring-core "1.7.0-RC1"]
                 [ring/ring-jetty-adapter "1.6.3"]]
  :main ^:skip-aot server.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
