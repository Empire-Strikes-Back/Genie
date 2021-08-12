(ns genie.core
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >! <!! >!!  take! put! offer! poll! alt! alts! close! onto-chan!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.string]
   [clojure.java.io :as io]

   [clojure.tools.build.api :as build.api]))

(do (set! *warn-on-reflection* true) (set! *unchecked-math* true))

(defn process
  [opts]
  (let [default-opts {:uberjar-name "out/program.standalone.jar"
                      :main-ns nil}
        process-dir (-> (io/file (System/getProperty "user.dir")) (.getCanonicalPath))
        {:keys [uberjar-name main-ns]} (merge default-opts opts)]
    (let [class-dir "out/classes"
          version "1"
          basis (build.api/create-basis {:project "deps.edn"})]
      (build.api/copy-dir
       {:src-dirs ["src"]
        :target-dir class-dir})
      (build.api/compile-clj {:basis basis
                              :src-dirs ["src"]
                              :class-dir class-dir
                              :compile-opts {:direct-linking true}})
      (build.api/uber
       {:class-dir class-dir
        :uber-file uberjar-name
        :basis basis
        :main (-> main-ns (str) (clojure.string/replace #"-" "_") (symbol))
        :manifest {"Manifest-Version" "1"
                   "Created-By" "library"}}))))