(ns am.ik.categolj.daccess.daccess
  (:use [am.ik.categolj.daccess.entities])
  (:import [am.ik.categolj.daccess.entities Entry Category User]))

(defprotocol DataAccess
  ;; entry
  (get-entry-by-id [this id])
  (get-entries-by-page [this page count])
  (get-entries-only-id-title [this count])
  (get-total-entry-count [this])
  (get-categorized-entries-by-page [this category page count])
  (get-categorized-entry-count [this category])
  (insert-entry [this entry])
  (update-entry [this entry])
  (delete-entry [this entry])
  ;; user
  (auth-user [this user])
  (get-user-by-id [this id])
  (insert-user [this user])
  (update-user [this user])
  (delete-user [this user]))