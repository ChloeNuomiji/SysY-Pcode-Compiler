package Compiler;

import java.util.ArrayList;
import java.util.HashMap;
// 活动栈的每一项，栈式增长
// RecordItem里面没有block and fatherBlock的概念，只有活动记录（stack）的概念
public class RecordItem {
   private int address; // 在内存中的地址
   private String name; // recordItem name, array[1]
   private Integer value = null; // 10, value or address
   private String identifierName; // var or array name
   private Consts.ValueType valueType; // INT or ADDRESS(1,2) or VOID
   private boolean constantFlag;
   private boolean isParameter;
   private boolean isPointer;
   private boolean isGlobalAddress;
   private boolean isArray; // isArray element
   private int dimension;
   private int size1; // the size of dimension1
   private int size2; // the size of dimension2

   private FormalParameter parameter; // if the record is para
   //private Integer arrayAddress;

   // var (not array)
   public RecordItem(int address, SymbolItem symbolItem) {
      this.address = address;
      identifierName = symbolItem.getName();
      name = identifierName;
      isArray = false;
      valueType = Consts.ValueType.INT_TYPE;
      dimension = symbolItem.getDimension();
      size1 = symbolItem.getSize1();
      size2 = symbolItem.getSize2();
      constantFlag = symbolItem.isConstantFlag();
      if (constantFlag) {
         value = 0;
      }
      isParameter = false;
      isPointer = false;
      parameter = null;
   }

   // 1 dimension array
   public RecordItem(int address, SymbolItem symbolItem, int index1) {
      this.address = address;
      identifierName = symbolItem.getName();
      name = identifierName + "[" + index1 + "]";
      isArray = true;
      valueType = Consts.ValueType.INT_TYPE;
      dimension = symbolItem.getDimension();
      size1 = symbolItem.getSize1();
      size2 = symbolItem.getSize2();
      constantFlag = symbolItem.isConstantFlag();
      if (constantFlag) {
         value = 0;
      }
      isParameter = false;
      isPointer = false;
      parameter = null;
   }

   // 2 dimension array
   public RecordItem(int address, SymbolItem symbolItem, int index1, int index2) {
      this.address = address;
      identifierName = symbolItem.getName();
      name = identifierName + "[" + index1 + "][" + index2 + "]";
      isArray = true;
      valueType = Consts.ValueType.INT_TYPE;
      dimension = symbolItem.getDimension();
      size1 = symbolItem.getSize1();
      size2 = symbolItem.getSize2();
      constantFlag = symbolItem.isConstantFlag();
      if (constantFlag) {
         value = 0;
      }
      isParameter = false;
      isPointer = false;
      parameter = null;
   }

   // parameter, address/pointer or value
   public RecordItem(int address, FormalParameter parameter, boolean isPointer) {
      this.address = address;
      identifierName = parameter.getIdentifierName();
      name = identifierName;
      isArray = false;
      valueType = parameter.getValueType(); // INT, ADDRESS1 or ADDRESS2
      this.parameter = parameter;
      constantFlag = false;
      isParameter = true;
      this.isPointer = isPointer;
      dimension = 0;
      size1 = 0;
      size2 = 0;
   }

   public String toString() {
      return valueType + " " + identifierName;
   }

   public int getAddress() {
      return address;
   }

   public String getName() {
      return name;
   }

   public String getIdentifierName() {
      return identifierName;
   }

   public Consts.ValueType getValueType() {
      return valueType;
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

   public boolean isArray() {
      return isArray;
   }

   public boolean isPointer() {
      return isPointer;
   }

   // func(int arr[][2+3]) -> return 2+3
   public Expression getParaConst2() {
      return parameter.getConstExp2();
   }

   public int getValue() {
      return value;
   }

   public void setValue(int newValue) {
      // if the recode item is a constant, it can only be setValue when initial
      value = newValue;
   }

   public void setGlobalAddress(boolean isGlobalAddress) {
      this.isGlobalAddress = isGlobalAddress;
   }
}
