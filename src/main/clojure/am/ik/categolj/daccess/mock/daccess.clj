(ns am.ik.categolj.daccess.mock.daccess
  (:use [am.ik.categolj.daccess daccess entities])
  (:use [am.ik.categolj.utils date-utils])
  (:import [am.ik.categolj.daccess.entities Entry Category User]))

(def *data* (ref {44 (Entry.
                      {}                  
                      {:id 44, 
                       :title "WindowsでLeiningen", 
                       :created-at (parse-date "2010/10/05 02:24:44")
                       :updated-at (parse-date "2010/10/05 02:47:30")
                       :content "知らない間にLeiningenがWindows対応していました"
                       :category ["Programming" "Lisp" "Clojure" "Leiningen"]
                       }),
                  43 (Entry.
                      {}
                      {:id 43, 
                       :title "ELPA(Emacs Lisp Package Archive) を使う", 
                       :created-at (parse-date "2010/10/02 11:35:22")
                       :updated-at (parse-date "2010/10/02 11:54:29")
                       :content "以下を`*scratch*`に貼り付けて`Ctrl+j`で.emacs.elがupdateされる。"
                       :category ["開発環境" "IDE" "Emacs"]
                       }),
                       }))

(defrecord MockDataAccess []
  DataAccess
  (get-entry-by-id 
   [this id]
   (let [id (if (string? id) (Long/valueOf id) id)]
     (get @*data* id)))
  (get-entries-by-page 
   [this page count]
   (vals @*data*))
  (get-entries-only-id-title
   [this count]
   (get-entries-by-page this 1 count))
  (get-total-entry-count
   [this]
   (count @*data*))
  (insert-entry
   [this entry]
   (let [id (inc (apply max (keys @*data*)))]
     (dosync 
      (alter *data* assoc id entry))))
  (update-entry
   [this entry]
   (let [id (if (string? (:id entry)) (Long/valueOf (:id entry)) (:id entry))]
     (dosync
      (alter *data* assoc id entry))))
  (delete-entry
   [this entry]
   (let [id (if (string? (:id entry)) (Long/valueOf (:id entry)) (:id entry))]
     (dosync
      (alter *data* dissoc (:id entry)))))
  )

(defn create-daccess [params]
  (MockDataAccess.))
