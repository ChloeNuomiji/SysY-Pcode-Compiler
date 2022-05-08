package CodePackage;
import Compiler.*;

// recordAddressStack.push(baseAddress); // push the new Record's baseAddress
public class AddRecordCode extends FourAddressCode{

   public AddRecordCode(Consts.FourAddressType type, String blockName) {
      super(type, blockName); // but I think for AddRecordCode, blockName is redundant
   }

   public String toString() {
      return "ADD_RECORD";
   }
}
