(ns docs-gen.transform-test)
(comment
  (:require [clojure.test :refer :all]
            [docs-gen.view :refer :all]))

;** If starts with http leave it.
;** If starts with . append
;** If starts with / replace

(def root "http://nclverse.github.io")
(def subsite "code")
(def test-page "<html><body><div><a href=\"./punk.html\">Dirty Harry</a></div></body></html>")
(def test-page-2 "<html><body><div><a href=\"/punk.html\">Dirty Harry</a></div></body></html>")
(def test-page-without-href "<html><body><div><a>Dirty Harry</a></div></body></html>")
(def test-page-with-link "<html><head><link href=\"./css/styles.css\"/></head><body><div><a href=\"/punk.html\">Dirty Harry</a></div></body></html>")
(def test-page-with-image "<html><body><div><img src=\"/punk.html\" />Dirty Harry</div></body></html>")

(def transformed-page "<html><body><div><a href=\"http://nclverse.github.io/code/punk.html\">Dirty Harry</a></div></body></html>")
(def transformed-page-2 "<html><body><div><a href=\"http://nclverse.github.io/punk.html\">Dirty Harry</a></div></body></html>")
(def transformed-page-with-link "<html><head><link href=\"http://nclverse.github.io/code/css/styles.css\" /></head><body><div><a href=\"http://nclverse.github.io/punk.html\">Dirty Harry</a></div></body></html>")
(def transformed-page-without-href "<html><body><div><a>Dirty Harry</a></div></body></html>")
(def transformed-page-with-image "<html><body><div><img src=\"http://nclverse.github.io/punk.html\" />Dirty Harry</div></body></html>")

(deftest helper-test
  (testing "Inline URL transform"
    (let [ready-url-transform (partial url-transform root subsite)]
      (is (= (ready-url-transform "http://hello.com") "http://hello.com"))
      (is (= (ready-url-transform "./hello") (str root "/" subsite "/hello")))
      (is (= (ready-url-transform "/hello") (str root "/hello")))
      (is (= (ready-url-transform "bleh") "Malformed"))))

  (testing "Inline Root Test")
  (is (= (attach-root root subsite test-page) transformed-page))
  (is (= (attach-root root subsite test-page-2) transformed-page-2))
  (is (= (attach-root root subsite test-page-without-href) transformed-page-without-href))
  (is (= (attach-root root subsite test-page-with-link) transformed-page-with-link))
  (is (= (attach-root root subsite test-page-with-image) transformed-page-with-image)))

(deftest navtest
  (testing "Presence of all"
    (is (= (has-all-keys? {:category "nil" :subtitle "this" :title "false"}) true))
    (is (= (has-all-keys? {:category "nil"}) false))))
)
