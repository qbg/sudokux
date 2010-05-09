;;;; The controller of the application
(ns sudokux.controller
  (:require [sudokux.model :as model]
	    [sudokux.core :as core]))

(defn place-number
  "Place v at (x, y) if it is legal to do so"
  [v x y]
  (dosync
   (alter model/current-board model/place-number v x y)))

(defn new-game
  "Create a new game"
  []
  (dosync
   (ref-set model/current-board (model/blank-board))))

(let [solving (atom false)]
  (defn solve-game
    "Give up and solve the game"
    []
    (when (= @solving false)
      (reset! solving true)
      (future
       (dosync
	(alter model/current-board model/solve-board))
       (reset! solving false)))))

(defn increment-position
  "Increment the position at (x, y)"
  [x y]
  (dosync
   (alter model/current-board model/increment-number x y)))

(defn load-sample
  "Load the hard board"
  []
  (dosync
   (ref-set model/current-board core/hard-board2)))