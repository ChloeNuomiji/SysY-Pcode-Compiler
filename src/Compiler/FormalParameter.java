package Compiler;

// 形参
public class FormalParameter {
   private Consts.ValueType valueType; // INT or ADDRESS
   private int dimension; // 0 or 1 or 2
   private Expression constExp2; // int arr[][2] -> 2
   private String identifierName; // 仅在打印函数定义四元式时有用

   /**
    * if dimension == 0 -> var
    * else if dimension == 1 -> array[]
    * else if dimension == 2 -> array[][const]
    * @param valueType
    * @param dimension
    * @param constExp2
    */
   public FormalParameter(Consts.ValueType valueType, int dimension, Expression constExp2, String identifierName) {
      if (dimension == 0) {
         this.valueType = valueType; // all INT_TYPE
      } else if (dimension == 1){
         this.valueType = Consts.ValueType.ADDRESS1;
      } else {
         this.valueType = Consts.ValueType.ADDRESS2;
      }
      this.dimension = dimension;
      this.constExp2 = constExp2;
      this.identifierName = identifierName;
   }

   public String toString() {
      String res = identifierName;
      if (dimension == 1) {
         res += "[]";
      } else if (dimension == 2) {
         res += "[]" + "[" + constExp2.toString() + "]";
      }
      return res;
   }

   public boolean isErrorFormalParameter() {
      if (dimension == -1) {
         return true;
      } else {
         return false;
      }
   }

   public Consts.ValueType getValueType() {
      return valueType;
   }

   public int getDimension() {
      return dimension;
   }

   public Expression getConstExp2() {
      return constExp2;
   }

   public String getIdentifierName() {
      return identifierName;
   }

}
