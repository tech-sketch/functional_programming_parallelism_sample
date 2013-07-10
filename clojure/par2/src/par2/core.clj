(ns par2.core
  (:require [clojure.java.io :as io]
            [clojure.string :as cs]
            [clojure.core.reducers :as r])
  (:import  [org.atilika.kuromoji Tokenizer Token])
  (:gen-class))

(defn readFile [file enc]
  (with-open [rdr (io/reader file :encoding enc)]
    (doall (line-seq rdr))))

(defn morphological [lines]
  (let [tokenizer (.build (Tokenizer/builder))
        tokens    (r/flatten (r/map #(into [] (.tokenize tokenizer %)) lines))
        parts     ["名詞" "動詞" "形容詞" "副詞"]]
    (into [] (r/map (fn [token] (if (.isKnown token) (.getBaseForm token) (.getSurfaceForm token)))
         (r/filter (fn [token] (some #(= (first (cs/split (.getPartOfSpeech token) #",")) %) parts)) tokens)))))

(defn wordCount [words]
  (sort-by second > (map #(vector (first %) (count (second %))) (group-by str words))))

(defn -main [file enc]
  (let [lines (readFile file enc)]
    (println (apply str (map #(str (first %) ":" (second %) "\n") (wordCount (morphological lines)))))))
