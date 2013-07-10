import scala.io.Source
import scala.collection.JavaConverters._
import org.atilika.kuromoji._

package par.core {
  object Par {
    def main(args:Array[String]):Unit = {
      wordCount(args(0), args(1)).foreach(
        item => println("%s : %s".format(item.word, item.count))
      )
    }
    def wordCount(file:String, enc:String):List[Item] = {
      val tokenizer = Tokenizer.builder.build
      val text = Source.fromFile(file, enc).getLines.toList

      text.par
          .flatMap(line => tokenizer.tokenize(line).asScala)
          .filter(token => List("名詞", "動詞", "形容詞", "副詞").contains(token.getPartOfSpeech.split(",")(0)))
          .map(token => if (token.isKnown) token.getBaseForm else token.getSurfaceForm)
          .groupBy(s => s)
          .toList
          .par
          .map(entry => Item(entry._1, entry._2.length))
          .toList
          .sortWith(_.count > _.count)
    }
    case class Item(word:String, count:Int)
  }
}
