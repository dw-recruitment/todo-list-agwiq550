(ns anon-todo.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [ring.util.response :refer [redirect]]
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

(defn get-todos
  []
  (sql/query sql-address ["SELECT * FROM todos"]))

(defroutes app-routes
  (GET "/" []
       (hiccup/html
        [:head]
        [:body
         [:ul
          (map (fn [todo] (if (:done todo)
                            [:li [:del (:description todo)]]
                            [:li (:description todo)])) (get-todos))]
         [:form {:action "/add-todo" :method "POST"}
          (anti-forgery-field)
          [:input {:type "Text" :name "description" :placeholder "Todo: "}]
          [:input {:type "submit" :value "Add todo to list"}]]]))

  (GET "/about" []
       "<p>This project is an anonymous todo list.  For democracy works!</p>")

  (POST "/add-todo" [_ & rest]
        (println _ rest)
        (sql/insert! sql-address :todos {:description (:description rest) :done false})
        (redirect "/"))
  (route/not-found "Not Found"))


(def app
  (wrap-defaults app-routes site-defaults))
