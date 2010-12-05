(ns am.ik.categolj.feed
  (:use [am.ik.categolj common markdown])
  (:require [clojure.contrib.logging :as log])
  (:import [am.ik.categolj.feed CustomizedWireFeedOutput])
  (:import [com.sun.syndication.feed.synd SyndContent SyndContentImpl SyndEntry SyndEntryImpl SyndFeed SyndFeedImpl])
  (:import [com.sun.syndication.io SyndFeedOutput FeedException]))

(defn- create-entry [{:keys [id title content created-at updated-at category]} link]
  (let [^Syndcontent description (doto (SyndContentImpl.)
                                   (.setValue (str "<![CDATA[\n" (markdown content) "\n]]>"))
                                   (.setType "text/html"))
        ^SyndEntry entry (doto (SyndEntryImpl.)
                           (.setTitle title)
                           (.setLink (str link (get-entry-view-url id title)))
                           (.setPublishedDate created-at)
                           (.setUpdatedDate updated-at)
                           (.setDescription description))]
    entry))

(defn create-feed [{:keys [title description author link feed-type]} entries]
  (let [^SyndFeed feed (doto (SyndFeedImpl.)
                         (.setTitle title)
                         (.setDescription description)
                         (.setPublishedDate (java.util.Date.))
                         (.setAuthor author)
                         (.setLink link)
                         (.setFeedType feed-type)
                         (.setEntries (map #(create-entry % link) entries)))
        ^CustomizedWireFeedOutput output (CustomizedWireFeedOutput.)]
    (try      
      (.outputString output (.createWireFeed feed))
      (catch FeedException e
        (let [msg "feed output failed"]
          (log/error e msg)
          (create-feed {:title msg :description msg :author "system" :link "/" :feed-type "rss_2.0"} [])
          )))))

