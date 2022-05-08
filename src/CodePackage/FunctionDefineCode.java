package CodePackage;
import Compiler.*;

import java.util.ArrayList;


// 函数声明/定义四元式
// 编译FunctionDefineCode时，不需要！增加活动记录（其实就一个label的功能？）
public class FunctionDefineCode extends FourAddressCode {
   private Function function;

   public FunctionDefineCode(Function function, String blockName) {
      super(Consts.FourAddressType.FUNCTION_DEF, blockName);
      this.function = function;
   }

   /**
    * int foo()
    * para int a
    * para int b
    */
   public String toString() {
      String res =  "\n" + function.getReturnType().toString() + " ";

      res += function.getFuncName() + "(" + function.getFormalParameters()  + ")";
      return res;
   }

   public ArrayList<FormalParameter> getFormalParameters() {
      return function.getFormalParameters();
   }


   public String getFuncName() {
      return function.getFuncName();
   }

}
