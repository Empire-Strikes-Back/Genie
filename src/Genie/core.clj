(ns Genie.core
  (:require 
    [clojure.core.async :as a :refer [<! >! <!! >!! chan put! take! go alt! alts! do-alts close! timeout pipe mult tap untap 
                                      pub sub unsub mix admix unmix dropping-buffer sliding-buffer pipeline pipeline-async to-chan! thread]]
    [clojure.string]
    [clojure.java.io :as io]
    [clojure.tools.build.api :as build.api]
    ))


(defn process
  [opts]
  (let [default-opts {:main-ns nil
                      :filename nil
                      :paths nil}
        {:keys [main-ns filename paths]} (merge default-opts opts)]
  
    (let [class-dir "out/classes"
          basis (build.api/create-basis {:project "deps.edn"})]
      (build.api/copy-dir
        {:src-dirs paths
         :target-dir class-dir }
      )
      (build.api/compile-clj 
        {:basis basis
         :src-dirs paths
         :class-dir class-dir
         :compile-opts {:direct-linking true}
         }
      )
      (build.api/uber 
        {:class-dir class-dir
         :uber-file filename
         :basis basis
         :main (-> main-ns (str) (clojure.string/replace #"-" "_") (symbol))
         :manifest {"Manifest-Version" "1"
                    "Created-By" "library"}
         }
      )
    )
  )
)