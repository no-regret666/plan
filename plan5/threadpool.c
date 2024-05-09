#include <stdio.h>
#include <pthread.h>
#include <stdlib.h>
#include <stdbool.h>
#include <string.h>

#define NUM 2

// 任务
typedef struct
{
    void *(*function)(void *);
    void *arg;
} task_t;

// 任务队列
typedef struct
{
    task_t *queue;         // 任务数组
    int capacity;          // 容量
    int head;              // 队首
    int tail;              // 队尾
    int size;              // 队列大小
    pthread_mutex_t mutex; // 互斥锁
    pthread_cond_t cond;   // 条件变量，判断任务队列为空/为满
} pool_t;

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

    pthread_t manager;         // 管理者线程ID
    pthread_t *threads;        // 工作的线程ID
    int minNum;                // 最小线程数量
    int maxNum;                // 最大线程数量
    int busyNum;               // 忙的线程数量，变化机会较大
    int liveNum;               // 存活线程数量
    int exitNum;               // 要杀死的线程数量
    pthread_mutex_t mutexBusy; // 锁busyNum变量
    int shutdown;              // 线程池状态，销毁为1，不销毁为0
} threadPool_t;

void pool_push(pool_t *pool, task_t task)
{
    pthread_mutex_lock(&pool->mutex);
    // 等待队列非满
    while (pool->size == pool->capacity)
    {
        pthread_cond_wait(&pool->cond, &pool->mutex);
    }
    pool->queue[pool->tail] = task;
    pool->tail = (pool->tail + 1) % pool->capacity;
    pool->size++;
    pthread_cond_signal(&pool->cond);
    pthread_mutex_unlock(&pool->mutex);
    return 0;
}

threadPool_t* threadPool_init(int min, int max, int queueSize)
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

    threadPool->threads = (pthread_t *)malloc(sizeof(pthread_t) * max);
    memset(threadPool->threads, 0, sizeof(pthread_t) * max);
    threadPool->minNum = min;
    threadPool->maxNum = max;
    threadPool->busyNum = 0;
    threadPool->liveNum = min;
    threadPool->exitNum = 0;
    pthread_mutex_init(&threadPool->mutexBusy, NULL);
    threadPool->shutdown = 0;

    // 创建线程
    pthread_create(&threadPool->manager, NULL, manager, NULL);
    for (int i = 0; i < min; i++)
    {
        pthread_create(&threadPool->threads[i], NULL, worker, threadPool);
    }

    return threadPool;
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
            threadPool_exit(pool);
        }

        // 从任务队列中取出一个任务
        task_t task = pool->queue[pool->head];
        pool->head = (pool->head + 1) % pool->capacity;
        pool->size--;

        pthread_cond_signal(&pool->cond);
        pthread_mutex_unlock(&pool->mutexPool);

        pthread_mutex_lock(&pool->mutexBusy);
        pool->busyNum++;
        pthread_mutex_unlock(&pool->mutexBusy);
        (*task.function)(task.arg);
        pthread_mutex_lock(&pool->mutexBusy);
        pool->busyNum--;
        pthread_mutex_unlock(&pool->mutexBusy);
    }
}

// 管理者线程函数
void *manager(void *arg)
{
    threadPool_t *pool = (threadPool_t *)arg;
    while (!pool->shutdown)
    {
        // 每隔3s检测一次
        sleep(3);

        // 取出线程池中任务的数量和存活线程的数量
        pthread_mutex_lock(&pool->mutexPool);
        int queueSize = pool->size;
        int liveNum = pool->liveNum;
        pthread_mutex_unlock(&pool->mutexPool);

        // 取出忙的线程的数量
        pthread_mutex_lock(&pool->mutexBusy);
        int busyNum = pool->busyNum;
        pthread_mutex_unlock(&pool->mutexBusy);

        //添加线程
        //任务个数 > 存活线程的个数 && 存活线程个数 < 最大线程数量
        if(queueSize > liveNum && liveNum < pool->maxNum){
            pthread_mutex_lock(&pool->mutexPool);
            int cnt = 0;
            for(int i = 0;i < pool->maxNum && cnt < NUM && pool->liveNum < pool->maxNum;i++){
                if(pool->threads[i] == 0){
                    pthread_create(pool->threads[i],NULL,worker,pool);
                    pool->liveNum++;
                    cnt++;
                }
            }
            pthread_mutex_unlock(&pool->mutexPool);
        }

        //销毁线程
        
    }
}

int threadPool_destroy(threadPool_t *pool)
{
    if (pool == NULL)
    {
        return -1;
    }
    pool->shutdown = 1;

    // 回收管理者线程
    pthread_join(pool->manager, NULL);

    // 唤醒阻塞的消费者线程
    for (int i = 0; i < pool->liveNum; i++)
    {
        pthread_cond_signal(&pool->cond);
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
    pthread_mutex_destroy(&pool->mutexBusy);
    pthread_mutex_destroy(&pool->mutexPool);

    free(pool);
    pool = NULL;

    return 0;
}

void threadPool_exit(threadPool_t *pool)
{
}