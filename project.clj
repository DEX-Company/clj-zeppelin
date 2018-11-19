(defproject clj-zeppelin "0.1.0"
  :description "Clojure library for managing Zeppelin notebooks"
  :url "https://github.com/DEX-Company/clj-zeppelin"
  :license {:name "Apache 2.0 License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.json "0.2.6"]
                 ;;http requests
                 [http-kit "2.2.0"]
                 ;;logging
                 [com.taoensso/timbre "4.10.0"]
                 ]

  :plugins [[lein-codox "0.10.5"]]
  :codox {:output-path "docs"}
  :profiles {:test {:dependencies [[docker-fixture "0.1.2"]]}}
  )
