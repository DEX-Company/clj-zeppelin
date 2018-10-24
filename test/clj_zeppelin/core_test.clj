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
  (let [note-id (create-note! nbserver1  {:name "clojure note12"})
       ids (map :id (get-in (list-notes nbserver1) [:body]))]
   {:created-note-id note-id :retrieved-note-ids ids})) 


;;delete-note used in fixture function
(defn delete-note
  []
(let [list-id (list-notes nbserver1) 
     ids-to-delete  (->> (get-in list-id [:body])
                                    ;;filter those that have :name = clojure note 12
                                  (filter #(= (:name %) "clojure note12"))
                                    ;;get the ids of those that pass the filter.
                                   (map :id))]
 (doseq  [ids1 ids-to-delete] (delete-note! nbserver1 ids1))))


;;test-create-note
(deftest test-create-note
  (let [note-id (create-note-helper nbserver1)
   x (some #{(:created-note-id note-id)} (:retrieved-note-ids note-id))]
      (testing "note-id present in the returned list of ids or absent"
        (is x))))


;;test-delete-note
(deftest test-delete-note
  (let [ret-ids (create-note-helper nbserver1)
   note-id (:created-note-id ret-ids)
   return-ids (:retrieved-note-ids ret-ids)
   x (some #{note-id} return-ids)]
    (testing "note-id present in the returned list of ids or absent"
       (is x))
          (let [sts (delete-note! nbserver1 note-id)             
            y (some #{note-id} (map :id (get-in (list-notes nbserver1) [:body])))]
              (testing "note-id deleted or not deleted"
                (is (nil? y)))
       )))


;;create paragraph helper
(defn create-para-helper
  [note-id]
  (let [para-id (create-paragraph! nbserver1 note-id (-> {:title "intro"
                                                          :text  (+ 10 10)}))
        para-status (get-paragraph-status nbserver1 note-id para-id)]
    {:paragraph-id para-id :paragraph-status para-status}))


;;test-create-paragraph
(deftest test-create-para
  (let [ret-ids (create-note-helper nbserver1)
   note-id (:created-note-id ret-ids)
   return-ids (:retrieved-note-ids ret-ids)
   x (some #{note-id} return-ids)]
     (testing "note id created or not"
       (is x))
     (let [para-id-sts (create-para-helper note-id)
           para-id (:paragraph-id para-id-sts)
           para-status (:paragraph-status para-id-sts)]
       (testing "paragraph-id returned or not"
          (is para-id))
        (testing "paragraph status check"
            (is (= "READY" (:status para-status))))
        (let [res (:text (get-paragraph-info nbserver1 note-id para-id))]
            (testing "paragraph content check"
              (is (= "20"  res)))))))
     
  
;;test-run-paragraph-asynchronously
(deftest test-para-async
  (let [ret-ids (create-note-helper nbserver1)
   note-id (:created-note-id ret-ids)
   return-ids (:retrieved-note-ids ret-ids)
   x (some #{note-id} return-ids)]
     (testing "note id created or not"
       (is x))
       (let [para-id-sts (create-para-helper note-id)
           para-id (:paragraph-id para-id-sts)
           para-status (:paragraph-status para-id-sts)]
       (testing "paragraph-id returned or not"
          (is para-id))
        (testing "paragraph status check"
            (is (= "READY" (:status para-status))))
            (let [async-sts (run-paragraph-async nbserver1 note-id para-id)]
              (testing "check status of run paragraph asynchronously"
                 (is (= "OK" async-sts)))
              ))))
 

(defn create-para-helper-sync
  [note-id]
  (let [para-id (create-paragraph! nbserver1 note-id (-> {:title "intro"
                                                          :text  "%spark.pyspark
def nth_prime_number(n):
    # initial prime number list
    prime_list = [2]
    # first number to test if prime
    num = 3
    # keep generating primes until we get to the nth one
    while len(prime_list) < n:

        # check if num is divisible by any prime before it
        for p in prime_list:
            # if there is no remainder dividing the number
            # then the number is not a prime
            if num % p == 0:
                # break to stop testing more numbers, we know it's not a prime
                break

        # if it is a prime, then add it to the list
        # after a for loop, else runs if the break command has not been given
        else:
            # append to prime list
            prime_list.append(num)

        # same optimization you had, don't check even numbers
        num += 2

    # return the last prime number generated
    return prime_list[-1]
nth_prime_number(10000)
"}))
        para-status (get-paragraph-status nbserver1 note-id para-id)]
    {:paragraph-id para-id :paragraph-status para-status}))



;test-run-paragraph-synchronously
(deftest test-para-sync
    (let [ret-ids (create-note-helper nbserver1)
   note-id (:created-note-id ret-ids)
   return-ids (:retrieved-note-ids ret-ids)
   x (some #{note-id} return-ids)]
     (testing "note id created or not"
       (is x))
       (let [para-id-sts (create-para-helper-sync note-id)
           para-id (:paragraph-id para-id-sts)
           para-status (:paragraph-status para-id-sts)]
         (testing "paragraph-id returned or not"
          (is para-id))
        (testing "paragraph status check"
            (is (= "READY" (:status para-status))))
        (let [sync-sts (run-paragraph-sync nbserver1 note-id para-id)]
;        (println (:code sync-sts))))))
          (testing "check status of run paragraph synchronously"
                 (is (= "SUCCESS" (:code sync-sts))))
          ))))


 ;test status-run-para-sync
 (defn status-run-para-sync
   []
   (let [ret-ids (create-note-helper nbserver1)
   note-id (:created-note-id ret-ids)
   para-id (:paragraph-id (create-para-helper-sync note-id))]
  (def f 
  (future 
  (run-paragraph-sync nbserver1 note-id para-id)
  ))
  (get-paragraph-status nbserver1 note-id para-id)
  (println @f)
  ))

 
  (defn create-delete-fixture
  [f]
  (create-note-helper nbserver1)
  (f)
  (delete-note)
  )
  
  (clojure.test/use-fixtures :once create-delete-fixture)

  ;to run all the tests
;  (clojure.test/run-tests)


