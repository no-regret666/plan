#ifndef TLPI_HDR_H
#define TLPI_HDR_H //防止重定义

#include <sys/types.h> //许多程序使用的类型定义
#include <stdio.h> //标准I/O功能
#include <stdlib.h> //常用函数库的原型，加上 EXIT_SUCCESS 和 EXIT_FAILURE 常量

#include <unistd.h> //许多系统调用的原型
#include <errno.h> //声明errno并定义错误变量
#include <string.h> 

#include "get_num.h" //声明我们处理数字的函数

#include "error_functions.h" //声明错误处理函数

typedef enum { FALSE,TRUE } Boolean;

#define min(m,n) ((m) < (n) ? (m) : (n))
#define max(m,n) ((m) > (n) ? (m) : (n))

#endif