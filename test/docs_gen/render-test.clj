;(comment
;(ns docs-gen.templates-test
;  (:require [clojure.test :refer :all]
;            [blog-gen.templates :refer :all]))
;
;(def page-urls (list "/this" "/is" "/some" "urls"))
;
;(def page-content (list "Some" "Content" "Goes" "Here and There"))
;
;(def org-content "#+options: toc:nil num:nil\n* Hello there")
;
;(def pages (zipmap page-urls page-content))
;
;(def stereotypical-link "/category/01-title/02-subtitle.org")
;
;(deftest sometest
;  (testing "Org Helpers"
;    (is (= (word-count "Hello") 1))
;    (is (= (map word-count page-content) (list 1 1 1 3)))
;    (is (= (org->html org-content)
;           "<div id=\"outline-container-sec-1\" class=\"outline-1\">\n<h1 id=\"sec-1\">Hello there</h1>\n</div>\n")))
;  (testing "Link Helpers"
;  (is (= (strip-org stereotypical-link) "/category/01-title/02-subtitle"))
;  (is (= (clean-link stereotypical-link) "/category/title/subtitle/")))
;  (testing "Templates"
;    (is (= ((build-drop-down "Dissertation") ["Dissertation" "Test"]) "hello"))))
;  
;)
