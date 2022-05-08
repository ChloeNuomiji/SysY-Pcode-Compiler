import Compiler.*;

import java.io.PrintStream;

public class Compiler {
   public static void main(String[] args) throws Exception {
      boolean isDebug = true;
      boolean isForloop = false;
      Consts.PrintMode printmode = Consts.PrintMode.BOTH_CONSOLE_AND_FILE;
      String inputAddress = "input.txt";
      String resultAddress = "pcoderesult.txt";
      String sourceCodeAddress = "testfile.txt";
      String lexResultFileAddress = "lexicalAnalysis.txt";
      String outputAddress = "output.txt";
      String errorAddress = "error.txt";
      String intermediaCodeFile = "itmCode.txt";
      //
      System.out.println("------------BEGIN PARSING------------");
      Parser parser = new Parser(sourceCodeAddress, lexResultFileAddress,
              outputAddress, errorAddress, isDebug, printmode);
      parser.startParse();

      DataStructure dataStructure = parser.getDataStructure();
      PrintStream printImtCodeStream = new PrintStream(intermediaCodeFile);
      System.out.println("------------PRINT INTERMEDIA CODE------------");
      dataStructure.printIntermediaCode(printImtCodeStream);
      dataStructure.printIntermediaCode(System.out);

      System.out.println("------------BEGIN INTERPRETING------------");
      //Interpreter interpreter = new Interpreter(inputAddress, resultAddress, dataStructure, isDebug, printmode);
      //interpreter.startInterpret();



   }
}
