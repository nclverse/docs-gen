;; #+options: toc:nil num:nil

;; * Namespaces
;;   
;; #+BEGIN_SRC clojure

(ns docs-gen.transform
  (:require [prone.debug :refer [debug]])
  (:use net.cgrand.enlive-html))

;; #+END_SRC

;; * Layout Helpers

;;   
;; ** Build Drop Down
;;    Builds the site dropdown

;; #+BEGIN_SRC clojure

(defn build-drop-down [site sites]
(let [websites (list "code" "dissertation")]
  ;Inorder to maintain site always as the first element in the row
  ;Alert: Pretty hacky
  (clone-for [s (into [site] (remove #(= % site) sites))]
             [:a] (if (= s site) (do-> (add-class "active")
                                       (set-attr :href (str "/" s))
                                       (content (clojure.string/capitalize s)))
                      (do-> (set-attr :href (str "/" s)) (content (clojure.string/capitalize s)))))))
;; #+END_SRC

;; * Enlive Helpers

;; ** Update Attribute Helper

;; #+BEGIN_SRC clojure

(defn update-attr [attr f & args]
  (fn [node]
    (if (get (:attrs node) attr)
    (apply update-in node [:attrs attr] f args) node)))

;; #+END_SRC

;; * Transform Inline URLs
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

;; * Navigation Helpers
;; #+BEGIN_SRC clojure
   
(defn matches [k v m] (filter #(= (k %) v) m))

(defn sum-wc [k v t-map]
  {:pre [(every? #(contains? % :loc) t-map)]}
  (reduce + (map :loc (matches k v t-map))))

;; #+END_SRC

;; * Check for presence of all

;;    Duct tape functions.

;; #+BEGIN_SRC clojure

(def clean-numbering #(clojure.string/replace % #"\d+-" ""))

;; #+END_SRC


;; *** Format Navigation Links

;; Helper functions to strip all numbers followed by a single hyphen. Used to format the
;; URLs which have Kebab case.

;; #+BEGIN_SRC clojure

(defn format-nav-links [word]
  (clojure.string/join " " (map clojure.string/capitalize (clojure.string/split (clean-numbering word) #"-"))))
  
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

;; * Build Subtitles

;; #+BEGIN_SRC clojure

(defn build-subtitles [titles title current-subtitle site]
  (let [subtitles (matches :title title titles)]
    (clone-for [s subtitles]
               [:li :a.subtitle-link] (do-> (set-attr :href (rebuild-url site s))
                                            (if (= (:subtitle s) current-subtitle)
                                            (update-attr :class
                                            (fn [& args]
                                            (apply str (interpose " " args))) "active") identity)
                                            (content (format-nav-links (:subtitle s))
                                                     (html [:span.count (str (:loc s))]))))))

;; #+END_SRC

;; * Build Titles

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

;; * Build Categories

;;    Selecting only the valid categories. This means that index.html in
;;    the root directory and such other ones without a leaf node will be omitted.
;;    
;; #+BEGIN_SRC clojure

(defn build-categories [link nav site]
  (let [leaves (leaf-nodes nav)
        categories (map :category leaves)]
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
(defsnippet sidebar "templates/sidebar.html" [:nav] [current-url site sites nav page]
  [:img.site-logo] (set-attr :src "./img/logo.svg")
  [:ul.docs-site :li] (build-drop-down site sites)
  [:div.category] (build-categories current-url nav site))
;; #+END_SRC


;; #+BEGIN_SRC clojure

(deftemplate layout "templates/layout.html" [site sites nav page]
  [:title] (content (:title (:meta page)))
  [:#sidebar]  (content (sidebar (:url page) site sites nav page))
  [:#container :#read-area] (content (article (:page page))))

;; #+END_SRC

;; ** Transform Page

;; #+BEGIN_SRC clojure

 (defn transform-page [root site sites nav page]
     (attach-root root site (apply str (layout site sites nav page))))

;; #+END_SRC

;; # Local Variables:
;; # lentic-init: lentic-org-clojure-init
;; # End:
