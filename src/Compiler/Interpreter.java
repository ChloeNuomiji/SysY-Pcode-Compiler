package Compiler;

import CodePackage.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;

public class Interpreter {
   private String inputAddress; // 输入文件地址
   private ArrayList<Integer> inputIntList;
   private int intIndex;
   private String resultAddress; // 输出文件地址
   private PrintStream outFile;
   private DataStructure dataStructure;
   private Consts.PrintMode printmode;
   private HashMap<String, SymbolTable> symbolTables; // 存所有的符号表
   private HashMap<String, Integer> tmpExpMap; //临时变量（符号）表 <ti, value>
   private ArrayList<FourAddressCode> intermediaCodeList; // 中间代码
   private ArrayList<RecordItem> runningStack; // 运行栈
   private Stack<Integer> recordAddressStack; // 运行时，活动记录的base address的栈
   private Stack<Integer> callPCStack; // 函数调用的栈，记录pc
   private Stack<Integer> callMemoryPtrStack; // 函数调用的栈，记录baseAddress
   private ArrayList<Integer> argumentValueList; // 用于函数传参, 值 or 地址
   private Integer returnValue; // 用于记录函数调用返回值
   //private String tmpRetExpName; // ti = add(1,2);
   private Stack<Expression> retTargetExpStack;
   private int pc;//下一条要执行的指令的address
   private int stackPointer; // address from high to low
   private int globalPointer; // point to the top of the global data space (from low to high)
   private boolean interpretFinish;
   private boolean isDebug;

   public Interpreter(String inputAddress, String resultAddress, DataStructure dataStructure, boolean isDebug, Consts.PrintMode printmode) throws FileNotFoundException {
      this.inputAddress = inputAddress;
      this.resultAddress = resultAddress;
      this.isDebug = isDebug;
      outFile = new PrintStream(resultAddress);
      inputIntList = processInputFIle();
      intIndex = 0;
      this.dataStructure = dataStructure;
      this.printmode = printmode;
      this.symbolTables = dataStructure.getSymbolTables();
      this.tmpExpMap = dataStructure.getTmpExpMap();
      this.intermediaCodeList = dataStructure.getIntermediaCodeList();
      initialMemory(); // initial the memory
      pc = 0;
      stackPointer = Consts.MEMORY_HIGHEST_ADDRESS - 1;
      globalPointer = Consts.MEMORY_LOWEST_ADDRESS;
      recordAddressStack = new Stack<>();
      //recordAddressStack.push(stackPointer);
      callPCStack = new Stack<>();
      //callPCStack.push(dataStructure.getMainEntry());
      callMemoryPtrStack = new Stack<>();
      retTargetExpStack = new Stack<>();
      //callMemoryPtrStack.push(stackPointer);
      argumentValueList = new ArrayList<>();
      returnValue = 0;
      interpretFinish = false;
   }

   private ArrayList<Integer> processInputFIle() throws FileNotFoundException {
      Scanner scanner = null;
      if (isDebug) {
         File inFile = new File(inputAddress);
         scanner = new Scanner(inFile);
      }
      if (!isDebug) {
         scanner = new Scanner(System.in);
      }
      ArrayList<Integer> res = new ArrayList<>();
      while(scanner.hasNext()) {
         Integer number = scanner.nextInt();
         res.add(number);
      }
      return res;
   }

   private void initialMemory() {
      runningStack = new ArrayList<RecordItem>();
      for (int i = 0; i < Consts.MEMORY_HIGHEST_ADDRESS; i++) {
         runningStack.add(null);
      }
   }

   public void startInterpret() throws Exception {
      pc = 0;
      FourAddressCode code = intermediaCodeList.get(pc);
      while (code.getType() == Consts.FourAddressType.VAR_DECL_NO_INIT || code.getType() == Consts.FourAddressType.VAR_DECL_WITH_INIT
      || code.getType() == Consts.FourAddressType.CONST_DECL || code.getType() == Consts.FourAddressType.ASSIGN_STATE) {
         // must be global declare code OR assign code
         if (code.getType() == Consts.FourAddressType.ASSIGN_STATE) {
            assignCode((AssignCode) code);
         } else {
            globalDeclCode((DeclareCode) code);
         }
         code = intermediaCodeList.get(pc);
      }
      pc = dataStructure.getMainEntry(); // main入口的第一条指令codeNo
      code = intermediaCodeList.get(pc);
      while (!interpretFinish) { //code.getType() != Consts.FourAddressType.END_Code
         // interpret code
         switch (code.getType()) {
            case ADD_RECORD:
               addRecordCode((AddRecordCode) code);
               break;
            case ASSIGN_STATE:
               assignCode((AssignCode) code);
               break;
            case BEQ:
               beqCode((BranchEqualCode) code);
               break;
            case COMPARE_STATE:
               compareCode((CompareCode) code);
               break;
            case DELETE_RECORD:
               deleteRecordCode((DeleteRecordCode) code);
               break;
            case VAR_DECL_NO_INIT: // DeclareCode
            case VAR_DECL_WITH_INIT:
            case CONST_DECL:
               localDeclCode((DeclareCode) code);
               break;
            case FUNCTION_DEF:
               functionDefineCode((FunctionDefineCode) code);
               break;
            case FUNCTION_CALL:
               callFunctionCode((CallFunctionCode) code);
               break;
            case GOTO:
               gotoCode((GotoCode) code);
               break;
            case LABEL:
               labelCode((LabelCode) code);
               break;
            case PRINT_FMTSTR:
               printCode((PrintCode) code);
               break;
            case READ_INT:
               readCode((ReadCode) code);
               break;
            case RETURN_INT:
            case RETURN_VOID:
               returnCode((ReturnCode)code);
               break;
            default:
         }
         if (pc >= intermediaCodeList.size()) {
            break;
         }
         code = intermediaCodeList.get(pc);
      }
   }

   private void addRecordCode(AddRecordCode code) {
      int baseAddress = stackPointer; // 活动记录基地址
      recordAddressStack.push(baseAddress); // push the new Record's baseAddress
      pc++;
   }

   private void deleteRecordCode(DeleteRecordCode code) {
      int setStackPtr = recordAddressStack.pop();
      stackPointer = setStackPtr;
      pc++;
   }

   /**
    * 将const array/int or var array/int push in global memory
    * @param code
    */
   private void globalDeclCode(DeclareCode code) {
      SymbolItem symbolItem = code.getDeclareSymbol();
      int dimension = symbolItem.getDimension();
      if (dimension == 0) {
         RecordItem recordItem = new RecordItem(globalPointer, symbolItem);
         recordItem.setValue(0);
         runningStack.set(globalPointer, recordItem);
         globalPointer++; // no matter array or var
      } else if (dimension == 1) {
         int index1 = symbolItem.getSize1();
         for (int i = 0; i < index1; i++) {
            RecordItem recordItem = new RecordItem(globalPointer, symbolItem, i);
            recordItem.setValue(0);
            runningStack.set(globalPointer, recordItem);
            globalPointer++;
         }
      } else {
         int index1 = symbolItem.getSize1();
         int index2 = symbolItem.getSize2();
         for (int i = 0; i < index1; i++) {
            for (int j = 0; j < index2; j++) {
               RecordItem recordItem = new RecordItem(globalPointer, symbolItem, i, j);
               recordItem.setValue(0);
               runningStack.set(globalPointer, recordItem);
               globalPointer++;
            }
         }
      }
      pc++;
   }

   /**
    * 将const array/int or var array/int push in stack
    *  new RecordItem and sp will update
    * @param code
    */
   private void localDeclCode(DeclareCode code) {
      SymbolItem symbolItem = code.getDeclareSymbol();
      int dimension = symbolItem.getDimension();
      if (dimension == 0) {
         RecordItem recordItem = new RecordItem(stackPointer, symbolItem);
         runningStack.set(stackPointer, recordItem);
         stackPointer--; // no matter array or var
      } else if (dimension == 1) {
         int index1 = symbolItem.getSize1();
         for (int i = 0; i < index1; i++) {
            RecordItem recordItem = new RecordItem(stackPointer, symbolItem, i);
            runningStack.set(stackPointer, recordItem);
            stackPointer--;
         }
      } else {
         int index1 = symbolItem.getSize1();
         int index2 = symbolItem.getSize2();
         for (int i = 0; i < index1; i++) {
            for (int j = 0; j < index2; j++) {
               RecordItem recordItem = new RecordItem(stackPointer, symbolItem, i, j);
               runningStack.set(stackPointer, recordItem);
               stackPointer--;
            }
         }
      }
      pc++;
   }

   // t1 = a[1][2] * t0
   // t0 = a +
   // t4 = arr + 3
   private void assignCode(AssignCode code) throws Exception {
      try {
         Expression targetExp = code.getTargetExp();
         Expression leftExp = code.getLeftExp();
         Expression rightExp = code.getRightExp();
         String blockName = code.getBlockName();
         Character op = code.getOp();
         Consts.ValueType leftExpValueType = leftExp.getValueType();
         Consts.ValueType rightExpValueType = rightExp.getValueType();

         Integer leftValue;
         Integer rightValue;
         int targetValue = 0;
         boolean isAddressResult = false;
         // t4 = arr + 3
         if (leftExpValueType == Consts.ValueType.ADDRESS1 || rightExpValueType == Consts.ValueType.ADDRESS1
            || leftExpValueType == Consts.ValueType.ADDRESS2 || rightExpValueType == Consts.ValueType.ADDRESS2) {
            isAddressResult = true;
            int offset = 0;
            if (leftExpValueType == Consts.ValueType.ADDRESS1 || leftExpValueType == Consts.ValueType.ADDRESS2) {
               leftValue = getRecordItemAddress(leftExp.getName(), blockName);
               rightValue = getExpressionValue(rightExp, blockName);
               offset = getExpressionOffset(leftExp, blockName);
               targetValue = (op == '+') ? leftValue - offset * rightValue :
                              (op == '-') ? leftValue + offset * rightValue : null;
            } else {
               rightValue = getRecordItemAddress(rightExp.getName(), blockName);
               leftValue = getExpressionValue(leftExp, blockName);
               offset = getExpressionOffset(rightExp, blockName);
               targetValue = (op == '+') ? offset * leftValue - rightValue: null;
            }
         } else { //t4 = arr[1][4] + 3
            leftValue = getExpressionValue(leftExp, blockName);
            rightValue = getExpressionValue(rightExp, blockName);
            if (op == '!') {
               targetValue = rightValue == 0 ? 1 : 0;
            } else {
               targetValue = (op == '+') ? leftValue + rightValue :
                              (op == '-') ? leftValue - rightValue :
                               (op == '*') ? leftValue * rightValue :
                               (op == '/') ? leftValue / rightValue :
                               (op == '%') ? leftValue % rightValue : null;
            }
         }
         // assign
         String targetName = targetExp.getName();
         Consts.ExpType targetType = targetExp.getExpType();
         Integer targetIndex1 = getExpressionValue(targetExp.getIndex1(), blockName);
         Integer targetIndex2 = getExpressionValue(targetExp.getIndex2(), blockName);
         setRecordItemValue(targetValue, targetName, targetType, blockName, targetIndex1, targetIndex2);
         pc++;
      } catch (Exception e) {
         System.out.println(code);
         System.exit(0);
      }

   }

   // if exp1.value == exp2.value, then pc jump
   // else, sequence pc++;
   private void beqCode(BranchEqualCode code) throws MyException {
      Expression exp1 = code.getExp1();
      Expression exp2 = code.getExp2();
      String blockName = code.getBlockName();
      Integer value1 = getExpressionValue(exp1, blockName);
      Integer value2 = getExpressionValue(exp2, blockName);
      if (value1 == value2) {
         String labelName = code.getGoToLabel().getLabelName();
         int pcJumpTo = dataStructure.getLabelCode(labelName).getCodeNo();
         pc = pcJumpTo;
      } else {
         pc++;
      }
   }

