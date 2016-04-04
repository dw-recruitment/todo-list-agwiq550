(ns anon-todo.model
  (:require [clojure.java.jdbc :as sql]))

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

