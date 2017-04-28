import java.io.*;
import java.util.*;
class Pager{

  // Pager characteristics
  public int P;  // since page size is constant independent of Process,
		 // we can have it as Pager Property, though not very 
		 // good thing to do.
  public String algo;
  public Page [] frameTable;
  public Scanner reader;
  public int count=1;
  public int debugLevel =-1;

  Pager(int P, String algo, Page[] ft, Scanner reader, int d){
    this.P=P;
    this.algo=algo;
    this.frameTable=ft;
    this.reader=reader;
    this.debugLevel = d;
  }

  // Accepts a process and fulfills its
  // paging requirement i.e is its next reference
  public void paging(Process p){
     // Get the page in which the references word lies
     Page pg = p.pages[p.nw/this.P]; //word/PageSize

     // this page is accessed so update its last accessed time
     pg.lastAccessed  = this.count;

     // Check if reference is Hit or Fault
     int hit = this.searchPage(pg);

     if(hit==-1){ // Fault
       p.faults++;
       // pg will now be inserted, hence set its inTime
       pg.inTime=count;
       // if frameTable has space
       if(this.frameTable[0]==null){ // Empty Space
         int k=0;
         while(this.frameTable[k]==null && k+1<this.frameTable.length){k++;}
         if(frameTable[k]!=null){k--;}
         if(this.debugLevel==1){
         System.out.format("%d references word %d (page %d) at time %d: Fault, using Free Frame %d\n", p.id, p.nw, pg.id, count,k);}
            this.frameTable[k]=pg; //Add to the frame table
       }
       else{// else run a replacement algorithm
         if(this.algo.equals("lifo")){
           this.lifo(p, pg);
         }
         else if(this.algo.equals("lru")){
           this.lru(p, pg);
         }
         else if(this.algo.equals("random")){
           this.random(p, pg);
         }
       }
     }
     else{ // Hit.
       if(this.debugLevel==1){
         System.out.format("%d references word %d (page %d) at time %d: Hit in Frame %d\n",p.id, p.nw, pg.id, count, hit);
       }
     }
     // This reference handled. hence reduce number of references
     p.refs--;
     this.count++;
  }

  // LIFO
  public void lifo(Process p, Page pg){
    // First frame will be the victim always
    // Choose Victim
    Page victim = this.frameTable[0];
    victim.p.residencyTime+=count-victim.inTime;
    victim.p.evictions++;

    // Replace
    this.frameTable[0] = pg;
    if(this.debugLevel==1){
    System.out.format("%d references word %d (page %d) at time %d: Fault, evicting Page %d of %d Frame %d \n", p.id, p.nw, pg.id, count, victim.id,victim.p.id, 0);}
  }

  // LRU
  public void lru(Process p, Page pg){
    // find the page with minimum lastUsed value
    int minIndex = 0;
    int lru = 1000000;

    // Choose victim
    for(int i=0; i < this.frameTable.length ;i++){
      if(this.frameTable[i].lastAccessed<lru){
        lru = this.frameTable[i].lastAccessed;
        minIndex = i;
      }
    }
    // Victimize
    Page victim = this.frameTable[minIndex];
    victim.p.residencyTime+=count-victim.inTime;
    victim.p.evictions++;

    // Replace
    this.frameTable[minIndex] = pg;
    if(this.debugLevel==1){
    System.out.format("%d references word %d (page %d) at time %d: Fault, evicting Page %d of %d Frame %d \n", p.id, p.nw, pg.id, count, victim.id, victim.p.id, minIndex);}
  }

  public void random(Process p, Page pg){

    // Select Victim
    int nr = this.reader.nextInt();
    int r = nr%(this.frameTable.length);
    Page victim = this.frameTable[r];

    // Victimize
    if(this.debugLevel==1){
    System.out.format("Updating residency for %d by %d\n",victim.p.id,count-victim.inTime);}
    victim.p.residencyTime+=count-victim.inTime;
    victim.p.evictions++;

    // Replace
    this.frameTable[r] = pg;
    if(this.debugLevel==1){
    System.out.format("%d references word %d (page %d) at time %d: Fault, evicting Page %d of %d Frame %d \n", p.id, p.nw, pg.id, count, victim.id,victim.p.id, r);}
  }
  
  public int searchPage(Page pg){
    for(int i=0;i<this.frameTable.length;i++){
     if(this.frameTable[i]!=null){
      if (this.frameTable[i].id==pg.id && this.frameTable[i].p.id==pg.p.id){
        return i;
      }}
    }
    return -1;
  }
}