   // 先把当前pc+1入callStack栈
   // 计算实参值，传参
   // 再调整pc 指向函数入口
   // todo: 传入array地址还没做
   private void callFunctionCode(CallFunctionCode code) throws MyException {
      callPCStack.push(pc + 1);
      callMemoryPtrStack.push(stackPointer);
      ArrayList<Expression> arguments = code.getArgumentList();
      String blockName = code.getBlockName();
      retTargetExpStack.push(code.getResultExp());
      //tmpRetExpName = code.getResultExp().getName();// must be temp exp
      argumentValueList = new ArrayList<>();
      for (int i = 0; i < arguments.size(); i++) {
         Expression argument = arguments.get(i);
         Consts.ValueType argValueType = argument.getValueType();
         Consts.ExpType expType = argument.getExpType();
         Integer value = null; // todo: 从stack还是global里面调
         if (expType == Consts.ExpType.GLOBAL_VAR || expType == Consts.ExpType.LOCAL_VAR) {
            String identifierName = argument.getName();
            RecordItem baseRecord = findBaseRecord(identifierName, blockName);
            if (argValueType == Consts.ValueType.ADDRESS2 || argValueType == Consts.ValueType.ADDRESS1) {
               value = baseRecord.getAddress();
               if (argValueType == Consts.ValueType.ADDRESS1) {
                  boolean isGlobalAddress = isGlobalAddress(baseRecord.getAddress());
                  Integer index1 = getExpressionValue(argument.getIndex1(), blockName);
                  if (index1 != null) {
                     int offset = baseRecord.getSize2();
                     if (isGlobalAddress) { //def: array[10][100], call:array[2],
                        value += index1 * offset;
                     } else {
                        value -= index1 * offset;
                     }
                  }
               }
            } else {
               value = getExpressionValue(argument, blockName);
            }
         } else {
            value = getExpressionValue(argument, blockName);
         }
         argumentValueList.add(value);
      }
      String funcName = code.getFuncName();
      int funcEntry = dataStructure.getFuncEntry(funcName);
      pc = funcEntry;
   }

   private void functionDefineCode(FunctionDefineCode code) throws MyException {
      recordAddressStack.push(stackPointer); // 把刚进入函数，还未将参数和变量加进活动记录的基地址入栈
      ArrayList<FormalParameter> parameters = code.getFormalParameters();
      // 将参数 add Record，
      if (code.getFuncName().equals("main") || parameters == null) {
         pc++;
         return ;
      }
      for (int i = 0; i < parameters.size(); i++) {
         FormalParameter parameter = parameters.get(i);
         Consts.ValueType paraValueType = parameter.getValueType();
         RecordItem recordItem;
         int valueOrAddress = argumentValueList.get(i);
         if (paraValueType == Consts.ValueType.ADDRESS1 || paraValueType == Consts.ValueType.ADDRESS2) {
            // the parameter is array
            recordItem = new RecordItem(stackPointer, parameter, true);
            isGlobalAddress(valueOrAddress);
         } else {
            // the parameter is value
            recordItem = new RecordItem(stackPointer, parameter, false);
         }
         runningStack.set(stackPointer, recordItem);
         recordItem.setValue(valueOrAddress);
         stackPointer--; // no matter array or var
      }
      pc++;
   }

   private boolean isGlobalAddress(int address) {
      if (address < globalPointer) {
         return true;
      } else {
         return false;
      }
   }

   // callStack pop调整pc的位置，
   // stackPointer 连续pop到正确的位置？
   // retTargetExpStack出栈
   // 计算返回值，给tmp var赋值
   // 退栈，调整pc的位置，
   private void returnCode(ReturnCode code) throws MyException {
      Consts.FourAddressType codeType = code.getType();
      if (callPCStack.size() == 0) {
         interpretFinish = true;// int main() return 0;
      } else {
         String tmpRetExpName = retTargetExpStack.pop().getName();
         if (codeType == Consts.FourAddressType.RETURN_VOID) {
            returnValue = null;
            tmpExpMap.put(tmpRetExpName, returnValue);
         } else {
            Expression returnExp = code.getReturnExp();
            String blockName = code.getBlockName();
            returnValue = getExpressionValue(returnExp, blockName);
            tmpExpMap.put(tmpRetExpName, returnValue);
         }
         int pcToJump = callPCStack.pop();
         int stackPtr = callMemoryPtrStack.pop();
         int baseAddress = recordAddressStack.pop();
         while (baseAddress != stackPtr) {
            baseAddress = recordAddressStack.pop();
         }
         stackPointer = stackPtr;
         pc = pcToJump;
      }
   }

