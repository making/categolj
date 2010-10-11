(ns am.ik.categolj.core
  (:use [ring.adapter.jetty])
  (:use [ring.middleware reload stacktrace static])
  (:use [clojure.contrib.singleton])
  (:use [compojure core])
  (:use [am.ik.categolj.utils.string-utils])
  (:use [am.ik.categolj.daccess daccess entities])
  (:require [net.cgrand.enlive-html :as en])
  (:require [compojure.route :as route])
  (:require [clojure.contrib.logging :as log])
  (:require [clojure.java.io :as io])
  (:import [com.petebevin.markdown MarkdownProcessor]))

(defn ^java.net.URL get-resource [filename]
  (.getResource (.getContextClassLoader (Thread/currentThread)) filename))

(def *config* (read-string (slurp (get-resource "config.clj"))))
(def *theme-dir* (str "theme/" (:theme *config*)))
(def *content-type* (str "text/html;charset=" (:charset *config*)))

(def *dac* (global-singleton #(.newInstance (Class/forName (:daccess *config*)))))

(defn get-template [filename]
  (str *theme-dir* "/pages/" filename))

(defn get-entry-view-url [id title]
  (str "/entry/view/id/" id "/" (if title (str "title/" title "/"))))

(defn format-data [^java.util.Date date]
  (.format (java.text.SimpleDateFormat. "yyyy/MM/dd HH:mm:ss") date))

(def *markdown* (per-thread-singleton #(MarkdownProcessor.)))

;; snipetts
(en/defsnippet categolj-header
  (get-template "header.html")
  [:head]  
  [title]
  [:title] (en/content title)
  [:link#rss] (en/set-attr :title (str title " RSS Feed")))

(en/defsnippet categolj-sidebar
  (get-template "sidebar.html")
  [:div#sidebar]
  []
  [:.post]
  (en/clone-for [{:keys [id title]} (get-entries-by-page (*dac*) 1 (:count-per-oage *config*))]
                [:.post :a]
                (en/do-> (en/content title)
                         (en/set-attr :href (get-entry-view-url id title)))
                ))

(en/defsnippet categolj-footer
  (get-template "footer.html")
  [:div#footer]
  [])

(en/defsnippet categolj-content
  (get-template "main.html")
  [:div#contents]
  [{:keys [id title content created-at updated-at category]}]
  [:.article-title :a]  
  (en/do-> (en/content title)
           (en/set-attr :href (get-entry-view-url id title)))
  [:.article-content]
  (en/html-content (.markdown ^MarkdownProcessor (*markdown*) content))
  [:.article-created-at]
  (en/content (format-data created-at))
  [:.article-updated-at]
  (en/content (format-data updated-at))
  [:.article-category]
  (en/content (apply str (interpose "::" category))))
;;

;; templates
(en/deftemplate categolj-layout
  (get-template "layout.html")
  [title body]
  [:head] (en/substitute (categolj-header title))
  [:div#header :h1 :a] (en/do-> (en/content (:title *config*)) 
                                (en/set-attr :href "/"))
  [:div#sidebar] (en/substitute (categolj-sidebar))
  [:div#contents] body
  [:div#footer] (en/substitute (categolj-footer)))
;;

;; response
(defn res200 [body]
  {:status 200
   :headers {"Content-Type" *content-type*}
   :body body})

(defn res404 [body]
  {:status 404
   :headers {"Content-Type" *content-type*}
   :body body})
;;
    
;; view
(defn hello [req]
  (res200 (categolj-layout (:title *config*) 
                           (en/substitute (map categolj-content (get-entries-by-page (*dac*) 1 (:count-per-oage *config*)))))))

(defn view [id]
  (log/debug id)
  (let [id (Integer/parseInt id)]
    (res200 (categolj-layout (:title *config*) 
                             (en/substitute (categolj-content (get-entry-by-id (*dac*) id)))))))
  
(defn not-found []
  (res404 (categolj-layout "Error" 
                           (en/html-content "<h2>404 Not Found</h2>"))))
;;


;; rooting
(defroutes categolj
  (GET ["/entry/view/id/:id*", :id #"[0-9]+"] [id] (view id))
  (GET "/" req (hello req))
  (ANY "*" [] (not-found))
  )
;;

;; wrapper
(defn uri-matches [^String uri targets]
  (some #(.startsWith uri %) targets))

(defn trace-request [app excludes]
  (fn [req]
    (if-not (uri-matches (:uri req) excludes)
      (log/trace "[request ]" req))
    (app req)))

(defn trace-response [app excludes]
  (fn [req]
    (let [res (app req)]
      (if-not (uri-matches (:uri req) excludes)
        (log/trace "[response]" (dissoc res :body)))
      res)))
;;

;; app
(def app
     (let [excludes (:static-dir *config*)]
     (-> #'categolj
         (trace-request excludes)
         (wrap-stacktrace)
         (wrap-static (.getPath (get-resource (str *theme-dir* "/public/"))) (:static-dir *config*))
         (wrap-reload '(am.ik.categolj.core)) ;; hot reloading
         (trace-response excludes)
         )))
;;
     
(defn boot [& [port]]
  (log/info "start server")
  (run-jetty app {:port (:port *config*)}))
