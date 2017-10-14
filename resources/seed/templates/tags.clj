{:title "Tags"}

[:div
 [:h3 "Tags"]
 (map
  (fn[t]
    (let [[tag posts] t]
      [:div
       [:a {:name tag} tag]
       [:ul (map #(let [[url title] %]
                    [:li
                     [:a {:href url} title]])
                 posts)]]))
  (tag-map))]
