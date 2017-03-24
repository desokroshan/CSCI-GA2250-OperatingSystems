import java.util.*;
import java.io.*;

class Process{
  int A;
  int B;
  int C;
  int M;
  int finishingTime;
  int turnaroundTime;
  int ioTime=0;
  int waitingTime=0;
  String state="Unstarted";
  int key;
  int nextCpuBurst=0;
  int remCpuBurst=0;
  int remCpuTime=0;
  int remIoBurst=0;
  int readyTime=0;
  int q=2;
  int quantumExhausted=0;
  static int cpuBurst=0;
  static Scanner reader=null;
  //static int[] numbers=new int[10000];
  static int current=0;
  public Process(){
    try{
     reader= new Scanner(new FileReader("random.txt"));}
    catch(FileNotFoundException e){e.printStackTrace();}
    //for(int i=0;i<10000;i++){numbers[i]=reader.nextInt();}
  }

  //Methods
  public int getRemCpuBurst(){return this.remCpuBurst;}
  public void setRemCpuBurst(int t,int flag){
   if(t==87){System.out.format("%d %d %d next", cpuBurst, current,this.remCpuTime);}
   if (this.quantumExhausted==1){this.quantumExhausted=0;}
   else {
    if(current==0){cpuBurst = randomOS(this.B);}
    if (cpuBurst>this.remCpuTime) { this.remCpuBurst=this.remCpuTime;}
    else if(cpuBurst==this.remCpuTime){this.remCpuBurst=this.remCpuTime;
    // No IO but consume the random number
}
    else{
    this.remCpuBurst = cpuBurst;
    this.remIoBurst = this.M*(cpuBurst);
    this.ioTime+=this.remIoBurst;
    current=0;
   }
  }
  //System.out.println(this.ioTime);
  //if(t==84){System.out.format("%d,%d",cpuBurst,this.remCpuBurst);}
  if(t==87){System.out.format("end %d %d", cpuBurst, current);} 
  if(flag==1){this.waitingTime+=(t-this.readyTime);}
  //System.out.format("%d %d", this.waitingTime, this.readyTime);
}
  public void display(){
   System.out.format("(%d %d %d %d)",this.A,this.B,this.C,this.M);
  }
  public static int randomOS(int u){
    //read a random number from the file
    return (1+ (reader.nextInt()%u));
  }
  public Process run(int time, int flag){
    this.state="Running";
    this.setRemCpuBurst(time, flag);
 



   this.q=2;
    return this;
  }
  public void ready(PriorityQueue<Process> ready,int t, int quantumExhausted){
        this.state="Ready";
        this.readyTime=t;
        this.quantumExhausted=quantumExhausted;
        ready.add(this);
  }
  public void block(PriorityQueue<Process> blocked){
      this.state="Blocked";
      blocked.add(this);
  }
  public void terminate(int t){
    this.state="Terminated";
    this.finishingTime=t;
  }
}

class Scheduler{
  // Properties 
  static Comparator<Process> comp = new ProcessComparator(); 
  static Comparator<Process> comp1 = new ReadyComparator();
  static Comparator<Process> compSJF = new ReadyComparatorSJF();
  static PriorityQueue<Process> queue = new PriorityQueue<Process>(100, comp);
  static PriorityQueue<Process> backup =new PriorityQueue<Process>(100, comp);
  static PriorityQueue<Process> ready = new PriorityQueue<Process>(100, comp1);
  static PriorityQueue<Process> readySJF = new PriorityQueue<Process>(100, compSJF);
  static PriorityQueue<Process> blocked = new PriorityQueue<Process>(100, comp);
  static double cpuUtilization=0;
  static double ioUtilization=0;
  //PriorityQueue<Process> queue = new PriorityQueue<Process>(100, comparator);

