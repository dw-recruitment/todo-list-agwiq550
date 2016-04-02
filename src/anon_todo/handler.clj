(ns anon-todo.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [ring.util.response :refer [redirect]]
            [clojure.java.jdbc :as sql]
            [hiccup.core :as hiccup]))

;; so the db stuff goes to the model
;; the hiccup / view stuff goes to the view
;; the routing stuff goes to the controller


(def sql-address "postgresql://localhost:5432/anon-todo")

(defn mock-up-db
  "Set up the db"
  []
  (if (empty? (sql/query sql-address ["SELECT * FROM pg_class WHERE relname='todos'"]))
    (sql/db-do-commands sql-address
                        (sql/create-table-ddl :todos
                                              [:id "serial primary key"]
                                              [:description :text]
                                              [:done :bool]
                                              [:name :text]
                                              [:list :int])))

  (if (empty? (sql/query sql-address ["SELECT * FROM todos WHERE name = 'anon'"]))
    (sql/insert! sql-address
                :todos
                {:description "Find the flow" :done false :name "anon" :list 1}
                {:description "Nurture and nourish the flow" :done true :name "anon" :list 1}))

(mock-up-db)

(defn get-todos
  []
  (sql/query sql-address ["SELECT * FROM todos"]))

(defn post-todo
  [todo]
  (sql/insert! sql-address :todos {:description (:description todo) :done false}))

(defn update-done
  [id done-state]
  (sql/update! sql-address :todos {:done (not (read-string done-state))} [ "id = ?" (Integer/parseInt id)]))

(defroutes app-routes
  (GET "/" []
       (hiccup/html
         [:head]
         [:body
          [:ul
           (map (fn [todo]
                  (hiccup/html (if (:done todo)
                                 [:li [:del (:description todo)]]
                                 [:li (:description todo)])
                               ;; [:h1 (str todo)]
                               [:form {:action (str "/update-done/" (:id todo)) :method "POST" }
                                (anti-forgery-field)
                                [:input {:type "hidden" :name "done" :value (str (:done todo))}]
                                [:input {:type "submit" :value (if (:done todo)
                                                                 "Undo"
                                                                 "Complete")}]])) (get-todos))]
          [:form {:action "/add-todo" :method "POST"}
           (anti-forgery-field)
           [:input {:type "Text" :name "description" :placeholder "Todo: "}]
           [:input {:type "submit" :value "Add todo to list"}]]]))

  (GET "/about" []
       "<p>This project is an anonymous todo list.  For democracy works!</p>")

  (POST "/add-todo" [_ & rest]
        (post-todo rest)
        (redirect "/"))

  (POST "/update-done/:id" [id & rest]
        (update-done id (:done rest))
        (redirect "/")) ;; patch might not work

  (route/not-found "Not Found"))
;; (fn [e] (println e)
;;   (update-done todo))

(def app
  (wrap-defaults app-routes site-defaults))
