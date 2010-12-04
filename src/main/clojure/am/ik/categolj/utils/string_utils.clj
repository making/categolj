(ns am.ik.categolj.utils.string-utils
  (:import [org.apache.commons.lang.builder ToStringBuilder])
  (:import [org.apache.commons.codec.net URLCodec])
  (:import [java.security MessageDigest])
  (:require [clojure.string :as str]))

(defn ^String to-string [x]
  (ToStringBuilder/reflectionToString x))

(defn get-bytes [^String str]
  (.getBytes str))

(defn split-by-slash [s]
  (if s
    (remove nil? (str/split s #"\/"))))

(defn url-encode [str]
  (if str (java.net.URLEncoder/encode str) str))

(defn url-decode [str]
  (if str (java.net.URLDecoder/decode str) str))

(defn md5 [str]
  (if str
    (let [alg (doto (MessageDigest/getInstance "MD5")
                (.reset)
                (.update (get-bytes str)))]
      (.toString (BigInteger. 1 (.digest alg)) 16))))