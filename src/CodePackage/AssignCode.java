package CodePackage;
import Compiler.*;

public class AssignCode extends FourAddressCode {
   private Expression targetExp; // t1 = a * t0
   private Expression leftExp;
   private Expression rightExp;
   private Character op;

   // constructor for ASSIGN_STATE
   // PRE: type must be ASSIGN_STATE
   public AssignCode(Expression targetExp, Expression leftExp,
                          Expression rightExp, String op, String blockName) throws MyException {
      super(Consts.FourAddressType.ASSIGN_STATE, blockName);
      this.op = op.charAt(0);
      this.leftExp = (Expression) leftExp.clone();
      if (rightExp == null) {
         this.rightExp = Consts.ZERO_DIGIT_EXPRESSION;
      } else {
         this.rightExp = (Expression) rightExp.clone();
      }
      this.targetExp = targetExp;
   }

   public String toString() {
      String res = "ASSIGN ";
      res += targetExp.toString() + " = " + leftExp.toString() + " " + op + " " + rightExp.toString();
      return res;
   }

   public Expression getTargetExp() {
      Expression res = (Expression) targetExp.clone();
      return res;
   }

   public Expression getLeftExp() {
      return leftExp;
   }

   public Expression getRightExp() {
      return rightExp;
   }

   public Character getOp() {
      return op;
   }
}
