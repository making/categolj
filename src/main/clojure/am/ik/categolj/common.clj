(ns am.ik.categolj.common
  (:require [am.ik.categolj.utils.string-utils :as su])
  (:require [clojure.string :as str])
  (:require [clojure.set :as s])
  )

(defn ^java.net.URL get-resource [filename]
  (.getResource (.getContextClassLoader (Thread/currentThread)) filename))

(defn get-entry-view-url [id title]
  (str "/entry/view/id/" id "/" (if title (str "title/" (su/url-encode title) "/"))))

(defn get-entry-edit-url [id]
  (str "/entry/edit/id/" id "/"))

(defn get-entry-delete-url [id]
  (str "/entry/delete/id/" id "/"))

(defn get-category-url [category-seq]
  (let [categories (map su/url-encode category-seq)]
    (map (fn [x i] [x (str "/category/"
                           (str/join "/" (take (inc i) categories))
                           "/")])
         category-seq (range (count category-seq)))))

(defn keys-to-name [m]
  (zipmap (map name (keys m)) (vals m)))

(defn indexed-set [seq]
  (into #{} (apply sorted-set-by (fn [x y] (< (first x) (first y))) (map vector (iterate inc 1) seq))))

(defn difference-category
  "returns map which has the set of deleted value and index with :deleted key
   and the set of added value and index with :added key

  ex.
  (difference-category [\"hoge\" \"foo\" \"bar\"] [\"hoge\" \"foo\" \"boo\"])
   -> {:removed #{[3 \"bar\"], :added #{[3 \"boo\"]}}}

  (difference-category [\"hoge\" \"foo\"] [\"hoge\" \"foo\" \"bar\"])
   -> {:removed #{}, :added #{[3 \"bar\"]}}

  (difference-category [\"hoge\" \"bar\" \"foo\"] [\"hoge\" \"foo\" \"bar\"])
   -> {:removed #{[3 \"foo\"] [2 \"bar\"], :added #{[2 \"foo\"] [3 \"bar\"]}}}

  (difference-category [\"hoge\" \"foo\" \"bar\"] [\"hoge\" \"foo\" \"bar\"])
   -> {:removed #{}, :added #{}}
  "
  [from to]
  (let [fs (indexed-set from)
        ts (indexed-set to)]
    {:removed (s/difference fs ts),
     :added (s/difference ts fs),}
    ))

(defn calc-total-page [total-count count-per-page]
  (let [total-page (quot total-count count-per-page)]
    (if (and (pos? total-count) (zero? (rem total-count count-per-page)))
      total-page (inc total-page))))

(defn get-user [req]
  (get-in req [:session :user]))