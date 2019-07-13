import java.util.HashSet;
import java.util.Set;

public class PredictTable {

    public final char[] digits = {'0','1','2','3','4','5','6','7','8','9'};
    public final char[] delimiters = {' ', '\n', '\t', ';', ','};
    public final char[] addops = {'+','-'};
    public final char[] multiops = {'*','/','%'};

    public static void addTo(char[] list, Set<Character> set){
        for(int i = 0;i < list.length;i++){
            set.add(list[i]);
        }
    }

    public Set<Character> predict(PredictEnum e){
        Set<Character> set = new HashSet<>();
        switch(e){
            case EXPRESSION:
                addTo(digits,set);
            case EXPRESSIONTAIL:
            case TERM:
            case TERMTAIL:
            case FACTOR:
            case NUMBER:
            case STRING:
            case METASTATEMENT:

        }



        return set;
    }
}
