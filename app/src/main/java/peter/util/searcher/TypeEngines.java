package peter.util.searcher;

import java.io.Serializable;
import java.util.ArrayList;

public class TypeEngines<T> implements Serializable {
        public String title;
        public ArrayList<T> item;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public ArrayList<T> getItem() {
            return item;
        }

        public void setItem(ArrayList<T> item) {
            this.item = item;
        }
    }