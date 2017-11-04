import se.kth.id1020.util.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Union extends TinySearchEngine{

    private Scanner keyscanner;

    List<Document> listA = new ArrayList<Document>();
    List<Document> listB = new ArrayList<Document>();
    List<Document> listRes = new ArrayList<Document>();

    public Union(Scanner keyscanner){
        this.keyscanner = keyscanner;
    }

    public List<Document> union() {
        listA = search(keyscanner.next());
        listB = search(keyscanner.next());

        listRes.addAll(listA);

        for (Document docB : listB) {
            if (!listRes.contains(docB)){
                listRes.add(docB);
            }
        }
        return listRes;
    }

    private List<Document> getList(){
        return listRes;
    }
}
