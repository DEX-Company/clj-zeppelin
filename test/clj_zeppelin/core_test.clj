(ns clj-zeppelin.core-test
  (:require [clojure.test :refer :all]
            [docker.fixture :as docker]
            [org.httpkit.client :as http]
            [clj-zeppelin.core :refer :all]
                        ))



 ;;prove interaction with fixture by init-fn
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

 ;;did the init-fn interact with the fixture?
(deftest test-fixture-init
  (let [resp (:status @fixture-response)]
    (println " got response " resp)
    (is (= 200 resp))))

(deftest test-list-notes
  (let [resp (list-notes "http://localhost:8080")
        num-notes (-> resp :body count)]
    (println " list notes " num-notes)
    (is (> num-notes 0))))


(def nbserver1 "http://localhost:8080")

(defn create-note-helper
  [nbserver1]
  (let [note-id (create-note! nbserver1  {:name "trial clojure note"})
          ids (map :id (get-in (list-notes nbserver1) [:body]))]
     {:created-note-id note-id :retrieved-note-ids ids})) 
  
 
(deftest test-create-note
  (let [note-id (create-note-helper nbserver1)
   x (some #{(get-in note-id [:created-note-id])} (get-in note-id [:retrieved-note-ids]))]
    (println x)
    (is (not= nil x))))


(deftest delete-note-check
  (let [ret-ids (create-note-helper nbserver1)
   x (some #{(get-in ret-ids [:created-note-id])} (get-in ret-ids [:retrieved-note-ids]))]
       (if (not (= nil x))
          (let [sts (delete-note! nbserver1 (get-in ret-ids [:created-note-id]))
            y (some #{(get-in ret-ids [:created-note-id])} (get-in ret-ids [:retrieved-note-ids]))]
            (is (= nil y))))))






