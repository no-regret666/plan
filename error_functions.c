#include <stdarg.h>
#include "error_functions.h"
#include "tlpi_hdr.h"
#include "ename.c.inc"

#ifdef __GNUC__
__attribute__ ((__noreturn__))
#endif
static void
terminate(Boolean useExit3)
{
    char *s;
    //如果定义了 EF_DUMPCORE 环境变量并且是非空字符串，则传储核心；否则，根据 useExit 的值，调用 exit(2) 或 exit(3)
    s = getenv("EF_DUMPCORE");

    if(s != NULL && *s != '\0')
        abort();
    else if(useExit3)
        exit(EXIT_FAILURE);
    else
        _exit(EXIT_SUCCESS);
}