(ns clj-zeppelin.note
  (:require [clojure.data.json :as json]
            [clojure.walk :refer [keywordize-keys stringify-keys]]))


(defn paragraph-raw
  ([title magic text]
   {:title title :text (str "%" (name magic) "\n"
                            (clojure.string/join "\n" text))})
  )

(defn note
  [nbname paragraphs]
  {:name nbname :paragraphs paragraphs})

(defn finalize
  [inp]
  (-> inp
      stringify-keys
      json/write-str))
