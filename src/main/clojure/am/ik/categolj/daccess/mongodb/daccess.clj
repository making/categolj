(ns am.ik.categolj.daccess.mongodb.daccess
  (:use [am.ik.categolj.common])
  (:use [am.ik.categolj.utils.string-utils])
  (:use [am.ik.categolj.daccess daccess entities])
  (:require [clojure.contrib.logging :as log])
  (:import [am.ik.categolj.daccess.entities Entry Category User])
  (:import [com.mongodb BasicDBObject DB DBCollection DBCursor DBObject Mongo WriteResult]))

(def ^{:dynamic true} *entry* "entry")
(def ^{:dynamic true} *user* "user")
(def ^{:dynamic true} *seq* "seq")

(defn ^DBObject dbo
  ([]
     (BasicDBObject.))
  ([m]
     (let [obj (dbo)]
       (.putAll obj (keys-to-name m))
       obj)))

(defn ^DBCollection %coll [^DB db coll-name]
  (.getCollection db coll-name))

(defn ^WriteResult %insert [^DBCollection coll doc]
  (log/debug (str "insert " doc))
  (.insert coll doc))

(defn ^WriteResult %update [^DBCollection coll query doc]
  (log/debug (str "update " query " " doc))
  (.update coll query doc))

(defn ^WriteResult %remove [^DBCollection coll query]
  (log/debug (str "remove " query))
  (.remove coll query))

(defn ^DBCursor %find
  ([^DBCollection coll]
     (.find coll))
  ([^DBCollection coll query]
     (.find coll query))
  ([^DBCollection coll query fields]
     (.find coll query fields)))

(defn ^DBCursor %limit [^DBCursor cur n]
  (.limit cur n))

(defn ^DBCursor %skip [^DBCursor cur n]
  (.skip cur n))

(defn ^DBCursor %sort [^DBCursor cur ^DBObject order-by]
  (.sort cur order-by))

(defn doc-to-entry [doc]
  (if doc (Entry. {} (keys-to-keyword doc))))

(defn doc-to-user [doc]
  (if doc (User. {} (keys-to-keyword doc))))

(defn cur-seq [^DBCursor cur]
  (lazy-seq
    (if (.hasNext cur)
      (cons (.next cur) (cur-seq cur)))))

(defn ^DBCursor order-by-update-at [^DBCursor cur]
  (%sort cur (dbo {:updated-at -1})))

(defn combine-name-index [name index]
  (str name "|" index))

(defn ^DBObject categorized-query [category]
  (let [target (last (indexed category))]
    (dbo {:category-index (combine-name-index (second target) (first target))})))

(defn ^DBCursor add-option-to-get-by-page [^DBCursor cur offset count]
  (-> cur      
      (%skip offset)
      (%limit count)))

(defn inc-seq [db key]
  (let [^DBCollection coll (%coll db *seq*)
        query (dbo {:key key})
        update (dbo {:$inc (dbo {:value 1})})
        ret (.findAndModify coll query nil nil false update true true) ; upsert and returns new value
        ]
    (get ret "value")))

(defn get-category-index [category]
  (map #(combine-name-index (second %) (first %)) (indexed category)))

(deftype MongodbDataAccess [db closer]
  DataAccess
  (get-entry-by-id 
   [this id]
   (let [doc (.findOne (%coll db *entry*) (dbo {:id id}) (dbo {:_id 0, :category-index 0}))]
     (doc-to-entry doc)))
  
  (get-entries-by-page 
   [this page count]
   (let [coll (%coll db *entry*)
         offset (* (dec page) count)
         cur (order-by-update-at (%find coll nil (dbo {:_id 0, :category-index 0})))]
     (map doc-to-entry
          (cur-seq (add-option-to-get-by-page cur offset count)))))
  
  (get-entries-only-id-title
   [this count]
   (let [coll (%coll db *entry*)
         cur (order-by-update-at (%find coll nil (dbo {:_id 0, :title 1, :id 1})))]
     (map doc-to-entry
          (cur-seq (add-option-to-get-by-page cur 0 count)))))
   
  (get-total-entry-count
   [this]
   (.getCount (%coll db *entry*)))
  
  (insert-entry
   [this entry]
   (let [id (or (:id entry) (inc-seq db *entry*))
         category-index (get-category-index (:category entry))
         doc (dbo (assoc entry :id id, :category-index category-index))
         ^DBCollection coll (%coll db *entry*)]
     (.ensureIndex coll (dbo {:id 1}))
     (.ensureIndex coll (dbo {:category-index 1}))
     (%insert coll [doc])))
  
  (update-entry
   [this entry]
   (let [category-index (get-category-index (:category entry))
         doc (dbo (assoc entry :category-index category-index))]
     (%update (%coll db *entry*) (dbo {:id (:id entry)}) doc)))
  
  (delete-entry
   [this entry]
   (%remove (%coll db *entry*) (dbo {:id (:id entry)})))

  (get-categorized-entries-by-page
   [this category page count]
   (let [offset (* (dec page) count)
         coll (%coll db *entry*)
         query (categorized-query category)
         cur (order-by-update-at (%find coll query (dbo {:_id 0, :category-index 0})))]
     (map doc-to-entry (cur-seq (add-option-to-get-by-page cur offset count)))))
   
  (get-categorized-entry-count
   [this category]
   (let [coll (%coll db *entry*)
         query (categorized-query category)
         ^DBCursor cur (%find coll query (dbo {:_id 1}))]
     (.count cur)))

  (auth-user
   [this user]
   (let [query (dbo {:name (:name user), :password (md5 (:password user))})]
     (doc-to-user (.findOne (%coll db *user*) query (dbo {:password 0})))))

  java.io.Closeable
  (close
   [this]
   (try
     (closer)
     (catch Exception e
       (throw (.java.io.IOException e)))))
  )



(defn create-daccess [params]
  (let [{:keys [db host port]} params,
        connection (Mongo. (or host "localhost") (or port 27017))]
    (MongodbDataAccess. (.getDB ^Mongo connection db) #(.close connection))))

(comment
  (do (.close *d*) (def *d* (create-daccess {:db "test" ,:collection {:entry "entry"}})))
  ;; insert test data
  (insert-entry *d* {:title "Title1", :content "hogehoge", :created-at (java.util.Date.), :updated-at (java.util.Date.) :category ["foo" "bar" "zzz"]})
  (insert-entry *d* {:title "Title2", :content "`hogehoge`", :created-at (java.util.Date.), :updated-at (java.util.Date.) :category ["foo" "bar"]})
  (insert-entry *d* {:title "Title3", :content "### hogehoge", :created-at (java.util.Date.), :updated-at (java.util.Date.) :category ["foo" "bar" "zzz"]})
  (insert-entry *d* {:title "Title4", :content "**hogehoge**", :created-at (java.util.Date.), :updated-at (java.util.Date.) :category ["foo" "bar" "xxx"]})
  (insert-entry *d* {:title "Title5", :content "#### hogehoge", :created-at (java.util.Date.), :updated-at (java.util.Date.) :category ["foo" "bar"]})
  ;; insert test user
  (%insert (%coll (.db *d*) *user*) [(dbo {:id (inc-seq (.db *d*) *user*), :name "aaaa", :password (md5 "aaaa")})])
  )