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
    [:link {:rel "stylesheet" :href "https://maxcdn.bootstrapcdn.com/bootswatch/3.3.6/flatly/bootstrap.min.css"}]
    [:script {:src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js"}]]
   [:body
    [:nav {:class "navbar navbar-default"}
      [:div {:class "container-fluid"}
        [:div {:class "navbar-header"}
          [:button {:aria-expanded "false", :data-target "#bs-example-navbar-collapse-1", :data-toggle "collapse", :class "navbar-toggle collapsed", :type "button"}]
          [:span {:class "sr-only"} "Toggle navigation"]
          [:span {:class "icon-bar"}]
          [:span {:class "icon-bar"}]
          [:span {:class "icon-bar"}]
          [:a {:href "/", :class "navbar-brand"} "Todo Blisst"]]
        [:div {:id "bs-example-navbar-collapse-1", :class "collapse navbar-collapse"}
          [:ul {:class "nav navbar-nav"}
            [:li
              [:a {:href "/"} "Home"]]
            [:li.dividier {:role "separator"}]
            [:li
              [:a {:href "/about"} "About"]]]]]]
    body]))

(defn btn-changer
  [doneness]
  (if doneness
    {:class "btn btn-sm btn-primary" :value "Undo" :type "submit"}
    {:class "btn btn-sm btn-success" :value "Complete" :type "submit"}))

(defn todo-cluster
  [todo]
  (hiccup/html
    [:li.list-group-item
      [:div.well.well-sm (if (:done todo)
                            [:del (:description todo)]
                            (:description todo))]
      [:div.input-group
          [:div.input-group-btn
            [:form {:action (str "/update-done/" (:id todo)) :method "POST"}
              (anti-forgery-field)
              [:input (btn-changer (:done todo))]
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
    [:div.col-md-4
      [:form {:action (str "/add-list") :method "POST"}
        (anti-forgery-field)
        [:div.input-group
          [:input.form-control {:type "Text" :name "title" :placeholder "New List Title"}]
          [:span.input-group-btn
            [:input.btn.btn-success {:type "submit" :value "Add new list"}]]]]]))

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
      [:div.container
       [:div.row
        [:h1.jumbotron "This project is an anonymous todo list.  For DemocracyWorks!"]]])))




