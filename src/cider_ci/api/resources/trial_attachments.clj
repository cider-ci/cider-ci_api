; Copyright (C) 2013, 2014, 2015 Dr. Thomas Schank  (DrTom@schank.ch, Thomas.Schank@algocon.ch)
; Licensed under the terms of the GNU Affero General Public License v3.
; See the "LICENSE.txt" file provided with this software.

(ns cider-ci.api.resources.trial-attachments
  (:require 
    [cider-ci.api.pagination :as pagination]
    [cider-ci.api.util :as util]
    [drtom.logbug.debug :as debug]
    [cider-ci.utils.http :as http]
    [cider-ci.utils.rdbms :as rdbms]
    [clj-logging-config.log4j :as logging-config]
    [clojure.data.json :as json]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [compojure.core :as cpj]
    [honeysql.core :as hc]
    [honeysql.helpers :as hh]
    [ring.util.response :as response]
    ))



;### get-attachments ############################################################
(defn build-attachments-base-query  [trial-id]
  (-> (hh/from :trial_attachments)
      (hh/select :id :path)
      (hh/where [:like :path (str "/"trial-id"/%")])
      (hh/order-by [:path :asc])))


(defn filter-by-path-segment [query trial-id query-params]
  (if-let [pathsegment (:pathsegment query-params)]
    (-> query
        (hh/merge-where [:like :path (str "/" trial-id "/%" pathsegment "%")]))
    query))

(defn attachments-data [trial-id query-params]
  (let [query (-> (build-attachments-base-query trial-id)
                  (filter-by-path-segment trial-id query-params)
                  (pagination/add-offset-for-honeysql query-params)
                  hc/format)]
    (logging/debug {:query query})
    (jdbc/query (rdbms/get-ds) query)))

(defn get-attachments [request]
  (let [trial-id (-> request :route-params :trial_id)
        query-params (-> request :query-params)]
    {:body 
     {:trial_attachments
      (attachments-data trial-id query-params)}}))


;### routes #####################################################################
(def routes 
  (cpj/routes
    (cpj/GET "/trial/:trial_id/trial-attachments/" request (get-attachments request))
    ))



;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
