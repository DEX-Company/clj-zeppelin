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
  (let [note-id (create-note! nbserver1  {:name "clojure note1"})
           ids (map :id (get-in (list-notes nbserver1) [:body]))]
     {:created-note-id note-id :retrieved-note-ids ids})) 
  
 
(deftest test-create-note
  (let [note-id (create-note-helper nbserver1)
   x (some #{(:created-note-id note-id)} (:retrieved-note-ids note-id))]
   (is x)))


(deftest delete-note-check
  (let [ret-ids (create-note-helper nbserver1)
   note-id (:created-note-id ret-ids)
   return-ids (:retrieved-note-ids ret-ids)
   x (some #{note-id} return-ids)]
       (if (not (= nil x))
          (let [sts (delete-note! nbserver1 note-id)             
            y (some #{note-id} (map :id (get-in (list-notes nbserver1) [:body])))]
            (is (nil? y))))))


;;test for create para
(deftest test-create-para
  (let [ret-ids (create-note-helper nbserver1)
   note-id (:created-note-id ret-ids)
   return-ids (:retrieved-note-ids ret-ids)
   x (some #{note-id} return-ids)]
       (if (not (nil? x))
         (let [para-id (create-paragraph! nbserver1 note-id (-> {:title "intro"
                                                                 :text "\n Hello user"}))]          
           (if (not (nil? para-id))
             (let [sts (get-paragraph-status nbserver1 note-id para-id)]
                (if (= "READY" (:status sts))
                 (let [res (:text (get-paragraph-info nbserver1 note-id para-id))]
                  (is (= "\n Hello user"  res))))))))))
             
                    
      








