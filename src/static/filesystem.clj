(ns static.filesystem
  (:require
   [clojure.java.io :as io])
  (:import
   (org.apache.commons.io FileUtils FilenameUtils)))

(defn as-url
  "Converts a file to a URL"
  [file]
  (io/as-url file))

(defn directory?
  "Returns true if the path is a directory; false otherwise."
  [^String path]
  (.isDirectory path))

(defn file
  "Converts a path to a file"
  [p & more]
  (apply io/file p more))

(defmulti file? class)

(defmethod file? String
  [path]
  (.isFile (io/file path)))

(defmethod file? java.io.File
  [file]
  (.isFile file))


(defn delete-file
  [path]
  "Remove a directory. Will throw an exception
    if it is not empty or if the file cannot be deleted."
  (io/delete-file path))

(defn resource
  [path]
  (io/resource path))

(defn copy
  "Copy a directory, preserving last modified times by default."
  [from to & {:keys [preserve] :or {preserve true}}]
  (let [from-file (io/file from)
        to-file (io/file to)]
    (cond
      (and (file? from-file) (file? to-file))
        (FileUtils/copyFile from-file to-file preserve)
      (and (file? from-file) (directory? to-file))
        (FileUtils/copyFileToDirectory from-file to-file preserve)
      (and (directory? from-file) (directory? to-file))
        (FileUtils/copyDirectoryToDirectory from-file to-file)
      :default
        (FileUtils/copyDirectory from-file to-file (boolean preserve)))))

(defn exists?
  "Returns true if path exists"
  [path]
  (.exists (io/file path)))

(defn safe-delete [file-path]
  (if (exists? file-path)
    (try
      (io/delete-file file-path)
      (catch Exception e (str "exception: " (.getMessage e))))
    false))

(defn delete-directory [directory-path]
  (let [directory-contents (file-seq (io/file directory-path))
        files-to-delete (filter #(file? %) directory-contents)]
    (doseq [file files-to-delete]
      (safe-delete (.getPath file)))
    (safe-delete directory-path)))

(defn move
  "Try to rename a file, or copy and delete if on another filesystem."
  [from to]
  (let [from-file (io/file from)
        to-file (io/file to)]
    (cond
      (and (file? from-file)
           (or (file? to-file) (not (exists? to-file))))
        (FileUtils/moveFile from-file to-file)
      :default
        (FileUtils/moveToDirectory from-file to-file true))))

(defn filename
  "Gets the base name, minus the full path and extension, from a full filename."
  [path]
  (FilenameUtils/getBaseName path))

(defn without-extension
  "Removes the extension from a filename."
  [path]
  (FilenameUtils/removeExtension path))

(defn normalize
  "Normalizes a path, removing double and single dot path steps."
  [path]
  (FilenameUtils/normalize path))

(defn create-directory
  "Will create a folder given a path"
  [path]
  (.mkdir (io/file path)))

(defn extension
  [path]
  (FilenameUtils/getExtension path))


(defn list-files
  [path extensions]
  (FileUtils/listFiles path extensions true))

(defn write-string
  [path content encoding]
  (FileUtils/writeStringToFile path content encoding))

(defn absolute-path
  [file]
  (.getAbsolutePath file))

(defn make-parents
  [path]
  (io/make-parents path))
