(defproject docs-gen "0.1.0-SNAPSHOT"
  :description "Docs Generator"
  :url "http://nclverse.github.io/"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [stasis "2.2.2"]
                 [ring "1.3.2"]
                 [clj-pdf "2.0.0"]
                 [org.tobereplaced/nio.file "0.3.1"]
                 [prone "0.8.1"]
                 [enlive "1.1.5"]]
  :plugins [[lein-ring "0.9.2"]]
  :aliases {"export" ["run" "-m" "docs-gen.render/exporter"]}
  :ring {:handler docs-gen.render/app :auto-reload? true :auto-refresh? true :reload-paths ["src" "resources"]}
  :profiles {:dev {:ring {:stacktrace-middleware prone.middleware/wrap-exceptions}}})
