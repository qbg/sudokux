;;;; The view for the game
(ns sudokux.view
  (:import [javax.swing JFrame JPanel JButton BoxLayout]
	   [java.awt GridLayout Dimension Color Font]
	   [java.awt.event ActionListener])
  (:require [sudokux.model :as model]
	    [sudokux.controller :as control]))

(defn add-action-listener
  [object f & args]
  (->>
   (reify
    ActionListener
    (actionPerformed [_ _]
      (apply f args)))
   (.addActionListener object)))

(defn- button-clicked
  [n]
  (control/increment-position (mod n 9) (int (/ n 9))))

(def button-board
     (vec
      (for [y (range 9), x (range 9)]
	(let [but (JButton.)
	      qm (mod (+ (int (/ x 3)) (+ 1 (int (/ y 3)))) 2)
	      colors [Color/lightGray Color/white]]
	  (add-action-listener but button-clicked (+ x (* y 9)))
	  (.setBackground but (colors qm))
	  (.setFont but (Font. Font/MONOSPACED Font/PLAIN 30))
	  but))))

(defn- update-buttons
  "Display the board on the buttons"
  [board]
  (doseq [y (range 9), x (range 9)]
    (.setText (button-board (+ x (* y 9)))
	      (let [n (model/get-number board x y)]
		(if (= n 0)
		  ""
		  (str n))))))

(defn register-hook
  []
  (add-watch model/current-board ::update #(update-buttons %4)))

(defn create-board-panel
  []
  (register-hook)
  (let [panel (JPanel. (GridLayout. 9 9))]
    (doseq [but button-board]
      (.add panel but))
    (update-buttons @model/current-board)
    panel))

(defn create-control-panel
  []
  (let [panel (JPanel. (GridLayout. 1 2))
	solve (JButton. "Solve")
	sample (JButton. "Sample")
	new-but (JButton. "New")]
    (add-action-listener new-but #(control/new-game))
    (.add panel new-but)
    (add-action-listener solve #(control/solve-game))
    (.add panel solve)
    (add-action-listener sample #(control/load-sample))
    (.add panel sample)
    panel))

(defn start
  "Start up the game"
  []
  (let [panel (JFrame. "SudokuX")
	cpanel (create-control-panel)]
    (control/new-game)
    (.setLayout panel (BoxLayout. (.getContentPane panel) BoxLayout/Y_AXIS))
    (.add panel (create-board-panel))
    (.add panel cpanel)
    (.setMaximumSize cpanel (Dimension. 60000 60))
    (.setSize panel 640 640)
    (.show panel)
    panel))

