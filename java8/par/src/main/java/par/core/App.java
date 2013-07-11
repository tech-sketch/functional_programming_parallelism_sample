package par.core;

import java.io.IOException;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.Arrays;
import java.util.List;

import java.util.stream.Collectors;

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

    List<Item> result = text.stream().parallel()
      .flatMap(line -> tokenizer.tokenize(line).stream().parallel())
      .filter(token -> 
        Arrays.asList("名詞", "動詞", "形容詞", "副詞").contains(token.getPartOfSpeech().split(",")[0]))
      .map(token -> (token.isKnown()) ? token.getBaseForm() : token.getSurfaceForm())
      .collect(Collectors.groupingByConcurrent(String::toString))
      .entrySet()
      .stream().parallel()
      .map(entry -> new Item(entry.getKey(), entry.getValue().size()))
      .sorted((l, r) -> r.count - l.count)
      .collect(Collectors.toList());

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
