;; #+options: toc:nil num:nil

;; * Namespaces
;;   
;; #+BEGIN_SRC clojure

(ns docs-gen.view
  (:require
   [clj-pdf.core :as doc]
   [clojure.java.shell :refer [sh]]
[prone.debug :refer [debug]])
  (:use net.cgrand.enlive-html))

;; #+END_SRC

;; #+BEGIN_SRC clojure
(def doc-sites ["code" "dissertation" "design" "docs"])
;; #+END_SRC

;; ** Helpers

;; #+BEGIN_SRC clojure
(defn build-drop-down [site]
(let [site (clojure.string/capitalize site)
      doc-sites (map clojure.string/capitalize doc-sites)]
  ;Inorder to maintain site always as the first element in the row
  ;Alert: Pretty hacky
  (clone-for [s (into [site] (remove #(= % site) doc-sites))]
             [:a] (if (= s site) (do-> (add-class "active")
                                       (set-attr :href (str "/" s))
                                           (content s))
                      (do-> (set-attr :href (str "/" s)) (content s))))))
;; #+END_SRC

;; *** Enlive update-attr helper

;; #+BEGIN_SRC clojure

(defn update-attr [attr f & args]
  (fn [node]
    (if (get (:attrs node) attr)
    (apply update-in node [:attrs attr] f args) node)))

;; #+END_SRC


;; ** Transform Inline URLs
;;    Attach root site to inline URLs

;; #+BEGIN_SRC clojure
(defn url-transform [root site url]
(cond
(.startsWith url "/") (str root url)
(.startsWith url "./") (str root "/" site (clojure.string/replace-first url #"." ""))
(.startsWith url "http") url
:else "Malformed"))
;; #+END_SRC

;;  #+BEGIN_SRC clojure

(defn attach-root [root site page]
(let [update-fn #(update-attr % (fn [current] (url-transform root site current)))]
(apply str (emit* (at (html-resource (java.io.StringReader. page))
                      [:a] (update-fn :href)
                      [:link] (update-fn :href)
                      [:img] (update-fn :src))))))
    
;; #+END_SRC

;; ** Navigation Helpers
;; #+BEGIN_SRC clojure
   
(defn matches [k v m] (filter #(= (k %) v) m))

(defn sum-wc [k v t-map]
  (reduce + (map :count (matches k v t-map))))

;; #+END_SRC

;; ** Check for presence of all

;;    Duct tape functions.

;; #+BEGIN_SRC clojure

(def clean-numbering #(clojure.string/replace % #"\d+_" ""))

(defn format-nav-links [word]
  (clojure.string/join " " (map clojure.string/capitalize (clojure.string/split (clean-numbering word) #"_"))))
  
(defn rebuild-url [site s]
(let [format #(str "/" site "/" (apply str %) ".html")]
(format (interpose "/" (map clean-numbering ((juxt :category :title :subtitle) s))))))

;; #+END_SRC

;; #+BEGIN_SRC clojure

(defn has-all-keys? [nav-entry]
(let [{:keys [category title subtitle]} nav-entry]
(every? #(not (nil? %)) (list category title subtitle))))

(defn leaf-nodes [nav]
(filter has-all-keys? nav))

;; #+END_SRC

;; ** Build Subtitles

;; #+BEGIN_SRC clojure
(defn build-subtitles [titles title current-subtitle site]
  (let [subtitles (matches :title title titles)]
    (clone-for [s subtitles]
               [:li :a.subtitle-link] (do-> (set-attr :href (rebuild-url site s))
                                            (if (= (:subtitle s) current-subtitle) (update-attr :class (fn [& args] (apply str (interpose " " args))) "active") identity)
                                            (content (format-nav-links (:subtitle s))
                                                     (html [:span.count (str (:count s))]))))))
;; #+END_SRC

;; ** Build Titles

;;   #+BEGIN_SRC clojure
  
(defn highlight-link [link-title title]
(if (= link-title title) (update-attr :class (fn [& args] (apply str (interpose " " args))) "active") identity))

;; #+END_SRC 

;; #+BEGIN_SRC clojure
(defn build-titles [link c titles nav site]
    (clone-for [t (distinct titles)]
               [:li :a.title-link] (do-> (content (format-nav-links t)
                                                   (html [:span.count (str (sum-wc :title t nav) " LOC")]))
                                                   (highlight-link (:title link) t))
               [:li :ul.subtitles] (build-subtitles nav t (:subtitle link) site)))

;; #+END_SRC

;; ** Build Categories

;;    Selecting only the valid categories. This means that index.html in
;;    the root directory and such other ones without a leaf node will be omitted.
;;    
;; #+BEGIN_SRC clojure

(defn build-categories [link nav site]
  (let [categories (map :category (leaf-nodes nav))]
  (clone-for [c (distinct categories)]
             [:header.category-title :h1] (content c (html [:span.count (str (sum-wc :category c nav) " LOC")]))
             [:ul.titles] (build-titles link c (map :title (matches :category c nav)) nav site))))

;; #+END_SRC

;; * Layout
;;    Accepts a page with meta data of the structure.

;; ** Article
;; #+BEGIN_SRC clojure

(defsnippet article "templates/article.html" [:article] [entry]
  [:article :.content] (html-content entry))

;; #+END_SRC

;; ** Sidebar

;; #+BEGIN_SRC clojure
(defsnippet sidebar "templates/sidebar.html" [:nav] [current-url site nav page]
  [:img.site-logo] (set-attr :src "./img/logo.svg")
  [:ul.docs-site :li] (build-drop-down site)
  [:div.category]  (build-categories current-url nav site))
;; #+END_SRC


;; #+BEGIN_SRC clojure
(deftemplate layout "templates/layout.html" [site nav page]
  [:title] (content (:title (:meta page)))
  [:#sidebar]  (content (sidebar (:url page) site nav page))
  [:#container :#read-area] (content (article (:page page))))
;; #+END_SRC

;; ** Transform Page

;; #+BEGIN_SRC clojure

 (defn transform-page [root site nav page]
   (let [after-layout (apply str (layout site nav page))]
     (attach-root root site after-layout)))

;(update-in page [:page] #(attach-root root site %)))))
 

;; #+END_SRC

;; (comment 

;;                                                                 ;(if (not (empty? (:active i))) (update-attr :class (fn [& args] (apply str (interpose " " args))) (:active i)) identity))))


;; (defn subtitle [x]
;;   (select (html-resource (java.io.StringReader. x)) [:h2 text-node]))

;; #_(defn nav-builder [current title link words]
;;   {:title title :href link :words words :active (if (= current link) "active" "")})

;; (defn collect-meta [])

;; (defn non-empty [str] (not (clojure.string/blank? str)))

;; (defn get-number [x] (if x (Integer. (or (re-find #"\d+" x) 100)) 0))

;; (declare update-element)

;; (defn same? [k v coll] (= v (k coll)))

;; (defn include [item coll]
;;   (let [k (first (keys item))
;;         v (k item)]
;;     (nil? k) coll
;;     (if (same? k v coll)
;;       (assoc-in coll [:children]
;;                 (update-element (first (:children item)) (:children coll)))
;;       nil)))
;; )

;; # Local Variables:
;; # lentic-init: lentic-org-clojure-init
;; # End:
