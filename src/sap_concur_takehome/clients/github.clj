(ns sap-concur-takehome.clients.github
  (:require
   [buddy.core.keys :as keys]
   [buddy.sign.jwt :as sign-jwt]
   [cheshire.core :as ch]
   [clj-http.client :as http-client]
   [clojure.java.io :as io]
   [clojure.spec.alpha :as s]
   [clojure.spec.test.alpha :as st]
   [clojure.tools.logging :as l]
   [java-time :as time]
   [sap-concur-takehome.util.time :refer [instant->seconds-from-epoch]]))

(s/def :access-token-response/token string?)
(s/def :access-token-response/body
  (s/keys :req-un [:access-token-response/token]))
(s/def ::access-token-response (s/keys :req-un [:access-token-response/body]))
(s/def :repo/owner string?)
(s/def ::repo-name string?)
(s/def ::app-id int?)
(s/def ::private-key-filename string?)
(s/def ::cutoff (s/nilable time/instant?))
(s/def ::fetch-pull-requests-args
  (s/keys :req-un [:repo/owner
                   ::repo-name
                   ::app-id
                   ::private-key-filename
                   ::installation-id
                   ::cutoff]))

(def ^:private root "https://api.github.com")
(def ^:private content-type "application/vnd.github.machine-man-preview+json")

(defn- build-claim [app-id]
  (let [now (time/instant)
        ten-minutes-from-now (time/plus now (time/minutes 10))]
    {:iat (instant->seconds-from-epoch now)
     :exp (instant->seconds-from-epoch ten-minutes-from-now)
     :iss app-id}))

(defn- read-private-key [private-key-filename]
  (-> private-key-filename
      io/resource
      keys/private-key))

(s/fdef build-jwt
  :args (s/cat :app-id int? :private-key-filename string?))
(defn- build-jwt [app-id private-key-filename]
  (let [private-key (read-private-key private-key-filename)
        claim (build-claim app-id)]
    (sign-jwt/sign claim private-key {:alg :rs256})))

(s/fdef request-access-token
  :args (s/cat :installation-id int? :app-id int? :private-key-filename string?))
(defn- request-access-token
  [installation-id app-id private-key-filename]
  (let [jwt (build-jwt app-id private-key-filename)
        url (str root "/app/installations/" installation-id "/access_tokens")]
    (http-client/post
     url
     {:as :json
      :headers {:authorization (str "Bearer " jwt)
                :accept content-type}})))

(s/fdef parse-access-token-response
  :args (s/cat :response ::access-token-response))
(defn- parse-access-token-response [response]
  (get-in response [:body :token]))

(def ^:private create-access-token
  (comp parse-access-token-response request-access-token))

(defn- parse-pull-request-response [{body :body}]
  (-> body
      (ch/parse-string true)
      vec)) ; don't want any lazy surprises

(defn- apply-cutoff [prs cutoff]
  (let [pr->instant (comp time/instant :created_at)
        after-cutoff? #(-> % pr->instant (time/after? cutoff))]
    (if (nil? cutoff)
      prs
      (filter after-cutoff? prs))))

(s/fdef fetch-pull-requests
  :args (s/cat :fetch-pull-requests-args ::fetch-pull-requests-args))
(defn fetch-pull-requests
  [{:keys [owner repo-name app-id private-key-filename installation-id cutoff]
    :as fetch-pull-request-args}]
  (let [token (create-access-token installation-id app-id private-key-filename)
        url (str root "/repos/" owner "/" repo-name
                 "/pulls?sort=created&direction=asc")]
    (-> url
        (http-client/get {:headers {:authorization (str "token " token)
                                    :accept content-type}})
        parse-pull-request-response
        (apply-cutoff cutoff))))

(st/instrument)
