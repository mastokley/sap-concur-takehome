(ns sap-concur-takehome.util.oauth-test-fixtures)

(def deterministic-nonce
  (constantly "kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg"))

(def deterministic-timestamp (constantly "1318622958"))

(def oauth-parameters
  {"oauth_consumer_key" "xvz1evFS4wEEPTGEFPHBog"
   "oauth_nonce" (deterministic-nonce)
   "oauth_signature_method" "HMAC-SHA1"
   "oauth_timestamp" (deterministic-timestamp)
   "oauth_token" "370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb"
   "oauth_version" "1.0"})

(def request-context
  {:http-method "post"
   :url "https://api.twitter.com/1.1/statuses/update.json?include_entities=true"
   :body-parameters {"status"
                     "Hello Ladies + Gentlemen, a signed OAuth request!"}})

(def keys-and-secrets
  {:consumer-api-key "xvz1evFS4wEEPTGEFPHBog"
   :access-token "370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb"
   :consumer-api-secret-key "kAcSOqF21Fu85e7zjz7ZN2U4ZRhfV3WpwPAoE3Z7kBw"
   :access-token-secret "LswwdoUaIvS8ltyTt5jkRh4J50vUPVVHtR2YPi5kE"})

(def base-string
  "POST&https%3A%2F%2Fapi.twitter.com%2F1.1%2Fstatuses%2Fupdate.json&include_entities%3Dtrue%26oauth_consumer_key%3Dxvz1evFS4wEEPTGEFPHBog%26oauth_nonce%3DkYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg%26oauth_signature_method%3DHMAC-SHA1%26oauth_timestamp%3D1318622958%26oauth_token%3D370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb%26oauth_version%3D1.0%26status%3DHello%2520Ladies%2520%252B%2520Gentlemen%252C%2520a%2520signed%2520OAuth%2520request%2521")
