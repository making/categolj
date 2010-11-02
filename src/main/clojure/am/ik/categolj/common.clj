(ns am.ik.categolj.common
  (:require [clojure.string :as str])
  (:require [clojure.set :as s])
  )

(defn ^java.net.URL get-resource [filename]
  (.getResource (.getContextClassLoader (Thread/currentThread)) filename))

(defn get-entry-view-url [id title]
  (str "/entry/view/id/" id "/" (if title (str "title/" title "/"))))

(defn get-entry-edit-url [id]
  (str "/entry/edit/id/" id "/"))

(defn get-entry-delete-url [id]
  (str "/entry/delete/id/" id "/"))

(defn get-category-url [category-seq]
  (map (fn [x i] [x (str "/category/" (str/join "/" (take (inc i) category-seq)) "/")])
       category-seq (range (count category-seq))))

(defn keys-to-name [m]
  (zipmap (map name (keys m)) (vals m)))

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
  (let [fs (apply hash-set (map vector (iterate inc 1) from))
        ts (apply hash-set (map vector (iterate inc 1) to))]
    {:removed (s/difference fs ts),
     :added (s/difference ts fs),}
    ))