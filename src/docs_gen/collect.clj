;; #+options: toc:nil num:nil

;; * Namespaces

;; #+BEGIN_SRC clojure

(ns docs-gen.collect
  (:require [stasis.core :as stasis]
            [prone.debug :refer [debug]]))

;; #+END_SRC

;; * Settings

;; #+BEGIN_SRC clojure

(def settings {:supported-formats ["css" "svg"]})

;; #+END_SRC

;; * Collecting Files
;; ** Collection Helpers

;; #+BEGIN_SRC clojure

(defn scan-format [format]
  ({"css" #".*\.css"
    "svg" #".*\.svg"
    "org" #".*\.org"} format))

;; #+END_SRC

;; ** Get Assets

;;  Gets the assets from the given directory
;;  Only takes in the supported formats.

;; #+BEGIN_SRC clojure

(defn slurp-files [format dir]
  {:post [#((complement empty?) %)]}
  (let [format-regex (scan-format format)]
    (stasis/slurp-directory dir format-regex)))

;; #+END_SRC


;; #+BEGIN_SRC clojure

(def clean-numbering #(clojure.string/replace % #"\d+-" ""))

(defn replace-org [url replacement]
  (clojure.string/replace url #".org" replacement))

;; #+END_SRC

;; #+BEGIN_SRC clojure

(defn knit-url [& args]
  (apply str (interpose "/" args)))
  
(defn dest-url [site file-type source-dir]
  (knit-url "" site (str file-type source-dir)))

(defn page-url [site url]
  (knit-url "" (str site (replace-org (clean-numbering url) ".html"))))

;; #+END_SRC


;; #+BEGIN_SRC clojure

(defn asset-key [k] ({"css" :css "svg" :img} k))

(defn get-assets [asset site source-dir]
  (let [k (asset-key asset)
  asset-dir (knit-url source-dir (name k))
        asset-map (slurp-files asset asset-dir)
        source-urls (keys asset-map)
        assets (vals asset-map)
        dest-urls (map #(dest-url site (name k) %) source-urls)]
    {k (zipmap dest-urls assets)}))

(defn get-org-pages [site source-dir render-fn]
  (let [org-dir (knit-url source-dir "org")
        org-map (slurp-files "org" org-dir)
        source-urls (keys org-map)
        cleaned-urls (map #(replace-org % "") (keys org-map))
        org-pages (vals org-map)
        dest-urls (map #(page-url site %) source-urls)]
    (zipmap dest-urls (render-fn cleaned-urls org-pages))))

(defn collect-all [site source-dir render-fn]
  (into {:pages (get-org-pages site source-dir render-fn)}
        (map #(get-assets % site source-dir) (:supported-formats settings))))

;; #+END_SRC

;; # Local Variables:
;; # lentic-init: lentic-org-clojure-init
;; # End:
