
(ns reasoned-rhymer.db
  (:require [datomic.api :as d]
            [clojure.string :as str]))

(def uri "datomic:free://localhost:4334/rhymer")
(def schema (read-string (slurp (clojure.java.io/resource "schema.edn"))))

(defn create-db! []
  (d/create-database uri))

(defn connect-db! []
  (println "connecting to the db")
  (d/connect uri))

(defn init-db! []
  (println "initializing the database")
  (create-db!)
  (let [conn (connect-db!)]
    @(d/transact conn schema)
    conn))

(defn add-poem-analysis!
  "Takes a poem's title, the poem's text, and the poem's analysis and
   stores it in the db"
  [title text analysis]
  (let [conn (connect-db!)]
    @(d/transact conn [{:db/id #db/id[:db.part/user]
                        :poem/title title
                        :poem/text text
                        :poem/analysis analysis}])))

(defn get-poem-data [title]
  (let [conn (connect-db!)
        query '[:find ?analysis ?text :in $ ?title
                :where
                [?poem :poem/title ?title]
                [?poem :poem/analysis ?analysis]
                [?poem :poem/text ?text]]]
    (when-let [results (first (d/q query (d/db conn) title))]
      {:text (nth results 1) :title title
       :analysis (-> results first read-string)})))

(defn get-all-titles []
  (let [conn (connect-db!)
        query '[:find ?title
                :where
                [?poem :poem/analysis]
                [?poem :poem/title ?title]]]
    (remove str/blank? (map first (d/q query (d/db conn))))))
