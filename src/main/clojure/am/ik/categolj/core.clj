(ns am.ik.categolj.core
  (:use [ring.adapter.jetty])
  (:use [ring.middleware reload stacktrace static session multipart-params])
  (:use [ring.util.response])
  (:use [ring.middleware.json-params])
  (:use [clojure.contrib.singleton])
  (:use [compojure core])
  (:use [am.ik.categolj common markdown feed] :reload-all)
  (:use [am.ik.categolj.utils string-utils date-utils logging-utils] :reload-all)
  (:use [am.ik.categolj.daccess daccess entities])
  (:use [am.ik.categolj.uploader uploader])
  (:require [net.cgrand.enlive-html :as en])
  (:require [compojure.route :as route])
  (:require [clj-json.core :as json])
  (:require [clojure.contrib.logging :as log])
  (:require [clojure.java.io :as io])
  (:require [clojure.string :as str])
  (:import [am.ik.categolj.daccess.entities Entry Category User])
  )

(install-slfj-bridge-handler)

(def ^{:dynamic true} *config* (read-string (slurp (get-resource "config.clj"))))
(def ^{:dynamic true} *theme-dir* (str "theme/" (:theme *config*)))
(def ^{:dynamic true} *content-type* (str "text/html;charset=" (:charset *config*)))
(def ^{:dynamic true} *category-separator* (:category-separator *config*))

