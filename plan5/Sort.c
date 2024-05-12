#include <stdio.h>
#include <pthread.h>
#include <stdlib.h>
#include <limits.h>
#include <sys/time.h>
#include <string.h>

#define TOTAL_NUM 10000
pthread_mutex_t mutex;
int nums[TOTAL_NUM];
int sortedNums[TOTAL_NUM];
//要排序的数组信息
typedef struct{
    int startIndex; //数组起始下标
    int num; //要排序的数量
}SortInfo_t;

//qsort比较函数
int compare(const void* num1,const void* num2){
    int* num_1 = (int*)num1;
    int* num_2 = (int*)num2;
    return *num_1 - *num_2;
}

//线程需要实现的功能
static void* threadFunc(void *arg){
    pthread_mutex_lock(&mutex);
    SortInfo_t* sortInfo = (SortInfo_t*)arg;
    int index = sortInfo->startIndex;
    int num = sortInfo->num;
    qsort(&nums[index],num,sizeof(int),compare);
    pthread_mutex_unlock(&mutex);
}

//合并排序
void merge(SortInfo_t* sortInfos,int threadNum){
    int index[threadNum];
    for(int i = 0;i < threadNum;i++){
        index[i] = sortInfos[i].startIndex;
    }
    for(int i = 0;i < 1000;i++){
        int minIndex = -1;
        int minValue = INT_MAX;
        for(int j = 0;j < threadNum;j++){
            if(index[j] - sortInfos[j].startIndex < sortInfos[j].num && nums[index[j]] < minValue){
                minIndex = j;
                minValue = nums[index[j]];
            }
        }
            sortedNums[i] = minValue;
            index[minIndex]++;
    }
}

int main(int argc,char *argv[]){
    pthread_mutex_init(&mutex,NULL);

    srand((unsigned)time(NULL));
    for(int i = 0;i < TOTAL_NUM;i++){
        nums[i] = rand() % 100;
    }

    printf("请输入所需线程数量：\n");
    int threadNum;
    scanf("%d",&threadNum);
    int err;
    int perNum,lastNum; //每一个线程所需要排序的数据量
    //需要判断1000是否能被线程数量整除
    if(TOTAL_NUM % threadNum == 0){
        perNum = TOTAL_NUM / threadNum;
        lastNum = TOTAL_NUM / threadNum;
    }
    else{
        perNum = TOTAL_NUM / (threadNum - 1);
        lastNum = TOTAL_NUM % (threadNum - 1);
    }

    //分配排序信息
    SortInfo_t* sortInfos = (SortInfo_t*)malloc(threadNum * sizeof(SortInfo_t));
    memset(sortInfos,0,sizeof(SortInfo_t) * threadNum);
    for(int i = 0;i < threadNum;i++){
        if(i != threadNum - 1){
            sortInfos[i].startIndex = i * perNum;
            sortInfos[i].num = perNum;
        }
        else{
            sortInfos[i].startIndex = i * perNum;
            sortInfos[i].num = lastNum;
        }
    }

    //记录时间
    struct timeval start,end;
    long long start_usec,end_usec;
    double total;
    gettimeofday(&start,NULL);

    //创建线程
    pthread_t* pids = (pthread_t *)malloc(threadNum * sizeof(pthread_t));
    for(int i = 0;i < threadNum;i++){
        err = pthread_create(&pids[i],NULL,threadFunc,(void*)(&sortInfos[i]));
        if(err != 0){
            perror("pthread_create");
            exit(EXIT_FAILURE);
        }
    }

    //等待所有线程完成
    for(int i = 0;i < threadNum;i++){
        err = pthread_join(pids[i],NULL);
        if(err != 0){
            perror("pthread_join");
            exit(EXIT_FAILURE);
        }
    }
    merge(sortInfos,threadNum);

    for(int i = 0;i < 1000;i++){
        printf("%d ",sortedNums[i]);
    }

    gettimeofday(&end,NULL);
    start_usec = start.tv_sec * 1000000 + start.tv_usec;
    end_usec = end.tv_sec * 1000000 + end.tv_usec;
    total = (double)(end_usec - start_usec)/1000000.0;
    printf("\n%d个线程:%fs\n",threadNum,total);

    free(sortInfos);
    free(pids);
}