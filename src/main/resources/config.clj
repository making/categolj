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
           :params {:db {:classname "org.hsqldb.jdbcDriver"
                         :subprotocol "hsqldb"
                         :subname "mem:categolj"
                         :user "sa"
                         :password ""}
                    :ddl "create-table.sql"
                    }},
 :count-per-page 3,
 :count-of-recently 5}