package Compiler;

import java.util.ArrayList;

public class SymbolItem {
   private String identifierName;
   private Consts.SymbolItemType symbolType; //INT_TYPE or ARRAY or POINTER
   private Consts.ValueType valueType = Consts.ValueType.INT_TYPE;// INT_TYPE or CHAR_TYPE
   private int dimension;
   private int size1; // the size of dimension1
   private int size2; // the size of dimension2
   private boolean constantFlag;
   private boolean isInitial;
   private ArrayList<ArrayList<Expression>> constExpList;
   private ArrayList<ArrayList<Expression>> initialVarList; // only using for 数组定义赋初值的情况
   //private int address; // if the symbol is array, then address refers to the base address
   private boolean isLocal; //global: !isTemp && !isLocal
   private boolean isTemp = false; // why isTemp

   private boolean isPara = false;
   private FormalParameter parameter = null; // 如果符号为形参

   public SymbolItem() {

   }

   // constructor for symbolItem that is var and with NO initialValue/initialList
   public SymbolItem(String identifierName, Consts.ValueType valueType, int dimension, int size1, int size2, boolean constantFlag, boolean isLocal) {
      this.identifierName = identifierName;
      this.valueType = valueType;
      this.dimension = dimension;
      this.size1 = size1;
      this.size2 = size2;
      this.constantFlag = constantFlag;
      this.isInitial = false;
      /*
      if (isLocal) {
         valueList = null;
      } else {
         ArrayList<Integer> zeroList = new ArrayList<>();
         valueList = new ArrayList<>();
         if (dimension == 0) {
            zeroList.add(0);
            valueList.add(zeroList);
         } else if (dimension == 1) {
            for (int i = 0; i < size1; i++) {
               zeroList.add(0);
            }
            valueList.add(zeroList);
         } else {
            for (int i = 0; i < size1; i++) {
               zeroList = new ArrayList<>();
               for (int j = 0; j < size2; j++) {
                  zeroList.add(0);
               }
               valueList.add(zeroList);
            }
         }
      }
       */
      this.isLocal = isLocal;
   }

   // constructor for symbolItem that is var and with initialVarList
   public SymbolItem(String identifierName, Consts.ValueType valueType, int dimension, int size1, int size2, boolean constantFlag,
                     boolean isLocal, ArrayList<ArrayList<Expression>> initialExpList) {
      this.identifierName = identifierName;
      this.valueType = valueType;
      this.dimension = dimension;
      this.size1 = size1;
      this.size2 = size2;
      this.constantFlag = constantFlag;
      this.initialVarList = initialExpList;
      this.isLocal = isLocal;
      this.isInitial = true;
      /*
      this.valueList = new ArrayList<>();
      ArrayList<Integer> nullList = new ArrayList<>();
      if (dimension == 0) {
         nullList.add(null);
         this.valueList.add(nullList);
      } else if (dimension == 1) {
         for (int i = 0; i < size1; i++) {
            nullList.add(null);
         }
         this.valueList.add(nullList);
      } else {
         for (int i = 0; i < size1; i++) {
            nullList = new ArrayList<>();
            for (int j = 0; j < size2; j++) {
               nullList.add(null);
            }
            valueList.add(nullList);
         }
      }

       */
   }

   // constructor for symbolItem that IS const
   public SymbolItem(String identifierName, Consts.ValueType valueType, int dimension, int size1, int size2, boolean constantFlag,
                     ArrayList<ArrayList<Expression>> initialList, boolean isLocal) {
      this.identifierName = identifierName;
      this.valueType = valueType;
      this.dimension = dimension;
      this.size1 = size1;
      this.size2 = size2;
      this.constantFlag = constantFlag;
      this.constExpList = initialList;
      this.isLocal = isLocal;
      this.isInitial = true;
   }

   // constructor for symbolItem that is formal parameter (函数定义中的形参)
   public SymbolItem(FormalParameter parameter) {
      identifierName = parameter.getIdentifierName();
      dimension = parameter.getDimension();
      if (dimension >= 1) {
         symbolType = Consts.SymbolItemType.POINTER;
      }
      constantFlag = false;
      isPara = true;
      this.parameter = parameter;
   }

   public String toString() {
      String res = "";
      if (constantFlag) {
         res = "const " + identifierName;
         if (dimension == 1) {
            res += "[" + size1 + "]";
         } else if (dimension == 2) {
            res += "[" + size1 + "][" + size2 + "]";
         }
      } else {
         res = "var " + identifierName;
         if (dimension == 1) {
            res += "[" + size1 + "]";
         } else if (dimension == 2) {
            res += "[" + size1 + "][" + size2 + "]";
         }
         if (isInitial) {
            res += " = "; // todo
         }
      }
      return res;
   }

   public String getName() {
      return identifierName;
   }

   public String getIdentifierName() {
      return identifierName;
   }

   public Consts.SymbolItemType getSymbolType() {
      return symbolType;
   }

   public Consts.ValueType getSymbolExpType() {
      if (dimension == 1) {
         return Consts.ValueType.ADDRESS1;
      } else if (dimension == 2) {
         return Consts.ValueType.ADDRESS2;
      }
      else {
         return Consts.ValueType.INT_TYPE;
      }
   }

   public ArrayList<ArrayList<Expression>> getConstValueList() {
      return constExpList;
   }

   public int getDimension() {
      return dimension;
   }

   public int getSize1() {
      return size1;
   }

   public int getSize2() {
      return size2;
   }

   public boolean isConstantFlag() {
      return constantFlag;
   }

   public boolean isInitial() {
      return isInitial;
   }

   public boolean isLocal() {
      return isLocal;
   }

   public void setInitialVarList(ArrayList<ArrayList<Expression>> initialVarList) {
      this.initialVarList = initialVarList;
   }

   public void setSymbolName(String symbolName) {
      this.identifierName = symbolName;
   }

   public void setSymbolType(Consts.SymbolItemType symbolType) {
      this.symbolType = symbolType;
   }

   public void setValueType(Consts.ValueType valueType) {
      this.valueType = valueType;
   }

   public void setDimension(int dimension) {
      this.dimension = dimension;
   }

   public void setSize1(int size1) {
      this.size1 = size1;
   }

   public void setSize2(int size2) {
      this.size2 = size2;
   }

   public void setConstantFlag(boolean constantFlag) {
      this.constantFlag = constantFlag;
   }

   public void setLocal(boolean local) {
      isLocal = local;
   }

   public void setTemp(boolean temp) {
      isTemp = temp;
   }
}
