{:theme "default",
 :title "CategoLJ",
 :port 8944,
 :charset "UTF-8",
 :static-dir ["/css/" "/images/" "/js/"],
 :category-separator "::"
 ;; MockDataAccess
 ;; :daccess {:ns am.ik.categolj.daccess.mock.daccess,
 ;;           :params {}},
 
 ;; MirageDataAccess
 :daccess {:ns am.ik.categolj.daccess.mirage.daccess,
           :params {
                    ;; MySQL
                    ;; :db {:classname "com.mysql.jdbc.Driver"
                    ;;      :subprotocol "mysql"
                    ;;      :subname "//localhost/categolj?zeroDateTimeBehavior=convertToNull&characterEncoding=UTF-8&characterSetResults=UTF-8"
                    ;;      :user "root"
                    ;;      :password ""}
                    ;; HSQLDB
                    :db {:classname "org.hsqldb.jdbcDriver"
                         :subprotocol "hsqldb"
                         :subname "mem:categolj"
                         :user "sa"
                         :password ""}
                    }},
 ;; Mongodb
 ;; :daccess {:ns am.ik.categolj.daccess.mongodb.daccess
 ;;           :params {
 ;;                    :db "categolj"
 ;;                    :host "localhost"
 ;;                    :port 27017}
           }
 
 ;; uploader
 :uploader {:ns am.ik.categolj.uploader.simple-uploader,
            :params {:upload-dir "/upload/",
                     :file-id-digits 5}},
 :count-per-page 3,
 :count-of-recently 5,
 
 ;; feed info
 :description "Blog powered by CategoLJ",
 :author "you",
 :link "http://example.com",
 :feed-type "rss_2.0",
}