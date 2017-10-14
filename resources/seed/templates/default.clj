(let
    [config (static.config/config)
     path-to #(str (:url-base (static.config/config)) %)
     post? (= (:type metadata) :post)]

  (html5
   [:head
    [:meta {:name "description", :content (if post?
                                            (:description metadata)
                                            (:site-description config))}]
    [:meta {:name "keywords", :content (if post?
                                         (:tags metadata)
                                         (:site-keywords config))}]
    [:meta {:name "author", :content (:author-name config)}]
    [:meta {:charset "UTF-8"}]
    [:meta {:name "viewport" :content "width-=device-width, initial-scale=1"}]
    [:title (:site-title config)]
    [:link {:rel "icon"
            :href (path-to "/images/rpkn-32x32.png")
            :type "image/png"}]
    [:link {:rel "apple-touch-icon-precomposed"
            :href (path-to "/images/rpkn-256x256.png")
            :type "image/png"}]
    [:link {:rel "stylesheet",
            :type "text/css",
            :href (path-to "/default.css")}]
    [:link {:rel "stylesheet",
            :type "text/css",
            :href (path-to "/fonts/cmun-serif.css")}]
    [:link
     {:rel "alternate",
      :type "application/rss+xml",
      :href (path-to "/rss-feed")}]
    (if post?
      [:link {:rel "canonical"
              :href (path-to (str (:site.url config) (:url metadata)))}])]
   [:body
    [:div
     {:id "wrap"}
     [:div
      {:id "header"}
      [:h1
       [:a
        {:href (path-to "/")}
        (:site-title config)]]
      [:ul
       {:id "navigation"}
       [:li
        [:a {:href (path-to "/" ), :class "page"} "Home"]]
       [:li
        [:a {:href (path-to "/archives.html"), :class "page"} "Archives"]]
       [:li
        [:a {:href (path-to "/tags/"), :class "page"} "Tags"]]
       [:li
        [:a {:href (path-to "/about.html"), :class "page" :rel "author"} "About"]]]
      [:div {:id "icons"}
       [:ul
        [:li
         [:a {:href "https://github.com/rk222ev"}
          [:img {:src (path-to "/images/GitHub-Mark-32px.png") :alt "My Github"}]]]
        [:li
         [:a {:href "https://twitter.com/ropkn" }
          [:img {:src (path-to "/images/TwitterLogo.png") :alt "My Twitter"}]]]]]]
     [:div
      {:id "content"}
      [:div
       {:id "post"}
       [:h1 (:title metadata)]
       content

       (if post?
         [:div {:class "post-tags"} "Tags: "
          (reduce
           (fn[h v]
             (conj h [:li  [:a {:href (path-to (str "/tags/#" v))} v]]))
           [:ul]
           (.split (:tags metadata) " "))])]

      (if post?
        [:div
         {:id "related"}
         [:h3 {:class "random-posts"} "Random Posts"]
         [:ul
          {:class "posts"}
          (map
           #(let [f %
                  url (static.core/post-url f)
                  [metadata _] (static.io/read-doc f)
                  date (static.core/parse-date
                        "yyyy-MM-dd" "dd MMM yyyy"
                        (re-find #"\d*-\d*-\d*" (str f)))]
              [:li [:span date " - " ] [:a {:href (path-to url)} (:title metadata) ]])
           (take 5 (shuffle (static.io/list-files :posts))))]])]

     [:div
      {:id "footer"}
      [:a {:href (path-to "/rss-feed")} " RSS Feed"]
      [:p
       [:a {:href (str "http://" (:site-title config) (:base-url config))} " Robin Karlsson"]]]]]))
