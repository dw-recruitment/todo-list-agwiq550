(ns anon-todo-2.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(defroutes app-routes
  (GET "/" [] "<img src=\"http://www.zlok.net/blog/wp-content/uploads/under-construction-13.gif\" alt=\"A man hitting the words 'under construction' with a hammer\"></img>")
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))
