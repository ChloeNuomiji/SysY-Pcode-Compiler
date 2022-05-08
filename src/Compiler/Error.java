package Compiler;

import java.io.FileNotFoundException;
import java.io.PrintStream;

public class Error {
   private PrintStream outFile;
   private Consts.PrintMode printmode;
   private boolean isDebug = false;

   public Error(String errorFile, Consts.PrintMode printmode, boolean isDebug) throws FileNotFoundException {
      outFile = new PrintStream(errorFile);
      this.printmode = printmode;
      this.isDebug = isDebug;
   }

   // 1 a <FormatString>中出现非法符号
   public void printError(int line, Consts.ErrorType errorType) {
      String str = line + " " + errorType;
      if (isDebug) {
         str = line + " " + errorType + " " + helperInfo(errorType);
      }
      printMode(str);
   }

   private String helperInfo(Consts.ErrorType errorType) {
      String info = "";
      switch (errorType) {
         case a:
            info = "<FormatString>中出现非法符号";
            break;
         case b:
            info = "名字重定义";
            break;
         case c:
            info = "未定义的名字";
            break;
         case d:
            info = "函数参数个数不匹配";
            break;
         case e:
            info = "函数参数类型不匹配";
            break;
         case f:
            info = "无返回值的函数存在不匹配的return语句";
            break;
         case g:
            info = "有返回值的函数缺少return语句";
            break;
         case h:
            info = "不能改变常量的值";
            break;
         case i:
            info = "缺少分号";
            break;
         case j:
            info = "缺少)";
            break;
         case k:
            info = "缺少]";
            break;
         case l:
            info = "printf中格式字符与表达式个数不匹配";
            break;
         case m:
            info = "在非循环块中使用break和continue语句";
            break;
         default:
            break;
      }
      return info;
   }

   private void printMode(String str) {
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
}
