; Copyright © 2013 - 2016 Dr. Thomas Schank <Thomas.Schank@AlgoCon.ch>
; Licensed under the terms of the GNU Affero General Public License v3.
; See the "LICENSE.txt" file provided with this software.

(ns cider-ci.api.resources.repositories
  (:require
    [cider-ci.api.pagination :as pagination]
    [cider-ci.utils.http :as http]
    [cider-ci.utils.http :as utils-http]
    [cider-ci.utils.http-server :as http-server]
    [cider-ci.utils.rdbms :as rdbms]
    [clojure.data.json :as json]
    [clojure.java.jdbc :as jdbc]
    [compojure.core :as cpj]
    [compojure.handler :as cpj.handler]
    [honeysql.core :as hc]
    [honeysql.helpers :as hh]
    [ring.adapter.jetty :as jetty]
    [ring.middleware.cookies :as cookies]
    [ring.middleware.json]
    [ring.util.response :as response]

    [clojure.tools.logging :as logging]
    [clj-logging-config.log4j :as logging-config]
    [logbug.catcher :as catcher]
    [logbug.debug :as debug]
    )
  (:use
    [clojure.walk :only [keywordize-keys]]
    ))

;### get-index ##################################################################

(defn build-repositories-base-query []
  (-> (hh/select :repositories.id :repositories.created_at)
      (hh/from :repositories)
      (hh/modifiers :distinct)
      (hh/order-by [:repositories.created_at :desc])
      ))

(defn log-debug-honeymap [honeymap]
  (logging/debug {:honeymap honeymap})
  honeymap)

(defn index [query-params]
  (let [query (-> (build-repositories-base-query)
                  log-debug-honeymap
                  (pagination/add-offset-for-honeysql query-params)
                  log-debug-honeymap
                  hc/format
                  log-debug-honeymap)
        _ (logging/debug "GET /repositories " {:query query})]
    (jdbc/query (rdbms/get-ds) query)))

(defn get-index [request]
  {:body {:repositories (index (:query-params request))}})


;### create repository #################################################################

(defn create-repository [request]
  (if-not (= (->> request :json-params keys (map keyword) set) #{:name :git_url :git_fetch_and_update_interval :update_notification_token :branch_trigger_include_match :branch_trigger_exclude_match :public_view_permission :foreign_api_endpoint :foreign_api_authtoken :foreign_api_owner :foreign_api_repo :foreign_api_type})
    {:status 422
     :body {:message "The request body must exactly contain the keys :name 'git_url', 'git_fetch_and_update_interval', 'update_notification_token', 'branch_trigger_include_match', 'branch_trigger_exclude_match', 'public_view_permission', 'foreign_api_endpoint', 'foreign_api_authtoken', 'foreign_api_owner', 'foreign_api_repo' and 'foreign_api_type'"}}
    (let [user-id (-> request :authenticated-user :id)
          url (utils-http/build-service-url :builder "/repositories/")
          body (-> request :json-params
                   (assoc :created_by user-id)
                   json/write-str)
          params {:body body
                  :throw-exceptions false
                  :socket-timeout 3000
                  :conn-timeout 3000}
          response (http/request :post url params)]
      (if (map? (:body response))
        (select-keys response [:body :status])
        {:status (:status response)
         :body {:message {:body (-> response :body str)}}}))))

;### routes #####################################################################

(def routes
  (cpj/routes
    (cpj/GET "/repositories/" request (get-index request))
    (cpj/POST "/repositories/" _ create-repository)))



;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
