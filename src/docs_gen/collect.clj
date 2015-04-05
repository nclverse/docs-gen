;; #+options: toc:nil num:nil

;; * Namespaces

;; #+BEGIN_SRC clojure

(ns docs-gen.collect
  (:require [stasis.core :as stasis]
            [clojure.java.shell :refer [sh]])
  (:use net.cgrand.enlive-html))

;; #+END_SRC

;; * Configuration
;; ** Collector functions
;;    
;; #+BEGIN_SRC clojure

(defn collect-vals [key config] 
(let [vals (map key (:sites config))]
(when (every? (complement nil?) vals) vals)))

;; #+END_SRC

;; ** Configuration Map

;; #+BEGIN_SRC clojure

(def config {
:mode :render
:server-root "http://localhost:3000"
:export-root "http://nclverse.github.io"
:supported-formats ["css" "svg" "org"]
:sites [
{:site "code"
:metadata [:loc]
:source "/Users/Prabros/Dropbox/ncl/stage3/clojure/apps/code/source"
:output "/Users/Prabros/Dropbox/ncl/stage3/clojure/apps"}
{:site "thesis"
:metadata [:word-count]
:source "/Users/Prabros/Dropbox/ncl/stage3/clojure/apps/thesis/source"
:output "/Users/Prabros/Dropbox/ncl/stage3/clojure/apps"}
{:site "design"
:metadata []
:source "/Users/Prabros/Dropbox/ncl/stage3/clojure/apps/design/source"
:output "/Users/Prabros/Dropbox/ncl/stage3/clojure/apps"}
{:site "help"
:metadata [:topics]
:source "/Users/Prabros/Dropbox/ncl/stage3/clojure/apps/help/source"
:output "/Users/Prabros/Dropbox/ncl/stage3/clojure/apps"}
]})

;; #+END_SRC


;; * Collecting Files

;; ** Scanners
;; #+BEGIN_SRC clojure
  
(defn scan-format [format]
({"css" #".*\.css"
"svg" #".*\.svg"
"org" #".*\.org"} format))

;; #+END_SRC

;; ** Get Assets

;;    Gets the assets from the given directory
;;    Only takes in the supported formats.

;; #+BEGIN_SRC clojure

  (defn get-assets [asset source-dir]
    {:pre [(some #(= asset %) (:supported-formats config))] :post [#((complement empty?) %)]}
    (let [format-regex (scan-format asset)
          asset-dir (str source-dir "/" asset)]
      (stasis/slurp-directory asset-dir format-regex)))

;; #+END_SRC


;; * Structure

;; #+BEGIN_SRC clojure
(comment

;; #+END_SRC

;; #+BEGIN_SRC clojure

(defn asset-key [k] ({"org" :pages "css" :stylesheets "img" :images} k))

(defn place-assets [type source-dir site]
(let [format-regex (scan-format asset)
      asset (get-asset type)]
  {(asset-key type) (zipmap (map #(str "/" site "/" asset %) (keys assets)) (vals assets))}))

;; #+END_SRC


;; ** Build Site Map

;; #+BEGIN_SRC clojure

    (defn build-site [site root with-meta]
    {:pre [(map? page-map)]}
    (let [urls (keys page-map)
    pages (vals page-map)
    html-urls (map #(replace-org (clean-numbering %) ".html") urls)
    cleaned-urls (map #(str "/" (:site config) %) html-urls)]
    (zipmap cleaned-urls
    (if with-meta (full-render root site urls pages)
    (map (partial quick-render root site) pages)))))

;; #+END_SRC


;;    

;; The progression is thus.

;; Collect -> Structure -> Transform -> Render

;; The first two stages are done here.

;; * Structure
;; * Collecting Files

;; #+BEGIN_SRC clojure

  (defn collect-files [config]
  (let [sites (collect-vals :sites config)
        sources (collect-sources :sites config)
        org-dirs (map #(str source "/org") sites)]
  (stasis/merge-page-sources
  (merge {:pages (map build-site (stasis/slurp-directory org-dir #"\.org$") config true)}
  (place-assets "css" source site)
  (place-assets "img" source site)))))

;; #+END_SRC

;; # Local Variables:
;; # lentic-init: lentic-org-clojure-init
;; # End:
;; * Building them

;; #+BEGIN_SRC clojure
)

;; #+END_SRC