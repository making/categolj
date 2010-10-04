(defproject categolj "1.0.0-SNAPSHOT"
  :description "FIXME: write"
  :dependencies [[org.clojure/clojure "1.3.0-alpha1"]
                 [org.clojure.contrib/def "1.3.0-alpha1"]
                 [org.clojure.contrib/singleton "1.3.0-alpha1"]
                 [ring "0.3.1"]
                 [enlive "1.0.0-SNAPSHOT"]
                 ]
  :dev-dependencies [[swank-clojure "1.3.0-SNAPSHOT"]
                     [lein-javac "1.2.1-SNAPSHOT"]
                     [lein-run "1.0.0-SNAPSHOT"]
                     [lein-clojars "0.5.0"]]
  
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}
  
  :source-path "src/main/clojure"
  :library-path "lib/dependency"
  :compile-path "target/classes"
  :test-path "src/test/clojure"
  :resources-path "src/main/resources"  
  :test-resources-path "src/test/resources"
  :java-source-path "src/main/java"
  :javac-fork "true"
  :jar-exclusions [#"(?:^|/).svn/"]
  :run-aliases {:server [am.ik.categolj.run -main]}
  )