  // Methods
  public static PriorityQueue<Process> debugging(PriorityQueue<Process> processes, String algo){ 
     PriorityQueue<Process> queue1= new PriorityQueue<Process>(100,comp);
     while (!processes.isEmpty()){
      Process p = (Process)processes.remove();
      System.out.print(p.state);
      System.out.print(" ");
      if (p.state=="Running" && (algo=="fcfs"||algo=="Uniprogrammed")) {System.out.print(p.remCpuBurst);}
      else if (p.state=="Running" && algo=="rr") {System.out.print(Math.min(p.q,p.remCpuBurst));}
      else if (p.state=="Blocked"){System.out.print(p.remIoBurst);}
      else {System.out.print(0);}
      System.out.print(" ");
      if(!queue1.contains(p)){queue1.add(p);}
     }
     return queue1;
  }
  public static void displaySummary(PriorityQueue<Process> queue,int num,double ioUtil, double cpuUtil){
   
    System.out.println("Processwise Summary");
    Iterator it = queue.iterator();
    int k=0;
    int finishTime=0;
    double avgTurnaround=0.0,avgWaitingTime=0.0,throughput=0.0;
    while (!queue.isEmpty()){
     Process p = queue.poll();
     System.out.format("Process %d:\n",k);
     System.out.format("\t(A,B,C,M)=(%d,%d,%d,%d)\n",p.A,p.B,p.C,p.M);
     System.out.format("\tFinishing Time: %d\n",p.finishingTime);
     System.out.format("\tTurnaround time: %d\n",(p.finishingTime-p.A));
     System.out.format("\tI/O time: %d\n",p.ioTime);
     System.out.format("\tWaiting time: %d\n",p.waitingTime);
     if (p.finishingTime>finishTime){finishTime=p.finishingTime;}
     avgTurnaround+=(p.finishingTime-p.A);
     avgWaitingTime+=p.waitingTime;
     k++;
    }
    throughput=((double)num/finishTime)*100;
    System.out.println("Summary Data");
    System.out.format("\tFinishing Time: %d\n",finishTime);
    System.out.format("\tCPU Utilization: %f\n",cpuUtil/finishTime);
    System.out.format("\tI/O Utilization: %f\n",ioUtil/finishTime);
    System.out.format("\tThroughput: %f processes per hundred cycles\n",throughput);
    System.out.format("\tAverage turnaround time: %f\n",avgTurnaround/num);
    System.out.format("\tAvg Waiting time: %f\n",avgWaitingTime/num);
  }
  public static int readFileIntoQueue(PriorityQueue<Process> queue, String filename){

    Scanner reader=null;
    try{
        reader = new Scanner(new File(filename));
    }catch (FileNotFoundException e){
        e.printStackTrace();
    }

    int num = reader.nextInt();
    System.out.print("The Original Input was: ");
    for (int i=0;i<num;i++){
      Process p = new Process();
      p.A = Integer.parseInt(reader.next().replace("(", ""));
      p.key = i;
      p.B = reader.nextInt();
      p.C = reader.nextInt();
      p.remCpuTime = p.C;
      p.M = Integer.parseInt(reader.next().replace(")", ""));
      queue.add(p);
      p.display();
      System.out.print(" ");
    }
    return num;
  }
  public static void displayQueueInorder(PriorityQueue<Process> queue,PriorityQueue<Process> backup){
      System.out.print("\nThe (sorted) input is: ");
      while(!queue.isEmpty()){
      Process p = queue.poll();
      System.out.format("(%d %d %d %d) ",p.A,p.B,p.C,p.M);
      backup.add(p);
    }
  }
  public static void fcfs(PriorityQueue<Process> queue,PriorityQueue<Process> ready,PriorityQueue<Process> blocked,int num,int verbose){
   
    // Variable Declaration
    int completedProcess = 0;
    int t=0;
    Process run = null;
    int flag=0;
    while(completedProcess<num){
     int ioFlag=0;
     if(verbose==1){
         System.out.format("\nBefore Cycle %d : ",t);
         queue = debugging(queue,"fcfs");}

     Iterator it = queue.iterator();
     while(it.hasNext()){
      Process p = (Process)it.next();
      if (p.A<=t && p.state=="Unstarted"){
       p.ready(ready,t,0);
      }
     }
     it=blocked.iterator();
     while(it.hasNext()){
       if (ioFlag==0){ioUtilization++;ioFlag=1;}
       Process p = (Process)it.next();
       p.remIoBurst--;
       if (p.remIoBurst==0){
        it.remove();
        p.ready(ready, t,0);
       }
     }
    if (run !=null){
      run.remCpuBurst--;run.remCpuTime--;cpuUtilization++;
      if (run.remCpuTime==0){
       run.terminate(t);
       completedProcess++;
       run = ready.poll();
       if(run!=null){run=run.run(t,1);}
      }
      else if(run.remCpuBurst==0){
      if (run.remIoBurst!=0){
        run.block(blocked);}else{run.ready(ready,t,0);}
       run = ready.poll();
       if(run!=null){run=run.run(t,1);}
      }
    }
    else{
       run = ready.poll();
       if(run!=null){run=run.run(t,1);}
    }
    t++;
    }
    System.out.println("\nScheduling Algorithm: FCFS");
    displaySummary(queue,num,ioUtilization,cpuUtilization);
}

