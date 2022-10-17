package Compiler;

import java.util.ArrayList;
import java.util.HashMap;
// 活动栈的每一项，栈式增长
// RecordItem里面没有block and fatherBlock的概念，只有活动记录（stack）的概念
public class RecordItem {
   private int address; // 在内存中的地址
   private String identifierName;
   private Consts.ValueType valueType; // INT or ADDRESS(1,2) or VOID
   private int dimension;
   private int size1; // the size of dimension1
   private int size2; // the size of dimension2
   private boolean constantFlag;
   private boolean isParameter;
   //private boolean isLocal; // todo: why need this field
   private ArrayList<ArrayList<Integer>> valueList;
   private FormalParameter parameter; // if the record is para

   public RecordItem(int address, SymbolItem symbolItem) {
      this.address = address;
      identifierName = symbolItem.getName();
      valueType = Consts.ValueType.INT_TYPE;
      dimension = symbolItem.getDimension();
      size1 = symbolItem.getSize1();
      size2 = symbolItem.getSize2();
      constantFlag = symbolItem.isConstantFlag();
      isParameter = false;
      //isLocal = symbolItem.isLocal();
      initialValueList();// no matter this record is in the golbal memory or the stack
      /*
      if (constantFlag) {
         valueList = symbolItem.getConstValueList();
      } else {
         initialValueList();// no matter this record is in the golbal memory or the stack
      }

       */
      parameter = null;
   }

   public RecordItem(int address, FormalParameter parameter) {
      this.address = address;
      identifierName = parameter.getIdentifierName();
      valueType = parameter.getValueType();
      this.parameter = parameter;
      constantFlag = false;
      isParameter = true;
      // todo: if parameter is address
      dimension = 0;
      size1 = 0;
      size2 = 0;
      initialValueList();

   }

   private void initialValueList() {
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

   public int getAddress() {
      return address;
   }

   public String getIdentifierName() {
      return identifierName;
   }

   public int getValue(int index1, int index2) {
      if (dimension == 1) {
         return valueList.get(0).get(index1);
      }
      return valueList.get(index1).get(index2);
   }

   public void setValue(int newValue, int index1, int index2) {
      // if the recode item is a constant, it can only be setValue when initial
      if (dimension == 1) {
         valueList.get(0).set(index1, newValue);
      } else {
         valueList.get(index1).set(index2, newValue);
      }
   }
}
