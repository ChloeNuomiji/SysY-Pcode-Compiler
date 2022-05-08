package CodePackage;
import Compiler.*;

// base address 退栈，reset stk ptr
public class DeleteRecordCode extends FourAddressCode {
   private int pcToReturn;
   private int stackPtrToReturn;

   public DeleteRecordCode(Consts.FourAddressType type, String blockName) {
      super(type, blockName);

   }

   public String toString() {
      return "DELETE_RECORD";
   }
}
