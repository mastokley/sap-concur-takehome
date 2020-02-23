(ns sap-concur-takehome.util.time
  (:require [java-time :as time]))

(defn instant->seconds-from-epoch [i]
  (-> i
      time/to-millis-from-epoch
      (/ 1000)
      int))
