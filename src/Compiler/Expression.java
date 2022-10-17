package Compiler;

public class Expression implements Cloneable {
   private String name;//表达式返回的名字--->统一规定,表达式都需要有一个返回变量名(临时变量)
   private Consts.ValueType valueType;//INT_TYPE, VOID, ADDRESS
   private Consts.ExpType expType; //DIGIT, LOCAL_VAR, GLOBAL_VAR, TEMP_VAR, RETURN
   private boolean isFixedValue;//是否确定isSurable,只有常量'a',和1111这样的是确认的
   private String scope;//如果是变量，所属的作用域, father
   private int number; // if isFixedValue, then have number
   private boolean isArray;
   private String arrayName;
   private int dimension;
   private Expression index1; //第一维
   private Expression index2;
   //private Consts.ExpType itype; // index type的处理?
   //private String index_father;
   private boolean isExpression = true;//用于Error
   private static int tempVarId = 0;
   private String printString;
   private boolean isConstLVal; // 左值表达式的ident是否为const ident, can be set

   public Expression() {

   }

   public Expression(String name, boolean isWrong) {
      if (isWrong) {
         valueType = Consts.ValueType.INT_TYPE;
         this.name = "ERROR_EXPRESSION";
         printString = name;
         isExpression = false;
         isConstLVal = false;
      } else {
         valueType = Consts.ValueType.INT_TYPE;
         this.name = name;
         printString = name;
         isExpression = true;
         isConstLVal = false;
      }
   }

   /**
    * 常量exp
    * new digit const expression
    * 100, 0 (all positive)
    * no scope, arrayName, index1, index2, index_father...
    * @param numStr
    */
   public Expression(String numStr) {
      valueType = Consts.ValueType.INT_TYPE;
      expType = Consts.ExpType.DIGIT;
      name = numStr;
      dimension = 0;
      isFixedValue = true;
      number = Integer.parseInt(name);
      isArray = false;
      isExpression = true;
      printString = numStr;
      isConstLVal = false;
   }

   /**
    * 全局变量/局部变量的表达式
    * @param varName
    * @param blockName
    */
   public Expression(String varName, String blockName, Consts.ValueType valueType) {
      this.valueType = valueType;
      this.name = varName;
      this.scope = blockName;
      if (blockName.equals("")) {
         expType = Consts.ExpType.GLOBAL_VAR;
      } else {
         expType = Consts.ExpType.LOCAL_VAR;
      }
      isFixedValue = false;
      isArray = false;
      dimension = 0;
      isExpression = true;
      printString = name;
      isConstLVal = false;
   }

   /**
    * Constructor for LVal -> Ident {'[' Exp ']'} 一维/二维数组变量
    * a[exp1][exp2]
    */
   public Expression(String arrayName, String blockName, int dimension,
                     Expression index1, Expression index2, Consts.ValueType valueType) throws MyException {
      this.valueType = valueType;
      if (blockName.equals("")) {
         expType = Consts.ExpType.GLOBAL_VAR;
      } else {
         expType = Consts.ExpType.LOCAL_VAR;
      }
      name = arrayName; // not arr[0][0]
      scope = blockName;
      this.dimension = dimension;
      if (dimension != 1 && dimension != 2) {
         throw new MyException("array epression's dimension must be 1 or 2");
      }
      if (dimension == 1) {
         printString = arrayName + "[" + index1.toString() + "]";
      } else {
         printString = arrayName + "[" + index1.toString() + "]" + "[" + index2.toString() + "]";
      }
      isFixedValue = false;
      isArray = true;
      this.index1 = index1;
      this.index2 = index2;
      this.arrayName = arrayName;
      isConstLVal = false;
   }

   /**
    * 构造临时变量
    */
   public Expression(Consts.ExpType expType, Consts.ValueType valueType) throws MyException {
      this.valueType = valueType;
      if (expType != Consts.ExpType.TEMP_VAR) {
         throw new MyException("should call another exp constructor, not this for temp");
      }
      name = "t" + tempVarId;
      this.expType = expType;
      isFixedValue = false;
      isExpression = true;
      tempVarId++;
      printString = name;
      isConstLVal = false;
   }

   public String toString() {
      return printString;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public int getNumber() {
      return number;
   }

   public int getDimension() {
      return dimension;
   }

   public Expression getIndex1() {
      return index1;
   }

   public Expression getIndex2() {
      return index2;
   }

   public void setNumber(int number) {
      name = String.valueOf(number);
      this.number = number;
   }

   public void setConstLVal(boolean constLVal) {
      isConstLVal = constLVal;
   }

   public boolean isConstLVal() {
      return isConstLVal;
   }

   public boolean isFixedValue() {
      return isFixedValue;
   }

   public boolean isExpression() {
      return isExpression;
   }

   public Consts.ExpType getExpType() {
      return expType;
   }

   public Consts.ValueType getValueType() {
      return valueType;
   }

   public void setArray(boolean array) {
      isArray = array;
   }

   public void setIndex1(Expression index1) {
      this.index1 = index1;
   }

   public void setIndex2(Expression index2) {
      this.index2 = index2;
   }

   public void setDimension(int dimension) {
      this.dimension = dimension;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof Expression) {
         Expression exp = (Expression) obj;
         boolean isEqual = true;
         if (!name.equals(exp.getName()) || isExpression != exp.isExpression()) {
            isEqual = false;
         }
         return isEqual;
      }
      return false;
   }

   public Object clone() {
      Expression o = null;
      try {
         o = (Expression)super.clone();
      } catch (CloneNotSupportedException e) {
         e.printStackTrace();
      }
      // todo: 需要把每个field给copy一遍吗
      return o;
   }
}
