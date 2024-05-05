#include <stdio.h>
#include <pthread.h>
#include <stdlib.h>
#include <limits.h>
#include <sys/time.h>
#include <string.h>

#define TOTAL_NUM 1000
pthread_mutex_t mutex;

//qsort比较函数
int compare(const void* num1,const void* num2){
    int* num_1 = (int*)num1;
    int* num_2 = (int*)num2;
    return *num_1 - *num_2;
}

//线程需要实现的功能
static void* threadFunc(void *arg){
    pthread_mutex_lock(&mutex);
    int* info = (int*)arg;
    qsort(info,sizeof(info)/4,sizeof(int),compare);
}

int main(int argc,char *argv[]){
    int num[1000];
    srand((unsigned)time(NULL));
    for(int i = 0;i < TOTAL_NUM;i++){
        num[i] = rand() % 100;
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

    //切分数组
    int sortNum[TOTAL_NUM][TOTAL_NUM];
    for(int i = 0;i < threadNum;i++){
        if(i != threadNum - 1){
            memcpy(sortNum[i],num,perNum);
        }
        else{
            memcpy(sortNum[i],num,lastNum);
        }
    }

    //记录时间
    int begin,end;
    begin = clock();

    //创建线程
    pthread_t pid;
    for(int i = 0;i < threadNum;i++){
        err = pthread_create(&pid,NULL,threadFunc,sortNum[i]);
        if(err != 0){
            perror("pthread_create");
            exit(EXIT_FAILURE);
        }
    }

    for(int i = 0;i < 1000;i++){
        printf("%d ",sortNum[0][i]);
    }

    end = clock();
    printf("%d个线程:%dms\n",threadNum,end - begin);
}