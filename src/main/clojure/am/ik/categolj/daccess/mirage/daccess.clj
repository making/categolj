(ns am.ik.categolj.daccess.mirage.daccess
  (:use [am.ik.categolj.utils date-utils])
  (:use [am.ik.categolj.daccess daccess entities])
  (:use [clojure.contrib singleton])
  (:require [clojure.contrib.logging :as log])
  (:import [am.ik.categolj.daccess.entities Entry Category User])
  (:import [am.ik.categolj.daccess.mirage.entities EntryEntity CategoryEntity EntryCategory])
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
         (log/error "Exception occured in transaction!" e#)
         (.rollback ~session)
         (throw e#))
       (finally
        (.release ~session)))))

;; (def (create-session "org.hsqldb.jdbcDriver" "hsqldb" "mem:categolj" "sa" ""))
(defn ^Session create-session [classname subprotocol subname user password]
  (JDBCSessionImpl. classname (str "jdbc:" subprotocol ":" subname) user password))

(defn %update-by-sql [^Session session sql-file params]
  (let [^SqlManager manager (.getSqlManager session)]
    (.executeUpdate (.getSqlManager session) sql-file params)))

(defn %get-single-entity [^Session session clazz sql-file params]
  (let [^SqlManager manager (.getSqlManager session)]
    (.getSingleResult manager clazz sql-file params)))

(defn %get-entities [^Session session clazz sql-file params]
  (let [^SqlManager manager (.getSqlManager session)]
    (.getResultList manager clazz sql-file params)))

(defn get-sql-path [subprotocol file-name]
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

(defn %get-total-entry-count [subprotocol ^Session session]
  (let [^SqlManager manager (.getSqlManager session)]
    (.getCount
     (.getSqlManager session)
     (get-sql-path subprotocol "get-total-entry-count.sql"))))
  
;; (defn %delete-entry-category [subprotocol ^Session session params]
;;   (%update-by-sql session "sql/delete-entry-category.sql" params))

(defn ^EntryEntity %get-entry-by-id [subprotocol ^Session session params]
  (%get-single-entity session EntryEntity (get-sql-path subprotocol "get-entry-by-id.sql") params))

(defn ^java.util.List %get-categories-by-entry-id [subprotocol ^Session session params]
  (%get-entities session CategoryEntity (get-sql-path subprotocol "get-categories-by-entry-id.sql") params))

(defn ^java.util.List %get-entries-by-page [subprotocol ^Session session params]
  (%get-entities session EntryEntity (get-sql-path subprotocol "get-entries-by-page.sql") params))

(defn ^java.util.List %get-entries-only-id-title [subprotocol ^Session session params]
  (%get-entities session EntryEntity (get-sql-path subprotocol "get-entries-only-id-title.sql") params))

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
        :category (map #(.name %)
                       (remove nil? (%get-categories-by-entry-id subprotocol session {"id" (:id entry)})))))))

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
     (doall (map #(entity-to-record-with-category subprotocol session %)
                 (%get-entries-by-page
                  subprotocol session
                  {"limit" count, "offset" (* count (dec page))})))))

  (get-entries-only-id-title
   [this count]
   (with-tx [session]
     (map entity-to-record
          (%get-entries-only-id-title subprotocol session {"limit" count}))))
  
  (get-total-entry-count
   [this]
   (with-tx [session]     
     (%get-total-entry-count subprotocol session)))

  (insert-entry
   [this entry]
   (with-tx [session]
     (%insert-entry subprotocol session (zipmap (map name (keys entry)) (vals entry)))
     ;; insert category...
     ))
  
  (update-entry
   [this entry]
   (with-tx [session]
     (%update-entry subprotocol session (zipmap (map name (keys entry)) (vals entry)))))
  
  (delete-entry
   [this entry]
   (with-tx [session]
     (%delete-entry subprotocol session {"id" (:id entry)})))
  )


(defn create-table
  "create Entry table if not exists."
  [subprotocol ^Session session ddl]
  (with-tx [session]    
    (let [^SqlManager manager (.getSqlManager session)]
      (try
        ;; Check whether ENTITY table exists.
        (.getSingleResultBySql (.getSqlManager session) Integer
                               "SELECT COUNT(id) FROM Entry")
        (catch SQLRuntimeException e
          ;; If not exits, then create.
          (if (coll? ddl)
            (dorun (map #(.executeUpdateBySql manager %) ddl))
            (.executeUpdateBySql manager ddl))
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
          (%insert-entry-category subprotocol session {"entry-id" 1, "category-id" 1})
          (%insert-entry-category subprotocol session {"entry-id" 1, "category-id" 2})
          (%insert-entry-category subprotocol session {"entry-id" 2, "category-id" 1})
          (%insert-entry-category subprotocol session {"entry-id" 2, "category-id" 2})
          (%insert-entry-category subprotocol session {"entry-id" 3, "category-id" 1})
          (%insert-entry-category subprotocol session {"entry-id" 4, "category-id" 1})
          (%insert-entry-category subprotocol session {"entry-id" 4, "category-id" 2})
          (%insert-entry-category subprotocol session {"entry-id" 4, "category-id" 4})
          (%insert-entry-category subprotocol session {"entry-id" 5, "category-id" 1})         
          (%insert-entry-category subprotocol session {"entry-id" 5, "category-id" 2})
          (%insert-entry-category subprotocol session {"entry-id" 5, "category-id" 3})
          (%insert-entry-category subprotocol session {"entry-id" 6, "category-id" 1})
          )))))


(defn create-daccess [params]
  (let [{:keys [classname subprotocol subname user password]} (:db params)
        session (create-session classname subprotocol subname user password)
        ddl (:ddl params)]
    (create-table subprotocol session ddl)    
    (MirageDataAccess. subprotocol session)))
