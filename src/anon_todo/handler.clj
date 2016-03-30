(ns anon-todo.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [clojure.java.jdbc :as sql]
            [hiccup.core :as hiccup]))

(def sql-address "postgresql://localhost:5432/anon-todo")

(defn mock-up-db
  []
  (if (not (empty? (sql/query sql-address ["SELECT * FROM pg_class WHERE relname='todos'"])))
    (sql/db-do-commands sql-address
                        (sql/drop-table-ddl :todos)))
  (sql/db-do-commands sql-address
                      (sql/create-table-ddl :todos
                                            [:id "serial primary key"]
                                            [:description :text]
                                            [:done :bool]))
  (sql/insert! sql-address
               :todos
               {:description "Find the flow" :done false}
               {:description "Nurture and nourish the flow" :done true}))

(mock-up-db)

(defroutes app-routes
  (GET "/" []
       (hiccup/html
        [:head]
        [:body
         [:ul
          (map (fn [todo] [:li (:description todo)]) (sql/query sql-address ["SELECT * FROM todos"]))]]))
  (GET "/about" []
       "<p>This project is an anonymous todo list.  For democracy works!</p>")
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))
