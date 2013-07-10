(ns par3.core
  (:require [clojure.java.io :as io]
            [clojure.string :as cs])
  (:import  [org.atilika.kuromoji Tokenizer Token]
            [java.util.concurrent Executors])
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

(defn wordCount [words]
  (sort-by second > (map #(vector (first %) (count (second %))) (group-by str words))))

(defn dopar [f lines]
  (let [nthreads (+ 2 (.. Runtime getRuntime availableProcessors))
        result (ref [])
        chunks (partition-all (quot (count lines) nthreads) lines)
        pool (Executors/newFixedThreadPool nthreads)
        tasks (map (fn [cnk] (fn [] (dosync (alter result conj (f cnk))))) chunks)]
    (doseq [future (.invokeAll pool tasks)]
      (.get future))
    (.shutdown pool)
    (flatten @result)))

(defn -main [file enc]
  (let [lines (readFile file enc)]
    (println (apply str (map #(str (first %) ":" (second %) "\n") (wordCount (dopar morphological lines)))))))
