(ns clj-zeppelin.core
  (:require [clojure.data.json :as json]
            [clojure.walk :refer [keywordize-keys stringify-keys]]
            [clj-zeppelin.note :refer [paragraph note finalize]]
            [org.httpkit.client :as ht]))

(defn- kwdize-resp
  "keywordize the response when successful"
  [resp]
  (-> resp :body json/read-str clojure.walk/keywordize-keys))

(defn json-req
  "create a json string from a clojure map"
  [payload]
  (-> payload 
      stringify-keys
      json/write-str))

(defn list-notes
  "
  List the existing notes
  https://zeppelin.apache.org/docs/0.8.0/usage/rest_api/notebook.html#list-of-the-notes

  "
  [notebook-server-url]
  (let [resp @(ht/request {:url (str notebook-server-url "/api/notebook")
                           :method :get})]
    (if (:error resp)
      (throw (ex-info " error listing notes " resp))
      (kwdize-resp resp))))

;;(list-notes "http://localhost:8081")
  (def nbserver "http://localhost:8080")
(defn create-note!
  "
  create a new note
  https://zeppelin.apache.org/docs/0.8.0/usage/rest_api/notebook.html#create-a-new-note

  Returns the id of the new note
  "
  [notebook-server-url payload]
  (let [resp @(ht/request {:url (str notebook-server-url "/api/notebook")
                           :method :post
                           :body (finalize payload)})]
    (if (:error resp)
      (throw (ex-info " error creating note notes " resp))
      (-> resp :body json/read-str (get "body")))))


#_(def note-id (create-note! nbserver
                             (-> {:name "Ocean Notebook"
                                  :paragraphs [{:title "intro"
                                                :text "%md\n Hello world\n"}]}
                                 stringify-keys
                                 json/write-str)))

(defn delete-note!
  "
  deletes a note. Needs the note id as argument
  https://zeppelin.apache.org/docs/0.8.0/usage/rest_api/notebook.html#delete-a-note
  Returns the status 
  "
  [notebook-server-url note-id]
  (let [resp @(ht/request {:url (str notebook-server-url "/api/notebook/" note-id )
                           :method :delete})]
    (if (:error resp)
      (throw (ex-info " error deleting note " note-id ", response:  " resp))
      (kwdize-resp resp))))

;;(delete-note! nbserver "2DRF4MVSW")

(defn import-note!
  "
  Imports a note
  https://zeppelin.apache.org/docs/0.8.0/usage/rest_api/notebook.html#import-a-note

  Returns the id of the imported note
  "
  []
  nil)

(defn run-all-paragraphs
  "runs all the paragraphs

  https://zeppelin.apache.org/docs/0.8.0/usage/rest_api/notebook.html#run-all-paragraphs
  Returns status as a map
  "
  [notebook-server-url note-id]
  (let [resp @(ht/request {:url (str notebook-server-url "/api/notebook/job/" note-id)
                           :method :post})]
    (if (:error resp)
      (throw (ex-info " error running note " resp))
      (kwdize-resp resp))))

;;(run-all-paragraphs nbserver "2DTJSPSWV")

(defn create-paragraph!
  "Creates a new paragraph, added to note-id.
  https://zeppelin.apache.org/docs/0.8.0/usage/rest_api/notebook.html#create-a-new-paragraph

  returns paragraph id
  "
  [notebook-server-url note-id paragraph-data]
  (let [resp @(ht/request {:url (str notebook-server-url "/api/notebook/" note-id "/paragraph")
                           :method :post
                           :body (finalize paragraph-data)})]
    (if (:error resp)
      (throw (ex-info " error creating paragraph " resp))
      (-> resp kwdize-resp :body))))

#_(create-paragraph! nbserver
                   "2DTMA8RR9"
                   (-> {:title "intro"
                        :text "%md\n Hello paragraph \n"}
                       json-req))
;;create first paragraph instead of appending to the last one 
#_(create-paragraph! nbserver
                   "2DTMA8RR9"
                   (-> {:title "intro"
                        :text "%md\n Hello first paragraph \n"
                        :index 0}
                       json-req))

(defn get-paragraph-status
  [notebook-server-url note-id paragraph-id]
  (let [resp @(ht/request {:url (str notebook-server-url "/api/notebook/job/" note-id "/" paragraph-id)
                           :method :get})]
    (if (:error resp)
      (throw (ex-info " error getting status " resp))
      ;resp
      (-> resp kwdize-resp :body))))

(defn get-paragraph-info
  "returns information about a paragraph
  https://zeppelin.apache.org/docs/0.8.0/usage/rest_api/notebook.html#get-a-paragraph-information

  Takes as arguments the notebook server, the note id and paragraph id"
  [notebook-server-url note-id paragraph-id]
  (let [resp @(ht/request {:url (str notebook-server-url "/api/notebook/" note-id "/paragraph/" paragraph-id)
                           :method :get})]
    (if (:error resp)
      (throw (ex-info " error getting paragraph info " resp))
      (-> resp kwdize-resp :body))))

#_(def res (let [note-id (create-note! nbserver (note "new2"
                                                    [(paragraph "title" :md ["hello new para"])]))
               para-id (create-paragraph! nbserver note-id 
                                          (paragraph "new1" :sh ["mkdir output2"]))
               res (run-all-paragraphs nbserver note-id)
               ]
           (get-paragraph-info nbserver note-id para-id)))

;(-> res )

(defn run-paragraph-async
  "runs a paragraph asynchronously
  https://zeppelin.apache.org/docs/0.8.0/usage/rest_api/notebook.html#run-a-paragraph-asynchronously

  Takes as arguments the notebook server, the note id and paragraph id.

  Returns immediately, and the status is usually PENDING"
  [notebook-server-url note-id paragraph-id]
  (let [resp @(ht/request {:url (str notebook-server-url "/api/notebook/job/" note-id "/" paragraph-id)
                           :method :post})]
    (if (:error resp)
      (throw (ex-info " error running paragraph " resp))
      (-> resp kwdize-resp :body))))

(defn run-paragraph-sync
  "runs a paragraph synchronously
  https://zeppelin.apache.org/docs/0.8.0/usage/rest_api/notebook.html#run-a-paragraph-synchronously

  Takes as arguments the notebook server, the note id and paragraph id.

  Returns after the paragraph completes execution "
  [notebook-server-url note-id paragraph-id]
  (let [resp @(ht/request {:url (str notebook-server-url "/api/notebook/run/" note-id "/" paragraph-id)
                           :method :post})]
    (if (:error resp)
      (throw (ex-info " error running paragraph " resp))
      (-> resp kwdize-resp :body))))

#_(let [note-id (create-note! nbserver (note "new5"
                                           [(paragraph "title" :md ["hello new para"])]))
      para-id (create-paragraph! nbserver note-id 
                                 (paragraph "new1" :sh ["mkdir output2"]))
      res (run-paragraph-sync nbserver note-id para-id)
      ]
  (get-paragraph-info nbserver note-id para-id))

