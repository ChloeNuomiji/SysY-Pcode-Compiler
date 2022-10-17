package Compiler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;

public class LexicalAnalyzer {
   private String sourceCodeAddress;
   private String lexResultFileAddress;
   private boolean isDebug;
   private PrintStream outFile;
   private int sourceCodeLength;
   private String sourceCode;
   private int currentLine;//记录已经读到source code的第几行
   private int index;//记录读到整个source code的第几个字符
   private char character;
   boolean isFinish;//是否c == EOF, 读到文件终结
   private String symbol;
   private Consts.SymbolType symbolType;
   private Consts.PrintMode printmode;
   private LexicalAnalyzerState backupState;
   private static final char NULL_CHAR = (char) 0;
   private int lastVnLine; // 错误处理时，最后一个非终结符的行数
   private int lastVnTmp;
   private int stringLine; // invalid format string所在行数
   private int rbraceLine; //error g
   private int returnLine; // error f ⽆返回值的函数存在不匹配的return语句
   private int breakContinueLine; // error m 在非循环块中使⽤break和continue语句


   private class LexicalAnalyzerState {
      private int currentLine;//记录已经读到source code的第几行
      private int index;//记录读到整个source code的第几个字符
      private char character;
      private boolean isFinish;//是否c == EOF, 读到文件终结
      private String symbol;
      private Consts.SymbolType symbolType;
      private Consts.PrintMode printmode;
      private int lastVnLine;
      private int lastVnTmp;
      private int stringLine;
      private int rbraceLine; //error g
      private int returnLine;
      private int breakContinueLine;

      public LexicalAnalyzerState(LexicalAnalyzer oldAnalyzer) {
         this.currentLine = oldAnalyzer.currentLine;
         this.index = oldAnalyzer.index;
         this.character = oldAnalyzer.character;
         this.isFinish = oldAnalyzer.isFinish;
         this.symbol = oldAnalyzer.symbol;
         this.symbolType = oldAnalyzer.symbolType;
         this.printmode = oldAnalyzer.printmode;
         this.lastVnLine = oldAnalyzer.lastVnLine;
         this.lastVnTmp = oldAnalyzer.lastVnTmp;
         this.stringLine = oldAnalyzer.stringLine;
         this.rbraceLine = oldAnalyzer.rbraceLine;
         this.returnLine = oldAnalyzer.returnLine;
         this.breakContinueLine = oldAnalyzer.breakContinueLine;
      }

      public void resetState(LexicalAnalyzer toResetAnalyzer) {
         toResetAnalyzer.currentLine = this.currentLine;
         toResetAnalyzer.index = this.index;
         toResetAnalyzer.character = this.character;
         toResetAnalyzer.isFinish = this.isFinish;
         toResetAnalyzer.symbol = this.symbol;
         toResetAnalyzer.symbolType = this.symbolType;
         toResetAnalyzer.printmode = this.printmode;
         toResetAnalyzer.lastVnLine = this.lastVnLine;
         toResetAnalyzer.lastVnTmp = this.lastVnTmp;
         toResetAnalyzer.stringLine = this.stringLine;
         toResetAnalyzer.rbraceLine = this.rbraceLine;
         toResetAnalyzer.returnLine = this.returnLine;
         toResetAnalyzer.breakContinueLine = this.breakContinueLine;
      }
   }

   public LexicalAnalyzer(String sourceCodeAddress, String lexResultFileAddress,
                          boolean isDebug, Consts.PrintMode printmode) throws IOException {
      this.sourceCodeAddress = sourceCodeAddress;
      this.lexResultFileAddress = lexResultFileAddress;
      outFile = new PrintStream(lexResultFileAddress);
      this.isDebug = isDebug;
      this.printmode = printmode;
      readfile();
      index = 0;
      currentLine = 1;
      isFinish = false;
      backupState = null;
      lastVnLine = -1;
      lastVnTmp = -1;
      stringLine = -1;
      rbraceLine = -1;
      returnLine = -1;
      breakContinueLine = -1;
   }

   /*
   向文件以及终端输出Lex分析到的单词s
   CONSTTK coNst
    */
   private void printLex() throws FileNotFoundException {
      String str = symbolType.name() + " " + symbol;
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
   }

   private void readfile() throws IOException {
         byte[] bytes = Files.readAllBytes(Paths.get(sourceCodeAddress));
         sourceCode = new String(bytes, StandardCharsets.UTF_8);
         sourceCodeLength = sourceCode.length();
   }

