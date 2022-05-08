package CodePackage;
import Compiler.*;


public class GotoCode extends FourAddressCode {
   private Label gotoLabel;

   public GotoCode(String blockName, Label gotoLabel) {
      super(Consts.FourAddressType.GOTO, blockName);
      this.gotoLabel = gotoLabel;
   }

   public String toString() {
      return "GOTO " + gotoLabel.getLabelName();
   }

   public Label getGotoLabel() {
      return gotoLabel;
   }
}
