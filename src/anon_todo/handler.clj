(ns anon-todo.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [ring.util.response :refer [redirect]]
            [clojure.java.jdbc :as sql]
            [hiccup.core :as hiccup]
            [hiccup.page :as hp]))

;; so the db stuff goes to the model
;; the hiccup / view stuff goes to the view
;; the routing stuff goes to the controller


(def sql-address "postgresql://localhost:5432/anon-todo")

(defn db-migrate
  "Set up the db"
  []
  (if (empty? (sql/query sql-address ["SELECT * FROM pg_class WHERE relname='todos'"]))
    (sql/db-do-commands sql-address
                        (sql/create-table-ddl :todos
                                              [:id :serial "primary key"]
                                              [:description :text]
                                              [:done :bool]
                                              [:name :text]
                                              [:list :int])))
  (if (empty? (sql/query sql-address ["SELECT * FROM todos WHERE name = 'anon'"]))
    (sql/insert! sql-address :todos
                 {:description "Find the flow" :done false :name "anon" :list 1}
                 {:description "Nurture and nourish the flow" :done true :name "anon" :list 1})))

(db-migrate)

(defn get-todos
  []
  (sql/query sql-address ["SELECT * FROM todos order by id asc"]))

(defn post-todo
  [todo]
  (sql/insert! sql-address :todos {:description (:description todo) :done false}))

(defn update-todo
  [col new-val id]
  (println new-val)
  (sql/update! sql-address :todos {col new-val} [ "id = ?" (Integer/parseInt id)]))

(defn delete-todo
  [id]
  (sql/delete! sql-address :todos [ "id = ?" (Integer/parseInt id)]))

(defn toggle-todo
  [todo]
  (update-todo :done (not (read-string (:done todo))) (:id todo)))

(defn layout-common
  [title & body]
  (hiccup/html
    [:head
      [:meta {:charset "utf-8"}]
      [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge, chrome=1"}]
      [:meta {:name "vieport" :content
              "width=device-width, initial-scale=1, maximum-scale=1"}]
      [:title title]
      [:link {:rel "stylesheet" :href "https://maxcdn.bootstrapcdn.com/bootswatch/3.3.6/sandstone/bootstrap.min.css"}]]
    [:body
     [:div.container body]]))

(defn todo-desc
  [todo]
  (if (:done todo)
    [:del (:description todo)]
    (:description todo)))

(defn todo-cluster
  [todo]
  (hiccup/html
    [:div.row
      [:li.list-group-item
        (todo-desc todo)
        [:div.input-group
          [:div.input-group-btn
            [:form {:action (str "/update-done/" (:id todo)) :method "POST"}
              (anti-forgery-field)
              [:input.btn.btn-info.btn-block {:type "submit" :value (if (:done todo)
                                                                        "Undo"
                                                                        "Complete")}]
              [:input {:type "hidden" :name "done" :value (str (:done todo))}]
              [:input {:type "hidden" :name "description" :value (str (:description todo))}]
              [:input {:type "hidden" :name "list" :value (str (:list todo))}]
              [:input {:type "hidden" :name "name" :value (str (:name todo))}]]
            [:form {:action (str "/delete-todo/" (:id todo)) :method "POST"}
              (anti-forgery-field)
              [:input.btn.btn-danger.btn-block {:type "submit" :value "Delete this Todo"}]]]]]]))

;; almost got all the view out of the routes.  That's next.

(defn todo-list
  []
  (hiccup/html
    [:div.col-md-4
     [:ul.list-group
      (map todo-cluster (get-todos))]
     [:div.input-group
      [:form.form-control {:action "/add-todo" :method "POST"}
        (anti-forgery-field)
        [:input {:type "Text" :name "description" :placeholder "Todo: "}]
       [:span.input-group-btn
        [:input.btn.btn-success {:type "submit" :value "Add todo to list"}]]]]]))


(defroutes app-routes
  (GET "/" []
       (layout-common "Todo Lists: Anonymous" (todo-list)))
  (GET "/about" []
      "<p>This project is an anonymous todo list.  For democracy works!</p>")
  (POST "/add-todo" [_ & rest]
        (post-todo rest)
        (redirect "/"))
  (POST "/update-done/:id" [id & rest]
        (toggle-todo (assoc-in rest [:id] id))
        (redirect "/")) ;; patch might not work
  (POST "/delete-todo/:id" [id & rest]
        (delete-todo id)
        (redirect "/"))
  (route/not-found "Not Found"))

;; (fn [e] (println e)
;;   (update-done todo))

(def app
  (wrap-defaults app-routes site-defaults))
