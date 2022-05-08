package Compiler;
import CodePackage.FourAddressCode;

import java.util.ArrayList;
import java.util.Arrays;

public class Consts {
   /*单词类别码
   38 个
   */
   public enum SymbolType {
      IDENFR,
      INTCON,
      STRCON,
      MAINTK,
      CONSTTK,
      INTTK,
      BREAKTK,
      CONTINUETK,
      IFTK,
      ELSETK,
      WHILETK,
      GETINTTK,
      PRINTFTK,
      RETURNTK,
      VOIDTK,
      NOT, // !
      AND, // &&
      OR, // ||
      PLUS,
      MINU,
      MULT,
      DIV,
      MOD,
      LSS, // <
      LEQ, // <=
      GRE, // >
      GEQ, // >=
      EQL, // ==
      NEQ, // !=
      ASSIGN,
      SEMICN,
      COMMA,
      LPARENT,
      RPARENT,
      LBRACK,
      RBRACK,
      LBRACE,
      RBRACE,
      END;

      @Override
      public String toString() {
         String name = "";
         switch (this) {
            case NOT:
               name = "!";
               break;
            case AND:
               name = "&&";
               break;
            case OR:
               name = "||";
               break;

            case PLUS:
               name = "+";
               break;
            case MINU:
               name = "-";
               break;
            case MULT:
               name = "*";
               break;
            case DIV:
               name = "/";
               break;
            case MOD:
               name = "%";
               break;
            case LSS:
               name = "<";
               break;
            case LEQ:
               name = "<=";
               break;
            case GRE:
               name = ">";
               break;
            case GEQ:
               name = ">=";
               break;
            case EQL:
               name = "==";
               break;
            case NEQ:
               name = "!=";
               break;
            default:
               name = this.name();
               break;
         }
         return name;
      }
   }

   /*
   12个保留字
    */
   public static final String[] RESERVED_WORDS = new String[] { "main", "const", "int", "break", "continue",
                                                "if", "else", "while", "getint", "printf", "return", "void"};
   public static final ArrayList<String> RESERVED_WORD_LIST = new ArrayList<>(Arrays.asList(RESERVED_WORDS));

   /*
   index in RESERVED_WORD_LIST + RESERVER_WORD_OFFSET = index in SymbolType
    */
   public static final int RESERVER_WORD_OFFSET = 3;
   public static final Expression ERROR_EXPRESSION = new Expression("ERROR_EXPRESSION", true);
   public static final Expression FAKE_EXPRESSION = new Expression("^FAKE_EXPRESSION", false);
   public static final Expression ZERO_DIGIT_EXPRESSION = new Expression("0");
   public static final Expression ONE_DIGIT_EXPRESSION = new Expression("1");
   public static final FormalParameter ERROR_FORMALPARAMETER = new FormalParameter(ValueType.INT_TYPE, -1, null, "");
   public static final FourAddressCode FINAL_CODE = new FourAddressCode(FourAddressType.END_Code);
   public static final int MEMORY_HIGHEST_ADDRESS = 100000;
   public static final int MEMORY_LOWEST_ADDRESS = 0;
   public static final String GLOBAL_SCOPE = "#GLOBAL";
   public static final String WHILE_BLOCK = "^while";
   //public static final String CONST_NO_SCOPE = "#CONST";
   public static final String INVALID_FORMAT_STRING = "#INVALID_FORMAT_STRING";
   public static final Function MAIN_FUNCTION = new Function(ValueType.INT_TYPE, "main", null);


   //默认只有 int (string?)
   public enum ValueType {
      INT_TYPE,
      VOID, // the return value from a function, it may be VOID
      ADDRESS1, // arr[]
      ADDRESS2 // arr[][]
   };

   // 符号表item的类型
   public enum SymbolItemType {
      INT_TYPE,
      ARRAY,
      POINTER
   }

   /**四元式结构体总设计
    1. 变量声明无初始化
    2. 变量声明有初始化
    3. 常量声明
    4. 增加一个活动记录（在每次call function or 进入一个new block时）
   1.值参传入push x,push y
   2.调用函数 call add
   3.赋值语句 i = ret or  i = t1 + 1 or t2 = t1 * t0
   4.条件判断 x == y  x<=y
   5.纯标签Label1:
   6.跳转语句 goto label1 bnz label1 ...
   7.函数返回 ret x   ret
   8.函数声明 int x
   9.参数表 param x
   10.print "xxxx"  print 'c' print 23 print a
   11.read int a, read char c
   */
   public enum FourAddressType {
      VAR_DECL_NO_INIT,
      VAR_DECL_WITH_INIT,
      CONST_DECL,
      ADD_RECORD, //
      DELETE_RECORD,

      VALUE_PARAM_DELIVER,
      VALUE_DEI_OVER,
      FUNCTION_CALL,
      ASSIGN_STATE,
      COMPARE_STATE,
      LABEL,
      FUNCTION_DEF,
      PARAM_DEF,
      GOTO, // 相当于j
      BEQZ,//不等于零
      BEQ,
      BNE,
      BGE,
      BGT,
      BLE,
      BLT,
      READ_INT,
      PRINT_FMTSTR,
      RETURN_INT,
      RETURN_VOID,
      OVER_PROCEDURE,
      END_Code
   };

   // 表达式类型 expression's type
   public enum ExpType {
      DIGIT, // 纯数字
      LOCAL_VAR,
      GLOBAL_VAR, // 全局变量
      TEMP_VAR, // ti
      RETURN // 函数调用返回值
   };


   public enum PrintMode {
      ONLY_TO_CONSOLE,
      ONLY_TO_FILE,
      BOTH_CONSOLE_AND_FILE,
      NEITHER_CONSOLE_NOR_FILE
   }

   /**
    * 错误类别码
    */
   public enum ErrorType {
      a,
      b,
      c,
      d,
      e,
      f,
      g,
      h,
      i,
      j,
      k,
      l,
      m
   }

   public enum LabelType {
      IF_BRANCH,
      ELSE_BRANCH,
      ENDIF_BRANCH,
      WHILE_BRANCH,
      ENDWHILE_BRANCH,
      OR_LABEL,
      AND_LABEL
   }


}
