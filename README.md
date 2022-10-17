# SysY-Pcode-Compiler

## 1. 中文 Chinese

### 运行

### Demo

#### testcase1

```c
int main() {
     int a = 1;
     int b = 2;
     int c = a + b;
     printf("%d",c); // 输出3
     return 0;
}
```

预期运行结果

```
3
```

![demo1](https://github.com/ChloeNuomiji/SysY-Pcode-Compiler/blob/main/demo1.gif)

#### isPrime

```c
// 素数判断
const int zero = 0,one = +1, minus_one = -1;
const int two = 2;
const int times = zero;
int while_times = zero;
int groups = 10;

void while_times_cal(){
    while_times = while_times + 1;
    return;
}

int is_prime(int in){
    int result = zero;
    int item = two;
    {

    }
    if(in == 2){
        result = one;
        ;
    }else{
        while(item < in){
            while_times_cal();
            if(in%item == 0){
                result = zero;
                break;
            }
            result = one;
            item = item + 1;
        }
    }
    return result;
}

int main(){
    int input;
    printf("input an integer,judge if it is prime number,10 groups in total\n");
    while(groups != 0){
        while_times_cal();
        input = getint();
        if(input <= 0){
            printf("input > 0 is needed\n");
            groups = groups - 1;
            continue;
        }
        if(input == 1){
            printf("1 is not concerned\n");
            groups = groups - 1;
            continue;
        }else{
            if(is_prime(input) >= 1){
                printf("%d is a prime number\n",input);
            }else{
                printf("%d is not a prime number\n",input);
            }
        }
        groups = groups - 1;
    }
    printf("while times is %d in total\n",while_times);
    return 0;
}
```

输入Input

```
-4
0
1
2
3
4
5
6
7
8
```

预期运行结果

```
input an integer,judge if it is prime number,10 groups in total
input > 0 is needed
input > 0 is needed
1 is not concerned
2 is a prime number
3 is a prime number
4 is not a prime number
5 is a prime number
6 is not a prime number
7 is a prime number
8 is not a prime number
while times is 22 in total

```

![demo2-isprime](https://github.com/ChloeNuomiji/SysY-Pcode-Compiler/blob/main/demo2-isprime.gif)

### 文件结构

```
SysY-Pcode-Compiler
├── README.md
├── testfiles
│   └── C // 测试用例
├── src // 编译器代码
│   ├── CodePackage // 中间代码
│   ├── Compiler // 词法分析器、语法分析器、解释器、错误处理分析
│   └── Compiler.java // 编译器入口
├── testfile.txt // SysY源程序
├── input.txt // SysY程序输入
├── output.txt // 运行输出，包括词法分析、中间代码、程序运行结果
├── itmCode.txt // 中间代码
├── lexicalAnalysis.txt // 词法分析结果
├── error.txt // 错误处理分析结果
└── pcoderesult.txt // SysY程序运行结果
```



### 文档

给定文法（见`2021编译文法.pdf`），实现编译器

### 1.1 文法

SysY语言简化版，C语言的子集

具有常量、变量、整数、字符串、一维/二维数组、函数（带参数）、赋值语句、if语句、while语句、break语句、continue语句、语句块、输入输出语句等；语义参考C语言

该版本支持Level C

### 1.2 词法分析和语法分析

### 1.3 中间代码

### 1.4 解释器

编写解释执行程序对中间代码解释执行

### 1.5 错误处理

识别若干类型的错误，包括语法和语义错误，输出错误所在的行号及类别码

### 1.6 测试

更多的testcase在`testfiles/C`文件夹

## 2. English 英文 (todo)