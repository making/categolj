(ns am.ik.categolj.daccess.mock.daccess
  (:use [am.ik.categolj.daccess daccess entities])
  (:import [am.ik.categolj.daccess.entities Entry Category User]))

(defprotocol DataProvider
  (get-data [this]))

(defrecord MockDataAccess []
  DataProvider
  (get-data
   [_]
   (map #(Entry. {} %) 
        [{:id 43, 
          :title "ELPA(Emacs Lisp Package Archive) を使う", 
          :created-at (.parse (java.text.SimpleDateFormat. "yyyy-MM-dd HH:mm:ss") "2010-10-02 11:35:22")
          :updated-at (.parse (java.text.SimpleDateFormat. "yyyy-MM-dd HH:mm:ss") "2010-10-02 11:54:29")
          :content "以下を`*scratch*`に貼り付けて`Ctrl+j`で.emacs.elがupdateされる。"
          :category ["開発環境" "IDE" "Emacs"]
          },
         {:id 44, 
          :title "WindowsでLeiningen", 
          :created-at (.parse (java.text.SimpleDateFormat. "yyyy-MM-dd HH:mm:ss") "2010-10-05 02:24:44")
          :updated-at (.parse (java.text.SimpleDateFormat. "yyyy-MM-dd HH:mm:ss") "2010-10-05 02:47:30")
          :content "知らない間にLeiningenがWindows対応していました"
          :category ["Programming" "Lisp" "Clojure" "Leiningen"]
          },
         ]))

  DataAccess
  (get-entry-by-id 
   [this id]
   (first (filter #(= (:id %) id) (get-data this))))
  (get-entries-by-page 
   [this page count]
   (get-data this)))
