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
   private HashMap<String, SymbolTable> symbolTables; // 存所有的符号表
   private HashMap<String, Integer> tmpExpMap; //临时变量（符号）表 <ti, value>
   private ArrayList<FourAddressCode> intermediaCodeList; // 中间代码
   private ArrayList<RecordItem> runningStack; // 运行栈
   private Stack<Integer> recordAddressStack; // 运行时，活动记录的base address的栈
   private Stack<Integer> callPCStack; // 函数调用的栈，记录pc
   private Stack<Integer> callMemoryPtrStack; // 函数调用的栈，记录baseAddress
   private ArrayList<Integer> argumentValueList; // 用于函数传参
   private Integer returnValue; // 用于记录函数调用返回值
   //private String tmpRetExpName; // ti = add(1,2);
   private Stack<Expression> retTargetExpStack;
   private int pc;//下一条要执行的指令的address
   private int stackPointer; // address from high to low
   private int globalPointer; // point to the top of the global data space (from low to high)
   private boolean interpretFinish;
   private boolean isDebug;

   public Interpreter(String inputAddress, String resultAddress, DataStructure dataStructure, boolean isDebug) throws FileNotFoundException {
      this.inputAddress = inputAddress;
      this.resultAddress = resultAddress;
      this.isDebug = isDebug;
      outFile = new PrintStream(resultAddress);
      inputIntList = processInputFIle();
      intIndex = 0;
      this.dataStructure = dataStructure;
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
            case FUNCTION_CALL:
               callFunctionCode((CallFunctionCode) code);
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
    * PRE: the code must be globalDecl
    * @param code
    */
   private void globalDeclCode(DeclareCode code) {
      SymbolItem symbolItem = code.getDeclareSymbol();
      RecordItem recordItem = new RecordItem(globalPointer, symbolItem);
      runningStack.set(globalPointer, recordItem);
      globalPointer++; // no matter array or var
      pc++;
   }

   /**
    * 将const array/int or var array/int push in stack
    *  new RecordItem and sp will update
    * @param code
    */
   private void localDeclCode(DeclareCode code) {
      SymbolItem symbolItem = code.getDeclareSymbol();
      RecordItem recordItem = new RecordItem(stackPointer, symbolItem);
      runningStack.set(stackPointer, recordItem);
      stackPointer--; // no matter array or var
      pc++;
   }

   // t1 = a[1][2] * t0
   // t0 = a +
   private void assignCode(AssignCode code) throws Exception {
      Expression targetExp = code.getTargetExp();
      Expression leftExp = code.getLeftExp();
      Expression rightExp = code.getRightExp();
      String blockName = code.getBlockName();
      Character op = code.getOp();
      int leftValue = getExpressionValue(leftExp, blockName);
      int rightValue = getExpressionValue(rightExp, blockName);
      int targetValue = 0;
      if (op == '!') {
         targetValue = rightValue == 0 ? 1 : 0;
      } else {
         targetValue = (op == '+') ? leftValue + rightValue :
                        (op == '-') ? leftValue - rightValue :
                        (op == '*') ? leftValue * rightValue :
                        (op == '/') ? leftValue / rightValue :
                        (op == '%') ? leftValue % rightValue : null;
      }
      // assign
      String targetName = targetExp.getName();
      Consts.ExpType targetType = targetExp.getExpType();
      int targetIndex1 = getExpressionValue(targetExp.getIndex1(), blockName);
      int targetIndex2 = getExpressionValue(targetExp.getIndex2(), blockName);
      setRecordItemValue(targetValue, targetName, targetType, blockName, targetIndex1, targetIndex2);
      pc++;
   }

   // if exp1.value == exp2.value, then pc jump
   // else, sequence pc++;
   private void beqCode(BranchEqualCode code) {
      Expression exp1 = code.getExp1();
      Expression exp2 = code.getExp2();
      String blockName = code.getBlockName();
      int value1 = getExpressionValue(exp1, blockName);
      int value2 = getExpressionValue(exp2, blockName);
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
   private void callFunctionCode(CallFunctionCode code) {
      callPCStack.push(pc + 1);
      callMemoryPtrStack.push(stackPointer);
      ArrayList<Expression> arguments = code.getArgumentList();
      String blockName = code.getBlockName();
      retTargetExpStack.push(code.getResultExp());
      //tmpRetExpName = code.getResultExp().getName();// must be temp exp
      argumentValueList = new ArrayList<>();
      for (int i = 0; i < arguments.size(); i++) {
         Expression argument = arguments.get(i);
         int value = getExpressionValue(argument, blockName);
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
         Integer parameterValue = argumentValueList.get(i);
         RecordItem recordItem = new RecordItem(stackPointer, parameter);
         runningStack.set(stackPointer, recordItem);
         // todo: parameter为array地址，还没做
         recordItem.setValue(parameterValue, 0, 0);
         stackPointer--; // no matter array or var
      }
      pc++;

   }

   // callStack pop调整pc的位置，
   // stackPointer 连续pop到正确的位置？
   // retTargetExpStack出栈
   // 计算返回值，给tmp var赋值
   // 退栈，调整pc的位置，
   private void returnCode(ReturnCode code) {
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
         while ( baseAddress != stackPtr) {
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
      int rightValue = getExpressionValue(rightExp, blockName);
      int targetValue = 0;
      String targetName = targetExp.getName();
      Consts.ExpType targetType = targetExp.getExpType();
      int targetIndex1 = getExpressionValue(targetExp.getIndex1(), blockName);
      int targetIndex2 = getExpressionValue(targetExp.getIndex2(), blockName);
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
            if (leftValue != 0 && rightValue != 0) {
               targetValue = 1;
            }
            break;
         case OR:
            if (leftValue != 0 || rightValue != 0) {
               targetValue = 1;
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

   private void printCode(PrintCode code) throws FileNotFoundException {
      ArrayList<Expression> expList = code.getPrintExps();
      ArrayList<Integer> valueList = new ArrayList<>();
      String blockName = code.getBlockName();
      for (int i = 0; i < expList.size(); i++) {
         Expression exp = expList.get(i);
         int value = getExpressionValue(exp, blockName); // todo, a++ and ++a
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
            //System.out.print(valueList.get(index));
            index++;
         } else if (c == '\\') {
            i++;
            printString += "\n";
            //System.out.println();
         }
         else {
            printString += c;
            //System.out.print(c);
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
      System.setOut(outFile);
      System.out.print(printStr);
      System.setOut(outConsole);
      System.out.print(printStr);
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
      int targetIndex1 = getExpressionValue(targetExp.getIndex1(), blockName);
      int targetIndex2 = getExpressionValue(targetExp.getIndex2(), blockName);
      setRecordItemValue(value, targetName, targetType, blockName, targetIndex1, targetIndex2);
      pc++;
   }


   /**
    * find recordItem in memory, based on identifierName
    * @param identifierName
    * @param blockName used to confirm a local or global identifier
    * @return
    */
   private RecordItem findRecordItem(String identifierName, String blockName) {
      int lookupPtr = 0;
      if (!blockName.equals(Consts.GLOBAL_SCOPE)) {
         lookupPtr = stackPointer + 1;
         while (lookupPtr < Consts.MEMORY_HIGHEST_ADDRESS) {
            String itemName = runningStack.get(lookupPtr).getIdentifierName();
            if (identifierName.equals(itemName)) {
               return runningStack.get(lookupPtr);
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
   private int getRecordItemValue(String identifierName, String blockName, int i1, int i2) {
      RecordItem recordItem = findRecordItem(identifierName, blockName);
      int value = recordItem.getValue(i1, i2);
      return value;
   }

   /**
    * 前置条件：该标识符一定已插入到活动记录中
    * 返回该标识符（通常是array）在活动记录中的地址
    * @param identifierName
    * @param blockName
    * @return
    */
   private int getRecordItemAddress(String identifierName, String blockName) {
      RecordItem recordItem = findRecordItem(identifierName, blockName);
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
                                  String baseBlockName, int i1, int i2) throws MyException {
      if (targetType == Consts.ExpType.TEMP_VAR) {
         if (!tmpExpMap.containsKey(identifierName)) {
            throw new MyException("The var " + identifierName + " does not exist");
         }
         tmpExpMap.put(identifierName, newValue);
      } else {
         RecordItem recordItem = findRecordItem(identifierName, baseBlockName);
         recordItem.setValue(newValue, i1, i2);
      }
   }

   public int getExpressionValue(Expression expression, String bloackName) {
      if (expression == null) {
         return 0;
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
               int i1 = getExpressionValue(index1, bloackName);
               int i2 = getExpressionValue(index2, bloackName);
               value = getRecordItemValue(expName, bloackName, i1, i2);
               break;
            case TEMP_VAR:
               value = tmpExpMap.get(expName);
               break;
            //case RETURN: todo
            //break;
            default:
               break;
         }
      } else if (valueType == Consts.ValueType.ADDRESS1 || valueType == Consts.ValueType.ADDRESS2) {
         // array_dim1, array_dim2[],
         // todo:
         value = getRecordItemAddress(expName, bloackName);
      }
      return value;
   }


}
