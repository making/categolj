(ns am.ik.categolj.utils.string-utils
  (:import [org.apache.commons.lang.builder ToStringBuilder])
  (:require [clojure.string :as str]))

(defn ^String to-string [x]
  (ToStringBuilder/reflectionToString x))

(defn split-by-slash [s]
  (if s
    (remove nil? (str/split s #"\/"))))

(defn url-encode [str]
  (if str (java.net.URLEncoder/encode str) str))

(defn url-decode [str]
  (if str (java.net.URLDecoder/decode str) str))