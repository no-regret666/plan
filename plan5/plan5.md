1. `pthread_create`：创建一个新的线程

`int pthread_create(pthread_t *thread,pthread_attr_t *attr,void *(*func)(void *),void *arg);`

参数：`thread`   指向 `pthread_t`类型变量的指针

    `attr`    指向pthread_attr_t类型变量的指针，或者为NULL

    `func`   指向新线程所运行函数的指针

    `arg`  传递给func的参数

返回值：  0   成功返回    ；     errcode  错误

2.`pthread_join`：等待某线程终止

`int pthread_join(pthread_t thread,void **retval);`

参数：`thread`  所等待的线程

    `retval`    指向某存储线程返回值的变量

返回值：0  成功返回  ；  errcode  错误
