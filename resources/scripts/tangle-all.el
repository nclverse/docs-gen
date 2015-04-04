(require 'org)         ;; The org-mode goodness
(require 'ob)          ;; org-mode export system
(require 'ob-tangle)

(add-to-list 'org-babel-tangle-lang-exts '("clojure" . "clj"))

(defun get-files (path) (directory-files path))

(defun tangle-file (file)
  (find-file file)
  (org-babel-tangle)
  (kill-buffer))

(defun tangle-files (path)
  "Given a directory, PATH, of 'org-mode' files, tangle source code out of all literate programming files."
  (mapc 'tangle-file (file-expand-wildcards path)))

(tangle-files "../../src/blog_gen/*.org")

