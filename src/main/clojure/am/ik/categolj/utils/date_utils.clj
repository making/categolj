(ns am.ik.categolj.utils.date-utils)

(defn format-date [^java.util.Date date]
  (if date
    (.format (java.text.SimpleDateFormat. "yyyy/MM/dd HH:mm:ss") date) ""))  

(defn parse-date [str]
  (if str
    (let [sdf (java.text.SimpleDateFormat. "yyyy/MM/dd HH:mm:ss")]
      (.parse sdf str))))
