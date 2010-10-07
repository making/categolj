(ns am.ik.categolj.core
  (:use [ring.adapter.jetty])
  (:use [ring.middleware reload stacktrace static])
  (:require [net.cgrand.enlive-html :as html])
  (:require [clojure.java.io :as io]))

(defn ^java.net.URL get-resource [f]
  (.getResource (.getContextClassLoader (Thread/currentThread)) f))

(def *config* (read-string (slurp (get-resource "config.clj"))))

(def *theme-dir* (str "theme/" (:theme *config*)))

(html/deftemplate categolj-layout
  (str *theme-dir* "/pages/index.html")
  [title]
  [:title] (html/content title)
  [:h1/a] (html/do-> (html/content title) (html/set-attr :href "http://github.com/making/categolj"))
  [:p#feature] (html/content "CategoLJ is a Simple Blog System written by Clojure featuring categories of the articles."))

(defn hello [req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (categolj-layout (:title *config*))})

(defn charset-filter [app]
  (fn [req]
    (let [orig (app req)]
      (assoc orig :charset (:charset *config*)))))

(def app
     (-> #'hello
         (charset-filter)
         (wrap-stacktrace)
         (wrap-static (.getPath (get-resource (str *theme-dir* "/public/"))) (:static-dir *config*))
         (wrap-reload '(am.ik.categolj.core))
         ))

(defn boot [& [port]]
  (run-jetty app {:port (:port *config*)}))
