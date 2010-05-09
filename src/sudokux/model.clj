;;;; The model for the application
(ns sudokux.model
  (:require [sudokux.core :as core]))

(defn blank-board
  "Create a new, blank board"
  []
  (vec (repeat 81 0)))

(defn place-number
  "Place v at (x, y) on the board if it is legal to do so"
  [board v x y]
  (let [new-board (assoc board (+ x (* y 9)) v)]
    (if (core/legal-board? new-board)
      new-board
      board)))

(defn get-number
  "Get the number at (x, y)"
  [board x y]
  (board (+ x (* y 9))))

(defn increment-number
  "Increment the number at (x, y)"
  [board x y]
  (let [num (mod (inc (get-number board x y)) 10)
	new-board (assoc board (+ x (* y 9)) num)]
    (if (core/legal-board? new-board)
      new-board
      (recur new-board x y))))

(defn solve-board
  "Solve the board"
  [board]
  (-> (core/board-to-xmat board)
      core/solve-xc
      core/solution-to-board))

(def current-board (ref (blank-board)))
