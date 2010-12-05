(ns am.ik.categolj.markdown
  (:use [clojure.contrib.singleton])
  (:import [com.petebevin.markdown MarkdownProcessor]))

(def ^{:dynamic true} *markdown* (per-thread-singleton #(MarkdownProcessor.)))

(defn ^String markdown [content]
  (.markdown ^MarkdownProcessor (*markdown*) content))