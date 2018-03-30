import java.text.DecimalFormat;
import java.util.ArrayList;
public class Bank
{
    int MAX_TASK;//Max Process Number
    int MAX_RESOURCE; //Max Resource Type
    int[] Available; //Available resource vector
    int[][] Max;//Max Need Matrix
    int[][] Allocation; //Allocate Matrix
    int[][] Need;//Need Matrix

    int Request_PROCESS;
    int[][] Request_COURCE ;//Number of Process requested resource

    public Bank()
    {

    }

    public void printMax(){
        System.out.println("MAX MATRIX:");
        for(int i=0;i<MAX_TASK;i++){
            for(int j=0;j<MAX_RESOURCE;j++){
                System.out.print(Max[i][j]+"  ");
            }
            System.out.println();
        }
    }

    public void printNeed(){
        System.out.println("*************NEED MATRIX*************");
        for(int i=0;i<MAX_TASK;i++){
            for(int j=0;j<MAX_RESOURCE;j++){
                System.out.print(Need[i][j]+"  ");
            }
            System.out.println();
        }
    }

    public void printAvailable() {
        System.out.println("*************AVAILABLE MATRIX*************");
        for(int i=0;i<Available.length;i++){
            System.out.print(Available[i]+"     ");
        }
        System.out.println();
    }

    public void printAllocation() {
        System.out.println("*************ALLOCATION MATRIX*************");
        for(int i=0;i<MAX_TASK;i++){
            for(int j=0;j<MAX_RESOURCE;j++){
                System.out.print(Allocation[i][j]+"  ");
            }
            System.out.println();
        }
    }

    public void initAlgorithm(int numOfTasks,int numOfResourceType){
        MAX_TASK = numOfTasks;//Max Process Number
        MAX_RESOURCE = numOfResourceType; //Max Resource Type
        Available=new int[MAX_RESOURCE]; //Available resource vector
        Max= new int[MAX_TASK][MAX_RESOURCE]; //Max Need Matrix
        Allocation=new int[MAX_TASK][MAX_RESOURCE]; //Allocate Matrix
        Need=new int[MAX_TASK][MAX_RESOURCE]; //Need Matrix
        Request_COURCE = new int[MAX_TASK][MAX_RESOURCE]; //Number of Process requested resource
    }

