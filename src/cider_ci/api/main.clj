; Copyright (C) 2013, 2014, 2015 Dr. Thomas Schank  (DrTom@schank.ch, Thomas.Schank@algocon.ch)
; Licensed under the terms of the GNU Affero General Public License v3.
; See the "LICENSE.txt" file provided with this software.


(ns cider-ci.api.main
  (:gen-class)
  (:require
    [cider-ci.utils.http :as utils-http]
    [cider-ci.api.resources :as resources]
    [cider-ci.api.web :as web]
    [cider-ci.utils.config :as config :refer [get-config]]
    [logbug.debug :as debug]
    [cider-ci.utils.http :as http]
    [cider-ci.utils.map :refer [deep-merge]]
    [cider-ci.utils.nrepl :as nrepl]
    [cider-ci.utils.rdbms :as rdbms]
    [logbug.catcher :as catcher]
    [logbug.thrown]
    [clojure.tools.logging :as logging]
    ))

(defn -main [& args]
  (catcher/with-logging {}
    (logbug.thrown/reset-ns-filter-regex #".*cider.ci.*")
    (config/initialize {:overrides {:service :api}})
    (rdbms/initialize (config/get-db-spec :api))
    (nrepl/initialize (-> (get-config) :services :api :nrepl))
    (utils-http/initialize (get-config))
    (web/initialize)
    nil))


;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)

