package CodePackage;

import Compiler.*;

// beq exp1 exp2 label

/**
 * if (exp1.value == exp2.value) goto label
 * else, sequence execute
 */
public class BranchEqualCode extends FourAddressCode {
   private Label goToLabel;
   private Expression exp1;
   private Expression exp2;

   public BranchEqualCode(String blockName, Label goToLabel, Expression exp1, Expression exp2) {
      super(Consts.FourAddressType.BEQ, blockName);
      this.goToLabel = goToLabel;
      this.exp1 = exp1;
      this.exp2 = exp2;
   }

   public void setGoToLabel(Label newLabel) {
      this.goToLabel = newLabel;
   }

   public String toString() {
      return "BEQ " + exp1.toString() + " " + exp2.toString() + " " + goToLabel.getLabelName();
   }

   public Label getGoToLabel() {
      return goToLabel;
   }

   public Expression getExp1() {
      return exp1;
   }

   public Expression getExp2() {
      return exp2;
   }
}
