(ns am.ik.categolj.daccess.mirage.daccess-test
  (:use [clojure.test])
  (:use [am.ik.categolj.daccess.daccess] :reload)
  (:use [am.ik.categolj.daccess.mirage.daccess] :reload)
  (:import [am.ik.categolj.daccess.mirage.daccess.MirageDataAccess])
  (:import [am.ik.categolj.daccess.entities Entry Category User])
  (:import [am.ik.categolj.daccess.mirage.entities EntryEntity])
  (:import [jp.sf.amateras.mirage SqlManager])
  (:import [jp.sf.amateras.mirage.session Session JDBCSessionImpl])
  (:import [jp.sf.amateras.mirage.exception SQLRuntimeException]))

(def *test-param* {:db {:classname "org.hsqldb.jdbcDriver"
                        :subprotocol "hsqldb"
                        :subname "mem:categolj"
                        :user "sa"
                        :password ""}
                   :ddl "CREATE TABLE ENTITY 
                          (ID BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 1) PRIMARY KEY, 
                           TITLE VARCHAR(256), 
                           CONTENT VARCHAR(256), 
                           CREATED_AT TIMESTAMP, 
                           UPDATED_AT TIMESTAMP)"})

(deftest test-create-daccess01
  (is (instance? am.ik.categolj.daccess.daccess.DataAccess (create-daccess *test-param*))))

(deftest test-get-total-count-01
  (let [dac (create-daccess *test-param*)]
    (is (= 7 (get-total-count dac)))))

(deftest test-get-entry-by-id-01
  (let [dac (create-daccess *test-param*)
        entry (get-entry-by-id dac 3)]
    (is (not (nil? entry)))
    (is (= 3 (:id entry)))
    (is (= "Title3" (:title entry)))
    (is (not (empty? (:content entry))))
    (is (instance? java.util.Date (:created-at entry)))
    (is (instance? java.util.Date (:updated-at entry)))
    ))

(deftest test-get-entries-by-page-01
  (let [dac (create-daccess *test-param*)
        entries (get-entries-by-page dac 1 3)]
    (is (= 3 (count entries)))
    (is (instance? am.ik.categolj.daccess.entities.Entry (nth entries 0)))
    (is (instance? am.ik.categolj.daccess.entities.Entry (nth entries 1)))
    (is (instance? am.ik.categolj.daccess.entities.Entry (nth entries 2)))
    ))

(run-tests)
