package Compiler;

import CodePackage.FourAddressCode;
import CodePackage.FunctionDefineCode;
import CodePackage.LabelCode;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;


public class DataStructure {
   private HashMap<String, SymbolTable> symbolTables; //存所有的符号表
   private HashMap<String, Integer> tmpExpMap; //临时变量（符号）表
   private ArrayList<FourAddressCode> intermediaCodeList; // 中间代码集
   private HashMap<String, Function> functionMap; //函数表
   private HashMap<String, Label> labelMap; // <labelName, Label>
   private HashMap<String, LabelCode> labelCodeMap;
   private HashMap<String, FunctionDefineCode> funcDefCodeMap; // <funcName, code>
   private int mainEntry; // main入口的第一条指令codeNo


   public DataStructure() {
      symbolTables = new HashMap<>();
      tmpExpMap = new HashMap<>();
      intermediaCodeList = new ArrayList<>();
      functionMap = new HashMap<>();
      labelMap = new HashMap<>();
      funcDefCodeMap = new HashMap<>();
      String globalSymbolTableName = Consts.GLOBAL_SCOPE;
      SymbolTable globalSymbolTable = new SymbolTable(globalSymbolTableName, "");
      symbolTables.put(globalSymbolTableName, globalSymbolTable);
      mainEntry = 0;

   }

   public void printIntermediaCode(PrintStream printStream) {
      for (FourAddressCode code : intermediaCodeList) {
         printStream.println(code.getCodeNo() + " " + code.toString());
      }
   }

   public void addSymbolTable(SymbolTable symbolTable) {
      symbolTables.put(symbolTable.getTableName(), symbolTable);
   }

   public void addIntermediaCode(FourAddressCode fourAddressCode) {
      intermediaCodeList.add(fourAddressCode);
   }

   /**
    * if the funcName is duplicated,
    * @param newFunc
    * @return
    */
   public void addFunction(Function newFunc) {
      functionMap.put(newFunc.getFuncName(), newFunc);
   }

   // PRE: the function has been defined and added to the functionMap
   public Function getFunction(String blocakName) {
      String funcName = getFuncName(blocakName);
      return functionMap.get(funcName);
   }

   private String getFuncName(String blockName) {
      int endIndex = blockName.indexOf('^');
      if (endIndex == -1) {
         return blockName;
      }
      String res = blockName.substring(0, endIndex);
      return res;
   }

   public int getFuncEntry(String funcName) {
      int entryCodeNo = funcDefCodeMap.get(funcName).getCodeNo();
      return entryCodeNo;
   }

   /**
    * check if the function has been defined
    * @param funcName
    * @return
    */
   public boolean isDefinedFunction(String funcName) {
      if (functionMap.containsKey(funcName)) {
         return true;
      } else {
         return false;
      }
   }

   /**
    * check whether the funcName is new
    * @param funcName
    * @return
    */
   public boolean isNewFunction(String funcName) {
      if (functionMap.containsKey(funcName)) {
         return false;
      } else {
         return true;
      }
   }

   // some helper functions

   /**
    * if it's a function, then the block name is the functionName itself;
    * else if not, add "^b"
    * 不存在global的block
    * @param fatherBlockName
    * @param isFuncName
    * @return
    */
   public String generateBlockName(String fatherBlockName, boolean isFuncName) {
      if (isFuncName) {
         return fatherBlockName; // "func1"
      } else {
         return fatherBlockName + "^b"; // "func1^b", "func1^b^b", "global"
      }
   }

   /**
    * "func1^b" -> "func1"; "func1^b^b" -> "func1^b"
    * "func1" -> Consts.GLOBAL_SCOPE
    * Consts.GLOBAL_SCOPE -> ""
    * @param blockName
    * @return
    */
   private String getFatherBlockName(String blockName) {
      if (blockName.equals(Consts.GLOBAL_SCOPE)) {
         return "";
      }
      String fatherBlockName = Consts.GLOBAL_SCOPE;
      blockName.replaceAll(Consts.WHILE_BLOCK, ""); // drop all the ^while
      if (blockName.contains("^")) {
         fatherBlockName = blockName.substring(0, blockName.length() - 2);
      }
      return fatherBlockName;
   }