   // index总是指向下一个未处理的char
   // index总是character的下一个
   public void getChar() {
      if (index == sourceCodeLength) {
         character = NULL_CHAR; //Null
         isFinish = true;
         return;
      }
      if (index > sourceCodeLength) {
         character = NULL_CHAR; //Null
         symbol = "END";
         symbolType = Consts.SymbolType.END;
         isFinish = true;
         return;
      }
      character = sourceCode.charAt(index);
      if (character == '\n') {
         currentLine++;
      }
      index++;
   }

   public void resetChar() {
      index--;
      if (index-1 < 0) {
         System.out.println("error: error when reset");
         System.exit(0);
      }
      character = sourceCode.charAt(index-1);
      if (character == '\n') {
         currentLine--;
      }
   }

   public Consts.SymbolType getSymbolType() {
      return symbolType;
   }
   
   public String getSymbol() {
      return symbol;
   }

   public int getIndex() {
      return index;
   }

   public int getCurrentLine() {
      return currentLine;
   }

   public int getLastVnLine() {
      return lastVnLine;
   }

   public int getStringLine() {
      return stringLine;
   }

   public int getRbraceLine() {
      return rbraceLine;
   }

   public int getReturnLine() {
      return returnLine;
   }

   public int getBreakContinueLine() {
      return breakContinueLine;
   }

   public boolean isFinish() {
      return isFinish;
   }

   public void setPrintmode(Consts.PrintMode printmode) {
      this.printmode = printmode;
   }

   // before reset, need to backup the info of the old lexicalAnalyzer
   public void backupState() {
      backupState = new LexicalAnalyzerState(this);
   }

   public void resetState() {
      backupState.resetState(this);
   }

   /**
    * error process, the current index and character points to the character next to the skipSign
    * @param skipSign
    * \n
    */
   public void skipread(char skipSign) {
      while (character != NULL_CHAR && character != skipSign) {
         getChar();
      }
      getChar();
   }

   /**
    * error process, the current character points to the skipSign
    * @param skipSign
    * )
    */
   public void skipreadBeforeSign(char skipSign) {
      while (character != NULL_CHAR && character != skipSign) {
         getChar();
      }
   }

   public void lexicalAnalysis() throws FileNotFoundException {
      if (isDebug) {
         System.out.println(sourceCodeAddress);
      }
      while(index < sourceCodeLength) {
         analyseSymbol();
      }
      if (isDebug) {
         boolean res = true;
         if (index < sourceCodeLength) {
            res = false;
         }
         if (!res) {
            System.out.println("current line is " + currentLine + "; current index is " + index
                    + "; sourceCodeLength: " + sourceCodeLength);
         }
         System.out.println("if index >= sourceCodeLength: " + res);
      }
   }

   public boolean analyseSymbol() throws FileNotFoundException {
      if (index == 0) {
         getChar();
      }
      //到这里的character 都是没有处理的character
      if (isFinish) { // empty source code
         return false;
      }
      // remove the whitespace
      while (isSpace(character)) {
         getChar();
         if (isFinish) {
            return false;
         }
      }
      boolean res = true;
      // 1. detecting string literal(when printf()), if c == '"':
      if (character == '"') {
         getStringConst();
         printLex();
      }
      // 2. detecting identifiers or reserved words
      // both of which must begin with a-z or A-Z or '_'
      else if (isValidFirstLetter(character)) {
         getIdentifierOrReservedWord();
         printLex();
      }
      // 3. Unsigned Int, if c IsDigit
      else if (isDigit(character)) {
         getIntConst();
         printLex();
      }
      //4. Delimiter with single char.
      // SingleDLM includes + - * % ; , ( ) [ ] { }, totally 12 signs
      else if (isSingleDLM(character)) {
         printLex();
      }
      // 5. Condition or logic or Assign symbol
      else if (isConditionOrAssign(character)) {
         printLex();
      }
      // 6. Slash: div, comment like // or comment like /*
      else if (isSlash(character)) {
         //after slashProcess, the index should 指向下一个未处理的char
         return slashProcess(); // return true when got Consts.SymbolType.DIV; false when is comment
         // no need to printLex()
      }
      else {
         res = false;
         System.out.println("error:不合法字符");
         System.exit(0);
      }
      return res;
   }

   private void getStringConst() {
      lastVnLine = lastVnTmp;
      String formatString = "\"";
      stringLine = currentLine;
      getChar();
      while (!isFinish && character != '"') {
         formatString += character;
         getChar();
      }
      if (isFinish) {
         System.out.println("error: 缺少\", 不合法的formatString");
         System.exit(0);
      }
      formatString += character;
      if (isValidFormatString(formatString)) {
         symbol = formatString; // include ""

      } else {
         symbol = Consts.INVALID_FORMAT_STRING;
      }
      symbolType = Consts.SymbolType.STRCON;
      lastVnTmp = currentLine;
      getChar();
   }

