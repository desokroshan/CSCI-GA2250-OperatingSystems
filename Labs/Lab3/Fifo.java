
import java.text.DecimalFormat;
import java.util.ArrayList;


public class Fifo {
    int MAX_TASK;//Max Process Number
    int MAX_RESOURCE; //Max Resource Type
    int[] Available; //Available resource vector
    int[][] Allocation;//Allocation resource vector

    public void printAvailable(){
        System.out.println("*************FIFO AVAILABLE MATRIX*************");
        for(int i=0;i<Available.length;i++){
            System.out.print(Available[i]+"  ");
        }
        System.out.println();
    }

    public void printAllocation(){
        System.out.println("*************FIFO ALLOCATION MATRIX*************");
        for(int i=0;i<MAX_TASK;i++){
            for(int j=0;j<MAX_RESOURCE;j++){
                System.out.print(Allocation[i][j]+"  ");
            }
            System.out.println();
        }
    }

    public void printFinishTime(Task[] task){
        System.out.println("          FIFO");
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

    public void initAlgorithm(int numOfTask, int numOfResource) {
        MAX_TASK = numOfTask;
        MAX_RESOURCE = numOfResource;
        Available = new int[MAX_RESOURCE];
        Allocation = new int[MAX_TASK][MAX_RESOURCE];
    }

    public void method(Task[] tasks){
        int cycle = 0;
        //System.out.println("FIFO LOOP START!!!!!!!!!------------------------------------------------------------");
        BlockQueue block = new BlockQueue();
        ArrayList<Task> blockToReady = new ArrayList<Task>();
        ArrayList<Task> wait = new ArrayList<Task>();
        Boolean isDanger = false;//if deadlock possibly happens in previous cycle
		/*executes until all process are ended or aborted*/
        while(true) {
            //numbers of the total activities execute in this cycle: delayed is not a successful activities
            int activities = 0;
            // number of blocked activities, if all tasks are requesting and failed to be allocated then no doubt it is a deadlock
            int blockedReq = 0;
            Task task; // temp variable, storing task that pop from the block queue
            wait.clear();
            //modify release counter: 1 array and two matrix
            int[] releasedResource = new int[MAX_RESOURCE];	//released resource in this cycle, modified when all tasks finished reading in activities
            for(int i=0;i<releasedResource.length;i++){
                releasedResource[i] = 0;
            }

            cycle++;
            //check for deadlock danger in the previous cycle
            if(isDanger){
                for(int i=0;i<tasks.length;i++){
					/*check unfinished or aborted tasks*/
                    if(!tasks[i].isFinished() && !tasks[i].isAborted()){
                        //from the minimun task number, abort task
                        tasks[i].abortTask();
                        //release task
                        for(int j=0;j<MAX_RESOURCE;j++){
                            Available[j] += Allocation[i][j];
                        }
                        block.blockQueue.remove(tasks[i]);
                        //if is not deadlock then stop aborting
                        if(!isDeadlock(tasks)){
                            isDanger = false;
                            break;
                        }
                    }/*finish check unfinished or aborted tasks*/
                }
            }
            //check blocked task first
            while(!block.blockQueue.isEmpty()){
                task = block.blockQueue.poll();
                activities++;
                if(tryAllocation(task)){
                    blockToReady.add(task);
                }
                else{
                    blockedReq++;
                    wait.add(task);
                }
            }
            block.blockQueue.addAll(wait);
			/*Read in all tasks not blocked*/
            for(int i=0;i<tasks.length;i++){
                String curActivity = new String();
				/*check tasks that are not blocked*/
                if(!block.blockQueue.contains(tasks[i]) && !blockToReady.contains(tasks[i])){
					/*tasks are not in delaying*/
                    if(tasks[i].computeTime==0){
                        if(tasks[i].hasNextActivity()){
                            curActivity = tasks[i].getNext();
                        }
						/*INITIATE*/
                        if(curActivity.contains("initiate")){
                            activities++;
                            //pointer jump to the next activities
                            tasks[i].next();
                        }

						/*-----REQUEST-----*/
                        else if(curActivity.contains("request")){
                            activities++;
                            if(tryAllocation(tasks[i])){
                            }
                            else{
                                blockedReq++;
                                block.blockQueue.add(tasks[i]);
                            }
                        }

						/*-----RELEASE-----*/
                        else if(curActivity.contains("release")){
                            activities++;
                            String splitRequest[] = curActivity.split("\\s+");
                            int resType = Integer.parseInt(splitRequest[2])-1;
                            int numRel = Integer.parseInt(splitRequest[3]);
                            releasedResource[resType]+=numRel;
                            Allocation[i][resType] -= numRel;
                            tasks[i].next();
                            if(tasks[i].isFinished()){
                                tasks[i].finishTask(cycle);
                            }
                        }

						/*-----COMPUTE-----*/
                        else if(curActivity.contains("compute")){
                            activities++;
                            String splitCompute[] = curActivity.split("\\s+");
                            int taskNum = Integer.parseInt(splitCompute[1])-1;
                            int compTime = Integer.parseInt(splitCompute[2])-1;
                            tasks[taskNum].computeTime = compTime;
                            tasks[taskNum].next();
                            if(tasks[i].isFinished() && tasks[i].computeTime ==0){
                                tasks[i].finishTask(cycle);
                            }
                        }

                        else{

                        }
                    }/*tasks are not in delaying*/
					/*tasks are delayed*/
                    else{
                        activities++;
                        tasks[i].compute();
                        if(tasks[i].computeTime==0 && tasks[i].isFinished()){
                            tasks[i].finishTask(cycle);
                        }
                    }
                }/*finish check tasks that are not blocked*/
            }

            //collect resource released in this cycle
            for(int i=0;i<MAX_RESOURCE;i++){
                Available[i]+=releasedResource[i];
            }

            //remove task from block to ready
            Task[] remTask = blockToReady.toArray(new Task[0]);
            for(int i=0;i<remTask.length;i++){
                blockToReady.remove(remTask[i]);
            }
            //is deadlock, used for checking at the very beginning of next cycle
            if(activities==blockedReq)	isDanger = true;
            else isDanger = false;

            //If all finished then stop loop
            if(ifAllTaskFinished(tasks)){
                break;
            }
        }
        printFinishTime(tasks);
    }

    //check deadlock process, this will work only when if all process are requesting resources, that may lead to deadlock
    private boolean isDeadlock(Task[] task) {
        int i;
        for(i=0;i<task.length;i++){
            //only check unfinished or unaborted
            if(!task[i].isAborted() && !task[i].isFinished()){
                String curActivity = task[i].getNext();
                String splitRequest[] = curActivity.split("\\s+");
                int resType = Integer.parseInt(splitRequest[2])-1;
                int numReq = Integer.parseInt(splitRequest[3]);
                //if one task can be allocated then of course no deadlock
                if(Available[resType] >= numReq){
                    return false;
                }
            }
        }
        //if all tasks cannot be allocated
        return true;
    }

    private Boolean tryAllocation(Task task) {
        String curActivity = task.getNext();
        String splitRequest[] = curActivity.split("\\s+");
        int resType = Integer.parseInt(splitRequest[2])-1;
        int numReq = Integer.parseInt(splitRequest[3]);
        //not enough resources
        if(Available[resType] - numReq<0){
            task.block();
            return false;
        }
        else{
            //pointer move
            task.next();
            //allocate
            Available[resType] -= numReq;
            Allocation[task.id-1][resType]+=numReq;
            return true;
        }
    }

    private boolean ifAllTaskFinished(Task[] task) {
        int i=0;
        for(i=0;i<task.length;i++){
            //if one process is not finished
            if(!task[i].isFinished()){
                return false;
            }
        }
        //if all processes are finished
        return true;
    }
}
