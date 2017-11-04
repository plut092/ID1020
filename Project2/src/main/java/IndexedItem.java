import se.kth.id1020.util.Attributes;
import se.kth.id1020.util.Document;
import se.kth.id1020.util.Word;

import java.util.ArrayList;
import java.util.HashMap;

public class IndexedItem{
    Word word;
    ArrayList<Attributes> attributes = new ArrayList<Attributes>();
    HashMap<Document, Double> docRelevance = new HashMap<Document, Double>();
    HashMap<Document, Integer> occurrenceCount = new HashMap<Document, Integer>();
    int wordCount = 0;

    public int compare(IndexedItem o1, IndexedItem o2) {
        if (o1.word.word.compareTo(o2.word.word)        == 0)   return 0;
        else if (o1.word.word.compareTo(o2.word.word)   < 0)    return -1;
        else return 1;
    }

    public IndexedItem(Word word, Attributes attributes){
        this.word = word;
        this.attributes.add(attributes);
        this.occurrenceCount.put(attributes.document, 1);
    }
}
