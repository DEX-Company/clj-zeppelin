(ns clj-zeppelin.core
  (:require [clojure.data.json :as json]
            [clojure.walk :refer [keywordize-keys stringify-keys]]
            [org.httpkit.client :as ht]))

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
      (-> resp :body json/read-str clojure.walk/keywordize-keys))))

;;(list-notes "http://localhost:8081")
(def nbserver "http://localhost:8081")
(defn create-note!
  "
  create a new note
  https://zeppelin.apache.org/docs/0.8.0/usage/rest_api/notebook.html#create-a-new-note

  Returns the id of the new note
  "
  [notebook-server-url payload]
  (let [resp @(ht/request {:url (str notebook-server-url "/api/notebook")
                           :method :post
                           :body payload})]
    (if (:error resp)
      (throw (ex-info " error creating note notes " resp))
      (-> resp :body json/read-str (get "body")))))


#_(create-note! nbserver
              (-> {:name "Ocean Notebook"
                :paragraphs [{:title "intro"
                              :text "%md\n Hello world\n"}]}
                  stringify-keys
                  json/write-str))

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
      (-> resp :body json/read-str clojure.walk/keywordize-keys))))

;;(delete-note! nbserver "2DRF4MVSW")

