(ns anon-todo.handler-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [anon-todo.handler :refer :all]))

(deftest test-app
  (testing "main route"
    (let [response (app (mock/request :get "/"))]
      (is (= (:status response) 200)))))
      ;;(is (= (:body response) "Hello World"))))

(testing "about route"
  (let [response (app (mock/request :get "/about"))]
    (is (= (:status response) 200)))
  
  (testing "not-found route"
    (let [response (app (mock/request :get "/invalid"))]
      (is (= (:status response) 404)))))
