;; * Namespaces

;; #+BEGIN_SRC clojure

(ns docs-gen.collect-test
  (:require [clojure.test :refer :all]
            [docs-gen.collect :refer :all] :reload))
;; #+END_SRC

;; * Configuration Helpers

;; #+BEGIN_SRC clojure

(deftest config-helpers
  (testing "Sites"
    (letfn [(contains-key [keys] (fn [val] (some #(= % val) keys)))]
      (is (every? (contains-key ["code" "dissertation" "design" "docs"]) (collect-vals :sites config))))))
;; #+END_SRC

;; #+BEGIN_SRC clojure
(defn contains-all-keys [coll keys]
  (every? #(contains? coll %) keys))
;; #+END_SRC

;; #+BEGIN_SRC clojure
(deftest contains-all-test
  (let [coll {:life 0
              :universe 1
              :everything "values"
              :answer 42}]
  (is (contains-all-keys coll [:life :universe :everything]))
  (is (not (contains-all-keys coll [:42])))))

;; #+END_SRC

;; #+BEGIN_SRC clojure

(deftest collection-test
  (testing "All formats present"
    (let [supported (:supported-formats config)]
  (is (every? (complement nil?) (map #(scan-format %) supported)))
  (is (every? (complement nil?) (map #(asset-key %) supported))))))

;; #+END_SRC      

;; #+BEGIN_SRC clojure

(deftest asset-test
  (let [source (first (collect-vals :source config))
        file-locs (keys (get-assets "css" source))
        files (vals (get-assets "css" source))]
  (testing "Get assets"
    (is (if ((complement empty?) files) (every? true? (map #(.endsWith % ".css") files)))))
(testing "Place Assets"
(is (place-asset ("css" source "code"))
{:stylesheets
(map (fn [loc file] {(str "/code/css/" loc) file}))}))))

;; #+END_SRC

;; #+BEGIN_SRC clojure





;; #+END_SRC



;; #_(deftest transform-tests
;;   (testing "Org Stripping"
;;     (is (= (replace-org "http://test.com/hello.org" "") "http://test.com/hello"))
;;     (is (= (replace-org "http://test.com/hello.org" ".html") "http://test.com/hello.html"))
;;     (is (= (replace-org "http://dont.do/anything" "") "http://dont.do/anything")))
;;   (testing "Clean Numbering"
;;     (is (= (clean-numbering "00-hello") "hello"))
;;     (is (= (clean-numbering "0-there-0-hello") "there-hello")))
;;   (testing "Format Nav Links"
;;     (is (= (format-nav-links "00-hello-there") "Hello There"))))

;; #_(deftest renderers
;;   (let [nav (list {:category "this" :title "is" :subtitle "it"} 
;;                   {:category "this" :title "is" :subtitle "so-good"} 
;;                   {:category "that" :title "was" :subtitle "it"})
;;     weaved-pages (map #(assoc %1 :url %2) (list
;;                   {:page org-output :meta {:title "Hello there"}}
;;                   {:page org-output :meta {:title "Hello there"}}
;;                   {:page org-output-2 :meta {:title "Hello there, "}}) nav)]
;;   (testing "URL Components"
;;     (is (= (map url-components urls) nav)))
;;   (testing "Weaving"
;;       (is (= (map collect-meta-data urls test-pages) weaved-pages)))))
;;     
;; (comment
;;   (def org-content "#+options: toc:nil num:nil\n* Hello there")
;;   (def org-content-2 "#+options: toc:nil num:nil\n* Hello there, [[./hello/there.html][world!]]")
;;   (def org-output "<div id=\"outline-container-sec-1\" class=\"outline-1\">\n<h1 id=\"sec-1\">Hello there</h1>\n</div>\n")

;;   (def org-output-2
;;     "<div id=\"outline-container-sec-1\" class=\"outline-1\">\n<h1 id=\"sec-1\">Hello there, <a href=\"./hello/there.html\">world!</a></h1>\n</div>\n")

;;   (def root "http://localhost:3000")
;;   (def site "code")
;;   (def urls ["this/is/it" "this/is/so-good" "that/was/it"])
;;   (def test-pages [org-output org-output org-output-2]))

;; # Local Variables:
;; # lentic-init: lentic-org-clojure-init
;; # End:
