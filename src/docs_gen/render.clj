;; #+options: toc:nil num:nil

;; * Namespaces
;;   
;; Usage of namespaces:

;; Stasis: collecting directories and files.
;; Ring middleware: Serving the correct content type.
;; Prone Debug: Debugging Clojure code in the browser.
;; Java Shell: Running Emacs to convert org files to HTML.
;; Enlive: Metadata collection.

;; #+BEGIN_SRC clojure
(ns docs-gen.render
  (:require [stasis.core :as stasis]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [clojure.java.shell :refer [sh]]
            [prone.debug :refer [debug]]
            [docs-gen.collect :as collect]
            [docs-gen.transform :as transform])
  (:use net.cgrand.enlive-html))

;; #+END_SRC

;; * Collector functions
;;    
;; #+BEGIN_SRC clojure

(defn collect-vals [key config] 
(let [vals (map key (:sites config))]
(when (every? (complement nil?) vals) vals)))

;; #+END_SRC

;; * Configuration

;; #+BEGIN_SRC clojure

(def config {
:mode :export
:render-mode :full
:serve {:root "http://localhost:3000"}
:export {:root "http://nclverse.github.io"}
 :output "/Users/Prabros/Dropbox/ncl/stage3/clojure/apps"
:sites [
{:site "code"
:metadata [:loc]
 :source "/Users/Prabros/Dropbox/ncl/stage3/clojure/apps/code/source"
 }
;{:site "thesis"
;:metadata [:word-count]
;:source "/Users/Prabros/Dropbox/ncl/stage3/clojure/apps/thesis/source"}
;{:site "design"
;:metadata []
;:source "/Users/Prabros/Dropbox/ncl/stage3/clojure/apps/design/source"
;}
;{:site "help"
;:metadata [:topics]
;:source "/Users/Prabros/Dropbox/ncl/stage3/clojure/apps/help/source"
;:output "/Users/Prabros/Dropbox/ncl/stage3/clojure/apps"}
]})

;; #+END_SRC

;; * URL Helpers
;;    
;; Replaces .org with given string




;; * Org Converter
;;     We convert each org page to HTML using Emacs.
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

;; * Metadata Collection Helpers


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
;;       documentation sites. Better to make this support. Need to
;;       change this to support arbitrary amount of nesting. 5 seems like
;;       a good limit if arbitrary nesting is a bad idea. Need to reflect
;;       on this problem further.

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

;; * Renderers
;;   
;;    Recombining the pages that have been transformed and rendering them.
;;    Comes in two flavours. One with meta data collection (involves
;;    expensive computation, slows down the generator considerably) and one
;;    without(quick).

;; #+BEGIN_SRC clojure

(defn quick-render [root site _ pages]
(map #(transform/transform-page root site [] (collect-meta-data site (org->html %))) pages))

;; #+END_SRC

;; *** Render with Meta

;;     Weaves the metadata of all pages with the current page.
;;     Gives out a map of url with the pages weaved with meta data.
;;     

;; #+BEGIN_SRC clojure

(defn nav-element [url page]
(let [components (url-components url)
count (loc page)
      sites (collect-vals :site config)]
(into components {:loc count :sites sites})))

(defn nav-builder [root site urls pages]
(let [nav (map nav-element urls pages)]
(sort-by #(vec (map % [:title :subtitle])) nav)))

(defn full-render [root site urls pages]
(let [html-pages (map org->html pages)
pages-with-meta-data (map collect-meta-data urls html-pages)
nav (nav-builder root site urls html-pages)]
  (map (partial transform/transform-page root site nav) pages-with-meta-data)))

(defn renderer [root site mode]
({:quick #(quick-render root site %1 %2)
  :full #(full-render root site %1 %2)} mode))

;; #+END_SRC

;; * Build Site Map
;;   
;; Builds a website.

;; #+BEGIN_SRC clojure

(defn build-site [root render-mode {:keys [site metadata source output]}]
  (let [render-fn (renderer root site render-mode)
  assets (collect/collect-all site source render-fn)]
assets))

;; #+END_SRC


;; The progression is thus.

;; Collect -> Transform -> Render

;; The first two stages are done here.

;; * Collecting Files
;; #+BEGIN_SRC clojure

(defn collect-files [{:keys [sites mode render-mode] :as config}]
  (let [root (:root (mode config))]
    (stasis/merge-page-sources (apply merge-with into (map #(build-site root render-mode %) sites)))))

;; #+END_SRC

;; * Finale
;;   
;;   Servers or exports the pages. A multimethod to either render or export based on the configuration.

;; #+BEGIN_SRC clojure

(defmulti render :mode)

(defmethod render :serve [config]
(wrap-content-type (stasis/serve-pages (collect-files config))))

(defmethod render :export [config]
  (stasis/export-pages (collect-files config) (:output config)))

(def app (render config))

;; #+END_SRC

;; # Local Variables:
;; # lentic-init: lentic-org-clojure-init
;; # End:
