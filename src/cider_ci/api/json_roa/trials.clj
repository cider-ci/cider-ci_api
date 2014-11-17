; Copyright (C) 2013, 2014 Dr. Thomas Schank  (DrTom@schank.ch, Thomas.Schank@algocon.ch)
; Licensed under the terms of the GNU Affero General Public License v3.
; See the "LICENSE.txt" file provided with this software.

(ns cider-ci.api.json-roa.trials
  (:require
    [cider-ci.api.json-roa.links :as links]
    [cider-ci.utils.debug :as debug]
    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging])
  )


(defn build [request response]
  (let [context (:context request)
        query-params (:query-prarams request)
        task-id (-> request :params :id)
        ids (-> response :body :trial_ids)]
    {:relations
     {
      :self (links/trials context task-id  query-params)
      :root (links/root context)
      :task (links/task context task-id)
      }
     :collection
     (conj
       {:relations (into {} (map (fn [id]
                                   [id (links/trial context id)])
                                 ids))}
       (when (seq ids)
         (links/next-link 
           (links/trials-path context task-id) 
           query-params)))}))



;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)

