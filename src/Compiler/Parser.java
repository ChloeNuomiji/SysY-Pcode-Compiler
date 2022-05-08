package Compiler;

import CodePackage.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Stack;

public class Parser {
   private LexicalAnalyzer lexicalAnalyzer;
   private Error error;
   private String sourceCodeAddress;
   private String parseResultFileAddress;
   private boolean isDebug;
   private PrintStream outFile;
   private String symbol; //未进行语法分析的单词
   private Consts.SymbolType symbolType;
   private Consts.PrintMode printmode;
   private boolean isFinish;
   private int printLineNo;
   private DataStructure dataStructure;
   private boolean isReturnStmt;
   private Stack<Label> whileStack; // 记录是否在while block里


   public Parser(String sourceCodeAddress, String lexResultFileAddress,
                 String parseResultFileAddress, String errorFileAddress, boolean isDebug, Consts.PrintMode printmode) throws IOException {
      this.sourceCodeAddress = sourceCodeAddress;
      this.parseResultFileAddress = parseResultFileAddress;
      lexicalAnalyzer = new LexicalAnalyzer(sourceCodeAddress, lexResultFileAddress, isDebug, Consts.PrintMode.NEITHER_CONSOLE_NOR_FILE);
      error = new Error(errorFileAddress ,printmode, isDebug);
      this.isDebug = isDebug;
      this.printmode = printmode;
      symbol = "";
      symbolType = null;
      outFile = new PrintStream(parseResultFileAddress);
      isFinish = false;
      printLineNo = 1;
      dataStructure = new DataStructure();
      isReturnStmt = false;
      whileStack = new Stack<>();
   }

   public DataStructure getDataStructure() {
      return dataStructure;
   }

   // 每次调用这个函数，则通过lexicalAnalyzer吐出一个symbol
   public void getSymbol() throws FileNotFoundException {
      boolean isGetSymbol = lexicalAnalyzer.analyseSymbol();
      while (!lexicalAnalyzer.isFinish() && !isGetSymbol) {
         isGetSymbol = lexicalAnalyzer.analyseSymbol();
      }
      symbol = lexicalAnalyzer.getSymbol();
      symbolType = lexicalAnalyzer.getSymbolType();
      if (lexicalAnalyzer.isFinish()) {
         if (!isFinish) {
            isFinish = true;
         } else {
            symbol = "END";
            symbolType = Consts.SymbolType.END;
         }
      }
   }

   // <Gramma Component>, like <CompileUnit>
   private void printParse(String ParseComponent) {
      /*
      if (isDebug) {
         ParseComponent = printLineNo + " " + ParseComponent;
      }
      PrintStream outConsole = System.out;
      if (printmode == Consts.PrintMode.BOTH_CONSOLE_AND_FILE) {
         System.setOut(outFile);
         System.out.println(ParseComponent);
         System.setOut(outConsole);
         System.out.println(ParseComponent);
      } else if (printmode == Consts.PrintMode.ONLY_TO_FILE) {
         System.setOut(outFile);
         System.out.println(ParseComponent);
      } else if (printmode == Consts.PrintMode.ONLY_TO_CONSOLE) {
         System.setOut(outConsole);
         System.out.println(ParseComponent);
      }
      System.setOut(outConsole);
      printLineNo++;

       */
   }

   /*
   向parseResult文件以及终端输出Lex分析到的单词s
   CONSTTK coNst
    */
   private void printLex() throws FileNotFoundException {
      String str = symbolType.name() + " " + symbol;
      if (isDebug) {
         //str = printLineNo + " " + str;
      }
      PrintStream outConsole = System.out;
      if (printmode == Consts.PrintMode.BOTH_CONSOLE_AND_FILE) {
         System.setOut(outFile);
         System.out.println(str);
         System.setOut(outConsole);
         System.out.println(str);
      } else if (printmode == Consts.PrintMode.ONLY_TO_FILE) {
         System.setOut(outFile);
         System.out.println(str);
      } else if (printmode == Consts.PrintMode.ONLY_TO_CONSOLE) {
         System.setOut(outConsole);
         System.out.println(str);
      }
      System.setOut(outConsole);
      printLineNo++;
   }

   private void printLex(String symbol, Consts.SymbolType symbolType) throws FileNotFoundException {
      String str = symbolType.toString() + " " + symbol;
      if (isDebug) {
         str = printLineNo + " " + str;
      }
      PrintStream outConsole = System.out;
      if (printmode == Consts.PrintMode.BOTH_CONSOLE_AND_FILE) {
         System.setOut(outFile);
         System.out.println(str);
         System.setOut(outConsole);
         System.out.println(str);
      } else if (printmode == Consts.PrintMode.ONLY_TO_FILE) {
         System.setOut(outFile);
         System.out.println(str);
      } else if (printmode == Consts.PrintMode.ONLY_TO_CONSOLE) {
         System.setOut(outConsole);
         System.out.println(str);
      }
      System.setOut(outConsole);
      printLineNo++;
   }


   public void startParse() throws Exception {
      getSymbol();
      CompUnit(); // Program()
      dataStructure.creatSomeCodeTable();
   }

   // CompUnit → {Decl} {FuncDef} MainFuncDef
   // 编译单元 -> {声明}{函数定义}主函数定义
   private boolean CompUnit() throws Exception {
      // <声明>
      while (Decl(Consts.GLOBAL_SCOPE)) {
         continue;
      }
      // <函数定义>
      while (FuncDef()) {
         continue;
      }
      if (!MainFuncDef()) {
         return false;
      } else {
         printParse("<CompUnit>");
         //System.out.println("Compile finished and successfully");
         return true;
      }
   }

   // Decl → ConstDecl | VarDecl
   // 声明 -> 常量声明 | 变量声明
   // dont' print <Decl>
   // funcName 表示了Const/Var的作用域
   private boolean Decl(String blockName) throws Exception {
      if (ConstDecl(blockName)) {
         return true;
      }
      if (VarDecl(blockName)) {
         return true;
      }
      return false;
   }

   // ConstDecl → 'const' 'int' ConstDef { ',' ConstDef } ';'
   // 常量声明 -> 'const' 'int'  为ConstDef 传入const + int的信息
   private boolean ConstDecl(String blockName) throws Exception {
      if (symbolType != Consts.SymbolType.CONSTTK) {
         return false;
      }
      printLex();
      getSymbol();
      if (symbolType != Consts.SymbolType.INTTK) {
         // Error, should be 'int'
         return false;
      }
      Consts.ValueType constValueType = Consts.ValueType.INT_TYPE;
      printLex();
      getSymbol();
      if (!ConstDef(blockName, constValueType)) {
         // Error
         return false;
      }
      while (symbolType == Consts.SymbolType.COMMA) {
         printLex();
         getSymbol();
         if (!ConstDef(blockName, constValueType)) {
            // Error
            return false;
         }
      }
      if (symbolType != Consts.SymbolType.SEMICN) {
         error.printError(lexicalAnalyzer.getLastVnLine(), Consts.ErrorType.i); // lack ;
         // lexicalAnalyzer.skipread('\n'); no need to skip ;
         printParse("<ConstDecl>");
         return true;
      } else {
         printLex();
         getSymbol();
      }
      printParse("<ConstDecl>");
      return true;
   }

   // 常数定义 ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
   // array[], array[][], array
   private boolean ConstDef(String blockName, Consts.ValueType constValueType)
           throws Exception {
      SymbolTable thisSymbolTable = dataStructure.getSymbolTable(blockName);
      if (symbolType != Consts.SymbolType.IDENFR) {
         // Error, should be Ident
         return false;
      }
      String identifierName = symbol;
      // 查表，加表
      boolean isExist = dataStructure.lookupTableBeforeAdd(blockName, identifierName);
      if (isExist) {
         // Error b, 名字重定义
         int errorLine = lexicalAnalyzer.getCurrentLine();
         error.printError(errorLine, Consts.ErrorType.b);
         lexicalAnalyzer.skipreadBeforeSign(';');
         getSymbol();
         return true;
      }
      int dimension = 0;
      int size1 = 0;
      int size2 = 0;
      printLex();
      getSymbol();
      // the first []
      if (symbolType == Consts.SymbolType.LBRACK) {
         dimension = 1;
         printLex();
         getSymbol();
         Expression constIndex1Exp = ConstExp(blockName);
         if (constIndex1Exp.equals(Consts.ERROR_EXPRESSION)) {
            // Error, should be ConstExp
            return false;
         }
         size1 = constIndex1Exp.getNumber();
         if (symbolType != Consts.SymbolType.RBRACK) {
            // Error, lack ']'
            error.printError(lexicalAnalyzer.getLastVnLine(), Consts.ErrorType.k);
         } else {
            printLex();
            getSymbol();
         }
      }
      // the second []
      if (symbolType == Consts.SymbolType.LBRACK) {
         dimension = 2;
         printLex();
         getSymbol();
         Expression constIndex2Exp = ConstExp(blockName);
         if (constIndex2Exp.equals(Consts.ERROR_EXPRESSION)) {
            // Error, should be ConstExp
            return false;
         }
         size2 = constIndex2Exp.getNumber();
         if (symbolType != Consts.SymbolType.RBRACK) {
            // Error, lack ']'
            error.printError(lexicalAnalyzer.getLastVnLine(), Consts.ErrorType.k);
         } else {
            printLex();
            getSymbol();
         }
      }
      if (symbolType != Consts.SymbolType.ASSIGN) {
         // Error, lack '='
         return false;
      }
      printLex();
      getSymbol();
      ArrayList<ArrayList<Expression>> initialList = ConstInitVal(blockName);
      if (initialList == null || initialList.size() == 0) {
         // Error, should be ConstInitVal()
         return false;
      }
      /*
      if (dimension == 1) { // resize the initialList to {{}} when dimension == 1
         ArrayList<Expression> resize = resizeConstInitVal(initialList); // todo
         ArrayList<ArrayList<Expression>> oneDimInitVal = new ArrayList<>();
         oneDimInitVal.add(resize);
         initialList = oneDimInitVal;
      }
       */
      // 添加const symbol到thisSymbolTable
      boolean isLocal = dataStructure.isLocal(blockName);
      boolean constantFlag = true;
      SymbolItem newConst = new SymbolItem(identifierName, constValueType, dimension, size1, size2,
              constantFlag, initialList, isLocal);
      thisSymbolTable.addSymbolItem(newConst);
      Consts.FourAddressType codeType = Consts.FourAddressType.CONST_DECL;
      DeclareCode declareConstCode = new DeclareCode(codeType, blockName, newConst);
      dataStructure.addIntermediaCode(declareConstCode);
      // todo , add assign
      String op = "+";
      Expression zeroExp =  Consts.ZERO_DIGIT_EXPRESSION;
      Expression targetExp;
      AssignCode initialConstCode;
      if (dimension == 0) {
         targetExp = new Expression(identifierName, blockName, Consts.ValueType.INT_TYPE);
         initialConstCode = new AssignCode(targetExp, initialList.get(0).get(0), zeroExp, op, blockName);
         dataStructure.addIntermediaCode(initialConstCode);
      } else if (dimension == 1) {
         ArrayList<Expression> resize = resizeInitVal(initialList);
         ArrayList<ArrayList<Expression>> oneDimInitVal = new ArrayList<>();
         oneDimInitVal.add(resize);
         for (int i = 0; i < size1; i++) {
            Expression indexExp = new Expression(String.valueOf(i));
            targetExp = new Expression(identifierName, blockName, 1, indexExp, null, Consts.ValueType.INT_TYPE);
            initialConstCode = new AssignCode(targetExp, oneDimInitVal.get(0).get(i), zeroExp, op, blockName);
            dataStructure.addIntermediaCode(initialConstCode);
         }
      } else {
         for (int i = 0; i < size1; i++) {
            for (int j = 0; j < size2; j++) {
               Expression index1Exp = new Expression(String.valueOf(i));
               Expression index2Exp = new Expression(String.valueOf(j));
               targetExp = new Expression(identifierName, blockName, 2, index1Exp, index2Exp, Consts.ValueType.INT_TYPE);
               initialConstCode = new AssignCode(targetExp, initialList.get(i).get(j), zeroExp, op, blockName);
               dataStructure.addIntermediaCode(initialConstCode);
            }
         }
      }
      printParse("<ConstDef>"); //常数定义
      return true;
   }

   private ArrayList<Expression> resizeConstInitVal(ArrayList<ArrayList<Expression>> oriList) {
      ArrayList<Expression> res = new ArrayList<>();
      for (ArrayList<Expression> sublist : oriList) {
         res.add(sublist.get(0));
      }
      return res;
   }

   // 常量初值 ConstInitVal → ConstExp |'{' [ ConstInitVal { ',' ConstInitVal } ] '}'
   // 改写文法 ConstInitVal → ConstExp |'{' ConstInitVal { ',' ConstInitVal } '}'
   // 1 or {var1,var1 + constA ,1} or {{var1,var1 + constA ,1}, {2,2,2}, {3,3,3}} or {}
   private ArrayList<ArrayList<Expression>> ConstInitVal(String blockName) throws FileNotFoundException, Exception {
      ArrayList<ArrayList<Expression>> res = new ArrayList<>();
      Expression constExp = ConstExp(blockName);
      if (constExp.equals(Consts.ERROR_EXPRESSION)) {
         if (symbolType != Consts.SymbolType.LBRACE) {
            // Error, should be a ConstExp or '{'
            return null;
         }
         printLex();
         getSymbol();
         ArrayList<ArrayList<Expression>> constInitValList = ConstInitVal(blockName);
         if (constInitValList == null || constInitValList.size() == 0) { // {}
            // 不用考虑{}这种情况
            return null;
         } else {
            res.add(resizeConstInitVal(constInitValList));
            while (symbolType == Consts.SymbolType.COMMA) {
               printLex();
               getSymbol();
               constInitValList = ConstInitVal(blockName);
               if (constInitValList == null || constInitValList.size() == 0) {
                  // Error, should be ConstInitVal
                  return null;
               }
               res.add(resizeConstInitVal(constInitValList));
            }
            if (symbolType != Consts.SymbolType.RBRACE) {
               // Error, lack }
               return res;
            }
            printLex();
            getSymbol();
            printParse("<ConstInitVal>");
            return res;
         }
      } else { // only one ConstExp
         ArrayList<Expression> subList = new ArrayList<>();
         subList.add(constExp);
         res.add(subList);
         printParse("<ConstInitVal>");
         return res; // {{1}}
      }
   }

   //变量声明 VarDecl → 'int' VarDef { ',' VarDef } ';'
   // int func(  与 int num , / int num[  区分函数定义与变量定义
   private boolean VarDecl(String blockName) throws Exception {
      Consts.PrintMode parseOldPrintMode = printmode;
      lexicalAnalyzer.backupState();
      if (symbolType == Consts.SymbolType.INTTK) {
         Consts.ValueType valueType = Consts.ValueType.INT_TYPE;
         getSymbol();
         getSymbol();
         if (symbolType == Consts.SymbolType.LPARENT) {
            reset(parseOldPrintMode);
            return false; // not VarDecl, must be int func(, 函数定义
         } else {
            reset(parseOldPrintMode);
            printLex(); // int
            getSymbol();
            if (!VarDef(blockName, valueType)) {
               // Error, skip
               return false;
            } else {
               while (symbolType == Consts.SymbolType.COMMA) {
                  printLex();
                  getSymbol();
                  if (!VarDef(blockName, valueType)) {
                     // Error, skip
                     return false;
                  }
               }
               if (symbolType != Consts.SymbolType.SEMICN) {
                  error.printError(lexicalAnalyzer.getLastVnLine(), Consts.ErrorType.i);// Error, lack ;
                  // lexicalAnalyzer.skipread('\n'); no need to skip ;
                  printParse("<VarDecl>");
                  return true;
               } else {
                  printLex();
                  getSymbol();
                  printParse("<VarDecl>");
                  return true;
               }
            }
         }
      }
      else {
         return false;
      }
   }

   // VarDef → Ident { '[' ConstExp ']' }
   //        | Ident { '[' ConstExp ']' } '=' InitVal
   // 变量定义 num or num[ConstExp] or num[ConstExp][ConstExp]
   private boolean VarDef(String blockName, Consts.ValueType valueType) throws Exception {
      if (symbolType != Consts.SymbolType.IDENFR) {
         return false;
      }
      // 加表前先查表
      String identifierName = symbol;
      boolean isExist = dataStructure.lookupTableBeforeAdd(blockName, identifierName);
      if (isExist) {
         // Error b, 名字重定义
         int errorLine = lexicalAnalyzer.getCurrentLine();
         error.printError(errorLine, Consts.ErrorType.b);
         lexicalAnalyzer.skipreadBeforeSign(';');
         getSymbol();
         return true;
      }
      int dimension = 0;
      int size1 = 0;
      int size2 = 0;
      printLex();
      getSymbol();
      if (symbolType == Consts.SymbolType.LBRACK) { // the first [
         dimension = 1;
         printLex();
         getSymbol();
         Expression constExp1 = ConstExp(blockName);
         if (constExp1.equals(Consts.ERROR_EXPRESSION) || !constExp1.isFixedValue()) {
            // Error, skip
            return false;
         } else {
            size1 = constExp1.getNumber();
            if (symbolType != Consts.SymbolType.RBRACK) { // the first ]
               // Error, skip, lack ]
               error.printError(lexicalAnalyzer.getLastVnLine(), Consts.ErrorType.k);
            } else {
               printLex();
               getSymbol();
            }
            if (symbolType == Consts.SymbolType.LBRACK) { // the second [
               dimension = 2;
               printLex();
               getSymbol();
               Expression constExp2 = ConstExp(blockName);
               if (constExp2.equals(Consts.ERROR_EXPRESSION) || !constExp2.isFixedValue()) {
                  // Error, skip
                  return false;
               } else {
                  size2 = constExp2.getNumber();
                  if (symbolType != Consts.SymbolType.RBRACK) { // the second ]
                     // Error, lack ]
                     error.printError(lexicalAnalyzer.getLastVnLine(), Consts.ErrorType.k);
                  } else {
                     printLex();
                     getSymbol();
                  }
               }
            }

         }
      }
      // 是否赋初值
      SymbolTable thisSymbolTable = dataStructure.getSymbolTable(blockName);
      boolean isLocal = dataStructure.isLocal(blockName);
      if (symbolType != Consts.SymbolType.ASSIGN) {
         // 变量定义无赋初值
         // 填表
         SymbolItem varSymbol = new SymbolItem(identifierName, valueType, dimension, size1, size2,
                 false, isLocal);
         thisSymbolTable.addSymbolItem(varSymbol); // todo, global == 0; local == null
         Consts.FourAddressType declCodeType = Consts.FourAddressType.VAR_DECL_NO_INIT;
         DeclareCode varDeclCode = new DeclareCode(declCodeType, blockName, varSymbol);
         dataStructure.addIntermediaCode(varDeclCode);
         printParse("<VarDef>");
         return true;
      } else { // Ident { '[' ConstExp ']' } '=' InitVal, 变量定义有赋初值
         printLex();
         getSymbol();
         ArrayList<ArrayList<Expression>> initValRes = InitVal(blockName);
         if (initValRes == null || initValRes.size() == 0) {
            // Error
            return false;
         } else {
            // 变量定义赋初值, 插入DECL四元式 AND 插入ASSIGN四元式
            // int array[0][0] = {{exp0 + 0}};
            SymbolItem varSymbol = new SymbolItem(identifierName, valueType, dimension, size1, size2,
                    false, isLocal, null); // will be set initialExp later
            Consts.FourAddressType declCodeType = Consts.FourAddressType.VAR_DECL_WITH_INIT;
            DeclareCode varDeclCode = new DeclareCode(declCodeType, blockName, varSymbol);
            dataStructure.addIntermediaCode(varDeclCode);

            String op = "+";
            Expression zeroExp =  Consts.ZERO_DIGIT_EXPRESSION;
            Expression targetExp;
            AssignCode initialVarCode;
            if (dimension == 0) {
               targetExp = new Expression(identifierName, blockName, Consts.ValueType.INT_TYPE);
               initialVarCode = new AssignCode(targetExp, initValRes.get(0).get(0), zeroExp, op, blockName);
               dataStructure.addIntermediaCode(initialVarCode);
            } else if (dimension == 1) {
               ArrayList<Expression> resize = resizeInitVal(initValRes);
               ArrayList<ArrayList<Expression>> oneDimInitVal = new ArrayList<>();
               oneDimInitVal.add(resize);
               for (int i = 0; i < size1; i++) {
                  Expression indexExp = new Expression(String.valueOf(i));
                  targetExp = new Expression(identifierName, blockName, 1, indexExp, null, Consts.ValueType.INT_TYPE);
                  initialVarCode = new AssignCode(targetExp, oneDimInitVal.get(0).get(i), zeroExp, op, blockName);
                  dataStructure.addIntermediaCode(initialVarCode);
               }
            } else {
               for (int i = 0; i < size1; i++) {
                  for (int j = 0; j < size2; j++) {
                     Expression index1Exp = new Expression(String.valueOf(i));
                     Expression index2Exp = new Expression(String.valueOf(j));
                     targetExp = new Expression(identifierName, blockName, 2, index1Exp, index2Exp, Consts.ValueType.INT_TYPE);
                     initialVarCode = new AssignCode(targetExp, initValRes.get(i).get(j), zeroExp, op, blockName);
                     dataStructure.addIntermediaCode(initialVarCode);
                  }
               }
            }
            varSymbol.setInitialVarList(initValRes);
            thisSymbolTable.addSymbolItem(varSymbol);
            printParse("<VarDef>");
            return true;
         }
      }
   }

