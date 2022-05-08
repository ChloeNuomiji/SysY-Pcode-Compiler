package CodePackage;
import Compiler.*;

import java.util.ArrayList;

public class PrintCode extends FourAddressCode {
   private ArrayList<Expression> printExps;
   private String formatString;

   // construtors for imt code in PRINT
   public PrintCode(String formatString, ArrayList<Expression> printExps, String blockName) throws MyException {
      super(Consts.FourAddressType.PRINT_FMTSTR, blockName);
      this.formatString = formatString;
      this.printExps = printExps;
   }

   public String toString() {
      return "PRINT " + formatString;
   }

   // print
   public ArrayList<Expression> getPrintExps() {
      return printExps;
   }

   // print
   public String getFormatString() {
      return formatString;
   }
}
