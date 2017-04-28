import java.io.*;
import java.util.*;


class Main{

  public static Page [] frameTable;
  public static int M;
  public static int P;
  public static int S;
  public static int J;
  public static int N;
  public static String R;
  public static int D;
  public static void main(String [] args){
    
    //Read the Input
    if (args.length<6){ 
     System.out.println("Porgram needs 6 arguments\n");
     System.exit(0);
    }
    Queue<Process> done = new LinkedList<Process>();
    M = Integer.parseInt(args[0]);
    P = Integer.parseInt(args[1]);
    S = Integer.parseInt(args[2]);
    J = Integer.parseInt(args[3]);
    N = Integer.parseInt(args[4]);
    String R = args[5];
    D = Integer.parseInt(args[6]);
    
    int numFrames = M/P;
    frameTable = new Page[numFrames];
    for(int i=0;i<numFrames;i++){frameTable[i]=null;}
    Scanner reader = null;
    try{reader = new Scanner(new FileReader("random.txt"));}
    catch(FileNotFoundException e){e.printStackTrace();}
    
    // Display the output
    System.out.format("Machine Size is: %d\n", M);
    System.out.format("The Page Size is: %d\n", P);
    System.out.format("The Process Size is: %d\n", S);
    System.out.format("The Job Mix is: %d\n", J);
    System.out.format("The number of references per process is: %d\n", N);
    System.out.format("The replacement algorithm is: "+ R + "\n");
    System.out.format("The Debug level is: "+ D + "\n");

    // Construct process queue depending on J-value
    Queue<Process> q=null;
    q = getQueue(J,N,S);

    // Process the jobs in Round Robin Fashion
    Pager pgr =new Pager(P,R,frameTable, reader, D);
    int quantum=0;
    while(!q.isEmpty()){
      Process p = q.poll();
      quantum = 3;
      while(quantum!=0 && p.refs!=0){
        // compute next word for the process
        pgr.paging(p);
        quantum --;
        p.updateNextWord(J, reader);
      }
      if (quantum==0 && p.refs!=0){
        q.add(p);
      }
      else if(p.refs==0){
        done.add(p);
      }
    }
    
    // Display the stats
    displayStats(done);
  }

  public static void displayStats(Queue<Process> done){
    // Display stats per process
    int totalFault=0;
    int totalResidency=0;
    int totalEvictions=0;

    System.out.println();
    Iterator it = done.iterator();
    while(it.hasNext()){
      Process p = (Process)it.next();
        if (p.evictions!=0){
        System.out.format("Process %d had %d faults and %f average residency \n", p.id,p.faults,(double)(p.residencyTime/(double)p.evictions));

        totalFault+=p.faults;
        totalResidency += p.residencyTime;
        totalEvictions += p.evictions;
        }
        else{
          System.out.format("Process %d had %d faults and average residency is undefined. \n", p.id,p.faults);
          totalFault+=p.faults;
         }
     }

    // Display overall stats
    if (totalEvictions!=0){
      System.out.format("\n The Total number of faults is %d and the overall average residency is %f\n", totalFault, (double)(totalResidency/(double)totalEvictions));
    }
    else{
      System.out.format("\n The Total number of faults is %d and the overall average residency is undefined", totalFault);
    }

  }

  public static Queue<Process> getQueue(int J,int N, int S){

    Queue<Process> q = new LinkedList<Process>();
    switch(J){

     case 1: q.add(new Process(1,1.0,0.0,0.0,N,S,P)); 
             break;
     case 2: q.add(new Process(1,1.0,0.0,0.0,N,S,P));
             q.add(new Process(2,1.0,0.0,0.0,N,S,P));
             q.add(new Process(3,1.0,0.0,0.0,N,S,P));
             q.add(new Process(4,1.0,0.0,0.0,N,S,P));
             break;
     case 3: q.add(new Process(1,0.0,0.0,0.0,N,S,P));
	     q.add(new Process(2,0.0,0.0,0.0,N,S,P));
	     q.add(new Process(3,0.0,0.0,0.0,N,S,P));
	     q.add(new Process(4,0.0,0.0,0.0,N,S,P));
             break;
     case 4: q.add(new Process(1,0.75,0.25,0,N,S,P));
	     q.add(new Process(2,0.75,0,0.25,N,S,P));
	     q.add(new Process(3,0.75,0.125,0.125,N,S,P));
             q.add(new Process(4,0.5,0.125,0.125,N,S,P));
             break;
    }
    return q;
  }
}