   // {[exp}} -> exp
   private ArrayList<Expression> resizeInitVal(ArrayList<ArrayList<Expression>> initValRes) {
      ArrayList<Expression> res = new ArrayList<>();
      for (ArrayList<Expression> expList : initValRes) {
         res.add(expList.get(0));
      }
      return res;
   }


   //InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'// 1.表达式初值
   // 2.⼀维数组初值 3.⼆维数组初值
   // {{1,2},{3,4}} or {a,b,c}  or {exp1, exp2} (不存在{}的情况)
   private ArrayList<ArrayList<Expression>> InitVal(String blockName) throws Exception {
      ArrayList<ArrayList<Expression>> res = new ArrayList<>();
      // InitVal -> '{' [ InitVal { ',' InitVal } ] '}'
      if (symbolType == Consts.SymbolType.LBRACE) {
         printLex();
         getSymbol();
         ArrayList<ArrayList<Expression>> initValRes = InitVal(blockName);
         if (initValRes != null && initValRes.size() > 0) {
            res.add(resizeInitVal(initValRes)); // add {exp1, exp2, exp3}
            while (symbolType == Consts.SymbolType.COMMA) {
               printLex();
               getSymbol();
               initValRes = InitVal(blockName);
               if (initValRes == null || initValRes.size() == 0) {
                  // Error, should be InitVal
                  return null;
               }
               res.add(resizeInitVal(initValRes));
            }
         }
         if (symbolType == Consts.SymbolType.RBRACE) { // }
            printLex();
            getSymbol();
            printParse("<InitVal>");
            return res;
         } else {
            // Error, lack of }
            return res;
         }
      }
      else {
         Expression exp = Exp(blockName);
         if (!exp.equals(Consts.ERROR_EXPRESSION)) {
            ArrayList<Expression> subList = new ArrayList<>();
            subList.add(exp);
            res.add(subList);
            printParse("<InitVal>");
            return res;
         } else {
            return null;
         }
      }
   }

   // 函数定义 FuncDef → ('int' | 'void') Ident '(' [FuncFParams] ')' Block
   // FuncFParams 函数形参表
   // int func(  与 int num 区分函数定义与变量定义
   // 要区分是函数定义或者是主函数，已经确定不是变量定义（之前已经验证）
   // no need to new SymbolTalbe(), since it's done in Block()
   private boolean FuncDef() throws Exception {
      lexicalAnalyzer.backupState();
      Consts.PrintMode parseOldPrintMode = printmode;
      Consts.SymbolType funcType = null;
      String symbol1 = "";
      Consts.ValueType returnType = Consts.ValueType.INT_TYPE;
      if (symbolType == Consts.SymbolType.VOIDTK || symbolType == Consts.SymbolType.INTTK) {
         if (symbolType == Consts.SymbolType.VOIDTK) {
            returnType = Consts.ValueType.VOID;
         }
         symbol1 = symbol;
         funcType = symbolType;
         getSymbol();
         //printParse("<FuncType>");
      } else {
         return false;
      }
      if (symbolType != Consts.SymbolType.IDENFR) {
         reset(parseOldPrintMode);
         return false; // maybe error or int main()
      }
      printLex(symbol1, funcType); // print int|void
      printParse("<FuncType>");
      String funcName = symbol;
      boolean isNewFunc = dataStructure.isNewFunction(funcName);
      if (!isNewFunc) {
         // Error b, 名字重定义
         int errorLine = lexicalAnalyzer.getCurrentLine();
         error.printError(errorLine, Consts.ErrorType.b); // no need to skip
      }
      String fatherBlockName = Consts.GLOBAL_SCOPE;
      SymbolTable funcSymbolTable = new SymbolTable(funcName, fatherBlockName);
      dataStructure.addSymbolTable(funcSymbolTable);
      printLex(); // print func
      getSymbol();
      if (symbolType != Consts.SymbolType.LPARENT) {
         // Error, should be (
         return false;
      } else {
         printLex();
         getSymbol();
         ArrayList<FormalParameter> formalParameters = FuncFParams(funcName);
         // maybe the formalParameters has error formal parameter(Error b, redefined)
         // todo
         if (symbolType != Consts.SymbolType.RPARENT) {
            error.printError(lexicalAnalyzer.getLastVnLine(), Consts.ErrorType.j); // lack )
         } else {
            printLex();
            getSymbol();
         }
         Function newFunc = new Function(returnType, funcName, formalParameters);
         dataStructure.addFunction(newFunc);
         FunctionDefineCode defineFunc = new FunctionDefineCode(newFunc, funcName);
         dataStructure.addIntermediaCode(defineFunc);
         if (!Block(funcName, true)) {
            // Error
            return false;
         } else {
            if (returnType != Consts.ValueType.VOID && !isReturnStmt) {
               // Error g 有返回值的函数缺少return语句
               int returnErrorLine = lexicalAnalyzer.getRbraceLine();
               error.printError(returnErrorLine, Consts.ErrorType.g);
            } // Error f does not detect here
            ReturnCode returnCode = new ReturnCode(Consts.FourAddressType.RETURN_VOID, funcName, funcName, null);
            dataStructure.addIntermediaCode(returnCode); // maybe redundant
            printParse("<FuncDef>");
            return true;
         }

      }
   }

   private boolean hasErrorFPara(ArrayList<FormalParameter> parameters) {
      for (FormalParameter parameter : parameters) {
         if (parameter.isErrorFormalParameter()) {
            return true;
         }
      }
      return false;
   }

   // 主函数定义 MainFuncDef → 'int' 'main' '(' ')' Block
   // 需要reset
   private boolean MainFuncDef() throws Exception {
      lexicalAnalyzer.backupState();
      Consts.PrintMode parseOldPrintMode = printmode;
      if (symbolType == Consts.SymbolType.INTTK) {
         String intSymbol = symbol;
         getSymbol();
         if (symbolType == Consts.SymbolType.MAINTK) {
            printLex(intSymbol, Consts.SymbolType.INTTK);
            printLex();
            getSymbol();
            if (symbolType != Consts.SymbolType.LPARENT) {
               // Error, lack (, skip
               return false;
            } else {
               printLex();
               getSymbol();
               if (symbolType != Consts.SymbolType.RPARENT) {
                  // Error, lack ), skip
                  error.printError(lexicalAnalyzer.getLastVnLine(), Consts.ErrorType.j); // Error, lack )
               } else {
                  printLex();
                  getSymbol();
               }
               FunctionDefineCode defineMain = new FunctionDefineCode(Consts.MAIN_FUNCTION, "main");
               dataStructure.addIntermediaCode(defineMain);
               dataStructure.setMainEntry(defineMain.getCodeNo()); // 指向main函数的第一条指令
               String fatherBlockName = Consts.GLOBAL_SCOPE;
               SymbolTable mainSymbolTable = new SymbolTable("main", fatherBlockName);
               dataStructure.addSymbolTable(mainSymbolTable);
               Function mainFunc = Consts.MAIN_FUNCTION;
               dataStructure.addFunction(mainFunc);
               if (!Block("main", true)) {
                  // Error, lack Block, skip
                  return false;
               } else {
                  if (!isReturnStmt) {
                     // Error g 有返回值的函数缺少return语句
                     int returnErrorLine = lexicalAnalyzer.getRbraceLine();
                     error.printError(returnErrorLine, Consts.ErrorType.g);
                  }
                  printParse("<MainFuncDef>");
                  return true;
               }
            }
         } else {
            reset(parseOldPrintMode);
            return false;
         }
      } else {
         return false;
      }

   }

   // 函数类型 FuncType → 'void' | 'int'
   // 改写文法后，没再用了
   private boolean FuncType() throws FileNotFoundException {
      if (symbolType == Consts.SymbolType.VOIDTK || symbolType == Consts.SymbolType.INTTK) {
         printLex();
         getSymbol();
         printParse("<FuncType>");
         return  true;
      } else {
         return false;
      }
   }

