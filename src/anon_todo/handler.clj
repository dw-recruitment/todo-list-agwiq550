(ns anon-todo.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [ring.util.response :refer [redirect]]
            [clojure.java.jdbc :as sql]
            [hiccup.core :as hiccup]
            [hiccup.page :as hp]))

;; -- MODEL

(def sql-address "postgresql://localhost:5432/anon-todo")

(defn db-migrate
  "Set up the db"
  []
  (if (empty? (sql/query sql-address ["SELECT * FROM pg_class WHERE relname='listoflists'"]))
    (sql/db-do-commands sql-address
                        (sql/create-table-ddl :listoflists
                                              [:id :serial "primary key"]
                                              [:title :text])))
  (if (empty? (sql/query sql-address ["SELECT * FROM listoflists WHERE title = 'Initial Todo List'"]))
    (sql/insert! sql-address :listoflists
                 {:title "Initial Todo List"}))
  (if (empty? (sql/query sql-address ["SELECT * FROM pg_class WHERE relname='todos'"]))
    (sql/db-do-commands sql-address
                        (sql/create-table-ddl :todos
                                              [:id :serial "primary key"]
                                              [:description :text]
                                              [:done :bool]
                                              [:name :text]
                                              [:list :int "references listoflists"])))
  (if (empty? (sql/query sql-address ["SELECT * FROM todos WHERE name = 'anon'"]))
    (sql/insert! sql-address :todos
                 {:description "Find the flow" :done false :name "anon" :list 1}
                 {:description "Nurture and nourish the flow" :done true :name "anon" :list 1})))

(db-migrate)

(defn get-todos
  [listid]
  (sql/query sql-address ["SELECT * FROM todos where list = ? order by id asc" listid]))

(defn get-listoflists
  []
  (sql/query sql-address ["SELECT * FROM listoflists"]))

(defn post-todo
  [todo]
  (sql/insert! sql-address :todos {:description (:description todo) :done false :list (Integer/parseInt (:list todo))}))

(defn post-new-list
  [title]
  (sql/insert! sql-address :listoflists {:title title}))

(defn update-todo
  [col new-val id]
  (sql/update! sql-address :todos {col new-val} [ "id = ?" (Integer/parseInt id)]))

(defn delete-todo
  [id]
  (sql/delete! sql-address :todos [ "id = ?" (Integer/parseInt id)]))

(defn delete-list
  [id]
  (sql/delete! sql-address :listoflists [ "id = ?" (Integer/parseInt id)]))

(defn toggle-todo
  [todo]
  (update-todo :done (not (read-string (:done todo))) (:id todo)))

;; -- VIEW

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
  [:h2 (if (:done todo)
         [:del (:description todo)]
         (:description todo))])

(defn todo-cluster
  [todo]
  (hiccup/html
   [:li.list-group-item
    (todo-desc todo)
    [:div.input-group
     [:div.input-group-btn
      [:form {:action (str "/update-done/" (:id todo)) :method "POST"}
       (anti-forgery-field)
       [:input.btn.btn-info.btn-sm {:type "submit" :value (if (:done todo)
                                                            "Undo"
                                                            "Complete")}]
       [:input {:type "hidden" :name "done" :value (str (:done todo))}]
       [:input {:type "hidden" :name "description" :value (str (:description todo))}]
       [:input {:type "hidden" :name "list" :value (str (:list todo))}]
       [:input {:type "hidden" :name "name" :value (str (:name todo))}]]
      [:form {:action (str "/delete-todo/" (:id todo)) :method "POST"}
       (anti-forgery-field)
       [:input.btn.btn-danger.btn-sm {:type "submit" :value "Delete this Todo"}]]]]]))

;; almost got all the view out of the routes.  That's next.

;; get-todos is called twice in the todo-list view function
;; so there should be a function in the controller that calls get-todos once
;; and passes the result into todo-list
(defn todo-list
  [list-info]
  (let [todos (get-todos (:id list-info))]
    (hiccup/html
     [:div.col-md-4
      [:div.panel.panel-default
        [:div.panel-heading (str (:title list-info))
          [:a.btn.btn-danger.btn-sm.pull-right {:href (str "/delete-list/" (:id list-info)) :style (str "visibility:" (if (empty? todos)
                                                                                                                        "visible"
                                                                                                                        "hidden"))}
            [:span.glyphicon.glyphicon-minus]]]
          [:div.panel-body
            [:div.row]
            [:ul.list-group
              (map todo-cluster todos)]
            [:form {:action "/add-todo" :method "POST"}
              (anti-forgery-field)
              [:div.input-group]
              [:input {:type "hidden" :name "list" :value (:id list-info)}]
              [:input.form-control {:type "Text" :name "description" :placeholder "Todo: "}]
              [:span.input-group-btn
              [:input.btn.btn-success {:type "submit" :value "Add todo to list"}]]]]]])))

(defn list-add-form
  []
  (hiccup/html
   [:form {:action (str "/add-list") :method "POST"}
    (anti-forgery-field)
    [:div.input-group
     [:input.form-control {:type "Text" :name "title" :placeholder "New List Title"}]
     [:span.input-group-btn
      [:input.btn.btn-success {:type "submit" :value "Add new list"}]]]]))

(defn main-page
  []
  [:div (map todo-list (get-listoflists))
   (list-add-form)])

;; -- CONTROLLER

(defroutes app-routes
  (GET "/" []
       (layout-common "Todo Lists: Anonymous" (main-page)))
  (GET "/about" []
       "<p>This project is an anonymous todo list.  For democracy works!</p>")
  (POST "/add-todo" [_ & rest]
        (if (empty? (:description rest))
          (post-todo (assoc-in rest [:description] "Fill in the todo field before adding a todo"))
          (post-todo rest))
        (redirect "/"))
  (POST "/add-list" req
        ;; (println (:params req))
        (post-new-list (:title (:params req)))
        (redirect "/"))
  (POST "/update-done/:id" [id & rest]
        (toggle-todo (assoc-in rest [:id] id))
        (redirect "/")) ;; patch might not work
  (POST "/delete-todo/:id" [id & rest]
        (delete-todo id)
        (redirect "/"))
  (GET "/delete-list/:id" [id & rest]
       (delete-list id)
       (redirect "/"))
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))
