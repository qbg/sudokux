;;;; The view for the game
(ns sudokux.view
  (:import [javax.swing JFrame JPanel JButton BoxLayout SwingUtilities
	    JMenu JMenuItem JMenuBar]
	   [java.awt GridLayout Dimension Color Font]
	   [java.awt.event ActionListener])
  (:require [sudokux.model :as model]
	    [sudokux.controller :as control]))

(defn add-action-listener
  "Add an action listener that dispatches f with args when invoked on object"
  [object f & args]
  (->>
   (reify
    ActionListener
    (actionPerformed [_ _]
      (apply f args)))
   (.addActionListener object)))

(defn- button-clicked
  "Called with n (the cell number) when clicked"
  [n]
  (control/increment-position (mod n 9) (int (/ n 9))))

(def board-buttons
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
  "Display the buttons on the board"
  [board]
  (doseq [y (range 9), x (range 9)]
    (.setText (board-buttons (+ x (* y 9)))
	      (let [n (model/get-number board x y)]
		(if (= n 0)
		  ""
		  (str n))))))

(defn register-hook
  "Add the update hook to the model"
  []
  (let [hook (fn [_ _ _ board]
	       (SwingUtilities/invokeLater #(update-buttons board)))]
    (add-watch model/current-board ::update hook)))

(defn create-board
  "Create the panel that displays the board"
  []
  (register-hook)
  (let [panel (JPanel. (GridLayout. 9 9))]
    (doseq [but board-buttons]
      (.add panel but))
    (update-buttons @model/current-board)
    panel))

(defn cons-menu
  "Create a menu from the description in form titled title"
  [title form]
  (let [menu (JMenu. title)]
    (doseq [e form]
      (let [[element & args] e]
	(case element
	  :sep (.addSeparator menu)
	  :menu (.add menu (cons-menu (first args) (rest args)))
	  :item (.add menu (doto (JMenuItem. (first args))
			     (add-action-listener (second args)))))))
    menu))
  
(defn create-menubar
  "Create the menubar"
  []
  (doto (JMenuBar.)
    (.add (cons-menu
	   "Puzzle"
	   [[:item "New" control/new-game]
	    [:menu
	     "Load"
	     [:item "Wikipedia Sample" #(control/load-sample :wiki)]
	     [:item "Hard" #(control/load-sample :hard)]
	     [:item "Difficult" #(control/load-sample :difficult)]
	     [:sep]]
	    [:sep]
	    [:item "Solve" control/solve-game]]))))

(defn start
  "Start up the game"
  []
  (let [panel (JFrame. "SudokuX")]
    (control/new-game)
    (doto panel
      (.setLayout (GridLayout. 1 1))
      (.add (create-board))
      (.setJMenuBar (create-menubar))
      (.setSize 640 640)
      (.setResizable false)
      .show)
    panel))
