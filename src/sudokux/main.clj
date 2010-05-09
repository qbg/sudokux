;;;; Application startup code
(ns sudokux.main
  (:import [javax.swing JFrame])
  (:require [sudokux.view :as view])
  (:gen-class))

(defn -main
  []
  (let [jframe (view/start)]
    (.setDefaultCloseOperation jframe JFrame/EXIT_ON_CLOSE)))