   private void getIdentifierOrReservedWord() {
      lastVnLine = lastVnTmp;
      String symbol = "" + character;
      getChar();
      while (!isFinish && (isLetter(character) || isDigit(character) || character == '_')) {
         symbol += character;
         lastVnTmp = currentLine;
         getChar();
      }
      if (isFinish) {
         System.out.println("error: 提前终止");
         System.exit(0);
      }
      //now c has got the next char value,
      //getting ready for the next token and go to while ((isspace(c)))'.
      this.symbol = symbol;
      isReservedWord(symbol);
   }

   private void getIntConst() {
      lastVnLine = lastVnTmp;
      String symbol = "" + character;
      getChar();
      while(!isFinish && isDigit(character)) {
         symbol += character;
         lastVnTmp = currentLine;
         getChar();
      }
      if (isFinish) {
         System.out.println("error:缺少;");
         System.exit(0);
      }
      symbolType = Consts.SymbolType.INTCON;
      this.symbol = symbol;
   }

   /*
   if character is ' ' || '\t' || '\r' || '\n' || '\f'
    */
   private boolean isSpace(char c) {
      if ((c == ' ') || (c == '\t') || (c == '\r') || (c == '\n') || (c == '\f') || (c == NULL_CHAR)) {
         return true;
      }
      return false;
   }

