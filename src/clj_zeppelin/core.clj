(ns clj-zeppelin.core
  (:require [clojure.data.json :as json]
            [clojure.walk :refer [keywordize-keys stringify-keys]]
            [clj-zeppelin.note :refer [paragraph-raw note finalize]]
            [org.httpkit.client :as ht]
                       ))

(defn- kwdize-resp
  "keywordize the response when successful"
  [resp]
  (let [istr (-> resp :body )]
        (try
          (do 
            (clojure.walk/keywordize-keys (json/read-str istr)))
          (catch Exception e
            (println " kwdize error " istr)))))

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
  [notebook-api-url]
  (let [resp @(ht/request {:url notebook-api-url
                           :method :get})]
     (if (:error resp)
       (throw (ex-info " error listing notes " resp))
       (kwdize-resp resp))
    )
  )

(defn create-note!
  "
  create a new note
  https://zeppelin.apache.org/docs/0.8.0/usage/rest_api/notebook.html#create-a-new-note

  Returns the id of the new note
  "
  [notebook-api-url payload]
  (let [resp @(ht/request {:url notebook-api-url
                           :method :post
                           :body (finalize payload)
                           })]
    (if (:error resp)
      (throw (ex-info " error creating note notes " resp))
      (-> resp :body json/read-str (get "body")))))

(defn delete-note!
  "
  deletes a note. Needs the note id as argument
  https://zeppelin.apache.org/docs/0.8.0/usage/rest_api/notebook.html#delete-a-note
  Returns the status 
  "
  [notebook-api-url note-id]
  (let [resp @(ht/request {:url (str notebook-api-url note-id )
                           :method :delete})]
    (if (:error resp)
      (throw (ex-info " error deleting note " note-id ", response:  " resp))
      (kwdize-resp resp))))

(defn json-convert
  [url-data]
  (let [finalize-data (finalize (slurp url-data))
        jsondata (json/read-str finalize-data)]
       jsondata))   

(defn import-note!
  "
  Imports a note
  https://zeppelin.apache.org/docs/0.8.0/usage/rest_api/notebook.html#import-a-note

  Returns the id of the imported note
  "
  [notebook-api-url note_json_url]
  (try
  (let [resp @(ht/request {:url (str notebook-api-url "import")
                           :method :post
                           :body (json-convert note_json_url )
                           })]
     
      (-> resp :body json/read-str (get "body")))
   (catch Exception e (println (str "caught exception: " (.toString e))))
      ))

(defn run-all-paragraphs
  "runs all the paragraphs

  https://zeppelin.apache.org/docs/0.8.0/usage/rest_api/notebook.html#run-all-paragraphs
  Returns status as a map
  "
  [notebook-api-url note-id]
  (let [resp @(ht/request {:url (str notebook-api-url "job/" note-id)
                           :method :post})]
    (if (:error resp)
      (throw (ex-info " error running note " resp))
      (kwdize-resp resp))))

(defn create-paragraph!
  "Creates a new paragraph, added to note-id.
  https://zeppelin.apache.org/docs/0.8.0/usage/rest_api/notebook.html#create-a-new-paragraph

  returns paragraph id
  "
  [notebook-api-url note-id paragraph-data]
  (let [resp @(ht/request {:url (str notebook-api-url note-id "/paragraph")
                           :method :post
                           :body (finalize paragraph-data)})]
    (if (:error resp)
      (throw (ex-info " error creating paragraph " resp))
      (-> resp kwdize-resp :body))))

(defn get-paragraph-status
  [notebook-api-url note-id paragraph-id]
  (let [resp @(ht/request {:url (str notebook-api-url "job/" note-id "/" paragraph-id)
                           :method :get})]
    (if (:error resp)
      (throw (ex-info " error getting status " resp))
      ;resp
      (-> resp kwdize-resp :body))))

(defn get-paragraph-info
  "returns information about a paragraph
  https://zeppelin.apache.org/docs/0.8.0/usage/rest_api/notebook.html#get-a-paragraph-information

  Takes as arguments the notebook server, the note id and paragraph id"
  [notebook-api-url note-id paragraph-id]
  (let [resp @(ht/request {:url (str notebook-api-url  note-id "/paragraph/" paragraph-id)
                           :method :get})]
    (if (:error resp)
      (throw (ex-info " error getting paragraph info " resp))
      (-> resp kwdize-resp :body))))

(defn run-paragraph-async
  "runs a paragraph asynchronously
  https://zeppelin.apache.org/docs/0.8.0/usage/rest_api/notebook.html#run-a-paragraph-asynchronously

  Takes as arguments the notebook server, the note id and paragraph id.

  Returns immediately, and the status is usually PENDING"
  [notebook-api-url note-id paragraph-id]
  (let [resp @(ht/request {:url (str notebook-api-url "job/" note-id "/" paragraph-id)
                           :method :post})]
    
    (if (:error resp)
      (throw (ex-info " error running paragraph " resp))
      (-> resp kwdize-resp :status))))

(defn run-paragraph-sync
  "runs a paragraph synchronously
  https://zeppelin.apache.org/docs/0.8.0/usage/rest_api/notebook.html#run-a-paragraph-synchronously

  Takes as arguments the notebook server, the note id and paragraph id.

  Returns after the paragraph completes execution "
  [notebook-api-url note-id paragraph-id]
  (let [resp @(ht/request {:url (str notebook-api-url "run/" note-id "/" paragraph-id)
                           :method :post})]
    (if (:error resp)
      (throw (ex-info " error running paragraph " resp))
      (-> resp kwdize-resp :body))))



