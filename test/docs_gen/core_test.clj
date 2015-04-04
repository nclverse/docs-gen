(ns docs-gen.core-test
  (:require [clojure.test :refer :all]
            [docs-gen.core :refer :all] :reload))

(def org-content "#+options: toc:nil num:nil\n* Hello there")
(def org-content-2 "#+options: toc:nil num:nil\n* Hello there, [[./hello/there.html][world!]]")
(def org-output "<div id=\"outline-container-sec-1\" class=\"outline-1\">\n<h1 id=\"sec-1\">Hello there</h1>\n</div>\n")

(def org-output-2
"<div id=\"outline-container-sec-1\" class=\"outline-1\">\n<h1 id=\"sec-1\">Hello there, <a href=\"./hello/there.html\">world!</a></h1>\n</div>\n")

(def root "http://localhost:3000")
(def site "code")
(def urls ["this/is/it" "this/is/so-good" "that/was/it"])
(def test-pages [org-output org-output org-output-2])

#_(deftest core
  (let [org-pages (keys (get-org-files (:org-files config)))]
    (testing "Collecting Pages"
      (is (= (every? #(.endsWith % ".org") org-pages))))
    (testing "Index Page Exists"
      (is (true? (some #(= % "/index.org") org-pages))))
    #_(testing "Transforms"
        (is (= (map modify-url ["index.org" "something/else.org"])
               ["index.html" "something/else.html"]))
        (is (= (org->html org-content) org-output)))
    (testing "Recombination"
      (is (= (weave-meta-data ["index.org" "some/page.org" "some/other/page.org"]
                                   (map org->html [org-content org-content org-content-2]) "root")
             [{:title "Hello there" :page org-output :word-count 2 :site "root"}
                   {:title "Hello there" :page org-output :word-count 2 :site "root"}
                   {:title "Hello there, " :page org-output-2 :word-count 3 :site "root"}])))))

(deftest renderers
  (let [nav (list {:category "this" :title "is" :subtitle "it"} 
                  {:category "this" :title "is" :subtitle "so-good"} 
                  {:category "that" :title "was" :subtitle "it"})
    weaved-pages (list
                  {:page org-output :url "this/is/it" :meta {:title "Hello there" :word-count 2}}
                  {:page org-output :url "this/is/so-good" :meta {:title "Hello there" :word-count 2}}
                  {:page org-output-2 :url "that/was/it" :meta {:title "Hello there, " :word-count 3}})]
  (testing "URL Components"
    (is (= (map url-components urls) nav)))
  (testing "Weaving"
      (is (= (map collect-meta-data urls test-pages) weaved-pages)))))
    
    