   private boolean isLetter(char c) {
      int byteAscii = (int) c;
      if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
         return true;
      }
      return false;
   }

   private boolean isDigit(char c) {
      if (c >= '0' && c <= '9') {
         return true;
      }
      return false;
   }

   // The ValidFirstLetter includes a-z, A-Z, "_"
   private boolean isValidFirstLetter(char c) {
      if (isLetter(c) || c == '_') {
         return true;
      }
      return false;
   }

   /**
    * check the formatString, like "", "abc", "aaa^^^" is valid or not
    * @param formatString
    * @return
    */
   private boolean isValidFormatString(String formatString) {
      for (int i = 1; i < formatString.length() - 1; i++) {
         char c = formatString.charAt(i);
         if (!isValidStringLetter(c)) {
            if (c == '%' && formatString.charAt(i + 1) == 'd') {
               continue;
            } else {
               return false;
            }
         }
         if (c == '\\') { //'\'（编码92）出现当且仅当为'\n'
            if (formatString.charAt(i+1) != 'n') {
               return false;
            }
         }
      }
      return true;
   }

   // The string literal includes a-z, A-Z and _ .<字符串>
   // ⼗进制编码为32,33,40-126的ASCII字符， '\'（编码92）出现当且仅当为'\n'
   private boolean isValidStringLetter(char c) {
      int byteAscii = c;
      if (byteAscii == 32 || byteAscii == 33 || (byteAscii >= 40 && byteAscii <= 126)) {
         return true;
      }
      return false;
   }

   private void isReservedWord(String symbol) {
      String lowerString = symbol.toLowerCase(Locale.ROOT);
      int index = Consts.RESERVED_WORD_LIST.indexOf(lowerString);
      if (index == -1) {
         symbolType = Consts.SymbolType.IDENFR;
      } else {
         symbolType = Consts.SymbolType.values()[index + Consts.RESERVER_WORD_OFFSET];
      }
      if (symbolType == Consts.SymbolType.RETURNTK) {
         returnLine = currentLine;
      }
      if (symbolType == Consts.SymbolType.BREAKTK || symbolType == Consts.SymbolType.CONTINUETK) {
         breakContinueLine = currentLine;
      }
   }

   /*
   SingleDLM includes + - * % ; , ( ) [ ] { }
   totally 13 signs
    */
   private boolean isSingleDLM(char c) {
      boolean res = true;
      switch (c) {
         case '+': //1
            symbol = "+";
            symbolType = Consts.SymbolType.PLUS;
            break;
         case '-': //2
            symbol = "-";
            symbolType = Consts.SymbolType.MINU;
            break;
         case '*': //3
            symbol = "*";
            symbolType = Consts.SymbolType.MULT;
            break;
         case '%': //4
            symbol = "%";
            symbolType = Consts.SymbolType.MOD;
            break;
         case ';': //5
            symbol = ";";
            symbolType = Consts.SymbolType.SEMICN;
            break;
         case ',': //6
            symbol = ",";
            symbolType = Consts.SymbolType.COMMA;
            break;
         case '(': //7
            symbol = "(";
            symbolType = Consts.SymbolType.LPARENT;
            break;
         case ')': //8
            symbol = ")";
            symbolType = Consts.SymbolType.RPARENT;
            break;
         case '[': //9
            symbol = "[";
            symbolType = Consts.SymbolType.LBRACK;
            break;
         case ']': //10
            symbol = "]";
            symbolType = Consts.SymbolType.RBRACK;
            break;
         case '{': //11
            symbol = "{";
            symbolType = Consts.SymbolType.LBRACE;
            break;
         case '}': //12
            symbol = "}";
            symbolType = Consts.SymbolType.RBRACE;
            rbraceLine = currentLine;
            break;
         default:
            res = false;
      }
      if (res) {
         lastVnTmp = currentLine;
         getChar();
      }
      return res;
   }

   private boolean isSlash(char c) throws FileNotFoundException {
      if (c == '/') {
         //slashProcess(); //after slashProcess, the index should 指向下一个未处理的char
         return true;
      }
      return false;
   }

   /**
    *
    * @return true: if get legal symbol Consts.SymbolType.DIV
    *         false: is comment
    * @throws FileNotFoundException
    */
   private boolean slashProcess() throws FileNotFoundException {
      if (index >= sourceCodeLength) {
         System.out.println("error: 提前终止");
         System.exit(0);
      }
      char cNext = sourceCode.charAt(index);
      if (cNext == '*') {
         getChar();//此时index指向/*后的第1个字符
         getChar();//此时index指向/*后的第2个字符
         char cCurrent = character;
         cNext = sourceCode.charAt(index);
         while (index < sourceCodeLength && !(cCurrent == '*' && cNext == '/')) {
            getChar();
            cCurrent = character;
            cNext = sourceCode.charAt(index);
         }
         getChar(); //此时index指向*/的/的后一个字符
         getChar();
         return false;
      } else if (cNext == '/') {
         getChar();//此时character = 第2个'/'，index指向//后的第1个字符
         getChar();//此时index指向//后的第2个字符
         while(character != '\n') {
            getChar();
         }
         getChar();
         return false;
      } else {
         symbol = "/";
         symbolType = Consts.SymbolType.DIV;
         getChar();
         printLex();
         return true;
      }
   }

   /*
   condition sign or logic sign or assign sign
   	if ((c == '<') || (c == '=') || (c == '>') || (c == '!') || (c == '&') || (c == '|'))
   	<, =, >, !, &, |
    */
   private boolean isConditionOrAssign(char c) {
      char cNext = '0';
      if (index < sourceCodeLength) {
         cNext = sourceCode.charAt(index); //预读一个c
      } else {
         System.out.println("error: 缺少;");
         System.exit(0);
      }
      boolean res = true;
      int oldLastVnTmp = lastVnTmp;
      lastVnTmp = currentLine;
      getChar();
      switch (c) {
         case '=':
            if (cNext == '=') {
               symbol = "==";
               symbolType = Consts.SymbolType.EQL;
               getChar();
            } else {
               symbol = "=";
               symbolType = Consts.SymbolType.ASSIGN;
            }
            break;
         case '<':
            if (cNext == '=') {
               symbol = "<=";
               symbolType = Consts.SymbolType.LEQ;
               getChar();
            } else {
               symbol = "<";
               symbolType = Consts.SymbolType.LSS;
            }
            break;
         case '>':
            if (cNext == '=') {
               symbol = ">=";
               symbolType = Consts.SymbolType.GEQ;
               getChar();
            } else {
               symbol = ">";
               symbolType = Consts.SymbolType.GRE;
            }
            break;
         case '!':
            if (cNext == '=') {
               symbol = "!=";
               symbolType = Consts.SymbolType.NEQ;
               getChar();
            } else {
               symbol = "!";
               symbolType = Consts.SymbolType.NOT;
            }
            break;
         case '&':
            if (cNext == '&') {
               symbol = "&&";
               symbolType = Consts.SymbolType.AND;
               getChar();
            } else {
               System.out.println("error: 缺少'&'");
               System.exit(0);
            }
            break;
         case '|':
            if (cNext == '|') {
               symbol = "||";
               symbolType = Consts.SymbolType.OR;
               getChar();
            } else {
               System.out.println("error: 缺少'|'");
               System.exit(0);
            }
            break;
         default:
            lastVnTmp = oldLastVnTmp;
            resetChar();
            res = false;
      }
      return res;
   }
}

