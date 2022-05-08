import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import Compiler.*;

public class CompilerTest {
   public static void main(String[] args) throws Exception {
      boolean isDebug = true;
      boolean isForloop = true;
      Consts.PrintMode printmode = Consts.PrintMode.ONLY_TO_FILE;
      String inputAddress = "input.txt";
      String resultAddress = "pcoderesult.txt";
      String sourceCodeAddress = "testfile.txt";
      String lexResultFileAddress = "lexicalAnalysis.txt";
      String outputAddress = "output.txt";
      String errorAddress = "error.txt";
      String intermediaCodeFile = "itmCode.txt";
      int i = 28;
      if (isForloop) {
         String path = "testfiles/A/";
         inputAddress = path + "input" + i + ".txt";
         sourceCodeAddress = path + "testfile" + i + ".txt";
         // 其他的覆盖就好了
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
         Interpreter interpreter = new Interpreter(inputAddress, resultAddress, dataStructure, isDebug, printmode);
         interpreter.startInterpret();
      }
      compareResult(i);
   }

   public static boolean compareResult(int i) throws IOException {
      System.out.println("------------EXPECTED RESULT------------");
      String path = "testfiles/A/";
      String expected = path + "output" + i + ".txt";
      String expectedContent = readfile(expected);
      System.out.printf(expectedContent);

      System.out.println("\n------------MY RESULT------------");
      String myResult = "pcoderesult.txt";
      String myContent = readfile(myResult);
      System.out.printf(myContent);
      System.out.println("\n------------END------------");

      return true;
   }

   private static String readfile(String fileAddress) throws IOException {
      byte[] bytes = Files.readAllBytes(Paths.get(fileAddress));
      String content = new String(bytes, StandardCharsets.UTF_8);
      return content;
   }
}
