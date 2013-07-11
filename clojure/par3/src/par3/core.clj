(ns par3.core
  (:require [clojure.java.io :as io]
            [clojure.string :as cs])
  (:import  [org.atilika.kuromoji Tokenizer Token])
  (:gen-class))

(defn readFile [file enc]
  (with-open [rdr (io/reader file :encoding enc)]
    (doall (line-seq rdr))))

(defn morphological [lines]
  (let [tokenizer (.build (Tokenizer/builder))
        tokens    (flatten (map #(into [] (.tokenize tokenizer %)) lines))
        parts     ["名詞" "動詞" "形容詞" "副詞"]]
    (map (fn [token] (if (.isKnown token) (.getBaseForm token) (.getSurfaceForm token)))
         (filter (fn [token] (some #(= (first (cs/split (.getPartOfSpeech token) #",")) %) parts)) tokens))))

(defn combiner [words]
  (map #(vector (first %) (count (second %))) (group-by str words)))

(defn wordCount [futures]
  (let [combined (apply concat (map deref futures))]
    (sort-by second > (map (fn [w] (vector (first w) (reduce + (map #(second %) (second w))))) (group-by #(first %) combined)))))

(defn -main [file enc]
  (let [nthreads (.. Runtime getRuntime availableProcessors)
        lines (readFile file enc)
        fragments (partition-all (quot (count lines) nthreads) lines)
        futures (map #(future (combiner (morphological %))) fragments)]
    (println (apply str (map #(str (first %) ":" (second %) "\n") (wordCount (doall futures)))))
    (shutdown-agents)))