   // 函数形参表 FuncFParams → FuncFParam { ',' FuncFParam }
   // like, int add(int a, int b[], int c[][2])
   // if parse error, return an ArrayList with SOME ERROR_FORMALPARAMETERs
   private ArrayList<FormalParameter> FuncFParams(String funcName) throws FileNotFoundException, Exception {
      ArrayList<FormalParameter> formalParameterList = new ArrayList<>();
      FormalParameter formalParameter = FuncFParam(funcName);
      if (formalParameter.isErrorFormalParameter()) {
         return formalParameterList;
      } else {
         while (symbolType == Consts.SymbolType.COMMA) {
            formalParameterList.add(formalParameter);
            printLex();
            getSymbol();
            formalParameter = FuncFParam(funcName);
            if (formalParameter.isErrorFormalParameter()) {
               // Error
               formalParameterList.add(Consts.ERROR_FORMALPARAMETER);
               printParse("<FuncFParams>");
               return formalParameterList;
            }
         }
         formalParameterList.add(formalParameter);
         printParse("<FuncFParams>");
         return formalParameterList;
      }
   }

   // 函数形参 FuncFParam → 'int' Ident ['[' ']' { '[' ConstExp ']' }]
   // 1.普通变量 int arr 2.⼀维数组变量 int arr[] 3.⼆维数组变量 int arr2[][2]
   // 将形参插入到符号表funcName
   private FormalParameter FuncFParam(String funcName) throws FileNotFoundException, Exception {
      //int tmpIndex = lexicalAnalyzer.getIndex();
      if (symbolType != Consts.SymbolType.INTTK) {
         return Consts.ERROR_FORMALPARAMETER;
      }
      Consts.ValueType valueType = Consts.ValueType.INT_TYPE;
      printLex();
      getSymbol();
      if (symbolType != Consts.SymbolType.IDENFR) {
         return Consts.ERROR_FORMALPARAMETER;
      } else {
         String paraName = symbol;
         // 查表，加表
         boolean isExist = dataStructure.lookupTableBeforeAdd(funcName, paraName);
         if (isExist) {
            // Error b, 名字重定义
            int errorLine = lexicalAnalyzer.getCurrentLine();
            error.printError(errorLine, Consts.ErrorType.b);
            lexicalAnalyzer.skipreadBeforeSign(')');
            getSymbol();
            return Consts.ERROR_FORMALPARAMETER;
         }
         printLex();
         getSymbol();
         int dimension = 0;
         Expression size2 =  Consts.ZERO_DIGIT_EXPRESSION;
         // arr[] or arr[][constExp]
         if (symbolType == Consts.SymbolType.LBRACK) { // the first [
            printLex();
            getSymbol();
            dimension = 1;
            if (symbolType != Consts.SymbolType.RBRACK) { // the first ]
               // Error, lack ]
               error.printError(lexicalAnalyzer.getLastVnLine(), Consts.ErrorType.k);
            } else {
               printLex();
               getSymbol();
            }
            if (symbolType == Consts.SymbolType.LBRACK) { // the second [, 可有可无
               dimension = 2;
               printLex();
               getSymbol();
               size2 = ConstExp(funcName);
               if (size2.equals(Consts.ERROR_EXPRESSION)) {
                  return Consts.ERROR_FORMALPARAMETER;
               } else {
                  if (symbolType != Consts.SymbolType.RBRACK) {
                     // Error, lack ]
                     error.printError(lexicalAnalyzer.getLastVnLine(), Consts.ErrorType.k);
                  } else { // the second ]
                     printLex();
                     getSymbol();
                  }
               }
            }
            FormalParameter formalParameter = new FormalParameter(valueType, dimension, size2, paraName);
            SymbolItem paraItem = new SymbolItem(formalParameter);
            SymbolTable thisSymbolTable = dataStructure.getSymbolTable(funcName);
            thisSymbolTable.addSymbolItem(paraItem);
            printParse("<FuncFParam>");
            return formalParameter;
         }
         // only: int var
         else {
            FormalParameter formalParameter = new FormalParameter(valueType, dimension, size2, paraName);
            SymbolItem paraItem = new SymbolItem(formalParameter);
            SymbolTable thisSymbolTable = dataStructure.getSymbolTable(funcName);
            thisSymbolTable.addSymbolItem(paraItem);
            printParse("<FuncFParam>");
            return formalParameter;
         }
      }
   }

   // if no return stmt or 'return ;', then
   // preread
   private boolean isReturnStmtWithExp() throws FileNotFoundException {
      lexicalAnalyzer.backupState();
      Consts.PrintMode parseOldPrintMode = printmode;
      boolean res = false;
      if (this.symbolType == Consts.SymbolType.RETURNTK) {
         getSymbol();
         if (this.symbolType == Consts.SymbolType.SEMICN) {
            res = false;
         } else {
            res = true;
         }
         reset(parseOldPrintMode);
      }
      return res;
   }

   // 语句块 Block → '{' { BlockItem } '}'
   // blockName, like "func1", "func1_b"
   private boolean Block(String fatherBlockName, boolean isFuncName) throws Exception {
      if (symbolType != Consts.SymbolType.LBRACE) {
         // Error, lack { skip
         return false;
      } else {
         String blockName = dataStructure.generateBlockName(fatherBlockName, isFuncName);
         if (!isFuncName) { // if isFuncName, the symbolTable has been new when define the function
            SymbolTable blockSymbolTable = new SymbolTable(blockName, fatherBlockName); // new a symbol table
            dataStructure.addSymbolTable(blockSymbolTable);
         }
         AddRecordCode newRecordCode = new AddRecordCode(Consts.FourAddressType.ADD_RECORD, blockName);
         dataStructure.addIntermediaCode(newRecordCode);
         printLex();
         getSymbol();
         boolean probe = isReturnStmtWithExp();
         isReturnStmt = probe; //这个block的最后一个stmt是不是returnStmt
         boolean isBlockItem = BlockItem(blockName);
         while (isBlockItem) {
            isReturnStmt = probe;
            probe = isReturnStmtWithExp();
            isBlockItem = BlockItem(blockName);
         }
         if (symbolType != Consts.SymbolType.RBRACE) {
            // Error, lack } skip
            return false;
         } else {
            DeleteRecordCode deleteRecordCode = new DeleteRecordCode(Consts.FourAddressType.DELETE_RECORD, blockName);
            dataStructure.addIntermediaCode(deleteRecordCode);
            printLex();
            getSymbol();
            printParse("<Block>");
            return true;
         }
      }
   }

   // 语句块项 BlockItem → Decl | Stmt
   // dont print <BlockItem>
   private boolean BlockItem(String blockName) throws Exception {
      if (Decl(blockName)) {
         return true;
      }
      else if (Stmt(blockName)) {
         return true;
      }
      else {
         return false;
      }
   }

   /**
    * count how many formatChar '%d'
    * @param formatString
    * @return
    */
   private int getFormatCharNumber(String formatString) {
      if (formatString == null || formatString.length() <= 1) {
         return 0;
      }
      int number = 0;
      for (int i = 0; i < formatString.length() - 1; i++) {
         if (formatString.charAt(i) == '%') {
            number++;
         }
      }
      return number;
   }

