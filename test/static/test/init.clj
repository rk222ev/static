(ns static.test.init
  (:require
   [clojure.test :refer :all]
   [static.io :as io]
   [static.filesystem :as fs]))

(defn init-fixture
  [f]
  (io/copy-init-folder {:title "test-init-folder"})
  (f)
  (fs/delete-directory "test-init-folder"))

(use-fixtures :once init-fixture)

(deftest init-generates-files
  (is (fs/directory? "test-init-folder"))
  (is (fs/file? "test-init-folder/config.clj"))
  (is (fs/directory? "test-init-folder/posts"))
  (is (fs/directory? "test-init-folder/public"))
  (is (fs/directory? "test-init-folder/site"))
  (is (fs/directory? "test-init-folder/templates")))
