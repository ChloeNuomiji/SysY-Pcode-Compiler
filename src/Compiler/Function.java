package Compiler;

import java.util.ArrayList;

public class Function {
   private Consts.ValueType returnType;
   private String funcName;
   private ArrayList<FormalParameter> formalParameters; // 形参
   private int parameterNumber; // 形参个数

   public Function(Consts.ValueType returnType, String funcName,
                   ArrayList<FormalParameter> formalParameters) {
      this.returnType = returnType;
      this.funcName = funcName;
      this.formalParameters = formalParameters;
      if (formalParameters == null) {
         parameterNumber = 0;
      } else {
         parameterNumber = formalParameters.size();
      }
   }

   public Consts.ValueType getReturnType() {
      return returnType;
   }

   public String getFuncName() {
      return funcName;
   }

   public ArrayList<FormalParameter> getFormalParameters() {
      return formalParameters;
   }

   public int getParameterNumber() {
      return parameterNumber;
   }
}
