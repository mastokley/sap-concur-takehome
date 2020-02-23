(ns sap-concur-takehome.util.url
  (:require [clojure.string :as string]))

(defn- joined-parameters->map [joined-parameters]
  (into {} (map #(string/split % #"=") joined-parameters)))

(defn parse-querystring-parameters [url]
  (let [[_ query] (string/split url #"\?")]
    (if (nil? query) {}
        (-> query
            (string/split #"&")
            joined-parameters->map))))

;; courtesy of https://github.com/mattrepl/clj-oauth see LICENSE
(defn url-encode
  "The java.net.URLEncoder class encodes for application/x-www-form-urlencoded,
  but OAuth requires RFC 3986 encoding."
  [s]
  (-> (java.net.URLEncoder/encode s "UTF-8")
      (.replace "+" "%20")
      (.replace "*" "%2A")
      (.replace "%7E" "~")))
