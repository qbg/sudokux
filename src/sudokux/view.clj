;;;; The view for the game
(ns sudokux.view
  (:require [sudokux.model :as model]
	    [sudokux.controller :as control]
	    [sudokux.simple-gui :as gui]))

(defn- button-text
  [model index]
  (let [val (get model index)]
    (if (= val 0)
      ""
      (str val))))

(defn- create-board
  []
  (let [colors [:light-gray :white]
	buttons
	(for [y (range 9), x (range 9)]
	  (let [index (+ (* y 9) x)
		color-index (mod (+ 1 (int (/ x 3)) (int (/ y 3))) 2)]
	    [:button [:text (fn [m] (button-text m index))
		      :background (colors color-index)
		      :font [:monospaced :plain 30]
		      :action (fn [_] (control/increment-position x y))]]))]
    `[:grid [:layout [:rows 9 :columns 9]]
      ~@buttons]))
      
(defn- create-controls
  []
  [:grid [:layout [:rows 1 :columns 3] :maximum-size [60000 60]]
   [:button [:text "New" :action (fn [_] (control/new-game))]]
   [:button [:text "Sample" :action (fn [_] (control/load-sample :wiki))]]
   [:button [:text "Solve" :action (fn [_] (control/solve-game))]]])

(defn start
  "Start up the game"
  ([] (start false))
  ([exit]
     (control/new-game)
     (gui/make-gui
      model/current-board
      [:window [:title "SudokuX"
		:size [640 640]
		:resizable? false
		:close-operation (if exit :exit :dispose)]
       [:vertical []
	(create-board)
	(create-controls)]])))
