(ns sap-concur-takehome.core-test
  (:require
   [clojure.test :refer [deftest is]]
   [sap-concur-takehome.clients.github :as github]
   [sap-concur-takehome.clients.twitter :as twitter]
   [sap-concur-takehome.core-test-fixtures :as f]
   [sap-concur-takehome.core :as sut]
   [java-time :as time]))

(defn fetch-pull-requests-stub
  [{:keys [cutoff] :as fetch-pull-request-args}]
  (if (= (time/instant f/timestamp-of-most-recent-tweet) cutoff)
    f/pull-requests
    (throw (ex-info "bad call to `fetch-pull-requests`"
                    {:fetch-pull-request-args fetch-pull-request-args}))))

(defn tweet!-stub
  [published-tweets client-map status]
  (if (= status
         "mastokley has opened a new pull request: https://github.com/mastokley/sap-concur-takehome/pull/2 (created at 2020-02-19T21:49:07Z)")
    (swap! published-tweets conj status)
    (throw (ex-info "bad call to `tweet!`"
                    {:client-map client-map
                     :status status}))))

(def get-most-recent-tweet-stub (constantly f/most-recent-tweet))

(def load-conf-stub (constantly f/conf))

(deftest main
  (let [published-tweets (atom [(:text f/most-recent-tweet)])]
    (with-redefs [github/fetch-pull-requests fetch-pull-requests-stub
                  twitter/tweet! (partial tweet!-stub published-tweets)
                  twitter/get-most-recent-tweet get-most-recent-tweet-stub
                  sut/load-conf load-conf-stub]
      (sut/-main)
      (is (= ["mastokley has opened a new pull request: https://github.com/mastokley/sap-concur-takehome/pull/1 (created at 2020-02-01T18:40:32Z)"
              "mastokley has opened a new pull request: https://github.com/mastokley/sap-concur-takehome/pull/2 (created at 2020-02-19T21:49:07Z)"]
             @published-tweets)))))
