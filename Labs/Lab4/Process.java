import java.io.*;
import java.util.*;

class Page{
  public int id=-1;
  public Process p=null;
  public int lastAccessed =0; //Last time the process was accessed
  public int inTime=0;  // Last Time process was put in the Frame Table
  Page(int id, Process p){
    this.id = id;
    this.p = p;
  }
}

class Process{

  //Properties
  public int id=-1;
  public double A = 0.0;
  public double B = 0.0;
  public double C = 0.0;
  public int refs = -1;
  public int nw=-1;
  public int size = -1;
  public Page[] pages;
  public int faults=0;
  public int evictions=0;
  public int residencyTime=0;

  Process(int id, double A, double B, double C, int refs, int s, int p){
    this.id = id;
    this.A = A;
    this.B = B;
    this.C = C;
    this.refs = refs;
    this.size = s;
    this.nw = (this.id*111)%this.size;
    this.addPages(s/p);
  }
  public void updateNextWord(int J, Scanner reader){
       int r = reader.nextInt();
       //System.out.format("Using %d\n", r);
       double y = r/(Integer.MAX_VALUE+1d);
       if(y<this.A){this.nw = (this.nw+1)%this.size;}
       else if(y< this.A+this.B){this.nw = (this.nw-5+this.size)%this.size;}
       else if (y< this.A+this.B+this.C){this.nw=(this.nw+4)%this.size;}
       else{
         int nr = reader.nextInt();
         //System.out.format("Using %d\n", nr);
         this.nw = (nr)%this.size;
        }
  }

  // Adds n-pages to the Process
  public void addPages(int n){
    this.pages = new Page[n];
    for(int i=0;i<n;i++){
      this.pages[i] = new Page(i, this);
    }
  }
}
