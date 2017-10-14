(ns static.test.core
  (:require [clojure.test :refer :all]
            [static.core :refer :all]
            [static.io :refer :all]
            [static.filesystem :as fs]
            [static.test.dummy-fs :refer :all]))

(defn dummy-fs-fixture [f]
  (setup-logging)
  (create-dummy-fs)
  (create)
  (f)
  (fs/delete-directory (fs/file "resources/"))
  (fs/delete-directory (fs/file "html/")))


(use-fixtures :once dummy-fs-fixture)

(deftest test-markdown
  (let [[metadata content] (read-doc "resources/site/dummy.markdown")]
    (is (= "unit test"  (:tags metadata)))
    (is (= "some dummy desc" (:description metadata)))
    (is (= "dummy content" (:title metadata)))
    (is (= "Some dummy file for unit testing."
           (re-find #"Some dummy file for unit testing." @content)))))

(deftest test-cssgen
  (let [[metadata content] (read-doc "resources/site/style.cssgen")]
    (is (= "font-size: 1em;" (re-find #"font-size: 1em;" @content)))))

(deftest test-org
  (let [[metadata content] (read-doc (fs/file "resources/posts/2050-07-07-dummy-future-post-7.org"))]
    (is (= "org-mode org-babel"  (:tags metadata)))
    (is (= "Dummy org-mode post" (:title metadata)))
    (is (= "Sum 1 and 2" (re-find #"Sum 1 and 2" @content)))
    (is (= 2 (count (:links metadata))))))

(deftest test-clj
  (let [[metadata content] (read-doc (fs/file "resources/site/dummy_clj.clj"))]
    (is (= "Dummy Clj File" (:title metadata)))
    (is (= "Dummy Clj Content" (re-find #"Dummy Clj Content" @content)))
    (is (= "<h3>" (re-find #"<h3>" @content)))))

(deftest test-io
  (is (= (count (list-files :posts)) 8))
  (is (fs/exists? (fs/file "html/first-alias/index.html")))
  (is (fs/exists? (fs/file "html/a/b/c/alias/index.html")))
  (is (fs/exists? (fs/file "html/second-alias/index.html"))))

(deftest test-st-template
  (let [file (fs/file "html/html_template.html")
        content (slurp file)]
    (is (= "Dummy Html Post Template"
           (re-find #"Dummy Html Post Template" content)))
    (is (= "<title>Html Template Test</title>"
           (re-find #"<title>Html Template Test</title>" content)))))

(deftest test-rss-feed
  (let [rss (fs/file "html/rss-feed")
        content (slurp rss)]
    (is (= true (fs/exists? rss)))
    (is (= "<title>Dummy Site</title>"
           (re-find #"<title>Dummy Site</title>" content)))
    (is (= "<link>http://www.dummy.com</link>"
           (re-find #"<link>http://www.dummy.com</link>" content)))
    (is (= "<title>dummy future post 1</title>"
           (re-find #"<title>dummy future post 1</title>" content)))
    (is (= "http://www.dummy.com/2050/04/04/dummy-future-post-4/"
           (re-find #"http://www.dummy.com/2050/04/04/dummy-future-post-4/"
                    content)))))

(deftest test-site-map
  (let [sitemap (fs/file "html/sitemap.xml")
        content (slurp sitemap)]
    (is (= true (fs/exists? sitemap)))
    (is (= "<loc>http://www.dummy.com</loc>"
           (re-find #"<loc>http://www.dummy.com</loc>" content)))
    (is (= "http://www.dummy.com/2050/01/01/dummy-future-post-1/"
           (re-find #"http://www.dummy.com/2050/01/01/dummy-future-post-1/"
                    content)))
    (is (= "<loc>http://www.dummy.com/dummy.html</loc>"
           (re-find #"<loc>http://www.dummy.com/dummy.html</loc>"
                    content)))))

(deftest test-rss-feed
  (let [tags (fs/file "html/tags/index.html")
        content (slurp tags)]
    (is (= 5 (count ((tag-map) "same"))))
    (is (= true (fs/exists? tags)))
    (is (= "<a name=\"e4e8\">e4e8</a>"
           (re-find #"<a name=\"e4e8\">e4e8</a>" content)))
    (is (= "<a href=\"/2050/01/01/dummy-future-post-1/\">"
           (re-find #"<a href=\"/2050/01/01/dummy-future-post-1/\">"
                    content)))))

(deftest test-latest-posts
  (let [page (fs/file "html/latest-posts/0/index.html")]
    (is (= true (fs/exists? page)))))

(deftest test-archives
  (let [index (fs/file "html/archives/index.html")
        a-2050-01 (fs/file "html/archives/2050/01/index.html")]
    (is (= true (fs/exists? index)))
    (is (= true (fs/exists? a-2050-01)))))

(deftest test-process-posts
  (let [post1 (fs/file "html/2050/02/02/dummy-future-post-2/index.html")
        post2 (fs/file "html/2050/04/04/dummy-future-post-4/index.html")]
    (is (= true (fs/exists? post1)))
    (is (= true (fs/exists? post2)))))

(deftest test-process-site
  (let [html (fs/file "html/dummy.html")
        static (fs/file "html/dummy.static")]
    (is (= true (fs/exists? html)))
    (is (= true (fs/exists? static)))
    (is (= "Some dummy file for unit testing."
           (re-find #"Some dummy file for unit testing." (slurp html))))
    (is (= "Hello, World!!" (re-find #"Hello, World!!" (slurp static))))))
