(ns sudokux.simple-gui
  (:import [java.awt EventQueue Color]
	   [java.awt.event ActionListener]
	   [javax.swing JPanel]))

(deftype Gui [model update-list names panel])

(defn gui-model
  "Return the gui's model"
  [gui]
  (.model gui))

(defn- perform-updates
  [gui]
  (let [model @(.model gui)]
    (EventQueue/invokeLater
     #(doseq [f (.update-list gui)]
	(f model)))))

(defn- install-watch
  [gui]
  (let [model (.model gui)]
    (add-watch model ::updater (fn [_ _ _ _] (perform-updates gui)))))

(declare components)
(declare getters)
(declare setters)

(declare ^{:private true} build-component)

(defn- make-component
  [type children props]
  ((components type) children props))
  
(defn- construct-children
  [gui children]
  (reduce (fn [[comps list names] form]
	    (let [[c l n] (build-component gui form)]
	      [(conj comps c) (concat list l) (merge names n)]))
	  [[] nil {}]
	  children))  

(defn- add-children
  [component children]
  (doseq [c children]
    (.add component c)))

(defn- remove-actions
  [props]
  (dissoc props :action :name))

(defn- set-prop
  [component prop val]
  ((setters prop) component val))

(defn- get-prop
  [component prop]
  ((getters prop) component))

(defn get-property
  "From the component named name in gui, get the value corresponding to the prop
property"
  [gui name prop]
  (get-prop ((.names gui) name) prop))

(defn- set-props
  [component props]
  (let [props (remove-actions props)]
    (loop [props props, ul nil]
      (if-let [[[prop val] & props] (seq props)]
	(if (fn? val)
	  (recur props (conj ul #(set-prop component prop (val %))))
	  (do
	    (set-prop component prop val)
	    (recur props ul)))
	ul))))

(defn- add-listener
  [component gui props]
  (let [action (:action props)
	al
	(reify
	 ActionListener
	 (actionPerformed [_ e]
	   (let [source (.getSource e)]
	     (action @gui))))]
    (if action
      (.addActionListener component al))))

(defn- mapify
  [kvs]
  (apply hash-map kvs))

(defn- build-component
  "Takes a gui atom and a form and returns a vector of the component and a
list of any update functions"
  [gui form]
  (let [[type props & children] form
	props (mapify props)
	[child-components update-list names] (construct-children gui children)
	[component ul]
	(make-component type child-components (remove-actions props))
	names (assoc names (:name props) component)
	update-list (concat update-list ul)]
    (add-listener component gui props)
    [component update-list names]))

(defn make-gui
  "Create a gui with a given initial model from the description in form"
  [model form]
  (let [guiref (atom nil)
	[root ul names] (build-component guiref form)]
    (doto root
      .pack
      .show)
    (let [gui (reset! guiref (Gui. model ul names root))]
      (perform-updates gui)
      (install-watch gui)
      gui)))

(defn- translate-color
  [name]
  (cond
   (symbol? name)
   ({:black Color/black
     :blue Color/blue
     :cyan Color/cyan
     :gray Color/gray
     :green Color/green
     :light-gray Color/lightGray
     :magenta Color/magenta
     :orange Color/orange
     :pink Color/pink
     :red Color/red
     :white Color/white
     :yell Color/yellow}
    name
    Color/white)
   :else name))

(defn- build-standard
  [ctor]
  (fn [children props]
    (let [comp (ctor)
	  ul (set-props comp props)]
      (doseq [c children]
	(.add comp c))
      [comp ul])))

(defn- build-layout
  [ctor]
  (fn [children props]
    (let [layout-props (mapify (:layout props))
	  layout (ctor)
	  props (dissoc props :layout)
	  panel (javax.swing.JPanel.)
	  ul (concat (set-props layout layout-props)
		     (set-props panel props))]
      (.setLayout panel layout)
      (doseq [c children]
	(.add panel c))
      [panel ul])))
	  
(def components
     {:window (build-standard #(javax.swing.JFrame.))
      :button (build-standard #(javax.swing.JButton.))
      :text (build-standard #(javax.swing.JTextField.))
      :label (build-standard #(javax.swing.JLabel.))
      :grid (build-layout #(java.awt.GridLayout.))
      :vertical (build-standard #(javax.swing.Box/createVerticalBox))
      :horizontal (build-standard #(javax.swing.Box/createHorizontalBox))})

(def getters
     {:text #(.getText %)})

(def setters
     {:title #(.setTitle %1 %2)
      :text #(.setText %1 %2)
      :rows #(.setRows %1 %2)
      :columns #(.setColumns %1 %2)
      :background #(.setBackground %1 (translate-color %2))})

(defn sample
  []
  (let [display #(:text %)
	get-text #(get-property % :entry :text)
	clicker #(dosync
		  (alter (gui-model %) assoc :text (get-text %)))]
    (make-gui
     (ref {:text "world!"})
     [:window [:title "Nice"]
      [:grid [:layout [:rows 2 :columns 2]]
       [:label [:text "Hello,"]]
       [:label [:text display]]
       [:text [:text "Fill me" :name :entry]]
       [:button [:text "Click me!" :action clicker]]]])))