   private void compareCode(CompareCode code) throws MyException {
      Expression targetExp = code.getTargetExp();
      Expression leftExp = code.getLeftExp();
      Expression rightExp = code.getRightExp();
      String blockName = code.getBlockName();
      Consts.SymbolType compareOp = code.getCompareSymbol();
      int leftValue = getExpressionValue(leftExp, blockName);
      int rightValue = 0;
      if (compareOp != Consts.SymbolType.AND && compareOp != Consts.SymbolType.OR) {
         rightValue = getExpressionValue(rightExp, blockName);
      }
      int targetValue = 0;
      String targetName = targetExp.getName();
      Consts.ExpType targetType = targetExp.getExpType();
      Integer targetIndex1 = getExpressionValue(targetExp.getIndex1(), blockName);
      Integer targetIndex2 = getExpressionValue(targetExp.getIndex2(), blockName);
      switch (compareOp) {
         case LSS: // <
            if (leftValue < rightValue) {
               targetValue = 1;
            }
            break;
         case LEQ: // <=
            if (leftValue <= rightValue) {
               targetValue = 1;
            }
            break;
         case GRE: // >
            if (leftValue > rightValue) {
               targetValue = 1;
            }
            break;
         case GEQ: // >=
            if (leftValue >= rightValue) {
               targetValue = 1;
            }
            break;
         case EQL: // ==
            if (leftValue == rightValue) {
               targetValue = 1;
            }
            break;
         case NEQ: // !=
            if (leftValue != rightValue) {
               targetValue = 1;
            }
            break;
         case AND:
            if (leftValue != 0) {
               rightValue = getExpressionValue(rightExp, blockName);
               if (rightValue != 0) {
                  targetValue = 1;
               }
            }
            break;
         case OR:
            if (leftValue != 0) {
               targetValue = 1;
            } else { // leftValue == 0
               rightValue = getExpressionValue(rightExp, blockName);
               if (rightValue != 0) {
                  targetValue = 1;
               }
            }
            break;
         default:
      }
      setRecordItemValue(targetValue, targetName, targetType, blockName, targetIndex1, targetIndex2);
      pc++;
   }

   private void gotoCode(GotoCode code) {
      String labelName = code.getGotoLabel().getLabelName();
      int pcJumpTo = dataStructure.getLabelCode(labelName).getCodeNo();
      pc = pcJumpTo;
   }

   private void labelCode(LabelCode code) {
      pc++;
   }

   private void printCode(PrintCode code) throws FileNotFoundException, MyException {
      ArrayList<Expression> expList = code.getPrintExps();
      ArrayList<Integer> valueList = new ArrayList<>();
      String blockName = code.getBlockName();
      for (int i = 0; i < expList.size(); i++) {
         Expression exp = expList.get(i);
         Integer value = getExpressionValue(exp, blockName);
         valueList.add(value);
      }
      String formatString = code.getFormatString();
      String printString = "";
      formatString = formatString.substring(1, formatString.length() - 1); // delete the beginning and ending "
      int index = 0;
      for (int i = 0; i < formatString.length(); i++) {
         char c = formatString.charAt(i);
         if (c == '%') {
            i++;
            printString += valueList.get(index).toString();
            index++;
         } else if (c == '\\') {
            i++;
            printString += "\n";
         }
         else {
            printString += c;
         }
      }
      printFormatString(printString);
      pc++;
   }

   /*
   向result文件以及终端输出编译解析程序的结果
   printf("%d", a);
    */
   private void printFormatString(String printStr) throws FileNotFoundException {
      PrintStream outConsole = System.out;
      if (printmode == Consts.PrintMode.BOTH_CONSOLE_AND_FILE) {
         System.setOut(outFile);
         System.out.printf(printStr);
         System.setOut(outConsole);
         System.out.printf(printStr);
      } else if (printmode == Consts.PrintMode.ONLY_TO_FILE) {
         System.setOut(outFile);
         System.out.printf(printStr);
      } else if (printmode == Consts.PrintMode.ONLY_TO_CONSOLE) {
         System.setOut(outConsole);
         System.out.printf(printStr);
      }
      System.setOut(outConsole);
   }

   private void readCode(ReadCode code) throws Exception {
      Expression targetExp = code.getTargetExp();
      String blockName = code.getBlockName();
      if (intIndex >= inputIntList.size()) {
         throw new Exception("lack of input int");
      }
      int value = inputIntList.get(intIndex);
      intIndex++;
      // assign value to the recordItem
      String targetName = targetExp.getName();
      Consts.ExpType targetType = targetExp.getExpType();
      Integer targetIndex1 = getExpressionValue(targetExp.getIndex1(), blockName);
      Integer targetIndex2 = getExpressionValue(targetExp.getIndex2(), blockName);
      setRecordItemValue(value, targetName, targetType, blockName, targetIndex1, targetIndex2);
      pc++;
   }


