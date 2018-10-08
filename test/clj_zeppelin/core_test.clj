(ns clj-zeppelin.core-test
  (:require [clojure.test :refer :all]
            [docker.fixture :as docker]
            [org.httpkit.client :as http]
            [clj-zeppelin.core :refer :all]
                        ))

;; prove interaction with fixture by init-fn
(def fixture-response (atom nil))

;; easy http GET
(defn component-http-get
  ([host]
   (let [resp @(http/get (str "http://localhost:8080/api/notebook/"))]
     (println " calling get, resp is " (:body resp) " host " host)
     resp)))

(use-fixtures :once
  (docker/new-fixture {:cmd ["docker" "run" "-d" "-p" "8080:8080"  "apache/zeppelin:0.8.0"]
                       :sleep 20000
                       :init-fn (fn [component]
                                  (reset! fixture-response
                                          (component-http-get (:host component))))}))

;; did the init-fn interact with the fixture?
(deftest test-fixture-init
  (let [resp (:status @fixture-response)]
    (println " got response " resp)
    (is (= 200 resp))))

(deftest test-list-notes
  (let [resp (list-notes "http://localhost:8080")
        num-notes (-> resp :body count)]
    (println " list notes " num-notes)
    (is (> num-notes 0))))



(deftest trial-check-note
    (let [nbserver1 "http://localhost:8080"
     note-id (-> (create-note! nbserver1 (-> {:name "trial clojure note"})))
     x (-> (some #{note-id} (map :id (get-in (list-notes nbserver1) [:body]))))]
    (is (= nil x))
 )
 )
