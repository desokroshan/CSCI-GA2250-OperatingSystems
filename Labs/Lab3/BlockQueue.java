import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;


public class BlockQueue {
    Queue<Task> blockQueue = new LinkedList<Task>();

    public void print(){
        System.out.println("-----------------------------------Queue content-----------------------------------");
        ArrayList<Task> tmp = new ArrayList<Task>();
        while(!blockQueue.isEmpty()){
            tmp.add(blockQueue.poll());
        }
        for(int i=0;i<tmp.size();i++){
            System.out.print(tmp.get(i).id+"  ");
        }
        System.out.println();
        blockQueue.addAll(tmp);
    }
}
