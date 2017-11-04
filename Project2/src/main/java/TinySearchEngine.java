import se.kth.id1020.TinySearchEngineBase;
import se.kth.id1020.util.Attributes;
import se.kth.id1020.util.Document;
import se.kth.id1020.util.Word;
import sun.tools.jar.resources.jar;

import java.util.*;

import static java.lang.String.valueOf;


public class TinySearchEngine implements TinySearchEngineBase {

    HashMap<String, IndexedItem> index = new HashMap<String, IndexedItem>();
    HashMap<Document, Integer> docWordCount = new HashMap<Document, Integer>();
    HashMap<Document, Double> docRel = new HashMap<Document, Double>();
    Stack<Boolean> orderby_relevance = new Stack<Boolean>();
    HashMap<String, Integer> docsPerWord = new HashMap<String, Integer>();
    int numberOfDocs = 0;

    public void insert(Word word, Attributes attributes) {
        // adds the first item in index
        if (index.size() == 0) {
            IndexedItem item = new IndexedItem(word, attributes);
            index.put(word.word, item);
            // wordCount increment
            docWordCount.put(attributes.document, 1);

        }
        else {
            // if the word is in the index already
            if (index.containsKey(word.word)) {
                index.get(word.word).attributes.add(attributes);
                Integer docCount = index.get(word.word).occurrenceCount.get(attributes.document);
                if (docCount == null)
                    docCount = 0;
                index.get(word.word).occurrenceCount.put(attributes.document, ++docCount);
            }
            // if the word is not in the index already
            else {
                IndexedItem item = new IndexedItem(word, attributes);
                index.put(word.word, item);
            }

            // wordCount increment
            if (docWordCount.get(attributes.document) == null){
                docWordCount.put(attributes.document, 1);
            }
            else{
                docWordCount.put(attributes.document, docWordCount.get(attributes.document)+1);
            }
        }
    }

    public List<Document> search(String key){
        // | Collection arson orderby relevance
        // | Collection arson
        // | | arson Collection | Market metaphysical
        // + + | nightmare stone | metaphysical stuck + dark night
        // Market orderby relevance

        // noting if it should be a ordered search
        if (key.contains("orderby relevance") && orderby_relevance.size() < 1) {
            orderby_relevance.push(true);
            key = key.substring(0, key.indexOf("orderby relevance")); // removing search commands from the search key
        }

        List<Document> result = new ArrayList<Document>();
        Scanner keyScanner = new Scanner(new String(key));

        // document stack setup
        Stack<List<Document>> docStack = new Stack<List<Document>>();
        Stack<String> ops = new Stack<String>();
        while (keyScanner.hasNext()){
            String currentKey = keyScanner.next();
            if (currentKey.equals("+")) {
                ops.push("+");
            } else if (currentKey.equals("|")) {
                ops.push("|");
            } else if (currentKey.equals("-")) {
                ops.push("-");
            } else {
                // if it should be ordered by relevance the relevance number is added
                if (orderby_relevance.size() == 0) {
                    docStack.push(listOfDocs(currentKey, false));
                }
                else{
                    docStack.push(listOfDocs(currentKey, orderby_relevance.peek()));
                }
            }
        }

        // performing the search operations on the document lists
        while (ops.size() > 0){
            String op = ops.pop();
            if (op == "+"){
                docStack.push(intersection(docStack.pop(), docStack.pop()));
            } else if (op == "|") {
                docStack.push(union(docStack.pop(), docStack.pop()));
            } else if (op == ("-")) {
                docStack.push(difference(docStack.pop(), docStack.pop()));
            }
        }
        // sorting the result list by relevance
        if (orderby_relevance.size() != 0) {
            if (orderby_relevance.pop()) {
                result.addAll(docStack.pop());
                sortDocument_rel(result);
                while (orderby_relevance.size() > 0)
                    orderby_relevance.pop();
                return result;
            }
        }
        result.addAll(docStack.pop());
        return result;
    }

    private void sortDocument_rel(List<Document> result){
        Collections.sort(result, new Comparator<Document>() {
            public int compare(Document o1, Document o2) {
                double docRel_1 = docRel.get(o1);
                double docRel_2 = docRel.get(o2);

                if (docRel_1 == docRel_2)
                    return 0;
                else if (docRel_1 > docRel_2)
                    return 1;
                else
                    return -1;
            }
        });
    }

    public List<Document> listOfDocs (String key, Boolean rel) {
        List<Document> list = new ArrayList<Document>();

        if (index.containsKey(key)) {
            for (int i = 0; i < index.get(key).attributes.size(); i++) {

                // check if document is already added.
                if (!list.contains(index.get(key).attributes.get(i).document)) {
                    list.add(index.get(key).attributes.get(i).document);

                    if (docsPerWord.get(key) == null)
                        docsPerWord.put(key, 0);

                    docsPerWord.put(key, docsPerWord.get(key) + list.size());

                    // calculate document relevance
                    Document currentDoc = index.get(key).attributes.get(i).document;

                    if (rel){
                        if (docRel.get(currentDoc) == null)
                            docRel.put(currentDoc, 0.0);
                        docRel.put(currentDoc, (docRel.get(currentDoc) + tf_idf(key, currentDoc)));
                    }
                }
            }
            return list;
        }
        else
            return list;
    }

    private double tf_idf (String q, Document d){
        // calc tf
        double nqd = index.get(q).occurrenceCount.get(d); // number of times q appear in doc d
        double td = docWordCount.get(d); // total number of terms in d
        double tf = nqd/td;
        // calc idf
        double nD = docWordCount.size(); // total number of docs
        double nDq = docsPerWord.get(q); // number of docs containing q
        double idf = Math.log10((nD/nDq));

        return tf * idf;
    }

    private List<Document> difference(List<Document> listB, List<Document> listA) { // reversed order due to stack
        List<Document> resultDocs = new ArrayList<Document>();

        resultDocs.addAll(listA);

        for (Document docB : listB) {
            if (resultDocs.contains(docB)){
                resultDocs.remove(docB);
            }
        }
        return resultDocs;
    }

    private List<Document> intersection(List<Document> listA, List<Document> listB) {
        List<Document> resultDocs = new ArrayList<Document>();

        for (Document docA : listA) {
            if (listB.contains(docA) && !resultDocs.contains(docA)) {
                resultDocs.add(docA);
            }
        }

        for (Document docB : listB) {
            if (listA.contains(docB) && !resultDocs.contains(docB)) {
                resultDocs.add(docB);
            }
        }
        return resultDocs;
    }

    private List<Document> union(List<Document> listA, List<Document> listB) {
        List<Document> resultDocs = new ArrayList<Document>();

        resultDocs.addAll(listA);

        for (Document docB : listB) {
            if (!resultDocs.contains(docB)){
                resultDocs.add(docB);
            }
        }
        return resultDocs;
    }

    /*
    Diagnostic tools: Stop program and wait for user input; Print the index
     */

    public void sortDocs (List<Document> docList) {
        Collections.sort(docList);
    }

    public void promptEnterKey() {
        System.out.println("Press \"ENTER\" to continue...");
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
    }
}

