(ns sap-concur-takehome.clients.twitter-test-fixtures
  (:require [cheshire.core :as ch]))

(def consumer-api-key "consumer-api-key")
(def access-token "access-token")
(def consumer-api-secret-key "consumer-api-secret-key")
(def access-token-secret "access-token-secret")
(def user-id 1)
(def client-map {:consumer-api-key consumer-api-key
                 :access-token access-token
                 :consumer-api-secret-key consumer-api-secret-key
                 :access-token-secret access-token-secret})
(def twitter-timeline-response
  {:body (ch/generate-string
          [{:text "mastokley has opened a new pull request: https://github.com/mastokley/sap-concur-takehome/pull/5 (created at 2020-02-21T18:40:32Z)"}
           {:text "mastokley has opened a new pull request: https://github.com/mastokley/sap-concur-takehome/pull/4 (created at 2020-02-21T18:08:55Z)"}])})
