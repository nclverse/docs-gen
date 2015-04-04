;; #+options: toc:nil num:nil

;; * Core

;;   Powers the collection and rendering ends of the docs generator.

;; * Namespaces

;; #+BEGIN_SRC clojure

(ns docs-gen.core
  (:require [stasis.core :as stasis]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.reload :refer [wrap-reload]]
            [prone.debug :refer [debug]]
            [org.tobereplaced.nio.file :as nio]
            [clojure.java.shell :refer [sh]]
            [docs-gen.view :as view])
  (:use net.cgrand.enlive-html))

;; #+END_SRC


;; * Configurations 

;; ** Configuration Map

;; #+BEGIN_SRC clojure

(def code-config {
:export true
:server-root "http://localhost:3000"
:export-root "http://nclverse.github.io"
:site "code"
:meta-data [:loc]
:source "/Users/Prabros/Dropbox/ncl/stage3/clojure/apps/code/source"
:output "/Users/Prabros/Dropbox/ncl/stage3/clojure/apps"
})

;; #+END_SRC

;; * Transform
;;   ** Transform URLS
;; Replacing org extension with .html is all we do here.

;; #+BEGIN_SRC clojure

(defn strip-org [url]
   (clojure.string/replace url #".org" ""))

(defn dot-org->dot-html [url]
   (clojure.string/replace url #".org" ".html"))

(def clean-numbering #(clojure.string/replace % #"\d+_" ""))

(defn format-nav-links [word]
  (clojure.string/join " " (map clojure.string/capitalize (clojure.string/split (clean-numbering word) #"_"))))

;; #+END_SRC

;; ** Transform Pages
;;     We convert each org page to HTML

;; *** Org to HTML
;;      This uses a script stored inside the resources directory to
;;      convert org file to HTML using the config options specified per
;;      file. Need to decide if a global org file config makes sense.

;; #+BEGIN_SRC clojure

   (defn org->html [org-content]
   "Prerequisite: Emacs with Org-Mode installed.
   Converts given org file to html."
   (:out (sh "emacs" "--script" "resources/scripts/org-to-html.el" org-content)))

;; #+END_SRC

;; *** Metadata Collection Helpers

;; *** Word Count

;; #+BEGIN_SRC clojure
(defn word-count [entry]
   (let [nodes (select-nodes* (html-snippet entry) [text-node])
   text (apply str nodes)]
   (count (re-seq #"\w+" (.toLowerCase text)))))

(defn loc [entry]
   (let [nodes (select-nodes* (select (html-snippet entry) [:pre]) [text-node])
   text (apply str nodes)]
   (count (re-seq #"\n" text))))

;; #+END_SRC

;; #+ATTR_HTML :class smell

;; *** URL Components

;;       Pretty contrived right now but supports all the current
;;       documentations sites. Better to make this support 

;; #+BEGIN_SRC clojure
(defn url-components [url]
  (zipmap [:category :title :subtitle] (filter not-empty (clojure.string/split url #"/"))))
;; #+END_SRC

;; *** Find Title

;;       Gets the title of the converted HTML page.

;; #+BEGIN_SRC clojure
(defn title [x]
  (first
   (select
   (html-snippet x) [:h1 text-node])))
;; #+END_SRC


;; *** Weave metadata

;; Collect metadata from HTML pages
;; Creates a map of the form:

;; #+BEGIN_SRC clojure

(defn collect-meta-data [url page] {:page page :url (url-components url) :meta {:title (title page)}})

;; #+END_SRC

;; * Render
;; Recombining the pages that have been transformed and rendering them.

;; ** Rendering Functions
;;    Comes in two flavours. One with meta data collection (involves
;;    expensive computation, slows down the generator considerably) and one
;;    without(quick).

;; #+BEGIN_SRC clojure

(defn quick-render [root site page]
(view/transform-page root site [] (collect-meta-data site (org->html page))))

;; #+END_SRC

;; *** Render with Meta
;;     Weaves the metadata of all pages with the current page.
;; Gives out a map of url with the pages weaved with meta data.

;; #+BEGIN_SRC clojure

(defn full-render [root site urls pages]
(let [html-pages (map org->html pages)
pages-with-meta-data (map collect-meta-data urls html-pages)
nav (map #(into (url-components (strip-org %1)) {:count (loc %2)}) urls html-pages)
sorted-nav (sort-by #(vec (map % [:title :subtitle])) nav)]
(map (partial view/transform-page root site sorted-nav) pages-with-meta-data)))
;; #+END_SRC


;; ** Build Site Map

;; #+BEGIN_SRC clojure

    (defn build-site [page-map config with-meta]
    {:pre [(map? page-map)]}
    (let [urls (keys page-map) pages (vals page-map)
    site (:site config)
    server-root (:server-root config)
    export-root (:export-root config)
    html-urls (map #(dot-org->dot-html (clean-numbering %)) urls)
    cleaned-urls (map #(str "/" (:site config) %) html-urls)]
    (zipmap cleaned-urls
    (if with-meta (full-render server-root site urls pages)
    (map (partial quick-render server-root site) pages)))))

;; #+END_SRC


;; ** Collecting Pages
;;    
;; #+BEGIN_SRC clojure

(defn scan-format [format]
(cond
(= format "css") #".*\.css"
(= format "img") #".*\.svg"
:else "Unknown format"))

(defn asset-key [k]
(get {"css" :stylesheets "img" :images} k))

(defn place-assets [asset source-dir site]
(let [format-regex (scan-format asset)
assets (stasis/slurp-directory (str source-dir "/" asset) format-regex)]
  {(asset-key asset) (zipmap (map #(str "/" site "/" asset %) (keys assets)) (vals assets))}))

;; #+END_SRC

;; #+BEGIN_SRC clojure

(defn collect-files [config]
(let [root (:serve-root-site config)
site (:site config)
source (:source config)
org-dir (str source "/org")]
(stasis/merge-page-sources
(merge {:pages (build-site (stasis/slurp-directory org-dir #"\.org$") config true)}
(place-assets "css" source site)
(place-assets "img" source site)))))

;; #+END_SRC

;; ** Serving Pages

;; #+BEGIN_SRC clojure

(def app 
(wrap-content-type (stasis/serve-pages (collect-files code-config))))


(if (:export code-config)
  (stasis/export-pages (collect-files code-config) (:output code-config)))

;; #+END_SRC

;; # Local Variables:
;; # lentic-init: lentic-org-clojure-init
;; # End: