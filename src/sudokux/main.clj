;;;; Application startup code
(ns sudokux.main
  (:import [javax.swing JFrame])
  (:require [sudokux.view :as view])
  (:gen-class))

(defn -main
  []
  (view/start true))

