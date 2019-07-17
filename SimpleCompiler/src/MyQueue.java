import java.util.LinkedList;

public class MyQueue<T> extends LinkedList<T> {

    public T pop(){
        return super.removeFirst();
    }

    public void push(T element){
        super.addLast(element);
    }
}
