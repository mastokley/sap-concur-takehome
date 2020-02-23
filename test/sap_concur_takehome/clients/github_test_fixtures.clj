(ns sap-concur-takehome.clients.github-test-fixtures
  (:require [cheshire.core :as ch]))

(def pull-request-response
  {:body
   (ch/generate-string
    [{:created_at "2020-02-14T19:41:52Z"
      :html_url "https://github.com/mastokley/sap-concur-takehome/pull/2"
      :user {:login "mastokley"}}
     {:created_at "2020-02-19T21:49:07Z"
      :html_url "https://github.com/mastokley/sap-concur-takehome/pull/3"
      :user {:login "mastokley"}}])})

(def installation-id 1)
(def access-token "abc")
(def secret-key "def")

