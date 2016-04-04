(ns anon-todo.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [clojure.string :as str]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.util.response :refer [redirect]]
            [anon-todo.model :as model]
            [anon-todo.view :as view]
            [hiccup.core :as hiccup]
            [hiccup.page :as hp]))

(model/db-migrate)


(defroutes app-routes
  (GET "/" []
       (view/main-page (map (fn [list-hash] (assoc-in list-hash [:todos] (model/get-todos (:id list-hash)))) (model/get-listoflists))))
  (GET "/about" []
       (view/about-page))
  (POST "/add-todo" [_ & rest]
        (when-not (str/blank? (:description rest))
          (model/post-todo rest))
        (redirect "/"))
  (POST "/add-list" req
        ;; (println (:params req))
        (model/post-new-list (:title (:params req)))
        (redirect "/"))
  (POST "/update-done/:id" [id & rest]
        (model/toggle-todo (assoc-in rest [:id] id))
        (redirect "/")) ;; patch might not work
  (POST "/delete-todo/:id" [id & rest]
        (model/delete-todo id)
        (redirect "/"))
  (GET "/delete-list/:id" [id & rest]
       (model/delete-list id)
       (redirect "/"))
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))