 public static void roundRobin(PriorityQueue<Process> queue,PriorityQueue<Process> ready,PriorityQueue<Process> blocked,int num,int verbose){
    int completedProcess = 0;
    int t=0;
    Process run = null;
    int flag=0;
    while(completedProcess<num){
     int ioFlag=0;
     if (verbose==1){
     System.out.format("\nBefore Cycle %d : ",t);
     queue = debugging(queue,"rr");
     }

     Iterator it = queue.iterator();
     while(it.hasNext()){
      Process p = (Process)it.next();
      if (p.A<=t && p.state=="Unstarted"){
       p.ready(ready,t,0);
      }
     }
     it=blocked.iterator();
     while(it.hasNext()){
       if (ioFlag==0){ioUtilization++;ioFlag=1;}//Even if 1 process is blocked IO is in use
       Process p = (Process)it.next();
       p.remIoBurst--;
       if (p.remIoBurst==0){
        it.remove();
        p.ready(ready, t,0);
       }
     }
    if (run !=null){
      run.remCpuBurst--;run.remCpuTime--;run.q--;cpuUtilization++;
      if (run.remCpuTime==0){
       run.terminate(t);
       completedProcess++;
       run = ready.poll();
       if(run!=null){run=run.run(t,1);}
      }
      else if(run.remCpuBurst==0){
      if (run.remIoBurst!=0){
        run.block(blocked);}else{run.ready(ready,t,0);}
       run = ready.poll();
       if(run!=null){run=run.run(t,1);}
      }
      else if(run.q==0){
        run.ready(ready,t,1);
        run = ready.poll();
        if(run!=null){run=run.run(t,1);}
      }
    }
    else{
       run = ready.poll();
       if(run!=null){run=run.run(t,1);}
    }
    t++;
    }
    System.out.println("\nScheduling Algorithm: RR");
    displaySummary(queue,num,ioUtilization,cpuUtilization);
}
 public static void uniprogrammed(PriorityQueue<Process> queue,PriorityQueue<Process> ready,int num,int verbose){
    int completedProcess = 0;
    int t=0;
    Process run = null;
    Process blocked=null;
    int flag=0;
    while(completedProcess<num){
     int ioFlag=0;
     if(verbose==1){
     System.out.format("\nBefore Cycle %d : ",t);
     queue = debugging(queue,"Uniprogrammed");}

     Iterator it = queue.iterator();
     while(it.hasNext()){
      Process p = (Process)it.next();
      if (p.A<=t && p.state=="Unstarted"){
       p.ready(ready,t,0);
      }
     }
     if(run==null && blocked==null){
       run = ready.poll();
       if(run!=null){run=run.run(t,1);}
     }
     else if (run!=null){
      run.remCpuBurst--;run.remCpuTime--;cpuUtilization++;
      if (run.remCpuTime==0){
       run.terminate(t);
       completedProcess++;
       run = ready.poll();
       if(run!=null){run=run.run(t,1);}
      }
      else if(run.remCpuBurst==0){
      if (run.remIoBurst!=0){
        run.state="Blocked";blocked=run;run=null; }
      }
     }
     else{
       if (ioFlag==0){ioUtilization++;ioFlag=1;}
       blocked.remIoBurst--;
       if (blocked.remIoBurst==0){
        run=blocked.run(t,0);
        blocked=null;
       }
     }
    t++;
    }
    System.out.println("\nScheduling Algorithm: Uniprogrammed");
    displaySummary(queue,num,ioUtilization,cpuUtilization);
}

