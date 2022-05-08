package Compiler;

public class Label {
   private String labelName; //automatically generate
   private Consts.LabelType labelType;
   private int labelId; // IF_BRANCH_2, 2, automatically generate
   private static int ifLabelId = 0;
   private static int whileLabelId = 0;
   private static int andLabelId = 0;
   private static int orLabelId = 0;
   //private static int normalLabelId = 0;



   // PRE: first construct if_branch and while_branch, then construct relevant else/end branch
   public Label(Consts.LabelType labelType) {
      this.labelType = labelType;
      String labelName = labelType.toString();
      switch (labelType) {
         case IF_BRANCH:
            labelName += "_" + ifLabelId;
            labelId = ifLabelId;
            ifLabelId++;
            break;
         case ELSE_BRANCH:
            labelName += "_" + (ifLabelId - 1);
            labelId = ifLabelId - 1;
            break;
         case ENDIF_BRANCH:
            labelName += "_" + (ifLabelId - 1);
            labelId = ifLabelId - 1;
            break;
         case WHILE_BRANCH:
            labelName += "_" + whileLabelId;
            labelId = whileLabelId;
            whileLabelId++;
            break;
         case ENDWHILE_BRANCH:
            labelName += "_" + (whileLabelId - 1);
            labelId = whileLabelId - 1;
            break;
         case OR_LABEL:
            labelName += "_" + orLabelId;
            labelId = orLabelId;
            orLabelId++;
            break;
         case AND_LABEL:
            labelName += "_" + andLabelId;
            labelId = andLabelId;
            andLabelId++;
//         case NORMAL_LABEL:
//            labelName += "_" + normalLabelId;
//            labelId = normalLabelId;
//            normalLabelId++;
         default:
      }
      this.labelName = labelName;
   }

   public String getLabelName() {
      return labelName;
   }

   public Consts.LabelType getLabelType() {
      return labelType;
   }

   public int getLabelId() {
      return labelId;
   }

   public static String getLastOrLabelName() {
      return Consts.LabelType.OR_LABEL.toString() + "_" + (orLabelId - 1);
   }

   public static String getLastAndLabelName() {
      return Consts.LabelType.AND_LABEL.toString() + "_" + (andLabelId - 1);
   }

//   public static String getLastNormalLabelName() {
//      return Consts.LabelType.NORMAL_LABEL.toString() + "_" + (normalLabelId - 1);
//   }
}
