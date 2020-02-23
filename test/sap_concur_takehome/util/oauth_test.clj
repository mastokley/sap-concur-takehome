(ns sap-concur-takehome.util.oauth-test
  (:require
   [clojure.test :refer [deftest is]]
   [sap-concur-takehome.util.oauth :as sut]
   [sap-concur-takehome.util.oauth-test-fixtures :as f]))

(def expected-authorization-header
  (str
   "OAuth oauth_consumer_key=\"xvz1evFS4wEEPTGEFPHBog\", oauth_nonce=\""
   (f/deterministic-nonce)
   "\", oauth_signature=\"tnnArxj06cWHq44gCs1OSKk%2FjLY%3D\", oauth_signature_method=\"HMAC-SHA1\", oauth_timestamp=\""
   (f/deterministic-timestamp)
   "\", oauth_token=\"370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb\", oauth_version=\"1.0\""))

(deftest build-authorization-header
  (with-redefs [sut/rand-str f/deterministic-nonce
                sut/oauth-timestamp f/deterministic-timestamp
                sut/calculate-oauth-signature (constantly "tnnArxj06cWHq44gCs1OSKk/jLY=")]
    (is (= expected-authorization-header
           (sut/build-authorization-header f/request-context
                                           f/keys-and-secrets)))))

(deftest build-signing-key
  (is (= "kAcSOqF21Fu85e7zjz7ZN2U4ZRhfV3WpwPAoE3Z7kBw&LswwdoUaIvS8ltyTt5jkRh4J50vUPVVHtR2YPi5kE"
         (sut/build-signing-key
          (:consumer-api-secret-key f/keys-and-secrets)
          (:access-token-secret f/keys-and-secrets)))))

(deftest calculate-oauth-signature
  (is (= "hCtSmYh+iHYCEqBWrE7C7hYmtUk="
         (sut/calculate-oauth-signature
          (:consumer-api-secret-key f/keys-and-secrets)
          (:access-token-secret f/keys-and-secrets)
          f/base-string))))

(deftest build-base-string
  (is (= f/base-string
         (sut/build-base-string
          f/request-context
          f/oauth-parameters))))
