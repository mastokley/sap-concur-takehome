(ns sap-concur-takehome.core-test-fixtures
  (:require [java-time :as time]))

(def conf
  {:github {:app-id 1
            :installation-id 1
            :owner "michael"
            :private-key-filename "private-key-filename.pem"
            :repo-name "repo-name"}
   :twitter {:access-token "access-token"
             :access-token-secret "access-token-secret"
             :consumer-api-key "consumer-api-key"
             :consumer-api-secret-key "consumer-api-secret-key"
             :user-id 1}})

(def timestamp-of-most-recent-tweet "2020-02-01T18:40:32Z")

(def timestamp-of-as-yet-untweeted-pull-request "2020-02-19T21:49:07Z")

(def most-recent-tweet
  {:text (str "mastokley has opened a new pull request: https://github.com/mastokley/sap-concur-takehome/pull/1 (created at " timestamp-of-most-recent-tweet ")")})

(def pull-requests
  [{:created_at timestamp-of-as-yet-untweeted-pull-request
    :html_url "https://github.com/mastokley/sap-concur-takehome/pull/2"
    :user {:login "mastokley"}}])
