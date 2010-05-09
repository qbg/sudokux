(ns sudokux.core)

(defrecord XMatrix [columns rows solution])

(defn filter-columns
  "Remove all column entries in row from columns"
  [columns row]
  (reduce dissoc columns row))

(defn find-useless-rows
  "Find all the rows to be disposed of"
  [columns row]
  (reduce into #{} (map columns row)))

(defn clean-columns
  "Remove all row entries in columns from rows in useless"
  [columns rows useless]
  (reduce (fn [columns row]
	    (let [cols (rows row)]
	      (reduce (fn [columns col]
			(assoc columns col
			       (disj (columns col) row)))
		      columns
		      cols)))
	  columns
	  useless))

(defn filter-rows
  "Remove useless rows from rows"
  [rows useless]
  (reduce dissoc rows useless))

(defn remove-row
  "Remove a row in an xmat, adding it to the solution"
  [xmat row]
  (let [row-ent ((:rows xmat) row)
	useless (find-useless-rows (:columns xmat) row-ent)
	columns (clean-columns (:columns xmat) (:rows xmat) useless)
	columns (filter-columns columns row-ent)
	rows (filter-rows (:rows xmat) useless)
	solution (conj (:solution xmat) row)]
    (XMatrix. columns rows solution)))

(defn find-small-column
  [xmat]
  (second (apply min-key #(count (second %)) (:columns xmat))))

(defn solve-xc
  "Solve the exact cover problem on xmat"
  [xmat]
  (if (seq (:columns xmat))
    (let [rows (find-small-column xmat)]
      (some identity (for [r rows] (solve-xc (remove-row xmat r)))))
    (:solution xmat)))

(defn floor
  "Return the floor of dividing x by n"
  [x n]
  (int (/ x n)))

(defn decode-box
  "Decode the box number for a position in sudoku"
  [x y]
  (+ (floor x 3) (* 3 (floor y 3))))

(defn transpose-map
  "Transpose a map of sets"
  [m]
  (reduce (fn [target [row cols]]
	    (reduce (fn [target col]
		      (assoc target col
			     (conj (target col #{})
				   row)))
		    target
		    cols))
	  {}
	  m))

(defn build-sudoku-template
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
  [board]
  (let [sel (mapcat (fn [val base]
		      (if (> val 0)
			[(+ base (dec val))]))
		    board
		    (range 0 (* 9 9 9) 9))]
    (reduce remove-row sudoku-template sel)))

(defn solution-to-board
  [solution]
  (reduce (fn [board row]
	    (let [pos (floor row 9)
		  val (inc (mod row 9))]
	      (assoc board pos val)))
	  (vec (repeat 81 0))
	  solution))

(defn print-board
  "Print a board"
  [board]
  (doseq [row (partition 9 board)]
    (println row)))
	       
(defn solve-sudoku
  "Solve sudoku"
  [board]
  (-> board
      board-to-xmat
      solve-xc
      solution-to-board
      print-board))

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
