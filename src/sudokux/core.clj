;;;; The core sudoku solver & support code
(ns sudokux.core)

(defrecord XMatrix [columns rows solution])

(defn- clean-row!
  "Remove all entries of r from columns"
  [rows columns r]
  (reduce #(assoc! %1 %2 (disj (%1 %2) r)) columns (rows r)))
  
(defn remove-row
  "Remove a row in an xmat, adding it to the solution"
  [xmat row]
  (let [columns (transient (:columns xmat))
	rows (:rows xmat)
	target-columns (rows row)
	target-rows (reduce into (map columns target-columns))
	columns (reduce (partial clean-row! rows) columns target-rows)
	columns (persistent! (reduce dissoc! columns target-columns))
	solution (conj (:solution xmat) row)]
    (XMatrix. columns rows solution)))

(defn solve-xc
  "Solve the exact cover problem on xmat"
  [xmat]
  (if-let [cols (seq (:columns xmat))]
    (let [rows (second (apply min-key #(count (second %)) cols))]
      (some identity (for [r rows] (solve-xc (remove-row xmat r)))))
    (:solution xmat)))

(defn- floor
  "Return the floor of dividing x by n"
  [x n]
  (int (/ x n)))

(defn- decode-box
  "Decode the box number for a position in sudoku"
  [x y]
  (+ (floor x 3) (* 3 (floor y 3))))

(defn transpose-map
  "Transpose a map of sets"
  [m]
  (loop [trans (transient {}), kvs (seq m)]
    (if-let [[[k v] & kvs] kvs]
      (recur (reduce #(assoc! %1 %2 (conj (%1 %2 #{}) k)) trans v) kvs)
      (persistent! trans))))

(defn- build-sudoku-template
  "Create a blank sudoku board xmat"
  []
  (let [drow (+ 0 (* 9 9))
	dcol (+ drow (* 9 9))
	dbox (+ dcol (* 9 9))
	rows (->>
	      (for [y (range 9) x (range 9) v (range 9)]
		(let [square-n (+ x (* y 9))
		      row-n (+ v (* y 9))
		      col-n (+ v (* x 9))
		      box-n (+ v (* (decode-box x y) 9))]
		  #{square-n
		    (+ drow row-n)
		    (+ dcol col-n)
		    (+ dbox box-n)}))
	      (zipmap (range (* 9 9 9))))
	cols (transpose-map rows)]
    (XMatrix. cols rows [])))

(def sudoku-template (build-sudoku-template))

(defn board-to-xmat
  "Create an xmat that represents board"
  [board]
  (let [sel (mapcat (fn [val base]
		      (if (> val 0)
			[(+ base (dec val))]))
		    board
		    (range 0 (* 9 9 9) 9))]
    (reduce remove-row sudoku-template sel)))

(defn solution-to-board
  "Create a board from the solution to solving xmat"
  [solution]
  (reduce (fn [board row]
	    (let [pos (floor row 9)
		  val (inc (mod row 9))]
	      (assoc board pos val)))
	  (vec (repeat 81 0))
	  solution))

(defn- print-board
  "Print a board"
  [board]
  (doseq [row (partition 9 board)]
    (println row)))
	       
(defn- solve-sudoku
  "Solve sudoku"
  [board]
  (-> board
      board-to-xmat
      solve-xc
      solution-to-board
      print-board))

(def board-regions
     (concat (for [y (range 9)]
	       (for [x (range 9)]
		 (+ x (* y 9))))
	     (for [x (range 9)]
	       (for [y (range 9)]
		 (+ x (* y 9))))
	     (for [x (range 3) y (range 3)]
	       (for [a (range 3) b (range 3)]
		 (+ (+ (* x 3) a) (* (+ (* y 3) b) 9))))))

(defn legal-board?
  "Return true if the board is in a legal state"
  [board]
  (every? (fn [region]
	    (let [vals (remove zero? (map board region))]
	      (= (count vals) (count (distinct vals)))))
	  board-regions))

(def sample
     (XMatrix. {0 #{1 3}
               1 #{2 4}
               2 #{0 2}
               3 #{1 3 5}
               4 #{0 5}
               5 #{0 2}
               6 #{1 4 5}}
              {0 #{2 4 5}
               1 #{0 3 6}
               2 #{1 2 5}
               3 #{0 3}
               4 #{1 6}
               5 #{3 4 6}}
              []))

(def hard-board2
     [0 0 1 0 0 4 0 0 0
      0 0 0 0 6 0 3 0 5
      0 0 0 9 0 0 0 0 0
      8 0 0 0 0 0 7 0 3
      0 0 0 0 0 0 0 2 8
      5 0 0 0 7 0 6 0 0
      3 0 0 0 8 0 0 0 6
      0 0 9 2 0 0 0 0 0
      0 4 0 0 0 1 0 0 0])

(def death7
     [0 0 0 0 0 1 0 0 2
      0 1 0 0 2 0 0 3 0
      4 0 0 5 0 0 0 0 0
      0 0 4 0 0 0 0 0 6
      0 7 0 0 3 0 0 1 0
      8 0 0 0 0 0 9 0 0
      5 0 0 0 0 8 0 0 0
      0 0 0 0 1 0 0 7 0
      0 0 6 4 0 0 5 0 0])

(def wikipedia-sample
     [5 3 0 0 7 0 0 0 0
      6 0 0 1 9 5 0 0 0
      0 9 8 0 0 0 0 6 0
      8 0 0 0 6 0 0 0 3
      4 0 0 8 0 3 0 0 1
      7 0 0 0 2 0 0 0 6
      0 6 0 0 0 0 2 8 0
      0 0 0 4 1 9 0 0 5
      0 0 0 0 8 0 0 7 9])
