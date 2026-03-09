#include <stdio.h>

int main() {
    int n,i,time=0,completed=0;

    char pid[20][10];
    int at[20],bt[20],pr[20];
    int ct[20],wt[20],tat[20];
    int done[20]={0};

    float avg_wt=0,avg_tat=0;

    printf("Enter number of processes: ");
    scanf("%d",&n);

    printf("Enter PID ArrivalTime BurstTime Priority\n");

    for(i=0;i<n;i++){
        printf("Process %d: ",i+1);
        scanf("%s %d %d %d",pid[i],&at[i],&bt[i],&pr[i]);
    }

    while(completed<n){
        int idx=-1;
        int min_pr=9999;

        for(i=0;i<n;i++){
            if(at[i]<=time && done[i]==0){
                if(pr[i]<min_pr){
                    min_pr=pr[i];
                    idx=i;
                }
            }
        }

        if(idx==-1){
            time++;
        }
        else{
            time += bt[idx];
            ct[idx]=time;

            tat[idx]=ct[idx]-at[idx];
            wt[idx]=tat[idx]-bt[idx];

            done[idx]=1;
            completed++;
        }
    }

    printf("\nWaiting Time:\n");
    for(i=0;i<n;i++){
        printf("%s %d\n",pid[i],wt[i]);
        avg_wt+=wt[i];
    }

    printf("\nTurnaround Time:\n");
    for(i=0;i<n;i++){
        printf("%s %d\n",pid[i],tat[i]);
        avg_tat+=tat[i];
    }

    printf("\nAverage Waiting Time: %.2f\n",avg_wt/n);
    printf("Average Turnaround Time: %.2f\n",avg_tat/n);

    return 0;
}