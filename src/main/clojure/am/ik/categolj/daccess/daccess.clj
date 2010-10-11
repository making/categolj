(ns am.ik.categolj.daccess.daccess
  (:use [am.ik.categolj.daccess.entities])
  (:import [am.ik.categolj.daccess.entities Entry Category User]))

(defprotocol DataAccess
  (get-entry-by-id [this id])
  (get-entries-by-page [this page count]))