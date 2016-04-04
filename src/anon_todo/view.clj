(ns anon-todo.view
  (:require [hiccup.core :as hiccup]
            [hiccup.page :as hp]
            [ring.util.anti-forgery :refer [anti-forgery-field]]))

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

(defn display-todo-list
  [list-info]
  (hiccup/html
    [:div.col-md-4
      [:div.panel.panel-default
        [:div.panel-heading (str (:title list-info))
          [:a.btn.btn-danger.btn-sm.pull-right {:href (str "/delete-list/" (:id list-info)) :style (str "visibility:" (if (empty? (:todos list-info))
                                                                                                                        "visible"
                                                                                                                        "hidden"))}
            [:span.glyphicon.glyphicon-minus]]
          [:div.panel-body
            [:div.row]
            [:ul.list-group
              (map todo-cluster (:todos list-info))]
            [:form {:action "/add-todo" :method "POST"}
              (anti-forgery-field)
              [:div.input-group]
              [:input {:type "hidden" :name "list" :value (:id list-info)}]
              [:input.form-control {:type "Text" :name "description" :placeholder "Todo: "}]
              [:span.input-group-btn]
              [:input.btn.btn-success {:type "submit" :value "Add todo to list"}]]]]]]))

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
  [data-model]
  (hiccup/html
    [:div
      (layout-common "The Todo Lists" (map display-todo-list data-model))
      (list-add-form)]))

(defn about-page
  []
  (layout-common "About This Todo List Project"
    (hiccup/html
      [:h1 "This project is an anonymous todo list.  For democracy works!"])))




