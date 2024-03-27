#include <stdio.h>
#include <pthread.h>
#include <ctype.h>
struct arg_set{
    char *fname; 
    int count; 
};

struct arg_set *mailbox;
pthread_mutex_t lock = PTHREAD_MUTEX_INITIALIZER;
pthread_cond_t flag = PTHREAD_COND_INITIALIZER;

int main(int argc,char *argv[]){
    pthread_t t1,t2;
    struct arg_set args1,args2;
    void *count_words(void *);
    int reports_in = 0;
    int total_words = 0;
    if(argc != 3){
        printf("usage: %s file1 file2\n",argv[0]);
        exit(1);
    }
    pthread_mutex_lock(&lock);
    
    args1.fname = argv[1];
    args1.count = 0;
    pthread_create(&t1,NULL,count_words,(void *)&args1);

    args2.fname = argv[2];
    args2.count = 0;
    pthread_create(&t2,NULL,count_words,(void *)&args2);

    while(reports_in < 2){
        printf("MAIN:waiting for flag to go up\n");
        pthread_cond_wait(&flag,&lock);
        printf("MAIN:Wow!flag was raised,i have the lock\n");
        printf("%7d:%s\n",mailbox->count,mailbox->fname);
    }
}