; Copyright © 2013 - 2016 Dr. Thomas Schank <Thomas.Schank@AlgoCon.ch>
; Licensed under the terms of the GNU Affero General Public License v3.
; See the "LICENSE.txt" file provided with this software.

(ns cider-ci.api.resources.repository
  (:require
    [logbug.debug :as debug]
    [cider-ci.utils.http-server :as http-server]
    [cider-ci.utils.rdbms :as rdbms]
    [clj-http.client :as http-client]
    [clj-logging-config.log4j :as logging-config]
    [clojure.data.json :as json]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [compojure.core :as cpj]
    [compojure.handler :as cpj.handler]
    [ring.adapter.jetty :as jetty]
    [ring.middleware.cookies :as cookies]
    [ring.middleware.json]
    [ring.util.response :as response]
    ))


;### get-repository ########################################################

(defn query-exeuction [id]
  (first (jdbc/query (rdbms/get-ds)
                     ["SELECT * from repository
                      WHERE id = ?" id])))

(defn repository-data [params]
  (let [id (:id params)
        repository (query-exeuction id)]))

(defn get-repository [request]
  {:body (repository-data (:params request))
   })


;### routes #####################################################################

(def routes
  (cpj/routes
    (cpj/GET "/repositories/:id" request (get-repository request))


;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
