#include <stdio.h>
#include <pthread.h>
#include <stdlib.h>
#include <stdbool.h>

//任务
typedef struct{
    void* (*function)(void*);
    void* arg;
}task_t;

//线程池
typedef struct{
    int pthreadNum;
    pthread_t* threads;
    task_t* taskQueue;
    pthread_mutex_t mutex;
    pthread_cond_t queue_ready;
    bool shutdown;
}threadpool_t;

int threadpool_create();