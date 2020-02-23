(ns sap-concur-takehome.clients.twitter
  (:require
   [cheshire.core :as ch]
   [clj-http.client :as http-client]
   [clojure.spec.alpha :as s]
   [clojure.spec.test.alpha :as st]
   [clojure.tools.logging :as l]
   [sap-concur-takehome.util.oauth :as oauth]
   [sap-concur-takehome.util.url :refer [url-encode]]))

(s/def ::consumer-api-key string?)
(s/def ::access-token string?)
(s/def ::consumer-api-secret-key string?)
(s/def ::access-token-secret string?)
(s/def ::client-map
  (s/keys :req-un [::consumer-api-key
                   ::access-token
                   ::consumer-api-secret-key
                   ::access-token-secret]))

(def ^:private root "https://api.twitter.com/1.1")

(defn- parse-recent-tweet-response
  "return the most recent tweet or `nil`"
  [{body :body}]
  (-> body
      (ch/parse-string true)
      vec
      first)) ; don't want any lazy surprises

(s/fdef get-most-recent-tweet
  :args (s/cat :client-map ::client-map :user-id int?))
(defn get-most-recent-tweet
  "return the most recent tweet or `nil`"
  [client-map user-id]
  (let [url (str root "/statuses/user_timeline.json?count=1&user_id=" user-id)
        request-context {:http-method "get" :url url :body-parameters {}}
        authorization (oauth/build-authorization-header request-context
                                                        client-map)]
    (-> url
        (http-client/get {:headers {:authorization authorization}})
        parse-recent-tweet-response)))

(s/fdef tweets :args (s/cat :client-map ::client-map :status string?))
(defn tweet! [client-map status]
  (let [partial-url (str root "/statuses/update.json?status=")
        request-context {:http-method "post"
                         :url (str partial-url status)
                         :body-parameters {}}
        authorization (oauth/build-authorization-header request-context
                                                        client-map)]
    (http-client/post
     (str partial-url (url-encode status))
     {:headers {:authorization authorization}})))

(st/instrument)
