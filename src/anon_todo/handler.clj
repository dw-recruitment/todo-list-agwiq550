(ns anon-todo.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(defroutes app-routes
  (GET "/" [] "<img src=\"http://www.zlok.net/blog/wp-content/uploads/under-construction-13.gif\" alt=\"A man hitting the words 'under construction' with a hammer\"></img>")
  (GET "/about" [] "<p>This project is an anonymous todo list.  For democracy works!</p>")
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))