   /**
    * judge whether the const/var is a local const/var
    * based on the blockName
    * if no blockName, it's global.
    * @param blockName
    * @return
    */
   public boolean isLocal(String blockName) {
      if (blockName.equals("")) {
         return false; // global symbolItem
      } else {
         return true;
      }
   }

   public SymbolTable getSymbolTable(String blockName) {
      if (symbolTables.containsKey(blockName)) {
         return symbolTables.get(blockName);
      } else {
         String fatherBlock = getFatherBlockName(blockName);
         return addSymbolTable(blockName, fatherBlock);
      }
   }

   public HashMap<String, Integer> getTmpExpMap() {
      return tmpExpMap;
   }

   public SymbolTable addSymbolTable(String blockName, String fatherBlock) {
      SymbolTable newSymbolTable = new SymbolTable(blockName, fatherBlock);
      symbolTables.put(blockName, newSymbolTable);
      return newSymbolTable;
   }

   public void addTmpExpression(Expression expression) throws MyException {
      if (expression.getExpType() != Consts.ExpType.TEMP_VAR) {
         throw new MyException("should add temp expression");
      }
      tmpExpMap.put(expression.getName(), 0);
   }

   // 添加符号前，根据symbolTableName和symbolItemName查表，只查一层
   public boolean lookupTableBeforeAdd(String blockName, String identifierName) {
      return symbolTables.get(blockName).tableContainsItem(identifierName);
   }

   // 使用符号时，查表，链式查找
   public SymbolItem lookupTableWhenUse(String blockName, String identifierName) {
      SymbolTable curTable = symbolTables.get(blockName);
      SymbolItem res = curTable.lookupItem(identifierName);
      String fatherBlockName = curTable.getFather();
      while (!fatherBlockName.equals("") && res == null) {
         curTable = symbolTables.get(fatherBlockName);
         res = curTable.lookupItem(identifierName);
         fatherBlockName = curTable.getFather();
      }
      return res;
   }

   /**
    * PRE: this function must be exist
    * @param blockName
    * @return
    */
   public Function lookupFunction(String blockName) {
      SymbolTable curTable = symbolTables.get(blockName);
      String curTableName = curTable.getTableName();
      Function function = functionMap.get(curTableName);
      while(function == null && !curTable.getFather().equals(Consts.GLOBAL_SCOPE)) {
         curTableName = curTable.getFather();
         function = functionMap.get(curTableName);
         curTable = symbolTables.get(curTableName);
      }
      return function;
   }

   public void addLabel(Label newLabel) {
      String labelName = newLabel.getLabelName();
      labelMap.put(labelName, newLabel);
   }

   // PRE: Label已存在
   public Label getLabel(String labelName) {
      Label retLabel = labelMap.get(labelName);
      if (retLabel == null) {
         throw new AssertionError();
      }
      return retLabel;
   }

   public Label getLastOrLabel() {
      String labelName = Label.getLastOrLabelName();
      return labelMap.get(labelName);
   }

   public Label getLastAndLabel() {
      String labelName = Label.getLastAndLabelName();
      return labelMap.get(labelName);
   }

   public void creatSomeCodeTable() {
      labelCodeMap = new HashMap<>();
      funcDefCodeMap = new HashMap<>();
      for(FourAddressCode code : intermediaCodeList) {
         if (code.getType() == Consts.FourAddressType.LABEL) {
            String labelName = ((LabelCode) code).getLabelName();
            labelCodeMap.put(labelName, (LabelCode) code);
         } else if (code.getType() == Consts.FourAddressType.FUNCTION_DEF) {
            String funcName = ((FunctionDefineCode) code).getFuncName();
            funcDefCodeMap.put(funcName, (FunctionDefineCode) code);
         }
      }
   }

   // PRE: labelName do exist
   public LabelCode getLabelCode(String labelName) {
      LabelCode code = labelCodeMap.get(labelName);
      if (code == null) {
         throw new AssertionError();
      }
      return code;
   }

   public HashMap<String, SymbolTable> getSymbolTables() {
      return symbolTables;
   }

   public ArrayList<FourAddressCode> getIntermediaCodeList() {
      return intermediaCodeList;
   }

   public int getCurrentCodeNum() {
      return intermediaCodeList.size();
   }

   public int getMainEntry() {
      return mainEntry;
   }

   public void setMainEntry(int codeNo) {
      mainEntry = codeNo;
   }
}
