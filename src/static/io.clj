(ns static.io
  (:require
   [clojure.core.memoize :refer [memo]]
   [clojure.java.shell :as sh]
   [clojure.tools.logging :as log]
   [clojure.string :as str]
   [cssgen :as css-gen]
   [hiccup.core :as hiccup]
   [static.config :as config]
   [static.filesystem :as fs]
   [stringtemplate-clj.core :as string-template]
   [stencil.core :as stencil]
   [cpath-clj.core :as cp])
  (:import
   (org.pegdown PegDownProcessor)))

(defn- split-file [content]
  (let [idx (.indexOf content "---" 4)]
    [(.substring content 4 idx) (.substring content (+ 3 idx))]))

(defn- prepare-metadata [metadata]
  (reduce (fn [h [_ k v]]
            (let [key (keyword (.toLowerCase k))]
              (cond
                (= :link key) (assoc h :links (conj (into [] (:links h)) v))
                (not (h key)) (assoc h key v)
                :else h)))
          {} (re-seq #"([^:#\+]+): (.+)(\n|$)" metadata)))

(defn- read-markdown [file]
  (let [[metadata content]
        (split-file (slurp file :encoding (:encoding (config/config))))]
    [(prepare-metadata metadata)
     (delay (.markdownToHtml (PegDownProcessor. org.pegdown.Extensions/TABLES) content))]))

(defn- read-html [file]
  (let [[metadata content]
        (split-file (slurp file :encoding (:encoding (config/config))))]
    [(prepare-metadata metadata) (delay content)]))

(defn read-org [file]
  (let [metadata (prepare-metadata
                  (apply str
                         (take 500 (slurp file :encoding (:encoding (config/config))))))
        content (delay
                 (:out (sh/sh "emacs"
                           "-batch" "-eval"
                           (str
                            "(progn "
                            (apply str (map second (:emacs-eval (config/config))))
                            " (find-file \"" (fs/absolute-path file) "\") "
                            (:org-export-command (config/config))
                            ")"))))]
    [metadata content]))

(defn- read-clj [file]
  (let [[metadata & content] (read-string
                              (str \( (slurp file :encoding (:encoding (config/config))) \)))]
    [metadata (delay (binding [*ns* (the-ns 'static.core)]
                       (->> content
                            (map eval)
                            last
                            hiccup/html)))]))

(defn- read-cssgen [file]
  (let [metadata {:extension "css" :template :none}
        content (read-string
                 (slurp file :encoding (:encoding (config/config))))
        to-css  #(str/join "\n" (doall (map css-gen/css %)))]
    [metadata (delay (binding [*ns* (the-ns 'static.core)] (-> content eval to-css)))]))

(defn read-doc [f]
  (let [extension (fs/extension (str f))]
    (cond (or (= extension "markdown") (= extension "md"))
          (read-markdown f)
          (= extension "md") (read-markdown f)
          (= extension "org") (read-org f)
          (= extension "html") (read-html f)
          (= extension "clj") (read-clj f)
          (= extension "cssgen") (read-cssgen f)
          :default (throw (Exception. "Unknown Extension.")))))

(defn dir-path [dir]
  (cond (= dir :templates) (str (:in-dir (config/config)) "templates/")
        (= dir :public) (str (:in-dir (config/config)) "public/")
        (= dir :site) (str (:in-dir (config/config)) "site/")
        (= dir :posts) (str (:in-dir (config/config)) "posts/")
        :default (throw (Exception. "Unknown Directory."))))

(defn list-files [d]
  (let [d (fs/file (dir-path d))]
    (if (fs/directory? d)
      (sort
       (fs/list-files d (into-array ["markdown"
                                     "md"
                                     "clj"
                                     "cssgen"
                                     "org"
                                     "html"]))) [] )))

(def read-template
  (memo
   (fn [template]
     (let [extension (fs/extension (str template))]
       (cond (= extension "clj")
             [:clj
              (-> (str (dir-path :templates) template)
                  (fs/file)
                  (#(str \(
                         (slurp % :encoding (:encoding (config/config)))
                         \)))
                  read-string)]
             :default
             [:html
              (string-template/load-template (dir-path :templates) template)])))))

(defn write-out-dir [file str]
  (fs/write-string
   (fs/file (:out-dir (config/config)) file) str (:encoding (config/config))))

(defn deploy-rsync [rsync out-dir host user deploy-dir]
  (let [cmd [rsync "-avz" "--delete" "--checksum" "-e" "ssh"
             out-dir (str user "@" host ":" deploy-dir)]]
    (log/info (:out (apply sh/sh cmd)))))

(defn- walk [file]
  (doall (filter #(fs/file? %) (file-seq file))))

(def ^:private seed-folder "seed")

(defn- render-file
  [URI options]
  (fs/make-parents (str (System/getProperty "user.dir") "/" (:title options) URI))
  (spit (str (:title options) URI)
        (stencil/render-file (str seed-folder URI) options)))

(defn copy-init-folder
  [options]
  (let [base (cp/resources (fs/resource seed-folder))]
    (doseq [path (map first base)]
      (render-file path options))))
