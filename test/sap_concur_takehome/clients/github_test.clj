(ns sap-concur-takehome.clients.github-test
  (:require
   [cheshire.core :as ch]
   [clj-http.client :as http-client]
   [clojure.test :refer [deftest is]]
   [java-time :as time]
   [sap-concur-takehome.clients.github :as sut]
   [sap-concur-takehome.clients.github-test-fixtures :as f]))

(def build-jwt-stub (constantly f/secret-key))

(defn http-post-stub
  [url {{authorization :authorization} :headers}]
  (let [access-token-url (str "https://api.github.com/app/installations/"
                              f/installation-id
                              "/access_tokens")]
    (if (and (= authorization (str "Bearer " f/secret-key))
             (= url access-token-url))
      {:body {:token f/access-token}}
      (throw (ex-info "badly formed post request" {:authorization authorization
                                                   :url url})))))

(defn http-get-stub
  [url {{authorization :authorization content-type :accept} :headers}]
  (if (and (= authorization (str "token " f/access-token))
           (= url "https://api.github.com/repos/michael/michaels_repo/pulls?sort=created&direction=asc"))
    f/pull-request-response
    (throw (ex-info "badly formed get request" {:authorization authorization
                                                :url url}))))

(deftest fetch-pull-requests
  (with-redefs [http-client/get http-get-stub
                http-client/post http-post-stub
                sut/build-jwt build-jwt-stub]
    (is (= [{:created_at "2020-02-19T21:49:07Z"
             :html_url "https://github.com/mastokley/sap-concur-takehome/pull/3"
             :user {:login "mastokley"}}]
           (sut/fetch-pull-requests
            {:owner "michael"
             :repo-name "michaels_repo"
             :app-id 1
             :private-key-filename "key.pem"
             :installation-id f/installation-id
             :cutoff (time/instant "2020-02-16T19:41:52Z")})))))