  public static void main(String [] args){
    String filename="input.txt"; int verbose=0;String algo=null;
    if (args.length==3){filename=args[0];verbose=1;algo=args[1];}
    else if(args.length==2){filename=args[0];algo=args[1];}
    else if(args.length<2){System.out.println("Filename and scheduling algo must be provided");System.exit(0);} 
    //1. read the input into queue 
    int num = 0;
    num=readFileIntoQueue(queue,filename);
    //2. Display the queue Inorder
    displayQueueInorder(queue,backup); 
    queue=new PriorityQueue<Process>(backup);
    //PriorityQueue<Process> queue1=new PriorityQueue<Process>(100,comp);
    //num=readFileIntoQueue(queue1,filename);
    if (algo.equals("fcfs")){fcfs(queue,ready,blocked,num,verbose);}
    else if(algo.equals("rr")){roundRobin(queue,ready,blocked,num,verbose);}
    else if(algo.equals("uni")){uniprogrammed(queue,ready,num,verbose);}
    else if (algo.equals("sjf")){sjf(queue,readySJF,blocked,num,verbose);}
    else{System.out.println("Algo must be one of fcfs,rr,uni,sjf");
      System.exit(0);
     }
    // FCFS
    //fcfs(queue,ready,blocked,num,verbose);
    // Round Robin
    //PriorityQueue<Process> queue2=new PriorityQueue<Process>(100,comp);
    //num=readFileIntoQueue(queue2,filename);
    //PriorityQueue<Process> ready1 = new PriorityQueue<Process>(100, comp1);
    //PriorityQueue<Process> blocked1 = new PriorityQueue<Process>(100, comp);
    //roundRobin(queue2,ready1,blocked1,num,verbose);
    // Uniprogrammed
    //PriorityQueue<Process> queue3=new PriorityQueue<Process>(100,comp);
    //num=readFileIntoQueue(queue3,filename);
    //PriorityQueue<Process> ready2 = new PriorityQueue<Process>(100, comp1);
    //uniprogrammed(queue3,ready2,num,verbose);
    // SJF
    //PriorityQueue<Process> queue4=new PriorityQueue<Process>(100,comp);
    //num=readFileIntoQueue(queue4,filename);
    //sjf(queue4,readySJF,blocked,num,verbose);
    
  }
 public static void sjf(PriorityQueue<Process> queue,PriorityQueue<Process> ready,PriorityQueue<Process> blocked,int num, int verbose){
    int completedProcess = 0;
    int t=0;
    Process run = null;
    int flag=0;
    while(completedProcess<num){
     int ioFlag=0;
     if(verbose==1){
     System.out.format("\nBefore Cycle %d : ",t);
     queue = debugging(queue,"fcfs");}

     Iterator it = queue.iterator();
     while(it.hasNext()){
      Process p = (Process)it.next();
      if (p.A<=t && p.state=="Unstarted"){
       p.ready(ready,t,0);
      }
     }
     it=blocked.iterator();
     while(it.hasNext()){
       if (ioFlag==0){ioUtilization++;ioFlag=1;}
       Process p = (Process)it.next();
       p.remIoBurst--;
       if (p.remIoBurst==0){
        it.remove();
        p.ready(ready, t,0);
       }
     }
    if (run !=null){
      run.remCpuBurst--;run.remCpuTime--;run.q--;cpuUtilization++;
      if (run.remCpuTime==0){
       run.terminate(t);
       completedProcess++;
       run = ready.poll();
       if(run!=null){run=run.run(t,1);}
      }
      else if(run.remCpuBurst==0){
      if (run.remIoBurst!=0){
        run.block(blocked);}else{run.ready(ready,t,0);}
       run = ready.poll();
       if(run!=null){run=run.run(t,1);}
      }
    }
    else{
       run = ready.poll();
       if(run!=null){run=run.run(t,1);}
    }
    t++;
    }
    System.out.println("\nScheduling Algorithm: SJF");
    displaySummary(queue,num,ioUtilization,cpuUtilization);
}


}

class ProcessComparator implements Comparator<Process>
{
  @Override
  public int compare(Process p1, Process p2){
    if (p1.A!=p2.A){
      return p1.A-p2.A;
    }
    else{
      return p1.key-p2.key;
    }
  }
}
class ReadyComparator implements Comparator<Process>
{
  @Override
  public int compare(Process p1, Process p2){
    if (p1.readyTime != p2.readyTime){
     return (p1.readyTime-p2.readyTime);
    }
    else if (p1.A!=p2.A){
     return p1.A-p2.A;
    }
    else{
      return p1.key-p2.key;
    }
  }
}
class ReadyComparatorSJF implements Comparator<Process>
{
  @Override
  public int compare(Process p1, Process p2){
    if (p1.remCpuTime != p2.remCpuTime){
     return (p1.remCpuTime-p2.remCpuTime);
    }
    else if (p1.A!=p2.A){
     return p1.A-p2.A;
    }
    else{
      return p1.key-p2.key;
    }
  }
}
