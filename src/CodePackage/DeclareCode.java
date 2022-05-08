package CodePackage;
import Compiler.*;

public class DeclareCode extends FourAddressCode {
   private SymbolItem declareSymbol;

   public DeclareCode(Consts.FourAddressType type, String blockName, SymbolItem declareSymbol) throws Exception {
      super(type, blockName);
      if (type != Consts.FourAddressType.CONST_DECL && type != Consts.FourAddressType.VAR_DECL_NO_INIT
              && type != Consts.FourAddressType.VAR_DECL_WITH_INIT) {
         throw new Exception("wrong construct DeclareCode");
      }
      this.declareSymbol = declareSymbol;
   }

   /**
    * 变量声明及初始化：
    * declare var int i
    * declare var int j = 1
    * 常量声明：
    * declare const int c = 10
    *
    */
   public String toString() {
      return "declare " + declareSymbol.toString();
   }

   public SymbolItem getDeclareSymbol() {
      return declareSymbol;
   }
}
