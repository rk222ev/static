[:atomic-build false
 :author-email "{{author-email}}"
 :author-name "{{author}}"
 :blog-as-index nil
 :copyright-year 2017
 :create-archives true
 :create-rss true
 :default-extension "html"
 :default-template "default.clj"
 :encoding "UTF-8"
 :in-dir ""
 :org-export-command '(progn
                       (org-html-export-as-html nil nil nil t nil)
                       (with-current-buffer "*Org HTML Export*"
                         (princ (org-no-properties (buffer-string)))))
 :out-dir "html/"
 :post-out-subdir ""
 :posts-per-page 1
 :site-keywords "{{keywords}}"
 :site-description "{{description}}"
 :site-url "https://{{title}}.com/"
 :url-base ""
 :site-title "{{title}}"]
