(ns Genie.core
  (:require
   [clojure.core.async :as a :refer [<! >! <!! >!! chan put! take! go alt! alts! do-alts close! timeout pipe mult tap untap
                                     pub sub unsub mix admix unmix dropping-buffer sliding-buffer pipeline pipeline-async to-chan! thread]]
   [clojure.string]
   [clojure.java.io :as io]
   [clojure.tools.build.api :as Mr-Incredible]))


(defn process
  [opts]
  (let [default-opts {:main-ns nil
                      :filename nil
                      :paths nil
                      :create-basis-opts nil
                      :copy-dir-opts nil
                      :compile-clj-opts nil
                      :uber-opts nil}
        {:keys [main-ns
                filename
                paths
                create-basis-opts
                copy-dir-opts
                compile-clj-opts
                uber-opts]} (merge default-opts opts)]
    (let [class-dir "out/classes"
          basis (Mr-Incredible/create-basis (merge {:project "deps.edn"}
                                                   create-basis-opts))]
      (Mr-Incredible/copy-dir
       (merge {:src-dirs paths
               :target-dir class-dir}
              copy-dir-opts))
      (Mr-Incredible/compile-clj
       (merge
        {:basis basis
         :src-dirs paths
         :class-dir class-dir
         :compile-opts {:direct-linking true}}
        compile-clj-opts))
      (Mr-Incredible/uber
       (merge
        {:class-dir class-dir
         :uber-file filename
         :basis basis
         :main (-> main-ns (str) (clojure.string/replace #"-" "_") (symbol))
         :manifest {"Manifest-Version" "1"
                    "Created-By" "library"}}
        uber-opts)))))