(ns am.ik.categolj.core-test
  (:use [am.ik.categolj.core] :reload)
  (:use [clojure.test]))

(deftest test-get-category-anchor-01
  (is (= (str "<span class='category'><a href='/category/hoge/'>hoge</a></span>::"
              "<span class='category'><a href='/category/hoge/foo/'>foo</a></span>::"
              "<span class='category'><a href='/category/hoge/foo/bar/'>bar</a></span>")
          (get-category-anchor ["hoge" "foo" "bar"]))))


(run-tests)