   /*
   * 语句 Stmt → LVal '=' Exp ';' 1
   *           | LVal '=' 'getint''('')'';' 8
               | [Exp] ';' //有⽆Exp两种情况 2
               | Block 3 // Block → '{' { BlockItem } '}'
               | 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // 1.有else 2.⽆else 4
               | 'while' '(' Cond ')' Stmt 5
               | 'break' ';' | 'continue' ';' 6
               | 'return' [Exp] ';' // 1.有Exp 2.⽆Exp 7
               | 'printf''('FormatString{,Exp}')'';' // 1.有Exp 2.⽆Exp 9
   * */
   private boolean Stmt(String blockName) throws Exception {
      lexicalAnalyzer.backupState();
      Consts.PrintMode parseOldPrintMode = printmode;
      // 0 } return false
      if (symbolType == Consts.SymbolType.RBRACE) {
         return false;
      }
      switch (symbolType) {
         case LBRACE: // 3 Stmt → Block
            return blockStmt(blockName);
         case PRINTFTK: // 9 Stmt → 'printf''('FormatString{,Exp}')'';'
            return printfStmt(blockName);
         case RETURNTK: // 7 'return' [Exp] ';' // 1.有Exp 2.⽆Exp 7
            return returnStmt(blockName);
         case BREAKTK:
         case CONTINUETK: // 6 'break' ';' | 'continue' ';'
            return breakAndContStmt(blockName);
         case WHILETK: // 5 'while' '(' Cond ')' Stmt
            return whileStmt(blockName);
         case IFTK: // 4. 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
            return ifStmt(blockName);
         default:
            break;
      }
      reset(parseOldPrintMode); // 重来，仅需判断case1+8+2+false
      boolean isAssignStmt = judgeAssignStmt();
      if (isAssignStmt) {
         //Stmt → LVal '=' Exp ';' 1
         // | LVal '=' 'getint''('')'';' 8
         int errorLine = lexicalAnalyzer.getCurrentLine();
         Expression targetExp = LVal(blockName);
         if (targetExp.equals(Consts.ERROR_EXPRESSION)) {
            return true;
         } else if (targetExp.equals(Consts.FAKE_EXPRESSION)) {
            printParse("<Stmt>");
            return true;
         } else {
            if (targetExp.isConstLVal()) {
               // Error h 不能改变常量的值
               error.printError(errorLine, Consts.ErrorType.h);
            }
            if (symbolType != Consts.SymbolType.ASSIGN) {
               // Error, should be =
               return false;
            } else {
               printLex();
               getSymbol();
               // 8 'getint''('')'';' 8
               if (symbolType == Consts.SymbolType.GETINTTK) {
                  printLex();
                  getSymbol();
                  Consts.FourAddressType codeType = Consts.FourAddressType.READ_INT;
                  if (symbolType != Consts.SymbolType.LPARENT) {
                     // Error, lack (, skip
                     return false;
                  } else {
                     printLex();
                     getSymbol();
                     if (symbolType != Consts.SymbolType.RPARENT) {
                        error.printError(lexicalAnalyzer.getLastVnLine(), Consts.ErrorType.j); // Error, lack )
                     } else {
                        printLex();
                        getSymbol();
                     }
                     FourAddressCode readImtCode = new ReadCode(blockName, targetExp);
                     dataStructure.addIntermediaCode(readImtCode);
                     if (symbolType != Consts.SymbolType.SEMICN) {
                        error.printError(lexicalAnalyzer.getLastVnLine(), Consts.ErrorType.i);// Error, lack ;
                        // lexicalAnalyzer.skipread('\n'); no need to skip ;
                        printParse("<Stmt>");
                        return true;
                     } else {
                        printLex();
                        getSymbol();
                        printParse("<Stmt>");
                        return true;
                     }

                  }
               }
               // Stmt → LVal '=' Exp ';' 1
               else {
                  Expression exp = Exp(blockName);
                  if (!exp.equals(Consts.ERROR_EXPRESSION)) {
                     if (symbolType != Consts.SymbolType.SEMICN) {
                        error.printError(lexicalAnalyzer.getLastVnLine(), Consts.ErrorType.i);// Error, lack ;
                        // lexicalAnalyzer.skipread('\n'); no need to skip ;
                        printParse("<Stmt>");
                        return true;
                     } else {
                        printLex();
                        getSymbol();
                        printParse("<Stmt>");
                        FourAddressCode readImtCode = new AssignCode(targetExp, exp, null, "+", blockName);
                        dataStructure.addIntermediaCode(readImtCode);
                        return true;
                     }
                  } else { // exp == ERROR_EXPRESSION
                     return true;
                  }
               }
            }
         }
      }
      // isNotAssignStmt, which means case 2 or error
      // [Exp] ';' case2 or Error
      else {
         //case2.2 no Exp
         if (symbolType == Consts.SymbolType.SEMICN) {
            printLex();
            getSymbol();
            printParse("<Stmt>");
            return true;
         }
         // case2.1 with Exp
         else {
            Expression exp = Exp(blockName);
            if (!exp.equals(Consts.ERROR_EXPRESSION)) {
               if (symbolType != Consts.SymbolType.SEMICN) {
                  error.printError(lexicalAnalyzer.getLastVnLine(), Consts.ErrorType.i);// Error, lack ;
                  // lexicalAnalyzer.skipread('\n'); no need to skip ;
                  printParse("<Stmt>");
                  return true;
               } else {
                  printLex();
                  getSymbol();
                  printParse("<Stmt>");
                  return true;
               }
            } else {
               reset(parseOldPrintMode); // a bit special, Error, todo
               return false;
            }
         }
      }
   }

   // 3 Stmt → Block
   // Block → '{' { BlockItem } '}'
   private boolean blockStmt(String blockName) throws Exception {
      if (!Block(blockName, false)) {
         // Error, should be Block
         return false;
      } else {
         printParse("<Stmt>");
         return true;
      }
   }

   // 9 Stmt → 'printf''('FormatString{,Exp}')'';'
   private boolean printfStmt(String blockName) throws Exception {
      printLex();
      getSymbol();
      if (symbolType != Consts.SymbolType.LPARENT) {
         // Error, lack (
         // skip
         return false;
      } else {
         printLex();
         getSymbol();
         if (symbolType != Consts.SymbolType.STRCON) {
            // Error, skip
            return false;
         } else {
            String formatString = symbol; // todo: check formatString
            if (formatString.equals(Consts.INVALID_FORMAT_STRING)) {
               // Error a 格式字符串中出现⾮法字符
               int errorLine = lexicalAnalyzer.getStringLine();
               error.printError(errorLine, Consts.ErrorType.a);
               lexicalAnalyzer.skipread('\n'); // skip to next line
               printParse("<Stmt>");
               getSymbol();
               return true;
            }

            int formatCharNumber = getFormatCharNumber(formatString);
            printLex();
            getSymbol();
            ArrayList<Expression> printExps = new ArrayList<>(); // if empty, null
            while (symbolType == Consts.SymbolType.COMMA) {
               printLex();
               getSymbol();
               Expression exp = Exp(blockName);
               if (exp.equals(Consts.ERROR_EXPRESSION)) {
                  // Error, skip
                  lexicalAnalyzer.skipreadBeforeSign(')');
               } else if (exp.equals(Consts.FAKE_EXPRESSION)) {
                  printParse("<Stmt>");
                  return true; // 已经skip到下一行
               }
               printExps.add(exp);
            }
            if (symbolType != Consts.SymbolType.RPARENT) {
               // Error, skip, lack )
               error.printError(lexicalAnalyzer.getLastVnLine(), Consts.ErrorType.j); // Error, lack )
            } else {
               printLex();
               getSymbol();
            }
            if (symbolType != Consts.SymbolType.SEMICN) {
               error.printError(lexicalAnalyzer.getLastVnLine(), Consts.ErrorType.i);// Error, lack ;
               // lexicalAnalyzer.skipread('\n'); no need to skip ;
            }
            if (formatCharNumber != printExps.size()) {
               // Error l, printf中格式字符与表达式个数不匹配
               error.printError(lexicalAnalyzer.getLastVnLine(), Consts.ErrorType.l);
            }
            FourAddressCode imtCode = new PrintCode(formatString, printExps, blockName);
            dataStructure.addIntermediaCode(imtCode);
            printLex();
            getSymbol();
            printParse("<Stmt>");
            return true;
         }
      }
   }

   // 7 'return' [Exp] ';' // 1.有Exp 2.⽆Exp 7
   private boolean returnStmt(String blockName) throws Exception {
      int errorLine = lexicalAnalyzer.getReturnLine();
      printLex();
      getSymbol();
      Function returnFunc = dataStructure.getFunction(blockName);
      Consts.ValueType returnType = returnFunc.getReturnType();
      String retFuncName = returnFunc.getFuncName();
      Expression returnExp = Exp(blockName);
      if (!returnExp.equals(Consts.ERROR_EXPRESSION) && returnType == Consts.ValueType.VOID) {
         // Error f, ⽆返回值的函数存在不匹配的return语句
         // return ; 合法
         error.printError(errorLine, Consts.ErrorType.f);
      }
      // todo int func( return ;) 算错误吗，暂时没有检测出来

      Consts.FourAddressType returnCodeType = Consts.FourAddressType.RETURN_INT;
      if (returnType == Consts.ValueType.VOID) {
         returnCodeType = Consts.FourAddressType.RETURN_VOID;
      }

      ReturnCode returnCode = new ReturnCode(returnCodeType, blockName, retFuncName, returnExp);
      dataStructure.addIntermediaCode(returnCode);
      if (symbolType != Consts.SymbolType.SEMICN) {
         // Error, lack ;
         error.printError(lexicalAnalyzer.getLastVnLine(), Consts.ErrorType.i);
         printParse("<Stmt>");
         return true;
      } else {
         printLex(); // ;
         getSymbol();
         printParse("<Stmt>");
         return true;
         }
   }

   // 6 'break' ';' | 'continue' ';'
   private boolean breakAndContStmt(String blockName) throws FileNotFoundException {
      if (whileStack.isEmpty()) {
         // Error m 在非循环块中使⽤break和continue语句
         int errorLine = lexicalAnalyzer.getBreakContinueLine();
         error.printError(errorLine, Consts.ErrorType.m);
      }
      int whileId = whileStack.peek().getLabelId();
      if (symbolType == Consts.SymbolType.BREAKTK) { // break stmt
         String endWhileLabelName = "ENDWHILE_BRANCH_" + whileId;
         Label endWhileLabel = dataStructure.getLabel(endWhileLabelName);
         GotoCode gotoCode = new GotoCode(blockName, endWhileLabel);
         dataStructure.addIntermediaCode(gotoCode);
      } else { // continue stmt
         String whileLabelName = "WHILE_BRANCH_" + whileId;
         Label whileLabel = dataStructure.getLabel(whileLabelName);
         GotoCode gotoCode = new GotoCode(blockName, whileLabel);
         dataStructure.addIntermediaCode(gotoCode);
      }
      printLex();
      getSymbol();
      if (symbolType != Consts.SymbolType.SEMICN) {
         error.printError(lexicalAnalyzer.getLastVnLine(), Consts.ErrorType.i);// Error, lack ;
         // lexicalAnalyzer.skipread('\n'); no need to skip ;
         printParse("<Stmt>");
         return true;
      } else {
         printLex();
         getSymbol();
         printParse("<Stmt>");
         return true;
      }
   }

