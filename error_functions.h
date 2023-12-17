#ifndef ERROR_FUNCTIONS_H
#define ERROR_FUNCTIONS_H

void errMsg(const char *format,...);

#ifdef __GNUC__ //如果我们使用以下函数终止main()或其他一些非void函数，这个宏将停止“gcc-Wall”抱怨“控制到达非void函数的末尾”

#define NORETURN __attribute__ ((__noreturn__))
#else
#define NORETURN
#endif

void errExit(const char *format,...) NORETURN ;

void err_exit(const char *format,...) NORETURN ;

void errExitEN(int errnum,const char *format,...) NORETURN ;

void fatal(const char *format,...) NORETURN ;

void usageErr(const char *format,...) NORETURN ;

void cmdLineErr(const char *format,...) NORETURN ;

#endif