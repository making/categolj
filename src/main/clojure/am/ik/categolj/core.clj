(ns am.ik.categolj.core
  (:use [ring.adapter.jetty])
  (:use [ring.middleware.reload])
  (:require [net.cgrand.enlive-html :as html]))

          
(html/deftemplate categolj-layout
  "pages/index.html"
  [title]
  [:title] (html/content title)
  [:h1/a] (html/do-> (html/content title) (html/set-attr :href "http://github.com/making/categolj"))
  [:p#feature] (html/content "CategoLJ is a Simple Blog System written by Clojure featuring categories of the articles."))

(defn hello [req] 
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (categolj-layout "CategoLJ")})

(def app
     (wrap-reload #'hello '(am.ik.categolj.core)))

(defn boot [& [port]]
  (run-jetty app {:port (if port (Integer/parseInt port) 8080)}))


