package nonpar.core;

import java.io.IOException;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.Collections;
import java.util.Comparator;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

import org.atilika.kuromoji.Token;
import org.atilika.kuromoji.Tokenizer;

public class App {
  public static void main(String[] args) {
    for (Item item : wordCount(args[0], args[1])) {
      System.out.format("%s : %d%n", item.word, item.count);
    }
  }
  private static List<Item> wordCount(String file, String enc) {
    Tokenizer tokenizer = Tokenizer.builder().build();
    List<String> text;
    try {
      text = Files.readAllLines(Paths.get(file), Charset.forName(enc));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    Map<String, Integer> words = new HashMap<String, Integer>();
    for (String line : text) {
      for (Token token : tokenizer.tokenize(line)) {
        String pos = token.getPartOfSpeech().split(",")[0];
        if ("名詞".equals(pos) || "動詞".equals(pos) || "形容詞".equals(pos) || "副詞".equals(pos)) {
          String word = (token.isKnown()) ? token.getBaseForm() : token.getSurfaceForm();
          if (!words.containsKey(word)) {
            words.put(word, 1);
          } else {
            words.put(word, words.get(word) + 1);
          }
        }
      }
    }

    List<Map.Entry<String, Integer>> sortedWords = new LinkedList<Map.Entry<String, Integer>>(words.entrySet());
    Collections.sort(sortedWords, new Comparator<Map.Entry<String, Integer>>() {
      public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
        return o2.getValue() - o1.getValue();
      }
    });

    List<Item> result = new ArrayList<Item>();
    for(Map.Entry<String, Integer> entry : sortedWords) {
      result.add(new Item(entry.getKey(), entry.getValue()));
    }

    return result;
  }

  private static class Item {
    public String word;
    public int count;
    public Item(String word, int count) {
      this.word = word;
      this.count = count;
    }
  }
}