   // 5 'while' '(' Cond ')' Stmt
   private boolean whileStmt(String blockName) throws Exception {
      printLex();
      getSymbol();
      Label whileLabel = new Label(Consts.LabelType.WHILE_BRANCH);
      dataStructure.addLabel(whileLabel);
      LabelCode whileLabelCode = new LabelCode(blockName, whileLabel);
      dataStructure.addIntermediaCode(whileLabelCode);
      Label endWhileLabel = new Label(Consts.LabelType.ENDWHILE_BRANCH);
      dataStructure.addLabel(endWhileLabel);
      whileStack.push(whileLabel); // 进入一层循环块
      Label orLabel = new Label(Consts.LabelType.OR_LABEL); // add orLabel
      dataStructure.addLabel(orLabel);
      Label andLabel = new Label(Consts.LabelType.AND_LABEL); // add andLabel
      dataStructure.addLabel(andLabel);
      if (symbolType != Consts.SymbolType.LPARENT) {
         // Error, skip, lack (
         return false;
      } else {
         printLex();
         getSymbol();
         Expression condExp = Cond(blockName);
         Expression cmpExp = Consts.ZERO_DIGIT_EXPRESSION;
         BranchEqualCode beqCode = new BranchEqualCode(blockName, endWhileLabel, condExp, cmpExp);
         dataStructure.addIntermediaCode(beqCode);
         if (condExp.equals(Consts.ERROR_EXPRESSION)) {
            return false;
         } else {
            if (symbolType != Consts.SymbolType.RPARENT) {
               error.printError(lexicalAnalyzer.getLastVnLine(), Consts.ErrorType.j); // Error, lack )
            } else {
               printLex();
               getSymbol();
            }
            LabelCode orCutCode = new LabelCode(blockName, orLabel); // while (A || B), A == 1, goto here(begin while)
            dataStructure.addIntermediaCode(orCutCode);
            if (!Stmt(blockName)) {
               return false;
            } else {
               GotoCode gotoCode = new GotoCode(blockName, whileLabel);
               dataStructure.addIntermediaCode(gotoCode);
               LabelCode endWhileCode = new LabelCode(blockName, endWhileLabel);
               dataStructure.addIntermediaCode(endWhileCode);
               LabelCode andCode = new LabelCode(blockName, andLabel);
               dataStructure.addIntermediaCode(andCode); // if (A && B) and A == 0, goto here
               whileStack.pop(); // 退出一层循环块
               printParse("<Stmt>");
               return true;
            }
         }
      }
   }

   // 4. 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
   private boolean ifStmt(String blockName) throws Exception {
      printLex();
      getSymbol();
      Label ifLabel = new Label(Consts.LabelType.IF_BRANCH);
      dataStructure.addLabel(ifLabel);
      LabelCode ifLabelCode = new LabelCode(blockName, ifLabel);//冗余code
      dataStructure.addIntermediaCode(ifLabelCode);
      Label endIfLabel = new Label(Consts.LabelType.ENDIF_BRANCH);
      dataStructure.addLabel(endIfLabel);
      Label orLabel = new Label(Consts.LabelType.OR_LABEL);
      dataStructure.addLabel(orLabel);
      Label andLabel = new Label(Consts.LabelType.AND_LABEL);
      dataStructure.addLabel(andLabel);
      if (symbolType != Consts.SymbolType.LPARENT) {
         //Error, skip, lack (
         return false;
      } else {
         printLex();
         getSymbol();
         Expression condExp = Cond(blockName);
         Expression cmpExp = Consts.ZERO_DIGIT_EXPRESSION;
         BranchEqualCode beqCode = new BranchEqualCode(blockName, endIfLabel, condExp, cmpExp);
         dataStructure.addIntermediaCode(beqCode);
         LabelCode orCode = new LabelCode(blockName, orLabel); // if (A || B) and A == 1, enter if block
         dataStructure.addIntermediaCode(orCode);
         if (condExp.equals(Consts.ERROR_EXPRESSION)) {
            return false;
         } else {
            if (symbolType != Consts.SymbolType.RPARENT) {
               error.printError(lexicalAnalyzer.getLastVnLine(), Consts.ErrorType.j); // Error, lack )
            } else {
               printLex();
               getSymbol();
            }
            if (!Stmt(blockName)) {
               // Error
               return false;
            } else {
               boolean hasElse = false;
               if (symbolType == Consts.SymbolType.ELSETK) {
                  // if part, go to end if
                  hasElse = true;
                  GotoCode gotoCode = new GotoCode(blockName, endIfLabel);
                  dataStructure.addIntermediaCode(gotoCode);
                  Label elseLabel = new Label(Consts.LabelType.ELSE_BRANCH);
                  dataStructure.addLabel(elseLabel);
                  LabelCode elseCode = new LabelCode(blockName, elseLabel);
                  dataStructure.addIntermediaCode(elseCode);
                  LabelCode andCode = new LabelCode(blockName, andLabel);
                  dataStructure.addIntermediaCode(andCode); // if (A && B) and A == 0 and hasElse, enter else block
                  beqCode.setGoToLabel(elseLabel);
                  printLex();
                  getSymbol();
                  if (!Stmt(blockName)) {
                     // Error
                     return false;
                  }
               }
               LabelCode endIfCode = new LabelCode(blockName, endIfLabel);
               dataStructure.addIntermediaCode(endIfCode);
               if (!hasElse) {
                  LabelCode andCode = new LabelCode(blockName, andLabel);
                  dataStructure.addIntermediaCode(andCode); // if (A && B) and A == 0 and hasElse, goto end if
               }
               printParse("<Stmt>");
               return true;
            }
         }
      }
   }

   /*
   * Stmt → LVal '=' Exp ';' case1
    *     | LVal '=' 'getint''('')'';' case8
    *     | [Exp] ';' //有⽆Exp两种情况 case2
   * */
   /**
    *
    * @return true, case1 or case8, assign stmt;
    * false, other cases
    * @throws FileNotFoundException
    */
   private boolean judgeAssignStmt() throws FileNotFoundException {
      lexicalAnalyzer.backupState();
      Consts.PrintMode parseOldPrintMode = printmode;
      boolean findAssign = false;
      printmode = Consts.PrintMode.NEITHER_CONSOLE_NOR_FILE;
      lexicalAnalyzer.setPrintmode((printmode));
      while (symbolType != Consts.SymbolType.END && symbolType != Consts.SymbolType.SEMICN) {
         if (symbolType == Consts.SymbolType.ASSIGN) {
            findAssign = true;
         }
         getSymbol();
      }
      reset(parseOldPrintMode);
      if (findAssign) {
         return true;
      } else {
         return false;
      }
   }

   //表达式 Exp → AddExp
   private Expression Exp(String blockName) throws FileNotFoundException, Exception {
      Expression res = AddExp(blockName);
      if (res.equals(Consts.ERROR_EXPRESSION)) {
         return Consts.ERROR_EXPRESSION;
      }
      printParse("<Exp>");
      return res;
   }

   // 条件表达式 Cond → LOrExp
   private Expression Cond(String blockName) throws Exception {
      Expression conditionExp = LOrExp(blockName);
      if (conditionExp.equals(Consts.ERROR_EXPRESSION)) {
         return Consts.ERROR_EXPRESSION;
      }
      printParse("<Cond>");
      return conditionExp;
   }

   // 左值表达式 LVal → Ident {'[' Exp ']'}
   // 1.普通变量 2.⼀维数组 3.⼆维数组
   // var/array or array[Exp] or array[Exp][Exp]
   private Expression LVal(String blockName) throws FileNotFoundException, Exception {
      int errorLine = lexicalAnalyzer.getCurrentLine();
      if (symbolType != Consts.SymbolType.IDENFR) {
         return Consts.ERROR_EXPRESSION;
      }
      String identifierName = symbol;
      //
      SymbolItem symbolToUse = dataStructure.lookupTableWhenUse(blockName, identifierName);
      // todo: is
      if (symbolToUse == null) {
         // Error c, 使用未定义的符号
         error.printError(errorLine, Consts.ErrorType.c);
         lexicalAnalyzer.skipread('\n');
         getSymbol();
         return Consts.FAKE_EXPRESSION;
      }
      boolean isSymbolConst = symbolToUse.isConstantFlag();
      Consts.ValueType valueType = symbolToUse.getSymbolExpType();
      Expression retExp = new Expression(identifierName, blockName, valueType); // 先假设是局部/全局变量
      retExp.setConstLVal(isSymbolConst);
      String arrayName = identifierName;
      int arrayDim = symbolToUse.getDimension();
      printLex();
      getSymbol();
      // ⼀维数组 or 二维数组
      if (symbolType == Consts.SymbolType.LBRACK) {
         printLex();
         getSymbol();
         Expression index1 = Exp(blockName);
         Expression index2 = null;
         int dimension = 1;
         if (index1.equals(Consts.ERROR_EXPRESSION)) {
            // Error, should be Exp, skip
            return Consts.ERROR_EXPRESSION;
         }
         if (symbolType != Consts.SymbolType.RBRACK) {
            // Error, lack ]
            error.printError(lexicalAnalyzer.getLastVnLine(), Consts.ErrorType.k);
         } else {
            if (arrayDim == 2) {
               valueType = Consts.ValueType.ADDRESS1; // func(int arr[]), func(arr2[][2]), arr2[]
            } else {
               valueType = Consts.ValueType.INT_TYPE; // arr1[1], 不考虑对var，进行var[]的问题
            }
            printLex();
            getSymbol();
         }
         if (symbolType == Consts.SymbolType.LBRACK) { // the second [
            dimension = 2;
            printLex();
            getSymbol();
            index2 = Exp(blockName);
            if (index2.equals(Consts.ERROR_EXPRESSION)) {
               // Error, should be Exp
               return Consts.ERROR_EXPRESSION;
            }
            valueType = Consts.ValueType.INT_TYPE;
            if (symbolType != Consts.SymbolType.RBRACK) {
               // Error, lack ]
               error.printError(lexicalAnalyzer.getLastVnLine(), Consts.ErrorType.k);
            } else {
               printLex();
               getSymbol();
            }
         }
         retExp = new Expression(arrayName, blockName, dimension, index1, index2, valueType);
         retExp.setConstLVal(isSymbolConst);
      }
      printParse("<LVal>");
      return retExp;
   }

