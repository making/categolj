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
  (:require [clojure.string :as str])
  (:import [com.petebevin.markdown MarkdownProcessor]))

(defn ^java.net.URL get-resource [filename]
  (.getResource (.getContextClassLoader (Thread/currentThread)) filename))

(def *config* (read-string (slurp (get-resource "config.clj"))))
(def *theme-dir* (str "theme/" (:theme *config*)))
(def *content-type* (str "text/html;charset=" (:charset *config*)))

;; load daccess
(require [(get-in *config* [:daccess :ns]) :as 'dac])
(def *dac* (global-singleton
            #(dac/create-daccess (get-in *config* [:daccess :params]))))

(defn get-template [filename]
  (str *theme-dir* "/pages/" filename))

(defn get-entry-view-url [id title]
  (str "/entry/view/id/" id "/" (if title (str "title/" title "/"))))

(defn get-entry-edit-url [id]
  (str "/entry/edit/id/" id "/"))

(defn get-entry-delete-url [id]
  (str "/entry/delete/id/" id "/"))

(defn get-category-url [category-seq]
  (map (fn [x i] [x (str "/category/" (str/join "/" (take (inc i) category-seq)))])
       category-seq (range (count category-seq))))

(defn get-category-anchor [category-seq]
  (str/join "::" (for [[name url] (get-category-url category-seq)] (str "<span class='category'><a href='" url "'>" name "</a></span>"))))

(defn format-data [^java.util.Date date]
  (.format (java.text.SimpleDateFormat. "yyyy/MM/dd HH:mm:ss") date))

(def *markdown* (per-thread-singleton #(MarkdownProcessor.)))

(defn logedin? "stub" []
  false)

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
  (en/clone-for [{:keys [id title]} (get-entries-by-page (*dac*) 1 (:count-of-recently *config*))]
                [:.post :a]
                (en/do-> (en/content title)
                         (en/set-attr :href (get-entry-view-url id title)))
                ))

(en/defsnippet categolj-footer
  (get-template "footer.html")
  [:div#footer]
  [])

(en/defsnippet categolj-edit
  (get-template "edit.html")
  [:.edit]
  [id]
  [:.edit-link]
  (en/set-attr :href (get-entry-edit-url id))
  [:.delete-link]
  (en/set-attr :href (str "/entry/delete/id/" id "/")))

(en/defsnippet categolj-content
  (get-template "main.html")
  [:div.contents]
  [{:keys [id title content created-at updated-at category]}]
  [:.article-title :a]  
  (en/do-> (en/content title)
           (en/set-attr :href (get-entry-view-url id title)))
  [:.article-content]
  (en/html-content (.markdown ^MarkdownProcessor (*markdown*) content))
  [:.edit-menu :.edit]
  (if (logedin?)
    (en/substitute (categolj-edit id)))
  [:.article-created-at]
  (en/content (format-data created-at))
  [:.article-updated-at]
  (en/content (format-data updated-at))
  [:.article-category]
  (en/html-content (get-category-anchor category)))
;;

;; templates
(en/deftemplate categolj-layout
  (get-template "layout.html")
  [title body contents-header current-page total-page]
  [:head] (en/substitute (categolj-header title))
  [:div#header :h1 :a] (en/do-> (en/content (:title *config*)) 
                                (en/set-attr :href "/"))
  [:div#sidebar] (en/substitute (categolj-sidebar))
  [:h2.contents-header]
  (if contents-header
    (en/do-> contents-header
             (en/set-attr :id "contents-header")))
  [:div.contents] body
  [:.pages :.page]
  ;; show paging navigation if total page is greater than 2.
  (en/clone-for [i (range 1 (if (pos? total-page) (+ total-page 2) 0))]
                [:.page]                
                (en/html-content (if (= i current-page)
                                   (str "<strong>" i "</strong>")
                                   (str "<a href='/page/" i "/'>" i "</a>"))))
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

(defn not-found [req]
  (res404 (categolj-layout "Error" 
                           (en/html-content (str "<h2>404 Not Found</h2>" "<p>" (:uri req) " is not found.</p>"))
                           nil
                           1 ; current page is 1.
                           0 ; no paging navigation.
                           )))

(defn hello
  "if the request parameter don't contain page, use default page 1."
  [req]
  (let [page (get-in req [:params "page"])
        current-page (if page (Integer/parseInt page) 1)
        total-page (int (/ (or (get-total-count (*dac*)) 0) (:count-per-page *config*)))]  
    (res200 (categolj-layout (:title *config*) 
                             (en/substitute (map categolj-content
                                                 (get-entries-by-page (*dac*) current-page (:count-per-page *config*))))
                             nil
                             current-page
                             total-page))))

(defn view [req]
  (let [id (Integer/parseInt (get-in req [:params "id"])),
        entry (get-entry-by-id (*dac*) id)]
    (log/debug id)
    (if entry
      (res200 (categolj-layout (:title *config*) 
                               (en/substitute (categolj-content entry))
                               (en/html-content (get-category-anchor (:category entry))) ; add category header 
                               1
                               0 ; single page
                               ))
      (not-found req))))
;;


;; rooting
(defroutes categolj
  (GET ["/entry/view/id/:id*", :id #"[0-9]+"] req (view req))
  (GET ["/page/:page*", :page #"[0-9]+"] req (hello req))
  (GET "/" req (hello req))
  (ANY "*" req (not-found req))
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
