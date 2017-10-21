(defproject static "1.1.0-SNAPSHOT"
  :description "Simple static site generator."
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.cli "0.3.5"]
                 [org.clojure/tools.logging "0.4.0"]
                 [cssgen "0.3.0-SNAPSHOT" :exclusions [org.clojure/clojure]]
                 [hiccup "1.0.5"]
                 [org.pegdown/pegdown "1.6.0"]
                 [ring/ring-core "1.6.2"]
                 [ring/ring-jetty-adapter "1.6.2"]
                 [watchtower "0.1.1"]
                 [stringtemplate-clj "0.1.0"]
                 [org.clojure/core.memoize "0.5.9"]
                 [stencil "0.5.0"]
                 [cpath-clj "0.1.2"]]
  :profiles {:dev {:plugins [[lein-ancient "0.6.12"]]}}
  :main static.core
  :aot :all
  :jar-name "static.jar"
  :uberjar-name "static-app.jar")