   // 基本表达式 PrimaryExp → '(' Exp ')' | LVal | Number
   private Expression PrimaryExp(String blockName) throws FileNotFoundException, Exception {
      int temp_index = lexicalAnalyzer.getIndex();
      Expression retExp = new Expression();
      if (symbolType != Consts.SymbolType.LPARENT) {
         retExp = LVal(blockName);
         if (retExp.equals(Consts.ERROR_EXPRESSION)) {
            retExp = Number();
            if (retExp.equals(Consts.ERROR_EXPRESSION)) {
               return Consts.ERROR_EXPRESSION;
            } else { // PrimaryExp → Number
               printParse("<PrimaryExp>");
               return retExp;
            }
         } else { // PrimaryExp → LVal
            printParse("<PrimaryExp>");
            return retExp;
         }
      } else { // PrimaryExp → '(' Exp ')'
         printLex();
         getSymbol();
         retExp = Exp(blockName);
         if (retExp.equals(Consts.ERROR_EXPRESSION)) {
            // Error, should be Exp
            return Consts.ERROR_EXPRESSION;
         }
         if (symbolType != Consts.SymbolType.RPARENT) { // todo: (Exp)中缺括号的处理
            // Error, lack )
            // skip
            return Consts.ERROR_EXPRESSION; // todo
         } else {
            printLex();
            getSymbol();
            printParse("<PrimaryExp>");
            return retExp;
         }
      }
   }

   // 数值 Number → IntConst
   private Expression Number() throws FileNotFoundException, Exception {
      if (symbolType != Consts.SymbolType.INTCON) {
         // Error, should be IntConst
         return Consts.ERROR_EXPRESSION;
      }
      Expression number = new Expression(symbol);//Consts.ExpType.DIGIT
      printLex();
      getSymbol();
      printParse("<Number>");
      return number;
   }

   // PRE: 形参实参个数相同
   private boolean parameterMatch(ArrayList<FormalParameter> parameters, ArrayList<Expression> arguments) {
      for (int i = 0; i < parameters.size(); i++) {
         Consts.ValueType parameterType = parameters.get(i).getValueType();
         Consts.ValueType argumentType = arguments.get(i).getValueType();
         if (parameterType != argumentType) {
            return false;
         }
      }
      return true;
   }

   // UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')'| UnaryOp UnaryExp
   // 3种情况
   // ⼀元表达式，区分func(a) 函数调用 和 PrimaryExp，开头都是标识符
   // todo: UnaryOp为!怎么办
   private Expression UnaryExp(String blockName) throws FileNotFoundException, Exception {
      String identifierName = symbol;
      int errorLine = lexicalAnalyzer.getCurrentLine();
      Consts.SymbolType symbol1Type = symbolType;
      lexicalAnalyzer.backupState();
      Consts.PrintMode parseOldPrintMode = printmode;
      getSymbol();
      Consts.SymbolType symbol2Type = symbolType;
      // 2. UnaryExp → Ident '(' [FuncRParams] ')' 函数调用
      if (symbol1Type == Consts.SymbolType.IDENFR
              && symbol2Type == Consts.SymbolType.LPARENT) {
         // Error c, 使⽤了未定义的标识符, 调用了未定义的函数
         if (!dataStructure.isDefinedFunction(identifierName)) {
            error.printError(errorLine, Consts.ErrorType.c);
            lexicalAnalyzer.skipread('\n');
            return Consts.ERROR_EXPRESSION;
         }
         Function callFunc = dataStructure.getFunction(identifierName);
         ArrayList<FormalParameter> parameterList = callFunc.getFormalParameters();
         int numFormalPara = callFunc.getParameterNumber();
         reset(parseOldPrintMode);
         printLex();
         getSymbol();
         printLex();
         getSymbol();
         ArrayList<Expression> argumentList = FuncRParams(blockName);
         int numArgument = argumentList.size();
         Expression exp = new Expression("0");
         if (numArgument != numFormalPara) {
            // Error, d 函数参数个数不匹配
            error.printError(errorLine, Consts.ErrorType.d);
         } else {
            boolean isParameterMatch = parameterMatch(parameterList, argumentList);
            if (!isParameterMatch) {
               // Error, e 函数参数类型不匹配
               error.printError(errorLine, Consts.ErrorType.e);
            }
            // 函数调用
            // todo: 建立函数调用四元式，把返回值assign给exp ti；同时处理exp 的valuetype 的问题
            Consts.ValueType retValueType = callFunc.getReturnType(); // 暂时new一个临时变量的exp
            exp = new Expression(Consts.ExpType.TEMP_VAR,  retValueType); // result exp, a tmp exp
            dataStructure.addTmpExpression(exp);
            String funcName = callFunc.getFuncName();
            CallFunctionCode callFunctionCode = new CallFunctionCode(blockName, funcName, argumentList, exp);
            dataStructure.addIntermediaCode(callFunctionCode);
         }
         if (symbolType != Consts.SymbolType.RPARENT) {
            error.printError(lexicalAnalyzer.getLastVnLine(), Consts.ErrorType.j); // lack )
            printParse("<UnaryExp>");
            return exp;
         } else {
            printLex();
            getSymbol();
            printParse("<UnaryExp>");
            return exp; // DUMMY CODE
         }
      }
      // 1 PrimaryExp or 3 UnaryOp UnaryExp
      else {
         reset(parseOldPrintMode);
         Expression retExp = PrimaryExp(blockName);
         if (!retExp.equals(Consts.ERROR_EXPRESSION)) {
            printParse("<UnaryExp>");
            return retExp;
         } else {
            String unaryOp = UnaryOp();
            if (!unaryOp.equals("")) {
               Expression unaryExp = UnaryExp(blockName);
               if (unaryExp.equals(Consts.ERROR_EXPRESSION)) {
                  // Error
                  return Consts.ERROR_EXPRESSION;
               } else {
                  retExp = unaryExp;
                  switch (unaryOp) {
                     case "+":
                        retExp = unaryExp;
                        break;
                     case "-":
                        Expression leftExp = new Expression("-1");
                        Consts.ExpType expType = Consts.ExpType.TEMP_VAR;
                        Expression targetExp = new Expression(expType, Consts.ValueType.INT_TYPE);
                        dataStructure.addTmpExpression(targetExp);
                        AssignCode assignItmCode = new AssignCode(targetExp, leftExp, unaryExp, "*", blockName);
                        retExp = targetExp;
                        dataStructure.addIntermediaCode(assignItmCode); // ti = -1 * exp
                        break;
                     case "!":
                        leftExp = Consts.ZERO_DIGIT_EXPRESSION;
                        expType = Consts.ExpType.TEMP_VAR;
                        targetExp = new Expression(expType, Consts.ValueType.INT_TYPE);
                        dataStructure.addTmpExpression(targetExp);
                        assignItmCode = new AssignCode(targetExp, leftExp, unaryExp, "!", blockName);
                        retExp = targetExp;
                        dataStructure.addIntermediaCode(assignItmCode); // ti = 0 ! exp 取反
                        //retExp =
                        break;
                  }
                  printParse("<UnaryExp>");
                  return retExp;
               }
            } else {
               return Consts.ERROR_EXPRESSION;
            }
         }
      }
   }


   // 单⽬运算符 UnaryOp → '+' | '−' | '!'
   private String UnaryOp() throws FileNotFoundException {
      if (symbolType == Consts.SymbolType.PLUS || symbolType == Consts.SymbolType.MINU
              || symbolType == Consts.SymbolType.NOT) {
         String op = symbol;
         printLex();
         getSymbol();
         printParse("<UnaryOp>");
         return op;
      } else {
         // Error, should be UnaryOp
         return "";
      }
   }

   // 函数实参表 FuncRParams → Exp { ',' Exp }
   // 实参 argument
   // if parse wrong, return a null list
   private ArrayList<Expression> FuncRParams(String blockName) throws FileNotFoundException, Exception {
      Expression exp = Exp(blockName);
      ArrayList<Expression> arguments = new ArrayList<>();
      if (exp.equals(Consts.ERROR_EXPRESSION)) {
         return arguments; // null list
      }
      arguments.add(exp);
      while (symbolType == Consts.SymbolType.COMMA) {
         printLex();
         getSymbol();
         exp = Exp(blockName);
         if (exp.equals(Consts.ERROR_EXPRESSION)) {
            arguments = new ArrayList<>();
            arguments.add(Consts.ERROR_EXPRESSION);
            return arguments;
         }
         arguments.add(exp);
      }
      printParse("<FuncRParams>");
      return arguments;
   }


   // MulExp -> UnaryExp {('*' | '/' |'%') UnaryExp}
   // MulExp → UnaryExp | MulExp ('*' | '/' | '%')
   // 乘除模表达式
   // 如果要进入到{}，就会生成若干四元式 Expression1 ('*' | '/' | '%') Expression2
   private Expression MulExp(String blockName) throws FileNotFoundException, Exception {
      Expression retExp = UnaryExp(blockName);
      if (retExp.equals(Consts.ERROR_EXPRESSION)) {
         // Error, should be UnaryExp
         return Consts.ERROR_EXPRESSION;
      }
      while (symbolType == Consts.SymbolType.MULT || symbolType == Consts.SymbolType.DIV
              || symbolType == Consts.SymbolType.MOD) {
         Consts.SymbolType opnow = symbolType;
         String op = symbol;
         printParse("<MulExp>");
         printLex();
         getSymbol();
         Expression expUnaryExp2 = UnaryExp(blockName);
         if (expUnaryExp2.equals(Consts.ERROR_EXPRESSION)) {
            // Error, should be UnaryExp
            return Consts.ERROR_EXPRESSION;
         }
         // like 5*6
         if (retExp.isFixedValue() && expUnaryExp2.isFixedValue()) {
            int a = retExp.getNumber();
            int b = expUnaryExp2.getNumber();
            Integer c = (opnow == Consts.SymbolType.MULT) ? a*b :
                        (opnow == Consts.SymbolType.DIV) ? a/b : a%b;
            retExp.setNumber(c);
            retExp.setName(c.toString());
            continue;
         }
         else {
            //准备生成t0 = a * b
            Consts.ExpType expType = Consts.ExpType.TEMP_VAR;
            Consts.ValueType targetValueType = judgeTargetExpValueType(retExp, expUnaryExp2);
            Expression targetExp = new Expression(expType, targetValueType);
            dataStructure.addTmpExpression(targetExp);
            AssignCode assignItmCode = new AssignCode(targetExp, retExp, expUnaryExp2, op, blockName);
            dataStructure.addIntermediaCode(assignItmCode);
            retExp = targetExp;
         }
      }
      printParse("<MulExp>");
      return retExp;
   }