   /**
    * find recordItem in memory, based on name
    * @param name, var, array[1], array2[1][2], should give specific index
    * @param blockName used to confirm a local or global identifier
    * @return
    */
   private RecordItem findRecordItem(String name, String blockName) {
      int lookupPtr = 0;
      if (!blockName.equals(Consts.GLOBAL_SCOPE)) {
         lookupPtr = stackPointer + 1;
         while (lookupPtr < Consts.MEMORY_HIGHEST_ADDRESS) {
            String itemName = runningStack.get(lookupPtr).getName();
            if (name.equals(itemName)) {
               RecordItem result = runningStack.get(lookupPtr);
               return result;
            }
            lookupPtr++;
         }
      }
      // if record is in global memory
      lookupPtr = 0;
      while (lookupPtr < globalPointer) {
         String itemName = runningStack.get(lookupPtr).getName();
         if (name.equals(itemName)) {
            return runningStack.get(lookupPtr);
         }
         lookupPtr++;
      }
      return null;
   }

   // using identifier to find the base array or the var
   private RecordItem findBaseRecord(String identifierName, String blockName) {
      int lookupPtr = 0;
      if (!blockName.equals(Consts.GLOBAL_SCOPE)) {
         lookupPtr = stackPointer + 1;
         while (lookupPtr < Consts.MEMORY_HIGHEST_ADDRESS) {
            String itemIdentifier = runningStack.get(lookupPtr).getIdentifierName();
            if (identifierName.equals(itemIdentifier)) {
               RecordItem result = runningStack.get(lookupPtr);
               if (result.isArray()) {
                  String name1 = identifierName + "[0]";
                  String name2 = identifierName + "[0][0]";
                  while (lookupPtr < Consts.MEMORY_HIGHEST_ADDRESS) {
                     String itemName = runningStack.get(lookupPtr).getName();
                     if (name1.equals(itemName) || name2.equals(itemName)) {
                        result = runningStack.get(lookupPtr);
                        return result;
                     }
                     lookupPtr++;
                  }
               } else {
                  return result;
               }
            }
            lookupPtr++;
         }
      }
      // if record is in global memory
      lookupPtr = 0;
      while (lookupPtr < globalPointer) {
         String itemName = runningStack.get(lookupPtr).getIdentifierName();
         if (identifierName.equals(itemName)) {
            return runningStack.get(lookupPtr);
         }
         lookupPtr++;
      }
      return null;
   }

   /**
    * 前置条件：该标识符一定已插入到活动记录中
    * @param identifierName
    * @param blockName
    * @return
    */
   private int getRecordItemValue(String identifierName, String blockName, Integer i1, Integer i2) throws MyException {
         String recordName = "";
         int value = 0;
         RecordItem recordItem = findRecordItem(identifierName, blockName);
         if (recordItem != null && recordItem.isPointer()) { // 数组的参数传递
            int baseAddress = recordItem.getValue();
            boolean isGlobalAddress = isGlobalAddress(baseAddress);
            if (i1 == null && i2 ==  null) {
               value = runningStack.get(baseAddress).getValue();
            } else if (i1 != null && i2 == null) {
               if (isGlobalAddress) {
                  value = runningStack.get(baseAddress + i1).getValue();
               } else {
                  value = runningStack.get(baseAddress - i1).getValue();
               }

            } else {
               int paraConst2 = getExpressionValue(recordItem.getParaConst2(), blockName);
               if (isGlobalAddress) {
                  value = runningStack.get(baseAddress + (i1 * paraConst2 + i2)).getValue();
               } else {
                  RecordItem item = runningStack.get(baseAddress - (i1 * paraConst2 + i2));
                  value = item.getValue();
               }
            }
         } else {
            if (i1 == null && i2 == null) {
               recordName = identifierName;
            } else if (i1 != null && i2 == null) {
               recordName = identifierName + "[" + i1 + "]";
            } else {
               recordName = identifierName + "[" + i1 + "][" + i2 + "]";
            }
            recordItem = findRecordItem(recordName, blockName);
            if (recordItem == null) {
               System.out.println(recordName);
            }
            value = recordItem.getValue();
         }
         return value;
   }


   /**
    * 前置条件：该标识符一定已插入到活动记录中
    * 返回该标识符（通常是array）在活动记录中的地址
    * @param identifierName
    * @param blockName
    * @return
    * array_dim1, array_dim2[2], array[][]
    * the baseAddress
    */
   private int getRecordItemAddress(String identifierName, String blockName) {
      //RecordItem recordItem = findRecordItem(identifierName, blockName);
      RecordItem recordItem = findBaseRecord(identifierName, blockName);
      return recordItem.getAddress();
   }

