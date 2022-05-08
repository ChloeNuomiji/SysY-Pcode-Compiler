package CodePackage;
import Compiler.*;

public class ReadCode extends FourAddressCode {
   private Expression targetExp; // arr[1] = getint();


   // constructors for imt code of READ_INT
   public ReadCode(String blockName, Expression targetExp) {
      super(Consts.FourAddressType.READ_INT, blockName);
      this.targetExp = targetExp; // read i; read i[a+b]
   }

   public String toString() {
      return "READ " + targetExp.toString();
   }

   public Expression getTargetExp() {
      return targetExp;
   }
}
