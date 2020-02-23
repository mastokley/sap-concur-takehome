(ns sap-concur-takehome.clients.twitter-test
  (:require
   [clj-http.client :as http-client]
   [clojure.test :refer [deftest is]]
   [sap-concur-takehome.clients.twitter :as sut]
   [sap-concur-takehome.clients.twitter-test-fixtures :as f]
   [clojure.string :as string]))

(defn http-get-stub
  [url {{authorization :authorization} :headers}]
  (let [get-timeline-url (str "https://api.twitter.com/1.1/statuses/user_timeline.json?count=1&user_id=" f/user-id)]
    (if (= url get-timeline-url)
      f/twitter-timeline-response
      (throw (ex-info "badly formed get request" {:authorization authorization
                                                  :url url})))))

(defn http-post-stub
  [published-tweets url {{authorization :authorization} :headers}]
  (let [status-update-url "https://api.twitter.com/1.1/statuses/update.json?status="]
    (if (string/starts-with? url status-update-url)
      (swap! published-tweets conj (second (string/split url #"=")))
      (throw (ex-info "badly formed post request" {:authorization authorization
                                                   :url url})))))

(deftest get-most-recent-tweet
  (with-redefs [http-client/get http-get-stub]
    (is (= {:text "mastokley has opened a new pull request: https://github.com/mastokley/sap-concur-takehome/pull/5 (created at 2020-02-21T18:40:32Z)"}
           (sut/get-most-recent-tweet f/client-map f/user-id)))))

(deftest tweet!
  (let [published-tweets (atom [])]
    (with-redefs [http-client/post (partial http-post-stub published-tweets)]
      (sut/tweet! f/client-map "hello")
      (is (= ["hello"] @published-tweets)))))
