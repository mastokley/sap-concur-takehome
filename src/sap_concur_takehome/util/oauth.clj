(ns sap-concur-takehome.util.oauth
  (:import [java.util Base64])
  (:require
   [clojure.string :as string]
   [pandect.algo.sha1 :refer [sha1-hmac-bytes]]
   [java-time :as time]
   [sap-concur-takehome.util.time :refer [instant->seconds-from-epoch]]
   [sap-concur-takehome.util.url :refer [parse-querystring-parameters url-encode]]))

(defn- rand-str [len]
  (string/join (repeatedly len #(char (+ (rand 26) 65)))))

(def ^:private oauth-timestamp
  (comp str instant->seconds-from-epoch time/instant))

(defn- build-oauth-parameters [api-key access-token]
  {"oauth_consumer_key" api-key
   "oauth_nonce" (rand-str 40)
   "oauth_signature_method" "HMAC-SHA1"
   "oauth_timestamp" (oauth-timestamp)
   "oauth_token" access-token
   "oauth_version" "1.0"})

(defn- url-encode-map-entries [m]
  (map (fn [[k v]] [(url-encode k) (url-encode v)]) m))

(defn- quote-values [key-value-pairs]
  (map (fn [[k v]] [k (str "\"" v "\"")]) key-value-pairs))

(defn- join-key-value-pairs [key-value-pairs]
  (map #(string/join "=" %) key-value-pairs))

(defn- build-parameter-string
  [querystring-parameters body-parameters oauth-parameters]
  (->>
   (merge querystring-parameters body-parameters oauth-parameters)
   url-encode-map-entries
   join-key-value-pairs
   sort
   (string/join "&")))

(defn- strip-query-from-url [url]
  (-> url
      (string/split #"\?")
      first))

(defn build-base-string
  [{:keys [http-method url body-parameters]} oauth-parameters]
  (let [querystring-parameters (parse-querystring-parameters url)
        parameter-string (build-parameter-string querystring-parameters
                                                 body-parameters
                                                 oauth-parameters)]
    (str
     (string/upper-case http-method)
     "&"
     (url-encode (strip-query-from-url url))
     "&"
     (url-encode parameter-string))))

(defn build-signing-key [consumer-secret oauth-token-secret]
  (str
   (url-encode consumer-secret)
   "&"
   (url-encode oauth-token-secret)))

(defn calculate-oauth-signature
  [consumer-secret oauth-token-secret base-string]
  (let [signing-key (build-signing-key consumer-secret oauth-token-secret)
        encoder (Base64/getEncoder)]
    (.encodeToString encoder (sha1-hmac-bytes base-string signing-key))))

(defn build-authorization-header
  [{:keys [http-method url body-parameters] :as request-context}
   {:keys [consumer-api-key
           consumer-api-secret-key
           access-token
           access-token-secret]}]
  (let [oauth-parameters (build-oauth-parameters consumer-api-key access-token)
        base-string (build-base-string request-context oauth-parameters)
        oauth-signature (calculate-oauth-signature consumer-api-secret-key
                                                   access-token-secret
                                                   base-string)]
    (->> oauth-parameters
         (merge {"oauth_signature" oauth-signature})
         url-encode-map-entries
         quote-values
         sort
         join-key-value-pairs
         (string/join ", ")
         (str "OAuth "))))
