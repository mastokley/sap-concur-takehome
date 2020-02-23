(ns sap-concur-takehome.core
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.string :as string]
   [clojure.tools.logging :as l]
   [java-time :as time]
   [sap-concur-takehome.clients.github :as github]
   [sap-concur-takehome.clients.twitter :as twitter])
  (:gen-class))

(defn load-conf []
  (-> "conf.edn"
      io/resource
      slurp
      edn/read-string))

(def ^:private tweet-created-at-prefix "created at ")
(def ^:private tweet-character-limit 280)

(defn- github-pull-request->tweet
  [{{user-name :login} :user url :html_url created-at :created_at
    :as github-pull-request}]
  (let [proposed-tweet (str user-name " has opened a new pull request: " url
                            " (" tweet-created-at-prefix created-at ")")]
    (if (> (count proposed-tweet) tweet-character-limit)
      (throw (ex-info "unable to form tweet without exceeding character limit"
                      {:user-name user-name
                       :url url
                       :created-at created-at
                       :proposed-tweet proposed-tweet
                       :tweet-character-limit tweet-character-limit}))
      proposed-tweet)))

(defn- tweet->created-at-instant [tweet]
  (let [date-re #"\d{4}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d:[0-5]\dZ"
        re (re-pattern (str tweet-created-at-prefix date-re))]
    (try
      (->> tweet
           :text
           (re-find re)
           (drop (count tweet-created-at-prefix))
           string/join
           time/instant)
      (catch Exception e
        (throw
         (ex-info "unable to parse created_at time from unstructured tweet"
                  {:tweet tweet}))))))

(defn- tweet-new-prs!
  [github-conf twitter-client cutoff]
  (let [prs (github/fetch-pull-requests (assoc github-conf :cutoff cutoff))
        statuses (map github-pull-request->tweet prs)]
    (l/info "retrieved pull requests")
    (l/info "built statuses" {:statuses statuses})
    (doseq [status statuses] (twitter/tweet! twitter-client status))))

(defn -main [& args]
  (let [{twitter-conf :twitter github-conf :github} (load-conf)
        latest-tweet (twitter/get-most-recent-tweet twitter-conf
                                                    (:user-id twitter-conf))
        cutoff (when-not (nil? latest-tweet)
                 (tweet->created-at-instant latest-tweet))]
    (l/info "retrieved most recent tweet")
    (l/info "determined cutoff" {:cutoff cutoff})
    (tweet-new-prs! github-conf twitter-conf cutoff)
    (l/info "tweeted new pull requests")))
