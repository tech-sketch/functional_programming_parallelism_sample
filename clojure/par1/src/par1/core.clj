(ns par1.core
  (:require [clojure.java.io :as io]
            [clojure.string :as cs])
  (:import  [org.atilika.kuromoji Tokenizer Token])
  (:gen-class))

(defn readFile [file enc]
  (with-open [rdr (io/reader file :encoding enc)]
    (doall (line-seq rdr))))

(defn morphological [lines]
  (let [tokenizer (.build (Tokenizer/builder))
        tokens    (flatten (pmap #(into [] (.tokenize tokenizer %)) lines))
        parts     ["名詞" "動詞" "形容詞" "副詞"]]
    (pmap (fn [token] (if (.isKnown token) (.getBaseForm token) (.getSurfaceForm token)))
         (filter (fn [token] (some #(= (first (cs/split (.getPartOfSpeech token) #",")) %) parts)) tokens))))

(defn wordCount [words]
  (sort-by second > (pmap #(vector (first %) (count (second %))) (group-by str words))))

(defn -main [file enc]
  (let [lines (readFile file enc)]
    (println (apply str (pmap #(str (first %) ":" (second %) "\n") (wordCount (morphological lines)))))
    (shutdown-agents)))
