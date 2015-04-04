(progn

(require 'org)         ;; The org-mode goodness
(require 'ob)          ;; org-mode export system

  (org-mode)
  (setq org-html-toplevel-hlevel 1)
(setq org-export-headline-levels 6)
  (with-output-to-temp-buffer "transient"
      (princ (car (last command-line-args))))
  (switch-to-buffer "transient")
  (org-html-export-as-html nil nil nil t nil)
  (princ (buffer-string)))
