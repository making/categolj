(ns am.ik.categolj.daccess.mock.daccess
  (:use [am.ik.categolj.daccess daccess entities])
  (:import [am.ik.categolj.daccess.entities Entry Category User]))

(def *data* (map #(Entry. {} %) 
                 [{:id 44, 
                   :title "WindowsでLeiningen", 
                   :created-at (.parse (java.text.SimpleDateFormat. "yyyy-MM-dd HH:mm:ss") "2010-10-05 02:24:44")
                   :updated-at (.parse (java.text.SimpleDateFormat. "yyyy-MM-dd HH:mm:ss") "2010-10-05 02:47:30")
                   :content "知らない間にLeiningenがWindows対応していました"
                   :category ["Programming" "Lisp" "Clojure" "Leiningen"]
                   },
                  {:id 43, 
                   :title "ELPA(Emacs Lisp Package Archive) を使う", 
                   :created-at (.parse (java.text.SimpleDateFormat. "yyyy-MM-dd HH:mm:ss") "2010-10-02 11:35:22")
                   :updated-at (.parse (java.text.SimpleDateFormat. "yyyy-MM-dd HH:mm:ss") "2010-10-02 11:54:29")
                   :content "以下を`*scratch*`に貼り付けて`Ctrl+j`で.emacs.elがupdateされる。"
                   :category ["開発環境" "IDE" "Emacs"]
                   },
                  ]))

(defrecord MockDataAccess []
  DataAccess
  (get-entry-by-id 
   [this id]
   (first (filter #(= (:id %) id) *data*)))
  (get-entries-by-page 
   [this page count]
   *data*)
  (get-total-count
   [this]
   (count *data*)))

(defn create-daccess [params]
  (MockDataAccess.))