   /**
    *
    * @param identifierName or tempVar name
    * @param baseBlockName
    * @param i1
    * @param i2
    */
   public void setRecordItemValue(int newValue, String identifierName, Consts.ExpType targetType,
                                  String baseBlockName, Integer i1, Integer i2) throws MyException {
      if (targetType == Consts.ExpType.TEMP_VAR) {
         if (!tmpExpMap.containsKey(identifierName)) {
            throw new MyException("The var " + identifierName + " does not exist");
         }
         tmpExpMap.put(identifierName, newValue);
      } else {
         RecordItem recordItem = findRecordItem(identifierName, baseBlockName);

         if (recordItem != null && recordItem.isPointer()) { // 数组的参数传递
            int baseAddress = recordItem.getValue();
            boolean isGlobalAddress = isGlobalAddress(recordItem.getAddress());
            if (i1 == null && i2 ==  null) {
               runningStack.get(baseAddress).setValue(newValue);
            } else if (i1 != null && i2 == null) {
               if (isGlobalAddress) {
                  runningStack.get(baseAddress + i1).setValue(newValue);
               } else {
                  runningStack.get(baseAddress - i1).setValue(newValue);
               }

            } else {
               int paraConst2 = getExpressionValue(recordItem.getParaConst2(), baseBlockName);
               if (isGlobalAddress) {
                  runningStack.get(baseAddress + (i1 * paraConst2 + i2)).setValue(newValue);
               } else {
                  runningStack.get(baseAddress - (i1 * paraConst2 + i2)).setValue(newValue);
               }
            }
         } else {
            String recordName = "";
            if (i1 == null && i2 == null) {
               recordName = identifierName;
            } else if (i1 != null && i2 == null) {
               recordName = identifierName + "[" + i1 + "]";
            } else {
               recordName = identifierName + "[" + i1 + "][" + i2 + "]";
            }
            recordItem = findRecordItem(recordName, baseBlockName);
            if (recordItem == null) {
               throw new MyException(identifierName + "does not exist");
            }
            recordItem.setValue(newValue);
         }
      }
   }

   public Integer getExpressionValue(Expression expression, String bloackName) throws MyException {
      if (expression == null) {
         return null;
      }
      Consts.ValueType valueType = expression.getValueType();
      Consts.ExpType expType = expression.getExpType();
      String expName = expression.getName();
      int value = 0;
      if (valueType == Consts.ValueType.INT_TYPE) {
         switch (expType) {
            case DIGIT:
               value = expression.getNumber();
               break;
            case LOCAL_VAR:
            case GLOBAL_VAR:
               Expression index1 = expression.getIndex1();
               Expression index2 = expression.getIndex2();
               Integer i1 = getExpressionValue(index1, bloackName);
               Integer i2 = getExpressionValue(index2, bloackName);
               value = getRecordItemValue(expName, bloackName, i1, i2); // todo
               break;
            case TEMP_VAR:
               value = tmpExpMap.get(expName);
               break;
            default:
               break;
         }
      } else if (valueType == Consts.ValueType.ADDRESS1 || valueType == Consts.ValueType.ADDRESS2) {
         // array_dim1, array_dim2,
         // the baseAddress
         if (expression.getExpType() == Consts.ExpType.TEMP_VAR) {
            value = tmpExpMap.get(expName);
         } else {
            value = getRecordItemAddress(expName, bloackName);
         }
      }
      return value;
   }

   // sum(arr + 3), given arr, I need to calculate the offset == 1
   public Integer getExpressionOffset(Expression expression, String bloackName) {
      String identifierName = expression.getName();
      int dimExp = expression.getDimension(); // if dim == 1, arr[]; if dim == 0
      RecordItem recordItem = findBaseRecord(identifierName, bloackName);
      int dimRecord = recordItem.getDimension();
      if (dimRecord == 1) {
         return 1;// array[] to use array, -->
      } else if (dimRecord == 2) {
         int size1 = recordItem.getSize1();
         int size2 = recordItem.getSize2();
         if (dimExp == 0) {
            return size2;//sum(arr + 3)
         } else if (dimExp == 1) {
            return 1; // arr[][] to use arr[], sum(arr[] + 3)
         }
      }
      // getRecord, arr[][], then get dimRecord
      // arr[][] to use arr[] / arr
      return 1;
   }




}
