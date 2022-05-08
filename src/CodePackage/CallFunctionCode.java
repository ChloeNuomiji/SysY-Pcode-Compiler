package CodePackage;
import Compiler.*;

import java.util.ArrayList;

public class CallFunctionCode extends FourAddressCode {
   private String funcName;// function Name
   private ArrayList<Expression> argumentList;// 实参
   private Expression resultExp; // 将函数调用的返回值，set到resultExp的标识符（tmp, ti）上


   // 返回值？

   public CallFunctionCode(String blockName, String funcName, ArrayList<Expression> argumentList, Expression resultExp) {
      super(Consts.FourAddressType.FUNCTION_CALL, blockName);
      this.funcName = funcName;
      this.argumentList = argumentList;
      this.resultExp = resultExp;
   }

   public String toString() {
      String arguments = argumentList.toString();
      String res = "CALL FUNCTION: " + resultExp + " = " + funcName + "(" + arguments + ")";
      return res;
   }

   public String getFuncName() {
      return funcName;
   }

   public ArrayList<Expression> getArgumentList() {
      return argumentList;
   }

   public Expression getResultExp() {
      return resultExp;
   }
}
