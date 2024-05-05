#include <stdio.h>
#include <time.h>
int main(){
    // int num[4] = {1,2,3,4};
    // printf("%d",sizeof(num));
    
    // int i = 100;
    // int start,end;
    // start = clock();
    // while(i--);
    // end = clock();
    // printf("%d ms",end - start);

    srand((unsigned)time(NULL));
    int a = rand();
    printf("%d",a);
}