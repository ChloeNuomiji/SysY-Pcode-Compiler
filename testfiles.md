source code 加上可以直接类似C语言地跑

```c
#include <stdio.h>

int getint(){
    int n;
    scanf("%d",&n);
    return n;
}
```

`gcc test.c -o test`



# BASE TESTCASES

```cassandraql
// if else
int main() {
    int a = 3;
    if (a == 1) {
        printf("a == 1");
    } else {
        printf("a != 1");
    }
    return 0;
}
```

`while`

```
int main() {
    int i = 0;
    while (i < 5) {
    printf("i = %d\n", i);
        i = i + 1;
    }
    return 0;
}
```



while+break+continue

```
int main() {
    int i = 0;

    while (i < 5) {
        int j = 0;
        while (j < 3) {
            if (j == 1) {
                break;
            }
            printf("i = %d, j = %d\n", i, j);
            j = j + 1;
        }


        if (i == 2) {
            break;
        }
        printf("i = %d\n", i);
        i = i + 1;
    }
    return 0;
}
```



复杂的条件表达式

```c
int main() {
    int a = 20
    int b = 10;
    int c = 3;
    int d = 4;
    int e = 1;
    printf("a = %d\n b = %d\nc = %d\nd = %d\ne = %d\n", a,b,c,d,e);
    if (a < b ||  c != d && a > e) { 
        printf("in if"); // print here
    } else {
        printf("else");
    }
    return 0;
}
```



简单的函数调用


```cassandraql
int add(int a, int b) {
    return a + b;
}

int main() {
    int num1 = 10, num2 = 20;
    int res;
    res = add(num1, num2);
    printf("res = %d", res);
    return 0;
}
```



递归 to retest

```
int recursion(int a) {
	if (a == 0) {
		return a;
	}
	return 2+recursion(a-1);
}

int main() {
    int res;
	res = recursion(2);
	printf("res = %d", res); // res = 4
	return 0;
}
```



fib 有问题

```
int fun5(int a)
{
    if (a == 1)
    {
        return 1;
    }
    else if (a == 2)
    {
        return 2;
    }
    return fun5(a - 1) + fun5(a - 2);
}

int main()
{
    printf("19373373\n");
    printf("scanf a to get Fibonacci\n");
    int fib;
    fib = getint();
    printf("fib is %d\n", fun5(fib));
    return 0;
}
```





```

int main() {
    printf("20060107");
    printf("\n");
    int num1 = 21;
    int num2 = 9;
    int resArr[5];
    resArr[0] = num1*num2;
    resArr[1] = num1/num2;
    resArr[2] = num1%num2;
    resArr[3] = num1/num2+num1%num2;
    resArr[4] = num1/num2-num1%num2;
    int index = 4;
    while(index >= 0) {
        printf("%d\n", resArr[index]);
        index = index-1;
    }
    return 0;
}
```






# Complicated testcases

if else 
```c
void func1() {
    int a = 0, b = 0, c = 0;
    if (a >= 0) {
        if (b >= 0) c = 0;
        else {
            a = 1;
        }
    }
}
```


while （双重嵌套）
```c
int main() {
    int b = 0;
    while (b < 10) {
        int j = 10;
        while (j >= 0) {
            j = j - 1;
            printf("b %d; j %d\n", b, j);
        }

        b = b + 1;
    }
    printf("end while\n");

    b = 1;
    b = 2;
    return 0;
}
```

break
```c
int main() {
    int i = 0, j = 0;
    while (i < 10) {

        while (j < 5) {
            if (j == 3) {
                printf("j == 3\n");
                break; // 嵌套循环
            }
            j = j+1;
        }

        i = i + 1;
        printf("i %d", i);
        break;
    }
}
```



continue

```c
int main() {
    int i = 0, j = 0;
    while (i < 10) {

        while (j < 5) {
            if (j < 2) {
                printf("j %d\n", j);
                j = j+1;
                continue; // 嵌套循环
            }
            j = j+1;
        }
        printf("i %d\n", i);
        i = i + 1;
        continue;
    }
}
```

