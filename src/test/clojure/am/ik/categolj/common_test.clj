(ns am.ik.categolj.common-test
  (:use [am.ik.categolj.common] :reload)
  (:use [clojure.test]))

(deftest test-get-resource-01
  (is (instance? java.net.URL (get-resource "config.clj"))))

(deftest test-get-entry-view-url-01
  (is (= "/entry/view/id/100/" (get-entry-view-url 100 nil))))

(deftest test-get-entry-view-url-02
  (is (= "/entry/view/id/100/title/foo/" (get-entry-view-url 100 "foo"))))

(deftest test-get-entry-delete-url-url01
  (is (= "/entry/delete/id/100/" (get-entry-delete-url 100))))

(deftest test-get-category-url-01
  (is (= (list ["" "/category//"])
         (get-category-url [""]))))

(deftest test-get-category-url-02
  (is (= (list ["hoge" "/category/hoge/"])
         (get-category-url ["hoge"]))))

(deftest test-get-category-url-03
  (is (= (list ["hoge" "/category/hoge/"]
               ["foo" "/category/hoge/foo/"])
         (get-category-url ["hoge" "foo"]))))

(deftest test-get-category-url-04
  (is (= (list ["hoge" "/category/hoge/"]
               ["foo" "/category/hoge/foo/"]
               ["bar" "/category/hoge/foo/bar/"])
         (get-category-url ["hoge" "foo" "bar"]))))

(deftest test-get-category-url-05
  (is (empty? (get-category-url []))))

(run-tests)