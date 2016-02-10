#include <stdio.h>
#include <stdlib.h>
#include <string.h>



int main(){
 FILE *f = fopen("a.txt", "r");
 int i;
 unsigned char c[] = { 195, 182,195,167,197,159,196,159,195,188,196,177};

 for(i=0; i<12; i++) printf("%c", c[i]);

  scanf("%d", &i);
  return 0;
 char u[128];

 while(1){
    fgets(u, 127, f);
    if(feof(f)) break;
    i=0;
    while(u[i] != 13){
     printf("%u ", (unsigned char)u[i]);
     i++;          
               
    }
     printf("%s", u);
 }

 return 0;
}