    public void bankmethod(Task[] tasks)
    {
        int cycle = 0;
        //System.out.println("LOOP START!!!!!!!!!------------------------------------------------------------");
        BlockQueue block = new BlockQueue();
        ArrayList<Task> blockToReady = new ArrayList<Task>();//Tasks that will be ready to procecute
        //during one cycle, finish add and clear at the end of a cycle
        ArrayList<Task> wait = new ArrayList<Task>();//Tasks that will wait during one cycle, finish add and clear in one cycle
		/*executes until all process are ended or aborted*/
        while(true){
            cycle++;
            Task task;//temp variable, storing task that pop from the block queue
            wait.clear();
            //modify release counter: 1 array and two matrix
            int[] releasedResource = new int[MAX_RESOURCE];	//released resource in this cycle, modified when all tasks finished reading in activities
            for(int i=0;i<releasedResource.length;i++){
                releasedResource[i] = 0;
            }
            int[][] releasedNeed = new int[MAX_TASK][MAX_RESOURCE];//need that will change due to release in this cycle
            int[][] releasedAllocation = new int[MAX_TASK][MAX_RESOURCE];//allocation that will change due to release in this cycle
            for(int i=0;i<MAX_TASK;i++){
                for(int j=0;j<MAX_RESOURCE;j++){
                    releasedNeed[i][j] = 0;
                    releasedAllocation[i][j] = 0;
                }
            }
            //block queue checked first
            while(!block.blockQueue.isEmpty()){
                //block.print();
                if(!block.blockQueue.isEmpty()){
                    task = block.blockQueue.poll();
                    if(banker(tasks,task)){
                        //if successfully allocated then remove from block queue,
                        //but turn into normal states after processing other tasks
                        blockToReady.add(task);
                    }
                    else{
                        //make this task wait
                        wait.add(task);
                    }
                }
            }
            //for blocked task that cannot be allocated then still blocks
            block.blockQueue.addAll(wait);
			/*Read in all tasks not blocked*/
            for(int i=0;i<tasks.length;i++){
                String curActivity = new String();
                //If is blocked then skip this task
                if(!block.blockQueue.contains(tasks[i]) && !blockToReady.contains(tasks[i])){
                    //if this task is not in computing
                    if(tasks[i].computeTime==0){
                        //System.out.println(tasks[i].iterator);
                        if(tasks[i].hasNextActivity()){
                            curActivity = tasks[i].getNext();
                            //System.out.print(curActivity);
                        }
						/*INITIATE*/
                        if(curActivity.contains("initiate")){
                            String splitInitiate[] = curActivity.split("\\s+");
                            int taskNum = Integer.parseInt(splitInitiate[1]);
                            int resType = Integer.parseInt(splitInitiate[2]);
                            int initClaim = Integer.parseInt(splitInitiate[3]);
                            //If exceeds the resources present
                            if(initClaim>Available[resType-1]){
                                //System.out.println("Abort Task "+ (taskNum));
                                tasks[taskNum-1].abortTask();
                            }
                            //else allocate
                            else{
                                Max[taskNum-1][resType-1] = initClaim;
                                Need[taskNum-1][resType-1] = initClaim;
                                tasks[i].next();
                            }
                        }
						/*-----REQUEST-----*/
                        else if(curActivity.contains("request")){
                            if(!banker(tasks,tasks[i])){
                                //if failed allocate then block in this cycle
                                block.blockQueue.add(tasks[i]);
                            }
                        }
						/*-----RELEASE-----*/
                        else if(curActivity.contains("release")){
                            String splitRelease[] = curActivity.split("\\s+");
                            int taskNum = Integer.parseInt(splitRelease[1]);
                            int resType = Integer.parseInt(splitRelease[2]);
                            int numRel = Integer.parseInt(splitRelease[3]);
                            //record the modification of resources, process after all tasks executed in this cycle
                            releasedResource[resType-1] += numRel;
                            releasedAllocation [taskNum-1][resType-1] = numRel;
                            releasedNeed  [taskNum-1][resType-1]  = numRel;
                            //pointer point to the next activities
                            tasks[i].next();
                            if(tasks[i].isFinished()){
                                tasks[i].finishTask(cycle);
                            }
                        }
						/*-----COMPUTE-----*/
                        else if(curActivity.contains("compute")){
                            String splitCompute[] = curActivity.split("\\s+");
                            int taskNum = Integer.parseInt(splitCompute[1])-1;
                            int compTime = Integer.parseInt(splitCompute[2])-1;
                            tasks[taskNum].computeTime = compTime;
                            //pointer point to the next activities
                            tasks[taskNum].next();
                            if(tasks[i].isFinished() && tasks[i].computeTime ==0){
                                tasks[i].finishTask(cycle);
                            }
                        }
                        else{

                        }
                    }
                    //if this task is in computing
                    else{
                        tasks[i].compute();
                        if(tasks[i].computeTime==0 && tasks[i].isFinished()){
                            tasks[i].finishTask(cycle);
                        }
                    }
                }/*finished operating all tasks*/

            }

            //collect resource released in this cycle
            collection(releasedResource, releasedAllocation, releasedNeed);
            //remove task from block to ready
            Task[] remTask = blockToReady.toArray(new Task[0]);
            for(int i=0;i<remTask.length;i++){
                blockToReady.remove(remTask[i]);
            }

            //If all finished then stop loop
            if(ifAllTaskFinished(tasks)){
                break;
            }
        }
        //System.out.println("main loop ends!  "+cycle);
        printFinishTime(tasks);
    }
    //collect resources that released in one cycle, parameters served as recorder
    public void collection(int[] releasedResource,int[][] releasedAllocation,int[][] releasedNeed){
        //released resource
        for(int i=0;i<MAX_RESOURCE;i++){
            Available[i]+=releasedResource[i];
        }
        //changes for the matrix
        for(int i=0;i<MAX_TASK;i++){
            for(int j=0;j<MAX_RESOURCE;j++){
                Allocation[i][j] -= releasedAllocation[i][j];
                Need[i][j] +=releasedNeed[i][j];
            }
        }
    }

