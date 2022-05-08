package CodePackage;
import Compiler.*;

public class ReturnCode extends FourAddressCode{
   private String funcName;
   private Expression returnExp;

   public ReturnCode(Consts.FourAddressType returnCodeType, String blockName,
      String funcName, Expression returnExp) {
      super(returnCodeType, blockName);
      this.funcName = funcName;
      this.returnExp = returnExp;
   }

   public String toString() {
      if (returnExp == null) {
         return "RETURN VOID";
      }
      return "RETURN " + returnExp.toString();
   }

   public Expression getReturnExp() {
      return returnExp;
   }
}
