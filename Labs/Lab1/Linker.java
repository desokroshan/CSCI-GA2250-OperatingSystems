import java.io.*;
import java.util.*;
import java.util.Map.Entry;

// Class to model content of each module
class Pair{
    String type;
    int content;
    Pair(String type, int content){
        this.type=type;
	this.content = content;
    }
    public String getType(){ return this.type; }
    public int getContent(){return this.content; }
    public void setType(String ty){this.type = ty;}
    public void setContent(int con){this.content = con;}
}

// Class to model Symbols of symbol code and associated
// error code if any
class SymbolData{
    int absAddr=0;
    int errCode=0;
    SymbolData(int absAddr){
	this.absAddr = absAddr;
    }
    public int getAbsAddr(){return this.absAddr;}
    public int getErrCode(){return this.errCode;}
    public void setAbsAddr(int absAddr){ this.absAddr=absAddr;}
    public void setErrCode(int errCode){ this.errCode = errCode;}
}

public class Linker{
public static void main(String [] args){
    FileReader in = null;
    int c;
    Scanner reader = null;
    // Used LinkedHashMap since it allows insertion order iteration; needed by output
    //1. Data Structures to store required information about modules
    LinkedHashMap<String, SymbolData> symbolTable = new LinkedHashMap<String, SymbolData>();
    HashMap<Integer, HashMap<String,Set<Integer>>> usageTable = new HashMap<Integer, HashMap<String, Set<Integer>>>();
    LinkedHashMap<Integer, ArrayList<Pair>> contentTable = new LinkedHashMap<Integer, ArrayList<Pair>>();
    HashMap<Integer, Integer> baseAddressTable = new HashMap<Integer, Integer>();
    Set<String> used = new HashSet<String>();
    Set<String> errorMessages = new HashSet<String>();
    LinkedHashMap<String, Integer> definedSymbols = new LinkedHashMap<String, Integer>();
    if(args.length==0){
      System.out.println("Error: Please provide input file name");
      System.exit(0);
    }
    try{
        reader = new Scanner(new File(args[0]));
    }catch (FileNotFoundException e){
        e.printStackTrace();
    }
    int numOfModules = reader.nextInt();
    int currBaseAddr = 0;

    // 2. Parse each module to populate all the data structures
    for(int i =0; i< numOfModules; i++){
	// 1. Parse the defined symbols
	int numDefinedSymbols = reader.nextInt();
        int absAddr=0;
        // Store base address for this module
        baseAddressTable.put((i+1), currBaseAddr);

        // Add defined symbols to the symbol table
        for (int j=0; j< numDefinedSymbols; j++){
            String sym = reader.next();
            absAddr = currBaseAddr+reader.nextInt();
            if (symbolTable.containsKey(sym)){
		symbolTable.get(sym).setErrCode(1);
	    }
	    else {
                symbolTable.put(sym, new SymbolData(absAddr));
                definedSymbols.put(sym, (i+1));
            }
	}
	
        //2. Parse all the symbols defined in this module
	int numUsedSymbols = reader.nextInt();
        String sym;
	int relAddr;
        HashMap<String, Set<Integer>> usedSym = new HashMap<String, Set<Integer>>();
        for (int j = 0; j < numUsedSymbols; j++){
            Set<Integer> usages = new HashSet<Integer>();
            sym = reader.next();
	    while((relAddr=reader.nextInt()) != -1){
	        usages.add(relAddr);
	    }
	    usedSym.put(sym, usages);
	    if (!used.contains(sym)){used.add(sym);}
        }
	usageTable.put((i+1), usedSym);

        //3. Parse the module content
        // Store Module Content in contentTable
        int modLength = reader.nextInt();
        //Set<Integer> content = new HashSet<Integer>();
        ArrayList<Pair> content = new ArrayList<Pair>();
	// Store module content
	for(int j =0 ; j< modLength; j++){
	    content.add(new Pair(reader.next(), reader.nextInt()));
	}
        contentTable.put((i+1), content);

	// 4. Check for module size violation error in symbol table ans usedsyms
	// 4.1 Check symbol table
        for(Map.Entry<String, SymbolData> entry : symbolTable.entrySet()){
	    SymbolData data = (SymbolData)entry.getValue();
            if (data.getAbsAddr() >= (currBaseAddr+modLength)){
		data.setErrCode(2);
  		data.setAbsAddr(currBaseAddr);
	    }
        }
        // 4.2 Check for used symbols
        for (Map.Entry<String, Set<Integer>> entry : usedSym.entrySet()){
	    String symbol = (String)entry.getKey();
            Set<Integer> usages = (Set<Integer>)entry.getValue();
            for (Integer use : usages){
	        if (use>=modLength){
                    errorMessages.add("Error: Use of " + symbol + " in module " + (i+1) +" exceeds module size; use ignored.");
                }
            }
        }
	//update base address
	currBaseAddr = currBaseAddr + modLength;
    }
    // Display Symbol Table
    Set<Entry<String,SymbolData>> hashSet=symbolTable.entrySet();
    System.out.println("Symbol Table");
    for(Entry entry:hashSet ){
	    String sym = (String)entry.getKey();
	    SymbolData data = (SymbolData)entry.getValue();
	    if (data.getErrCode() ==0){
		System.out.println(sym + "=" + data.getAbsAddr());
	    }
	    else if(data.getErrCode() == 2){
		System.out.println(sym + "=" + data.getAbsAddr() + " " + "Error: Definition exceeds module size; first word in module used.");
	    }
            else{
		System.out.println(sym + "=" + data.getAbsAddr() + " " + "Error: This variable is multiply defined; first value used.");
	    }
        }
    System.out.println("\nMemory Map");
    // Resolve relative address and external references
    int memLoc = 0;
    int errFlag = 0;
    for (int i =1 ; i<=numOfModules; i++){
	//for each module update their contents
	// get the array list
	ArrayList<Pair> contents = contentTable.get(i);
        HashMap<String, Set<Integer>> usedSyms = usageTable.get(i);

        int baseAddr = baseAddressTable.get(i);
	// Display the contents of array list
	int module_size = contents.size();
	for(int j =0; j < module_size;j++){
            String sym = null; int flag = 0;
            Pair pair = contents.get(j);
            if ((pair.type).equals("R")){
                // check relative address for module size violatio
                // and update accordingly
                if (pair.content%1000>module_size){errFlag=3;pair.setContent((pair.content/1000)*1000);}
		else {pair.setContent(pair.content + baseAddr);}
	    }
	    if ((pair.type).equals("E")){
                //find the symbol which is being refered
                // look for i in the use list of this module
                // for each symbol in the usedTable check if 

                for(Map.Entry<String, Set<Integer>> entry : usedSyms.entrySet()){
                    Set<Integer> useList = entry.getValue();
		    if (useList.contains(j)){
			if (flag ==0){ sym = entry.getKey();}
 		        else if (flag == 1){errFlag = 2;}
			flag++;
		    }
                }
		// Resolve external addresses
		// Check if symbol is present in symbol table
		if (sym !=null){
		    if(symbolTable.containsKey(sym)){
			pair.setContent((pair.content/1000)*1000 + symbolTable.get(sym).getAbsAddr());}
		    else{
			errFlag = 1;
			pair.setContent((pair.content/1000)*1000 + 0);
		    }
		}
	    }
	    if ((pair.type).equals("A")){
		if (pair.content%1000 > 199){errFlag=4;pair.setContent((pair.content/1000)*1000);}
	    }
            System.out.print(memLoc++ + " : ");
	    if(errFlag==1){
	        System.out.println(pair.getContent() + " Error: " + sym +" is not defined; zero used."); }
            else if(errFlag==2){
		System.out.println(pair.getContent() + " Error: Multiple variables used in instruction; all but first ignored.");
	    }
	    else if(errFlag==3){System.out.println(pair.getContent() + " Error: Relative address exceeds module size; zero used.");}
	    else if(errFlag==4){System.out.println(pair.getContent() + " Error: Absolute address exceeds machine size; zero used.");}
	    else{System.out.println(pair.getContent());}
            errFlag=0;
        }
    }

   // Print final warnings:
   System.out.println();
   for(Map.Entry<String, Integer> entry : definedSymbols.entrySet()){
        if(!used.contains((String)entry.getKey())){System.out.println("Warning: " + (String)entry.getKey() + " was defined in module "+(Integer)entry.getValue()+" but never used.");}
   }
   for(String messages : errorMessages){
       System.out.println(messages);
   }
 }
}
