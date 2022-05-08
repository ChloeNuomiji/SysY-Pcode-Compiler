package CodePackage;
import Compiler.*;

public class LabelCode extends FourAddressCode {
   private Label label; // 只有LabelCode才有codeNo的信息


   public LabelCode(String blockName, Label label) {
      super(Consts.FourAddressType.LABEL, blockName);
      this.label = label;
   }

   public String getLabelName() {
      return label.getLabelName();
   }

   public Consts.LabelType getLabelType() {
      return label.getLabelType();
   }

   public String toString() {
      return label.getLabelName();
   }




}
