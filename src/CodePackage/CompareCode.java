package CodePackage;
import Compiler.*;

public class CompareCode extends FourAddressCode {
   private Expression targetExp; // t1 = a < t0
   private Expression leftExp;
   private Expression rightExp;
   private Consts.SymbolType compareSymbol;

   // constructor for ASSIGN_STATE
   // PRE: type must be ASSIGN_STATE
   public CompareCode(Expression targetExp, Expression leftExp,
                     Expression rightExp, Consts.SymbolType compareSymbol, String blockName) {
      super(Consts.FourAddressType.COMPARE_STATE, blockName);
      this.compareSymbol = compareSymbol;
      this.leftExp = (Expression) leftExp.clone();
      if (rightExp == null) {
         this.rightExp = Consts.ZERO_DIGIT_EXPRESSION;
      } else {
         this.rightExp = (Expression) rightExp.clone();
      }
      this.targetExp = targetExp;
   }

   public String toString() {
      String res = "COMPARE ";
      res += targetExp.toString() + " = " + leftExp.toString() + " " + compareSymbol + " " + rightExp.toString();
      return res;
   }

   public Expression getTargetExp() {
      return targetExp;
   }

   public Expression getLeftExp() {
      return leftExp;
   }

   public Expression getRightExp() {
      return rightExp;
   }

   public Consts.SymbolType getCompareSymbol() {
      return compareSymbol;
   }
}
