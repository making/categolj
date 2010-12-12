(defproject categolj "1.0.0-alpha1"
  :description "FIXME: write"
  :repositories {"scala-tools" "http://scala-tools.org/repo-releases",
                 "amateras" "http://amateras.sourceforge.jp/mvn/"}
  :dependencies [[org.clojure/clojure "1.3.0-alpha3"]
                 [org.clojure.contrib/def "1.3.0-alpha3"]
                 [org.clojure.contrib/singleton "1.3.0-alpha3"]
                 [org.clojure.contrib/logging "1.3.0-alpha3"]
                 [org.slf4j/slf4j-api "1.6.1"]
                 [org.slf4j/jul-to-slf4j "1.6.1"]
                 [ch.qos.logback/logback-core "0.9.24"]
                 [ch.qos.logback/logback-classic "0.9.24"]
                 [commons-lang/commons-lang "2.5"]
                 [ring "0.3.5" :exclusions [org.clojure/clojure-contrib]]
                 [compojure "0.5.2" :exclusions [org.clojure/clojure-contrib]]
                 [ring-json-params "0.1.2" :exclusions [org.clojure/clojure-contrib]]
                 [enlive "1.0.0-SNAPSHOT" :exclusions [org.clojure/clojure-contrib]]
                 [org.markdownj/markdownj "0.3.0-1.0.2b4"]
                 [net.java.dev.rome/rome "1.0.0"]
                 ;; for rdbms
                 [jp.sf.amateras.mirage/mirage "1.0.5"]
                 [org.hsqldb/hsqldb "2.0.0"]
                 [mysql/mysql-connector-java "5.1.13"]
                 ;; for mongodb
                 [org.mongodb/mongo-java-driver "2.3"]
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
  :main am.ik.categolj.run
  :run-aliases {:server [am.ik.categolj.run -main]})
