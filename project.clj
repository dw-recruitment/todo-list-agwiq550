(defproject anon-todo-2 "0.1.0-SNAPSHOT"
  :description "An anonymous todo list application"
  :url "none"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.5.0"]
                 [ring/ring-defaults "0.2.0"]
                 [org.clojure/java.jdbc "0.5.0"]
                 [org.postgresql/postgresql "9.4.1208"]
                 [hiccup "1.0.5"]]
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler anon-todo.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]}})