   // AddExp -> MulExp {('+' | '−') MulExp}
   // AddExp → MulExp | AddExp ('+' | '−') MulExp
   // 加减表达式
   private Expression AddExp(String blockName) throws FileNotFoundException, Exception {
      Expression retExp = MulExp(blockName);
      if (retExp.equals(Consts.ERROR_EXPRESSION)) {
         return Consts.ERROR_EXPRESSION;
      }
      while (symbolType == Consts.SymbolType.PLUS || symbolType == Consts.SymbolType.MINU) {
         String op = symbol;
         printParse("<AddExp>");
         printLex();
         getSymbol();
         Expression mulExp2 = MulExp(blockName);
         if (mulExp2.equals(Consts.ERROR_EXPRESSION)) {
            // Error, should be MulExp()
            return Consts.ERROR_EXPRESSION;
         }
         // like 5+6
         if (retExp.isFixedValue() && mulExp2.isFixedValue()) {
            int a = retExp.getNumber();
            int b = mulExp2.getNumber();
            Integer c = (op.equals("+")) ? a + b : a - b;
            retExp.setNumber(c);
            retExp.setName(c.toString());
            continue;
         } else {
            //准备生成t0 = a + b
            Consts.ExpType expType = Consts.ExpType.TEMP_VAR;
            Consts.ValueType targetValueType = judgeTargetExpValueType(retExp, mulExp2);
            Expression targetExp = new Expression(expType, targetValueType);
            dataStructure.addTmpExpression(targetExp);
            AssignCode assignItmCode = new AssignCode(targetExp, retExp, mulExp2, op, blockName);
            dataStructure.addIntermediaCode(assignItmCode);
            retExp = targetExp;
         }
      }
      printParse("<AddExp>");
      return retExp;
   }

   private Consts.ValueType judgeTargetExpValueType(Expression lefExp, Expression rightExp) {
      Consts.ValueType type1 = lefExp.getValueType();
      Consts.ValueType type2 = rightExp.getValueType();
      if (type1 == Consts.ValueType.ADDRESS1 || type2 == Consts.ValueType.ADDRESS1) {
         return Consts.ValueType.ADDRESS1;
      } else if (type1 == Consts.ValueType.ADDRESS2 || type2 == Consts.ValueType.ADDRESS2) {
         return Consts.ValueType.ADDRESS2;
      } else {
         return Consts.ValueType.INT_TYPE;
      }
   }

   // 改写 RelExp → AddExp { ('<' | '>' | '<=' | '>=') AddExp}
   // RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
   // 关系表达式
   private Expression RelExp(String blockName) throws FileNotFoundException, Exception {
      Expression retExp = AddExp(blockName);
      if (retExp.equals(Consts.ERROR_EXPRESSION)) {
         // Error, should be AddExp
         return Consts.ERROR_EXPRESSION;
      }
      while (symbolType == Consts.SymbolType.LSS || symbolType == Consts.SymbolType.GRE
              || symbolType == Consts.SymbolType.LEQ || symbolType == Consts.SymbolType.GEQ) {
         Consts.SymbolType compareOp = symbolType;
         printParse("<RelExp>");
         printLex();
         getSymbol();
         Expression addExp2 = AddExp(blockName);
         if (addExp2.equals(Consts.ERROR_EXPRESSION)) {
            return Consts.ERROR_EXPRESSION;
         }
         //准备生成t0 = a < b
         Consts.ExpType expType = Consts.ExpType.TEMP_VAR;
         Expression targetExp = new Expression(expType, Consts.ValueType.INT_TYPE);
         dataStructure.addTmpExpression(targetExp);
         CompareCode compareItmCode = new CompareCode(targetExp, retExp, addExp2, compareOp, blockName);
         dataStructure.addIntermediaCode(compareItmCode);
         retExp = targetExp;
      }
      printParse("<RelExp>");
      return retExp;
   }

   // 改写文法EqExp → RelExp {('==' | '!=') RelExp}
   // EqExp → RelExp | EqExp ('==' | '!=') RelExp
   // 相等性表达式
   private Expression EqExp(String blockName) throws Exception {
      Expression retExp = RelExp(blockName);
      if (retExp.equals(Consts.ERROR_EXPRESSION)) {
         // Error, must be RelExp
         return Consts.ERROR_EXPRESSION;
      }
      while (symbolType == Consts.SymbolType.EQL || symbolType == Consts.SymbolType.NEQ) {
         Consts.SymbolType compareOp = symbolType;
         printParse("<EqExp>");
         printLex();
         getSymbol();
         Expression relExp2 = RelExp(blockName);
         if (relExp2.equals(Consts.ERROR_EXPRESSION)) {
            // Error, must be RelExp
            return Consts.ERROR_EXPRESSION;
         }
         //准备生成t0 = a == b
         Consts.ExpType expType = Consts.ExpType.TEMP_VAR;
         Expression targetExp = new Expression(expType, Consts.ValueType.INT_TYPE);
         dataStructure.addTmpExpression(targetExp);
         CompareCode compareItmCode = new CompareCode(targetExp, retExp, relExp2, compareOp, blockName);
         dataStructure.addIntermediaCode(compareItmCode);
         retExp = targetExp;
      }
      printParse("<EqExp>");
      return retExp;
   }

   // 改写LAndExp -> EqExp {'&&' EqExp}   相等性表达式
   // LAndExp → EqExp | LAndExp '&&' EqExp
   // 逻辑与表达式
   private Expression LAndExp(String blockName) throws Exception {
      Expression retExp = EqExp(blockName);
      Label andLabel = new Label(Consts.LabelType.AND_LABEL);//andCondEnd
      if (retExp.equals(Consts.ERROR_EXPRESSION)) {
         // Error, must be EqExp
         return Consts.ERROR_EXPRESSION;
      }
      while (symbolType == Consts.SymbolType.AND) {
         Consts.SymbolType compareOp = symbolType;
         printParse("<LAndExp>");
         printLex();
         getSymbol();

         Label shortCutLabel = dataStructure.getLastAndLabel();
         BranchEqualCode beqCode = new BranchEqualCode(blockName, shortCutLabel, retExp, Consts.ZERO_DIGIT_EXPRESSION);

         //BranchEqualCode beqCode = new BranchEqualCode(blockName, andLabel, retExp, Consts.ZERO_DIGIT_EXPRESSION);
         dataStructure.addIntermediaCode(beqCode);// todo
         Expression eqExp2 = EqExp(blockName);
         if (eqExp2.equals(Consts.ERROR_EXPRESSION)) {
            // Error, must be EqExp
            return Consts.ERROR_EXPRESSION;
         }
         //准备生成t0 = a && b
         Consts.ExpType expType = Consts.ExpType.TEMP_VAR;
         Expression targetExp = new Expression(expType, Consts.ValueType.INT_TYPE);
         dataStructure.addTmpExpression(targetExp);
         CompareCode compareItmCode = new CompareCode(targetExp, retExp, eqExp2, compareOp, blockName);
         dataStructure.addIntermediaCode(compareItmCode);
         retExp = targetExp;
      }
//      LabelCode andCode = new LabelCode(blockName, andLabel);
//      dataStructure.addIntermediaCode(andCode);
      printParse("<LAndExp>");
      return retExp;
   }

   // 逻辑或表达式
   // LOrExp → LAndExp | LOrExp '||' LAndExp
   // 改写LOrExp → LAndExp{ '||' LAndExp }
   private Expression LOrExp(String blockName) throws Exception {
      Expression retExp = LAndExp(blockName);
      if (retExp.equals(Consts.ERROR_EXPRESSION)) {
         // Error, must be LAndExp
         return Consts.ERROR_EXPRESSION;
      }
      while (symbolType == Consts.SymbolType.OR) {
         Consts.SymbolType compareOp = symbolType;
         printParse("<LOrExp>");
         printLex();
         getSymbol();

         Label shortCutLabel = dataStructure.getLastOrLabel();
         BranchEqualCode beqCode = new BranchEqualCode(blockName, shortCutLabel, retExp, Consts.ONE_DIGIT_EXPRESSION);
         dataStructure.addIntermediaCode(beqCode);

         Expression lAndExp2 = LAndExp(blockName);
         if (lAndExp2.equals(Consts.ERROR_EXPRESSION)) {
            // Error, must be LAndExp
            return Consts.ERROR_EXPRESSION;
         }
         //准备生成t0 = a || b
         Consts.ExpType expType = Consts.ExpType.TEMP_VAR;
         Expression targetExp = new Expression(expType, Consts.ValueType.INT_TYPE);
         dataStructure.addTmpExpression(targetExp);
         CompareCode compareItmCode = new CompareCode(targetExp, retExp, lAndExp2, compareOp, blockName);
         dataStructure.addIntermediaCode(compareItmCode);
         retExp = targetExp;
      }
      printParse("<LOrExp>");
      return retExp;
   }

   //  ConstExp → AddExp
   // 常量表达式
   private Expression ConstExp(String blockName) throws FileNotFoundException, Exception {
      //String fakeBlockName = Consts.CONST_NO_SCOPE;
      Expression retExp = AddExp(blockName);
      if (retExp.equals(Consts.ERROR_EXPRESSION)) {
         return Consts.ERROR_EXPRESSION;
      }
      printParse("<ConstExp>");
      return retExp;
   }

   private void reset(Consts.PrintMode parseOldPrintMode) {
      lexicalAnalyzer.resetState();
      symbol = lexicalAnalyzer.getSymbol();
      symbolType = lexicalAnalyzer.getSymbolType();
      printmode = parseOldPrintMode;
   }

}
