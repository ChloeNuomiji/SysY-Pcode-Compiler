package Compiler;

import java.util.ArrayList;
import java.util.HashMap;


public class SymbolTable {
   private ArrayList<SymbolItem> symbolList;
   private HashMap<String, SymbolItem> symbolMap; //<symbolName, SymbolItem>, quick for lookup
   private String father ; // of fatherTable
   private String tableName; // 函数名；块名的时候，自动generate name. 比如father为block1, 那么 block1_1
   //private int baseAddress; // 这个符号表在内存的基地址
   //private int curAddress; // 要填表的话，
   private boolean isGlobal;
   //private final static int SYMBOL_SIZE = 1; // 不同于$SP-4，这里是$SP-1(curAddress-1)

   /**
    * if father == "", name == "GLOBAL_SCOPE". then it's global symbol table
    * @param father
    * @param name
    */
   public SymbolTable(String name, String father) {
      if (father.equals("") && name.equals(Consts.GLOBAL_SCOPE)) {
         isGlobal = true;
      } else {
         isGlobal = false;
      }
      symbolList = new ArrayList<>();
      symbolMap = new HashMap<String, SymbolItem>();
      this.father = father;
      this.tableName = name;
      //this.baseAddress = baseAddress;
      //curAddress = baseAddress;
   }

   // 查找当前符号表,是否存在该符号，只查一层
   public boolean tableContainsItem(String identifierName) {
      return symbolMap.containsKey(identifierName);
   }

   // 查找当前符号表的该符号，返回该符号。如果不存在，返回null
   public SymbolItem lookupItem(String identifierName) {
      return symbolMap.get(identifierName);
   }

   // 填符号表
   // PRE: 该符号名字不会重定义
   public void addSymbolItem(SymbolItem symbolItem) {
      symbolList.add(symbolItem);
      symbolMap.put(symbolItem.getName(), symbolItem);
      int dimension = symbolItem.getDimension();
      int size1 = symbolItem.getSize1();
      int size2 = symbolItem.getSize2();
      /*
      if (isGlobal) {
         if (dimension == 0) {
            curAddress += 1;
         } else if (dimension == 1) {
            curAddress += size1;
         } else {
            curAddress += size1 * size2;
         }
      } else {
         if (dimension == 0) {
            curAddress -= 1;
         } else if (dimension == 1) {
            curAddress -= size1;
         } else {
            curAddress -= size1 * size2;
         }
      }
       */
   }

   public HashMap getSymbolMap() {
      return symbolMap;
   }

   public String getFather() {
      return father;
   }

   public String getTableName() {
      return tableName;
   }

}
