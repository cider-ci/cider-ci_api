(ns cider-ci.api.resources
  (:require 
    [cider-ci.api.resources.execution :as execution]
    [cider-ci.api.resources.executions :as executions]
    [cider-ci.api.resources.root :as resources.root]
    [cider-ci.api.resources.shared :as shared]
    [cider-ci.api.resources.task :as task]
    [cider-ci.api.resources.tasks :as tasks]
    [cider-ci.api.resources.trial :as trial]
    [cider-ci.api.resources.trial-attachments :as trial-attachments]
    [cider-ci.api.resources.trials :as trials]
    [cider-ci.utils.debug :as debug]
    [cider-ci.utils.exception :as exception]
    [cider-ci.utils.http :as http]
    [cider-ci.utils.with :as with]
    [clj-logging-config.log4j :as logging-config]
    [clojure.data.json :as json]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [compojure.core :as cpj]
    [compojure.handler :as cpj.handler]
    [compojure.route :as cpj.route]
    [ring.adapter.jetty :as jetty]
    [ring.middleware.cookies :as cookies]
    [ring.middleware.json]
    [ring.util.response :as response]
    ))


(defonce conf (atom nil))
(def context (atom nil))


(defn prefix-links [data]
  (clojure.walk/prewalk
    (fn [el]
      (condp = (type el)
        clojure.lang.MapEntry (if (and (= :href (key el))
                                       (not (re-matches #"^http.*" (val el))))
                                ; TODO fix hard coded context
                                [:href (str "/cider-ci/api_v1" (val el))]
                                el)
        el))
    data))

(defn sort-map [m]
  (into {} (sort m)))

(defn sort-map-recursive [m]
  (->> m 
       (clojure.walk/prewalk
         (fn [el]
           (if (map? el)
             (sort-map el)
             el)
           ))))

(defn write-json [data]
  (json/write-str data ))

(defn hal-json-reponse-builder [response]
  (-> response
      (response/header "Content-Type" "application/hal+json")
      (response/charset "UTF8")
      (conj {:body (->  (:hal_json_data response) sort-map-recursive write-json) })
      ))


(defn sanitize-request-params [request]
  (assoc request
         :query-params (-> request :query-params http/sanitize-query-params)
         ))

(defn build-routes-handler []
  (let [routes-handler (cpj/routes 
                         (cpj/GET "/" request (resources.root/get request))

                         (cpj/ANY "/executions*" [] executions/routes)
                         (cpj/ANY "/execution/:id*" [] execution/routes)

                         (cpj/ANY "/execution/:id/tasks*" [] tasks/routes)
                         (cpj/ANY "/task*" [] task/routes)

                         (cpj/ANY "/task/:id/trials" [] trials/routes)
                         (cpj/ANY "/trial/:id" [] trial/routes)

                         (cpj/ANY "/trial/:id/attachments*" [] trial-attachments/routes)
                         (cpj/ANY "/trial-attachment*" [] trial-attachments/routes)

                         (cpj/ANY "*" request {:status 404})
                         )]
    (fn [request]
      (hal-json-reponse-builder 
        (-> request sanitize-request-params routes-handler)))))

;### init #####################################################################

(defn initialize [new-conf]
  (reset! conf new-conf)
  (executions/initialize new-conf)
  (execution/initialize new-conf)
  (shared/initialize new-conf)
  (tasks/initialize new-conf)
  (task/initialize new-conf)
  (trials/initialize new-conf)
  (trial/initialize new-conf)
  (trial-attachments/initialize new-conf))


;### Debug ####################################################################
;(debug/debug-ns *ns*)
;(debug/debug-ns 'clojure.java.jdbc)
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)