    /*Main method for the banker algorithm*/
    public Boolean banker(Task[] tasks,Task task){
        //get current activities
        String curActivity = task.getNext();
        String splitRequest[] = curActivity.split("\\s+");
        int taskNum = Integer.parseInt(splitRequest[1])-1;
        int resType = Integer.parseInt(splitRequest[2])-1;
        int numReq = Integer.parseInt(splitRequest[3]);
        //All request resource set 0
        for(int k=0;k<MAX_TASK;k++){
            for(int l=0;l<MAX_RESOURCE;l++){
                Request_COURCE[taskNum][resType] = 0;
            }
        }
        Request_PROCESS = taskNum;
        Request_COURCE[taskNum][resType] = numReq;
        //Try Allocation
        for(int k=0;k<MAX_RESOURCE;k++)
        {
            //if allocated resource exceeds max claimed
            if(Need[Request_PROCESS][k] - Request_COURCE[Request_PROCESS][k]<0 ){
                //release the resources one task onws
                Available[k] = Available[k] + Request_COURCE[Request_PROCESS][k];
                Allocation[Request_PROCESS][k] = Allocation[Request_PROCESS][k] - Request_COURCE[Request_PROCESS][k];
                Need[Request_PROCESS][k] = Need[Request_PROCESS][k] + Request_COURCE[Request_PROCESS][k];
                task.abortTask();
                return true;
            }
            //if allocated does not exceed max claimed then allocate
            else
            {
                Available[k] = Available[k] - Request_COURCE[Request_PROCESS][k];
                Allocation[Request_PROCESS][k] = Allocation[Request_PROCESS][k] + Request_COURCE[Request_PROCESS][k];
                Need[Request_PROCESS][k] = Need[Request_PROCESS][k] - Request_COURCE[Request_PROCESS][k];
            }
        }
        //check for safety
        if(IsSafe(tasks))
        {
            //pointer move to next activities
            task.next();
            for(int k=0;k<MAX_RESOURCE;k++){
                Request_COURCE[Request_PROCESS][k] = 0;
            }
            //System.out.println(curActivity+" is safe, request succeeds.");
            return true;
        }
        else
        {
            //not safe
            //System.out.println(curActivity+" is not safe, request fails.");
            //release all kinds of resources requested in this cycle
            for(int k=0;k<MAX_RESOURCE;k++)
            {
                Available[k] = Available[k] + Request_COURCE[Request_PROCESS][k];
                Allocation[Request_PROCESS][k] = Allocation[Request_PROCESS][k] - Request_COURCE[Request_PROCESS][k];
                Need[Request_PROCESS][k] = Need[Request_PROCESS][k] + Request_COURCE[Request_PROCESS][k];
            }
            for(int k=0;k<MAX_RESOURCE;k++){
                Request_COURCE[Request_PROCESS][k] = 0;
            }
            task.block();
            //System.out.println(curActivity+" blocks");
            return false;
        }
    }
    //states judging
    public boolean IsSafe(Task[] tasks)
    {
        int[] work = new int[MAX_RESOURCE];
        boolean[] Finish = new boolean[MAX_TASK];
        for(int i=0;i<MAX_TASK;i++){
            if(tasks[i].isFinished()||tasks[i].isAborted()){
                Finish[i] = true;
            }
            else{
                Finish[i] = false;
            }
        }
        for(int i=0;i<MAX_RESOURCE;i++)
        {
            work[i] = Available[i];
        }
        //look for processes that Finish[i]=false,Need[i,j]<=Work[j]
        int i = 0;
        do
        {
            boolean flag = true;
            //if Need[i][j]<=work[j]
            for(int j=0;j<MAX_RESOURCE;j++)
            {
                if(Need[i][j]>work[j])
                {
                    flag = false;
                    break;
                }
            }
            //if Finish[i]=false && Need[i,j]<=Work[j]
            if(Finish[i]==false && flag)
            {
                for(int j=0;j<MAX_RESOURCE;j++)
                {
                    work[j] = work[j] + Allocation[i][j];
                }
                Finish[i] = true;
                i = -1; //retraverse unfinished process
            }
        }while(++i<MAX_TASK);
        i = 0;
        while(Finish[i]==true)
        {
            if(i == MAX_TASK-1){
                return true; //is safe states, return true
            }
            i++;
        }
        return false; //is danger states, return false
    }

    public void printAll(){
        System.out.println("*************ALL MATRIX*************");
        System.out.println("Available Max Need Allocation");
        for(int i=0;i<MAX_TASK;i++){
            for(int j=0;j<MAX_RESOURCE;j++){
                System.out.print(Available[j]);
                System.out.print("          "+Max[i][j]+"     ");
                System.out.print(Need[i][j]+"     ");
                System.out.print(Allocation[i][j]+"  ");
            }
            System.out.println();
        }
    }

    public void printFinishTime(Task[] task){
        /*for (int j = 0; j<task.length;j++){
            if (task[j].isAborted()){
                String res = task[j].Activities.get(0);
                String res1 = String.valueOf(res.charAt(12));
                String res2 = String.valueOf(res.charAt(14));
                System.out.println("Banker aborts task " + task[j].id + " before run begins:\n   claim for resource "
                        + res1 + " (" + res2 + ") exceeds number of units present (" + Available[0] + ")");
            }
        }*/
        System.out.println("          Banker");
        DecimalFormat dfPrint = new DecimalFormat("####");
        for(int i=0;i<task.length;i++){
            System.out.print("Task " + task[i].id+"       ");
            if(task[i].isAborted()){
                System.out.print("Aborted");
            }
            else{
                System.out.print(task[i].finishTime+"    ");
                System.out.print(task[i].blockedNum+"    ");
                float print = (float)task[i].blockedNum/task[i].finishTime;
                System.out.print(dfPrint.format(print*100)+"%");
            }
            System.out.println();
        }
        System.out.print("Total        ");
        int totalTime = 0;
        int totalBlockedTime = 0;
        for(int i=0;i<task.length;i++){
            totalTime+=task[i].finishTime;
            totalBlockedTime+=task[i].blockedNum;
        }
        float print = (float)totalBlockedTime/totalTime;
        System.out.print(totalTime+"    ");
        System.out.print(totalBlockedTime+"    ");
        System.out.print(dfPrint.format(print*100)+"%");
        System.out.println();
    }

    public static Boolean ifAllTaskFinished(Task[] task){
        int i=0;
        for(i=0;i<task.length;i++){
            if(!task[i].isFinished()){
                return false;
            }
        }
        return true;
    }
}
