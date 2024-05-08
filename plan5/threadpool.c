#include <stdio.h>
#include <pthread.h>
#include <stdlib.h>
#include <stdbool.h>

#define THREADPOOL_SIZE 4
#define TASKQUEUE_SIZE 16
int error = 0;

//任务
typedef struct{
    void* (*function)(void*);
    void* args;
}task_t;

//任务队列
typedef struct{
    task_t queue[TASKQUEUE_SIZE];
    int head; //队首
    int tail; //队尾
    int size; //队列大小
    pthread_mutex_t mutex; //互斥锁
    pthread_cond_t cond; //条件变量
}taskQueue_t;

//线程池
typedef struct{
    taskQueue_t* taskQueue;
    int threadNum; //线程池线程数量
    pthread_t threads[THREADPOOL_SIZE]; //线程
    int working_threads; 
    int shutdown; //线程池状态
}threadPool_t;

void taskQueue_init(taskQueue_t* queue){
    pthread_mutex_init(&queue->mutex,NULL);
    pthread_cond_init(&queue->cond,NULL);
    queue->head = 0;
    queue->tail = 0;
    queue->size = 0;
}

void taskQueue_push(taskQueue_t* taskQueue,task_t task){
    pthread_mutex_lock(&taskQueue->mutex);
    //等待队列非满
    while(taskQueue->size == TASKQUEUE_SIZE){
        pthread_cond_wait(&taskQueue->cond,&taskQueue->mutex);
    }
    taskQueue->queue[taskQueue->tail] = task;
    taskQueue->tail = (taskQueue->tail + 1) % TASKQUEUE_SIZE;
    taskQueue->size++;
    pthread_cond_signal(&taskQueue->cond);
    pthread_mutex_unlock(&taskQueue->mutex);
    return 0;
}

task_t taskQueue_pop(taskQueue_t* taskQueue){
    pthread_mutex_lock(&taskQueue->mutex);
    //等待队列非空
    while(taskQueue->size == 0){
        pthread_cond_wait(&taskQueue->cond,&taskQueue->mutex);
    }
    task_t task = taskQueue->queue[taskQueue->head];
    taskQueue->head = (taskQueue->head + 1) % TASKQUEUE_SIZE;
    taskQueue->size--;
    pthread_cond_signal(&taskQueue->cond);
    pthread_mutex_unlock(&taskQueue->mutex);
    return task;
}

void taskQueue_destroy(taskQueue_t* queue){
    pthread_mutex_destroy(&queue->mutex);
    pthread_cond_destroy(&queue->cond);
}

void threadPool_init(threadPool_t* threadPool){
    taskQueue_init(&threadPool->taskQueue);
    threadPool->taskQueue = (taskQueue_t*)malloc(sizeof(taskQueue_t));
    threadPool->working_threads = 0;
    threadPool->shutdown = 0;
    threadPool->threadNum = THREADPOOL_SIZE;
    for(int i = 0;i < threadPool->threadNum;i++){
        pthread_create(&threadPool->threads[i],NULL,execute_task,(void*)threadPool);
    }
}

void execute_task(void* args){
    threadPool_t* threadPool = (threadPool_t*)args;
    task_t task;
    while(1){
        if(threadPool->shutdown){
            break;
        }
        task = taskQueue_pop(&threadPool->taskQueue);
        (*task.function)(task.args);
    }
}

void threadPool_destroy(threadPool_t* threadPool){
    taskQueue_destroy(&threadPool->taskQueue);
    pthread_cond_signal(&threadPool->taskQueue->cond);
    for(int i = 0;i < threadPool->threadNum;i++){
        pthread_join(threadPool->threads[i],NULL);
    }
    if(threadPool->taskQueue){
        free(threadPool->taskQueue);
    }
}