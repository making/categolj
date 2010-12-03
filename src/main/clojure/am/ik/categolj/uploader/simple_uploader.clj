(ns am.ik.categolj.uploader.simple-uploader
  (:use [am.ik.categolj.uploader.uploader])
  (:require [clojure.contrib.logging :as log])
  (:require [clojure.string :as str])
  (:require [clojure.java.io :as io]))

(def *resources* (ref {}))

(defn filter-directory [fs]
  (filter #(.isDirectory ^java.io.File %) fs))

(defn delete-recursively [^java.io.File f]
  
  )

(defn init-resources [upload-dir]
  (let [^java.io.File dir (io/file upload-dir)]
    (dosync 
     (doseq [^java.io.File d (filter-directory (.listFiles dir))
             ^java.io.File f (.listFiles d)]
       (try
         (let [dir-name (.getName d)
               file-name (.getName f)
               id (Integer/parseInt dir-name)
               val {:dir dir-name, :file file-name}] 
           (alter *resources* assoc id val)
           (log/debug (str "add: " val)))
         (catch NumberFormatException ignore
           (log/error (str "skipped: " d " "f))))))))

(deftype SimpleUploadManager [upload-dir file-id-digits]
  UploadManager
  (upload
   [this file]
   (try
     (dosync 
      (let [ks (keys @*resources*)            
            id (if ks (inc (reduce max ks)) 1)
            splt (str/split (:filename file) #"\.")
            ext (last splt)
            dname (format (str "%0" file-id-digits "d") id)
            fname (if (> (count splt) 1)
                    (str (str/join "." (butlast splt)) "." (str/lower-case ext))
                    (:filename file))
            ^java.io.File dir (io/file upload-dir dname)
            _ (.mkdir dir)
            ^java.io.File out (io/file dir fname)
            tmp (:tempfile file)]
        (alter *resources* assoc id {:dir dname, :file fname})
        (log/info (str "upload: " file))
        (log/info (str "dest: " (.getAbsolutePath out)))
        (io/copy tmp out)
        (io/delete-file tmp true)
        {:res :ok,
         :file {:id id,
                :filename (str upload-dir dname "/" fname),
                :ext ext,
                :size (:size file)
                }}))
     (catch Throwable e
       (log/error e "upload failed!")
       {:res :ng})))

  (delete-uploaded-file-by-id
   [this id]
   (try
     (dosync
      (let [target (get @*resources* id)
            d (io/file upload-dir (:dir target))
            f (io/file d (:file target))]
        (log/info (str "delete: " target))
        (alter *resources* assoc id {})
        (io/delete-file f)
        (io/delete-file d true)
        {:res :ok, :id id}))
     (catch Throwable e
       (log/error e "delete failed!")
       {:res :ng})))
  
  (get-uploaded-files-by-page
   [this page count]
   ;; don't use page, count now
   (try
     (let [files (for [[id f] @*resources*]
                   (if (:file f)
                     (let [^java.io.File parent (io/file upload-dir (:dir f))
                           ^java.io.File target (io/file parent (:file f))
                           ext (last (str/split (:file f) #"\."))
                           size (.length target)]
                       {:id id,
                        :filename (str upload-dir (:dir f) "/" (:file f)),
                        :ext ext,
                        :size size})))]
       {:res :ok, :files (reverse (remove nil? files))})
     (catch Throwable e
       (log/error e "failed to get files!")
       {:res :ng}))
   ))

(defn create-upload-manager [params]
  (let [upload-dir (:upload-dir params)
        upload-dir (if (.startsWith ^String upload-dir "/") (str "." upload-dir) upload-dir)
        file-id-digits (:file-id-digits params)]
    (init-resources upload-dir)
    (SimpleUploadManager. upload-dir file-id-digits)))