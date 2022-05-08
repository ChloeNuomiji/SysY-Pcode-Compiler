package CodePackage;
import Compiler.*;
import java.util.ArrayList;

/**
 * 四元式形式的中间代码
 */

public class FourAddressCode {
   private int codeNo; // 指令的地址
   private Consts.FourAddressType type;
   private String blockName;
   private static int codeCounter = 0; // 一共有多少条指令

   // default constructor
   public FourAddressCode(Consts.FourAddressType type, String blockName) {
      this.codeNo = codeCounter;
      codeCounter++;
      this.type = type;
      this.blockName = blockName;
   }

   public FourAddressCode(Consts.FourAddressType type) {
      this.codeNo = codeCounter;
      this.type = Consts.FourAddressType.END_Code;
   }

   public String toString() {
      return "";
   }

   public int getCodeNo() {
      return codeNo;
   }

   public Consts.FourAddressType getType() {
      return type;
   }

   public String getBlockName() {
      return blockName;
   }

}
