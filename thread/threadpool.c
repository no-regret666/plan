#include <stdio.h>
#include <pthread.h>
#include <stdlib.h>
#include <stdbool.h>
#include <string.h>
#include <unistd.h>

#define NUM 2

// 任务
typedef struct
{
    void (*function)(void *);
    void *arg;
} task_t;

// 线程池
typedef struct
{
    // 任务队列
    task_t *queue;             // 任务数组
    int capacity;              // 容量
    int head;                  // 队首
    int tail;                  // 队尾
    int size;                  // 队列大小
    pthread_mutex_t mutexPool; // 互斥锁
    pthread_cond_t cond;       // 条件变量，判断任务队列为空/为满

    pthread_t *threads;        // 工作的线程ID
    int threadNum;             // 线程池中线程数量
    int busyNum;               // 正在工作的线程数量
    int shutdown;              // 线程池状态，销毁为1，不销毁为0
} threadPool_t;

void add_task(threadPool_t *pool, void (*function)(void *), void *arg);
threadPool_t *threadPool_init(int threadNum, int queueSize);
void *worker(void *arg);
int threadPool_destroy(threadPool_t *pool);
void task_function(void *arg);

threadPool_t *threadPool_init(int threadNum, int queueSize)
{
    threadPool_t *threadPool = (threadPool_t *)malloc(sizeof(threadPool_t));
    // 初始化任务队列
    pthread_mutex_init(&threadPool->mutexPool, NULL);
    pthread_cond_init(&threadPool->cond, NULL);
    threadPool->queue = (task_t *)malloc(sizeof(task_t) * queueSize);
    threadPool->capacity = queueSize;
    threadPool->head = 0;
    threadPool->tail = 0;
    threadPool->size = 0;

    threadPool->threads = (pthread_t *)malloc(sizeof(pthread_t) * threadNum);
    memset(threadPool->threads, 0, sizeof(pthread_t) * threadNum);
    threadPool->threadNum = threadNum;
    threadPool->shutdown = 0;

    // 创建线程
    for (int i = 0; i < threadNum; i++)
    {
        pthread_create(&threadPool->threads[i], NULL, worker, threadPool);
    }

    return threadPool;
}

void add_task(threadPool_t *pool, void (*function)(void *), void *arg)
{
    //添加线程
    if(pool->size > pool->threadNum){
        pthread_t thread;
        pthread_create(&thread,NULL,worker,pool);
        pool->threadNum++;
    }

    task_t task;
    task.function = function;
    task.arg = arg;
    pthread_mutex_lock(&pool->mutexPool);
    // 等待队列非满
    while (pool->size == pool->capacity && !pool->shutdown)
    {
        pthread_cond_wait(&pool->cond, &pool->mutexPool);
    }
    if (pool->shutdown)
    {
        pthread_mutex_unlock(&pool->mutexPool);
        return;
    }
    pool->queue[pool->tail] = task;
    pool->tail = (pool->tail + 1) % pool->capacity;
    pool->size++;
    pthread_cond_signal(&pool->cond);
    pthread_mutex_unlock(&pool->mutexPool);
}

// 工作线程函数
void *worker(void *arg)
{
    threadPool_t *pool = (threadPool_t *)arg;
    while (1)
    {
        pthread_mutex_lock(&pool->mutexPool);
        // 等待队列非空
        while (pool->size == 0 && !pool->shutdown)
        {
            pthread_cond_wait(&pool->cond, &pool->mutexPool);
        }
        if (pool->shutdown)
        {
            pthread_mutex_unlock(&pool->mutexPool);
            break;
        }

        // 从任务队列中取出一个任务
        task_t task = pool->queue[pool->head];
        pool->head = (pool->head + 1) % pool->capacity;
        pool->size--;

        pthread_cond_signal(&pool->cond);
        pthread_mutex_unlock(&pool->mutexPool);

        pool->busyNum++;
        (*task.function)(task.arg);
        pool->busyNum--;
    }
    pthread_exit(pool);
}

int threadPool_destroy(threadPool_t *pool)
{
    if (pool == NULL)
    {
        return -1;
    }
    pool->shutdown = 1;

    // 唤醒所有线程
    for (int i = 0; i < pool->threadNum; i++)
    {
        pthread_cond_signal(&pool->cond);
    }
    //等待所有线程完成
    while(pool->busyNum != 0){
        sleep(10);
    }
    for (int i = 0; i < pool->threadNum; i++)
    {
        pthread_join(pool->threads[i], NULL);
    }

    // 释放堆内存
    if (pool->threads)
    {
        free(pool->threads);
    }
    if (pool->queue)
    {
        free(pool->queue);
    }

    pthread_cond_destroy(&pool->cond);
    pthread_mutex_destroy(&pool->mutexPool);

    free(pool);
    pool = NULL;

    return 0;
}

void task_function(void *arg)
{
    int *num = (int *)arg;
    long long result = 1;
    for(int i = 1;i <= *num;i++){
        result *= i;
    }
    printf("thread %ld:%d的阶乘结果为%lld\n",pthread_self(),*num,result);
    //sleep的基本单位是秒,usleep的基本单位是微秒
    usleep(1000);
}

int main()
{
    threadPool_t *threadPool = threadPool_init(4, 16);
    for (int i = 1; i <= 20; i++)
    {
        int* num = (int *)malloc(sizeof(int));
        *num = i;
        add_task(threadPool, task_function, num);
    }
    sleep(1);
    threadPool_destroy(threadPool);
    return 0;
}