;; load daccess
(require [(get-in *config* [:daccess :ns]) :as 'dac])
(def ^{:dynamic true} *dac* (global-singleton
            #(dac/create-daccess (get-in *config* [:daccess :params]))))
;; load uploader
(require [(get-in *config* [:uploader :ns]) :as 'ul])
(def ^{:dynamic true} *uploader* (global-singleton
                 #(ul/create-upload-manager (get-in *config* [:uploader :params]))))

(defn get-template [filename]
  (str *theme-dir* "/pages/" filename))

(defn get-category-anchor [category-seq]
  (str/join *category-separator*
            (for [[name url] (get-category-url category-seq)]
              (str "<span class='category'><a href='" url "'>" name "</a></span>"))))

;; snipetts
(en/defsnippet categolj-header
  (get-template "header.html")
  [:head]  
  [title]
  [:title] (en/content title)
  [:link#rss] (en/set-attr :title (str title " RSS Feed")))

(en/defsnippet categolj-logged-in-menu
  (get-template "logged-in-menu.html")
  [:ul#menu]
  [])

(en/defsnippet categolj-logged-out-menu
  (get-template "logged-out-menu.html")
  [:ul#menu]
  [])

(en/defsnippet categolj-sidebar
  (get-template "sidebar.html")
  [:div#sidebar]
  [user]
  [:ul#menu]  
  (en/substitute (if user
                   (categolj-logged-in-menu)
                   (categolj-logged-out-menu)))
  [:.post]
  (let [entries (get-entries-only-id-title (*dac*) (:count-of-recently *config*))]
    (en/clone-for [{:keys [id title]} entries]
                  [:.post :a]
                  (en/do-> (en/content title)
                           (en/set-attr :href (get-entry-view-url id title)))
                  )))

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
  (en/set-attr :href (get-entry-delete-url id)))

(en/defsnippet categolj-content
  (get-template "main.html")
  [:div.contents]
  [{:keys [id title content created-at updated-at category]} user]
  [:.article-title :a]  
  (en/do-> (en/content title)
           (en/set-attr :href (get-entry-view-url id title)))
  [:.article-content]
  (en/html-content (markdown content))
  [:.edit-menu :.edit]
  (if user
    (en/substitute (categolj-edit id)))
  [:.article-created-at]
  (en/content (format-date created-at))
  [:.article-updated-at]
  (en/content (format-date updated-at))
  [:.article-category]
  (en/html-content (get-category-anchor category)))

(en/defsnippet categolj-form
  (get-template "form.html")
  [:div.contents]
  [{:keys [id title content created-at updated-at category]}]
  [:a.article-title]
  (en/do-> (en/content title)
           (en/set-attr :href (get-entry-view-url id title)))
  [:input#field-title]
  (en/set-attr :value title)
  [:input#field-id]
  (en/set-attr :value id)
  [:input#field-category]
  (en/set-attr :value (str/join *category-separator* category)) ; ["a" "b" "c"] => a::b::c
  [:textarea#field-body]
  (en/content content)
  [:input#field-created-at]
  (en/set-attr :value (format-date created-at))
  [:input#field-updated-at]
  (en/set-attr :value (format-date updated-at)))

(en/defsnippet categolj-delete
  (get-template "delete.html")
  [:div.contents]
  [{:keys [id title]}]
  [:span.delete-title]
  (en/content title)
  [:input#delete-id]
  (en/set-attr :value id))

(en/defsnippet categolj-login
  (get-template "login.html")
  [:div.contents]
  [errors referer]
  [:#errors]
  (if errors (en/content errors))
  [:input#field-referer]
  (en/set-attr :value referer)
  )
;;

;; templates
(en/deftemplate categolj-layout
  (get-template "layout.html")
  [req title body contents-header current-page total-page]
  [:head] (en/substitute (categolj-header title))
  [:div#header :h1 :a] (en/do-> (en/content (:title *config*)) 
                                (en/set-attr :href "/"))
  [:div#sidebar] (en/substitute (categolj-sidebar (get-user req)))
  [:h2.contents-header]
  (if contents-header
    (en/do-> contents-header
             (en/set-attr :id "contents-header")))
  [:div.contents] body
  [:.pages :.page]
  (let [category (get-in req [:params "category"])]
    (log/debug (str "category = " category))
    ;; show paging navigation if total page is greater than 2.
    (en/clone-for [i (range 1 (if (> total-page 1) (inc total-page) 0))]
                  [:.page]                
                  (en/html-content (if (= i current-page)
                                     (str "<strong>" i "</strong>")
                                     ;; if the current page shows category, add category infomation after URL
                                     (str "<a href='/page/" i "/"
                                          (if category (str "category/" category))
                                          "'>" i "</a>")))))
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

(defn json-response [data & [status]]
  {:status (or status 200)
   ;;   :headers {"Content-Type" "application/json"} ;; doesn't work with jquery.upload
   :headers {"Content-Type" *content-type*}
   :body (json/generate-string data)})

(defn feed-response [body & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "text/xml"}
   :body body})
;;

;; view
(defn not-found [req]
  (res404 (categolj-layout
           req
           "Error" 
           (en/html-content (str "<h2>404 Not Found</h2>" "<p>" (:uri req) " is not found.</p>"))
           nil ; no header
           1 ; current page is 1.
           0 ; no paging navigation.
           )))

(defn view-top
  "if the request parameter don't contain page, use default page 1."
  [req]
  (let [page (get-in req [:params "page"])
        current-page (if page (Long/parseLong page) 1)
        entry-count (get-total-entry-count (*dac*))
        count-per-page (:count-per-page *config*)
        total-page (calc-total-page entry-count count-per-page)]
    (res200 (categolj-layout
             req
             (:title *config*) 
             (en/substitute (map #(categolj-content % (get-user req))
                                 (get-entries-by-page (*dac*) current-page count-per-page)))
             nil ; no header
             current-page
             total-page))))

(defn view-entry [req]
  (let [id (Long/parseLong (get-in req [:params "id"])),
        entry (get-entry-by-id (*dac*) id)]
    (if entry
      (res200 (categolj-layout
               req
               (str (:title entry) " - " (:title *config*))
               (en/substitute (categolj-content entry (get-user req)))
               (en/html-content (get-category-anchor (:category entry))) ; add category header 
               1
               0 ; single page
               ))
      (not-found req))))
;;


(defn view-create [req]
  (res200 (categolj-layout
           req
           ""
           (en/substitute (categolj-form (let [now (java.util.Date.)]
                                           {:created-at now, :updated-at now})))
           nil ; no header
           1
           0 ; single page
           )))

(defn do-create [req]
  (let [entry (Entry. {} {:title (get-in req [:params "title"]),
                          :content (get-in req [:params "body"]),
                          :created-at (parse-date (get-in req [:params "created-at"])),
                          :updated-at (parse-date (get-in req [:params "updated-at"])),,
                          :category (when-let [category (get-in req [:params "category"])]
                                      (str/split category (java.util.regex.Pattern/compile *category-separator*)))
                          })]
    (log/info "create entry =" entry)
    (insert-entry (*dac*) entry)
    (redirect "/")))

(defn view-edit [req]
  (let [id (Long/parseLong (get-in req [:params "id"])),
        entry (get-entry-by-id (*dac*) id)]
    (if entry
      (res200 (categolj-layout
               req
               (:title *config*) 
               (en/substitute (categolj-form entry))
               nil
               1
               0 ; single page
               ))
      (not-found req))))

(defn do-edit [req]
  (let [id (Long/parseLong (get-in req [:params "id"])),
        updated-at
        (if (get-in req [:params "update-date"])
          (java.util.Date.) ; if "update-date" is on, set the current date to "upadate-at".
          (parse-date (get-in req [:params "updated-at"])))]
    (log/debug "params=" (:params req))
    (let [entry (Entry. {} {:id id,
                            :title (get-in req [:params "title"]),
                            :content (get-in req [:params "body"]),
                            :created-at (parse-date (get-in req [:params "created-at"])),
                            :updated-at updated-at,
                            :category (when-let [category (get-in req [:params "category"])]
                                        (str/split category (java.util.regex.Pattern/compile *category-separator*)))
                            })]
      (log/info "update entry =" entry)
      (update-entry (*dac*) entry)
      (redirect (get-entry-edit-url id)))))
     

(defn view-delete [req]
  (let [id (Long/parseLong (get-in req [:params "id"])),
        entry (get-entry-by-id (*dac*) id)]
    (if entry
      (res200 (categolj-layout
               req
               (:title *config*) 
               (en/substitute (categolj-delete entry))
               nil
               1
               0 ; single page
               ))
      (not-found req))))

(defn do-delete [req]
  (let [id (Long/parseLong (get-in req [:params "id"])),
        entry (get-entry-by-id (*dac*) id)]
    (log/info "delete entry =" entry)
    (if entry
      (delete-entry (*dac*) entry))
    (redirect "/")))

(defn view-category [req]
  (let [category (split-by-slash (get-in req [:params "category"]))
        page (get-in req [:params "page"])
        current-page (if page (Long/parseLong page) 1)
        entry-count (get-categorized-entry-count (*dac*) category)
        count-per-page (:count-per-page *config*)
        total-page (calc-total-page entry-count count-per-page)]
    (res200 (categolj-layout
             req
             (:title *config*) 
             (en/substitute (map #(categolj-content % (get-user req))
                                 (get-categorized-entries-by-page (*dac*)
                                                                  category current-page count-per-page)))
             (en/html-content (get-category-anchor category)) ; add category header 
             current-page
             total-page))))

(defn view-login
  ([req]
     (view-login req nil))
  ([req errors]
     (let [user (get-user req)]
       (if user
         (redirect "/")
         (res200 (categolj-layout
                  req
                  "Login" 
                  (en/substitute (categolj-login errors (get-in req [:headers "referer"])))
                  nil 1 0))))))

(defn do-login [req]
  (let [name (get-in req [:params "name"])
        password (get-in req [:params "password"])
        user (auth-user (*dac*) {:name name, :password password})
        referer (get-in req [:params "referer"])
        res (redirect (if (and referer (not (= referer "/login"))) referer "/"))]
    (if user 
      (assoc-in res [:session :user] user)
      (view-login req "Login is failed."))))

(defn do-logout [req]
  (let [res (redirect "/")]
    (assoc-in res [:session :user] nil)))

(defn json-do-upload [req]
  (let [file (get-in req [:params "file"])
        res (upload (*uploader*) file)]
    (json-response res)))

(defn json-do-delete-upload-file [req]
  (let [id (Long/parseLong (get-in req [:params "id"]))
        res (delete-uploaded-file-by-id (*uploader*) id)]
    (log/debug res)
    (json-response res)))

(defn json-view-uploaded-files [req]
  (let [page (Long/parseLong (get-in req [:params "page"]))
        count (Long/parseLong (get-in req [:params "count"]))
        res (get-uploaded-files-by-page (*uploader*) page count)]
    (json-response res)))

(defn publish-feed [req]
  (let [entries (get-entries-by-page (*dac*) 1 (:count-of-recently *config*))]
    (feed-response (create-feed *config* entries))))

(defn check-auth [f req]
  (let [user (get-user req)]
    (if user
      (f req)
      (redirect "/login"))))

(defn check-auth-json [f req]
  (let [user (get-user req)]
    (if user
      (f req)
      (json-response {:res "not-authorized"}))))
  
;; rooting
(defroutes categolj
  (GET ["/entry/view/id/:id*", :id #"[0-9]+"] req (view-entry req))
  (GET ["/page/:page/category/:category", :page #"[0-9]+", :category #"(.+\/)+"] req (view-category req))
  (GET ["/page/:page*", :page #"[0-9]+"] req (view-top req))
  (GET ["/category/:category", :category #"(.+\/)+"] req (view-category req))
  (GET ["/entry/create*"] req (check-auth view-create req))
  (POST ["/entry/create*"] req (check-auth do-create req))
  (GET ["/entry/edit/id/:id*", :id #"[0-9]+"] req (check-auth view-edit req))
  (POST ["/entry/edit/id/:id*", :id #"[0-9]+"] req (check-auth do-edit req))
  (GET ["/entry/delete/id/:id*", :id #"[0-9]+"] req (check-auth view-delete req))
  (POST ["/entry/delete/id/:id*", :id #"[0-9]+"] req (check-auth do-delete req))
  (GET ["/login"] req (view-login req))
  (POST ["/login"] req (do-login req))
  (GET ["/logout"] req (do-logout req))
  (POST ["/upload/delete/:id*", :id #"[0-9]+"] req
        (check-auth-json json-do-delete-upload-file req))
  (GET ["/upload/view/:page/:count*", :page #"[0-9]+", :count #"[0-9]+"] req
       (check-auth-json json-view-uploaded-files req))
  (wrap-multipart-params
   (POST ["/upload"] req
         (check-auth-json json-do-upload req)))
  (GET "/feed" req (publish-feed req))
  (GET "/rss" req (publish-feed req)) ;; for compatiblity with CategoL
  (GET "/favicon.ico*" req req)
  (GET "/" req (view-top req))
  (ANY "*" req (not-found req))
  )
;;

;; wrapper
(defn uri-matches [^String uri targets]
  (if uri
    (some #(.startsWith uri %) targets)))

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
     (let [upload-dir (get-in *config* [:uploader :params :upload-dir])
           excludes (into (:static-dir *config*) [upload-dir "/favicon.ico"])]
       (-> #'categolj
           (wrap-session)
           (trace-request excludes)
           (wrap-static (.getPath (get-resource (str *theme-dir* "/public/"))) (:static-dir *config*))
           (wrap-static (.getPath (java.io.File. ".")) [upload-dir])
           ;;(wrap-reload '(am.ik.categolj.core am.ik.categolj.common)) ;; hot reloading
           (trace-response excludes)
           (wrap-stacktrace)
           )))
;;

(defn boot [& [port]]
  (log/info "start server")
  (run-jetty app {:port (:port *config*)}))
