(ns am.ik.categolj.daccess.mirage.daccess
  (:use [am.ik.categolj.utils string-utils date-utils])
  (:use [am.ik.categolj.common])
  (:use [am.ik.categolj.daccess daccess entities])
  (:use [clojure.contrib singleton])
  (:require [clojure.contrib.logging :as log])
  (:require [clojure.string :as str])
  (:import [am.ik.categolj.daccess.entities Entry Category User])
  (:import [am.ik.categolj.daccess.mirage.entities EntryEntity CategoryEntity EntryCategory UserEntity])
  (:import [jp.sf.amateras.mirage SqlManager])
  (:import [jp.sf.amateras.mirage.session Session JDBCSessionImpl])
  (:import [jp.sf.amateras.mirage.exception SQLRuntimeException]))

(defmacro with-tx [[^Session session] & expr]
  `(do
     (.begin ~session)
     (try
       (let [ret# (do ~@expr)]
         (.commit ~session)                  
         ret#)
       (catch Exception e#
         (log/error e# "Exception occured in transaction!")
         (.rollback ~session)
         (throw e#))
       (finally
        (.release ~session)))))

;; (def session (create-session "org.hsqldb.jdbcDriver" "hsqldb" "mem:categolj" "sa" ""))
(defn ^Session create-session [classname subprotocol subname user password]
  (JDBCSessionImpl. classname (str "jdbc:" subprotocol ":" subname) user password))

(defn %update-by-sql [^Session session sql-file params]
  (log/debug (str "params=" params))
  (let [^SqlManager manager (.getSqlManager session)]
    (.executeUpdate manager sql-file params)))

(defn %get-single-entity-by-sql [^Session session clazz sql-file params]
  (log/debug (str "params=" params))
  (let [^SqlManager manager (.getSqlManager session)]
    (.getSingleResult manager clazz sql-file params)))

(defn %get-entities-by-sql [^Session session clazz sql-file params]
  (log/debug (str "params=" params))
  (let [^SqlManager manager (.getSqlManager session)]
    (.getResultList manager clazz sql-file params)))

(defn %get-count-by-sql [^Session session sql-file params]
  (%get-single-entity-by-sql session Long sql-file params))

(defn get-sql-path [subprotocol file-name]
  (log/debug (str "sqlfile=" subprotocol "/" file-name))
  (str "sql/" subprotocol "/" file-name))

(defn %insert-entry [subprotocol ^Session session params]
  (%update-by-sql session (get-sql-path subprotocol "insert-entry.sql") params))

(defn %insert-category [subprotocol ^Session session params]
  (%update-by-sql session (get-sql-path subprotocol "insert-category.sql") params))

(defn %insert-entry-category [subprotocol ^Session session params]
  (%update-by-sql session (get-sql-path subprotocol "insert-entry-category.sql") params))

(defn %update-entry [subprotocol ^Session session params]
  (%update-by-sql session (get-sql-path subprotocol "update-entry.sql") params))

(defn %delete-entry [subprotocol ^Session session params]
  (%update-by-sql session (get-sql-path subprotocol "delete-entry.sql") params))

(defn %delete-category [subprotocol ^Session session params]
  (%update-by-sql session (get-sql-path subprotocol "delete-category.sql") params))

(defn %get-entry-count [subprotocol ^Session session params]
  (%get-count-by-sql session (get-sql-path subprotocol "get-entry-count.sql") params))

(defn %get-category-count [subprotocol ^Session session params]
  (%get-count-by-sql session (get-sql-path subprotocol "get-category-count.sql") params))

(defn %delete-entry-category [subprotocol ^Session session params]
  (%update-by-sql session (get-sql-path subprotocol "delete-entry-category.sql") params))

(defn ^EntryEntity %get-entry-by-id [subprotocol ^Session session params]
  (%get-single-entity-by-sql session EntryEntity (get-sql-path subprotocol "get-entry-by-id.sql") params))

(defn ^java.util.List %get-categories-by-entry-id [subprotocol ^Session session params]
  (%get-entities-by-sql session CategoryEntity (get-sql-path subprotocol "get-categories-by-entry-id.sql") params))

(defn %get-all-category[^Session session]
  (let [^SqlManager manager (.getSqlManager session)]
    (.getResultListBySql manager CategoryEntity "SELECT * FROM Category")))

(defn %get-all-entry-cateogry [^Session session]
  (let [^SqlManager manager (.getSqlManager session)]
    (.getResultListBySql manager EntryCategory "SELECT * FROM EntryCategory")))

(defn ^java.util.List %get-entries-by-page [subprotocol ^Session session params]
  (%get-entities-by-sql session EntryEntity (get-sql-path subprotocol "get-entries-by-page.sql") params))

(defn ^java.util.List %get-entries-only-id-title [subprotocol ^Session session params]
  (%get-entities-by-sql session EntryEntity (get-sql-path subprotocol "get-entries-only-id-title.sql") params))

(defn %insert-category-if-not-exists [subprotocol ^Session session params]
  (if (< (%get-category-count subprotocol session params) 1)
    (%insert-category subprotocol session params) 0))

(defn %insert-user [subprotocol ^Session session params]
  (%update-by-sql session (get-sql-path subprotocol "insert-user.sql") params))

(defn %get-user [subprotocol ^Session session params]
  (%get-single-entity-by-sql session UserEntity (get-sql-path subprotocol "get-user.sql") params))

(defn ^Entry entity-to-record [^EntryEntity entity]
  (if entity
    (Entry.
     {}
     {:id (.id entity),
      :title (.title entity),
      :content (.content entity),
      :created-at (.createdAt entity),
      :updated-at (.updatedAt entity),
      :category nil
      })))

(defn ^EntryEntity entity-to-record-with-category [subprotocol ^Session session ^EntryEntity entity]
  (let [entry (entity-to-record entity)]
    (if entry
      (assoc entry
        :category (map name (%get-categories-by-entry-id subprotocol session {"id" (:id entry)}))))))

(defn ^java.util.List get-entry-records-with-category-by-page [subprotocol ^Session session params]
  (doall (map #(entity-to-record-with-category subprotocol session %)
              (%get-entries-by-page subprotocol session params))))

(defn ^EntryEntity record-to-entity [param]
  (if param
    (EntryEntity. (let [id (:id param)]
                    (if (string? id) (Long/valueOf (:id param)) id))
                  (:title param)
                  (:content param)
                  (let [created-at (:created-at param)]
                    (if (string? created-at) (parse-date created-at) created-at))
                  (let [updated-at (:updated-at param)]
                    (if (string? updated-at) (parse-date updated-at) updated-at))
                  (:category param)
                  )))

(defrecord MirageDataAccess [subprotocol ^Session session]
  DataAccess
  (get-entry-by-id 
   [this id]
   (with-tx [session]
     (entity-to-record-with-category subprotocol session
       (%get-entry-by-id subprotocol session {"id" id}))))
  
  (get-entries-by-page 
   [this page count]
   (with-tx [session]
     (get-entry-records-with-category-by-page subprotocol session
       {"limit" count, "offset" (* count (dec page))})))

  (get-entries-only-id-title
   [this count]
   (with-tx [session]
     (map entity-to-record
          (%get-entries-only-id-title subprotocol session {"limit" count}))))
  
  (get-total-entry-count
   [this]
   (with-tx [session]     
     (%get-entry-count subprotocol session {})))

  (insert-entry
   [this entry]
   (with-tx [session]
     (%insert-entry subprotocol session (keys-to-name entry))
     ;; insert category...
     (let [added (indexed-set (:category entry))]
       (doseq [[index name] added]
         (%insert-category-if-not-exists subprotocol session {"name" name, "index" index})
         (%insert-entry-category subprotocol session {"name" name, "entry_id" nil})))))
  
  (update-entry
   [this entry]
   (with-tx [session]
     (%update-entry subprotocol session (keys-to-name entry))
     (let [entry-id (:id entry)
           prev (map name (%get-categories-by-entry-id subprotocol session {"id" entry-id}))
           diff (difference-category prev (:category entry))
           removed (:removed diff)
           added (:added diff)]
       ;; delete removed categories
       (doseq [[index name] removed]
         (%delete-entry-category subprotocol session {"entry_id" entry-id, "name" name, "category_id" nil}))
       ;; insert added categories
       (doseq [[index name] added]
         (%insert-category-if-not-exists subprotocol session {"name" name, "index" index})
         (%insert-entry-category subprotocol session {"entry_id" entry-id, "name" name})))))
  
  (delete-entry
   [this entry]
   (with-tx [session]
     (%delete-entry subprotocol session {"id" (:id entry)})))

  (get-categorized-entries-by-page
   [this category page count]
   (let [target (last (indexed category))]
     ;; ignore butlast category... (TODO)
     (with-tx [session]
       (get-entry-records-with-category-by-page subprotocol session
         {"limit" count, "offset" (* count (dec page)),
          "index" (first target), "name" (second target)}))))
  
  (get-categorized-entry-count
   [this category]
   (let [target (last (indexed category))]
     ;; ignore butlast category... (TODO)
     (with-tx [session]
       (%get-entry-count subprotocol session
                         {"index" (first target), "name" (second target)}))))

  (get-all-category-list
   [this]
   (with-tx [session]
     ;; silly implementation!! (due to Mirage...)
     (let [cs (%get-all-category session)
           cs (sort (fn [^CategoryEntity x ^CategoryEntity y] ; shold be done in sql...
                      (< (.index x) (.index y))) cs)
           ecs (%get-all-entry-cateogry session)
           c-map (zipmap (map #(.id ^CategoryEntity %) cs) cs)
           ^java.util.Map m (java.util.HashMap.)]
       (doseq [^EntryCategory ec ecs]
         (let [cid (.categoryId ec)
               eid (.entryId ec)
               ^CategoryEntity c (get c-map cid)]
           (if-not (contains? m eid)
             (.put m eid (java.util.HashMap.)))
           (let [e (get m eid)]
             (.put ^java.util.Map e (.index c) (name c)))))
       (let [^java.util.Set s (java.util.TreeSet.
                               (fn [x y] (compare (apply str x) (apply str y))))]
         (.addAll s (map #(into [] (vals %)) (vals m)))
         s))))
  
  (auth-user
   [this user]
   (let [name (:name user)
         password (:password user)]
     (with-tx [session]
       (%get-user subprotocol session
                  {"name" name, "password" (md5 password)}))))
  )


(defn create-table
  "create Entry table if not exists."
  [subprotocol ^Session session]
  (with-tx [session]    
    (let [^SqlManager manager (.getSqlManager session)
          ddl-lines (str/split (slurp (get-resource (get-sql-path subprotocol "create-table.sql"))) #";")]
      (try
        ;; Check whether ENTITY table exists.
        (.getSingleResultBySql manager Integer "SELECT COUNT(id) FROM Entry")
        (catch SQLRuntimeException e
          ;; If not exits, then execute DDL
          (dorun 
           (map #(.executeUpdateBySql manager %) ddl-lines))
          ;; Insert test data.
          (%insert-entry subprotocol session {"id" 1, "title" "Title1", "content" "hogehoge", "created-at" (java.util.Date.), "updated-at" (java.util.Date.)})
          (%insert-entry subprotocol session {"id" 2, "title" "Title2", "content" "`hogehoge`", "created-at" (java.util.Date.), "updated-at" (java.util.Date.)})
          (%insert-entry subprotocol session {"id" 3, "title" "Title3", "content" "### hogehoge", "created-at" (java.util.Date.), "updated-at" (java.util.Date.)})
          (%insert-entry subprotocol session {"id" 4, "title" "Title4", "content" "**hogehoge**", "created-at" (java.util.Date.), "updated-at" (java.util.Date.)})
          (%insert-entry subprotocol session {"id" 5, "title" "Title5", "content" "#### hogehoge", "created-at" (java.util.Date.), "updated-at" (java.util.Date.)})
          (%insert-entry subprotocol session {"id" 6, "title" "Title6", "content" "    hogehoge", "created-at" (java.util.Date.), "updated-at" (java.util.Date.)})
          ;;
          (%insert-category subprotocol session {"id" 1, "name" "Hoge", "index" 1})
          (%insert-category subprotocol session {"id" 2, "name" "Foo", "index" 2})
          (%insert-category subprotocol session {"id" 3, "name" "Bar", "index" 3})
          (%insert-category subprotocol session {"id" 4, "name" "Aho", "index" 3})
          ;;
          (%insert-entry-category subprotocol session {"entry_id" 1, "category_id" 1})
          (%insert-entry-category subprotocol session {"entry_id" 1, "category_id" 2})
          (%insert-entry-category subprotocol session {"entry_id" 2, "category_id" 1})
          (%insert-entry-category subprotocol session {"entry_id" 2, "category_id" 2})
          (%insert-entry-category subprotocol session {"entry_id" 3, "category_id" 1})
          (%insert-entry-category subprotocol session {"entry_id" 4, "category_id" 1})
          (%insert-entry-category subprotocol session {"entry_id" 4, "category_id" 2})
          (%insert-entry-category subprotocol session {"entry_id" 4, "category_id" 4})
          (%insert-entry-category subprotocol session {"entry_id" 5, "category_id" 1})         
          (%insert-entry-category subprotocol session {"entry_id" 5, "category_id" 2})
          (%insert-entry-category subprotocol session {"entry_id" 5, "category_id" 3})
          (%insert-entry-category subprotocol session {"entry_id" 6, "category_id" 1})
          ;;
          (%insert-user subprotocol session {"id" 1, "name" "aaaa", "password" (md5 "aaaa")})
          )))))


(defn create-daccess [params]
  (let [{:keys [classname subprotocol subname user password]} (:db params)
        session (create-session classname subprotocol subname user password)]
    (create-table subprotocol session)    
    (MirageDataAccess. subprotocol session)))