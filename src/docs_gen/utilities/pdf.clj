(ns docs-gen.pdf
  (:require
   [clj-pdf.core :as doc]
  (:use net.cgrand.enlive-html)))

(defn enlive->clj-pdf [el])

(defn enlive->hiccup [el]
  (if-not (string? el)
    (->> (map enlive->hiccup (:content el))
         (concat [(:tag el) (:attrs el)])
         (keep identity)
         vec)
    el))

;(comp enlive->clj-pdf enlive->hiccup)


;(def stylesheet
; {:chunk {:ttf-name "TisaOT"
;         :color [255 100 20]
;         :size 24}})

;(doc/pdf
; [{:register-system-fonts? true
;   :font {:ttf-name "TisaOT"}
;   :stylesheet stylesheet
;   }
;   [:heading "Title of the entry"]
;  [:paragraph.foo "Most of it works."]]
; "/Users/Prabros/Desktop/doc.pdf")