# Level A Testcases

```

int vb[3][3] = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};

/*
void print_vec(int x[]) {
    printf("%d\n", x[0]);
	//printf("%d %d %d\n", x[0], x[1], x[2]);
}
*/


void print_mat(int m[][3]) {
	int dt = 0;
	while (!0) {

	    //printf("m[dt] = m[%d]\n", dt);
		//print_vec(m[dt]);
		dt = dt + 1;
		if (0 || 1 && 1 || 0) {
		    printf("test");
		    break;
		}


		if (dt < 3 && 1) {
		    printf("dt = %d\n", dt);
		    continue;
		} //1<3
		else {break;}
	}
}


int main() {
	int d1 = 0;
	print_mat(vb);
	return 0;
}


```







# Level B Testcases

数组定义

```
void assignArray(int arr[][3]) { // todo: arr[1][3], func(arr[2])
    int i = 0;
    while (i < 2) {
        arr[1][i] = i*5;
        printf("arr[1][%d] = %d ", i, arr[1][i]);
        i = i + 1;
    }
    printf("\n");
}

int main() {
	int array[2][5] = {{10,9,8,7,6},{5,4,3,2,1}};
	//int array[2][2] = {{4,3}, {2,1}};
	int i = 0, j = 0;
	assignArray(array);
    while (i < 5) {

        j = 0;
        while (j < 2) {
            printf("array[%d][%d] = %d\n", i, j, array[i][j]);
            j = j + 1;
        }
        i = i + 1;
    }





	return 0;
}

```







**新增**数组，包括数组定义，数组元素的使⽤、数组传参和部分传参等

![image-20211217235930589](C:\Users\chenz\AppData\Roaming\Typora\typora-user-images\image-20211217235930589.png)





# Level C Testcases

```
int global_var = 0;
int func() {
     global_var = global_var + 1;
     return 1;
}
int main() {
     if (0 && func()){
     ;
     }
     printf("%d",global_var); // 输出 0
     if (1 || func()) {
     ;
     }
     printf("%d",global_var); // 输出 0
     return 0;
}
```



# todo

错误处理，编译无法终止

```
void func1() {
    int a, b, c;
    if (a >= 0) {
        if (b >= 0) c = 0; c = 2; // 错误处理，编译无法终止
        else {
            a = 1;
        }
    }
}
```



```c
void func(int a) {

}

int main() {
	int b = 1;
	if (b < func(b)) { // 有问题吗
        b = 2;
    } else {
        b = 3;
    }
    printf("b: %d", b); 
    return 0;
}
```



C类testfile7 先看看source code有无错误。没有的话再说

# 考试回忆

### 2021 错误处理：5分

新增的错误类型，挺常见也挺好改的。准备的时候看看往年2年的题目，想想咋改就好。

学会怎么测试

ConstExp

![image-20211231143151926](C:\Users\chenz\AppData\Roaming\Typora\typora-user-images\image-20211231143151926.png)

如果AddExp为变量，需要报错

```
int var = 1;

int main() {
	const int constA[var][var] = {{1}}; //

}
```

### 2020 错误处理

![image-20211231144022761](C:\Users\chenz\AppData\Roaming\Typora\typora-user-images\image-20211231144022761.png)

### 代码生成：10分



代码生成与优化：10分

给了3个testfile，要跑出结果。根据结果正确性和竞速排名给分



```
int i; // 函数调用

func1() {
	i = getint();
	printf("global i is %n", i);
}

int main() {
	int i = getint();
	
}
```

```
卷积计算，涉及数组的计算
```



## 问答题

1、给出函数调用相关的运行栈（以testfile1为例）

2、列出一个小demo，给出访问数组元素的四元式，解释（以testfile3为例）

3、忘了



