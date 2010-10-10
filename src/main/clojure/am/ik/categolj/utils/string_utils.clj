(ns am.ik.categolj.utils.string-utils
  (:import [org.apache.commons.lang.builder ToStringBuilder]))

(defn ^String to-string [x]
  (ToStringBuilder/reflectionToString x))

