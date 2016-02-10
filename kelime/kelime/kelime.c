#include <stdio.h>
#include <stdlib.h>
#include <string.h>
unsigned char places[256] = {-1};
unsigned char abc[29] =  "abc0defg1hi2jklmno3prs4tu5vyz";


char* str[] = {"a","b","c","0","d","e","f","g","1","h","i","2","j","k","l","m","n","o","3","p","r","s","4","t","u","5","v","y","z"};

int chance[29] ={6, 3, 2, 2, 4, 6, 1, 2, 1, 2, 4, 5, 1, 4, 5, 6, 6, 2, 1, 2, 3, 2, 2, 3, 2, 2,2,  4, 2};
int totalChance;
unsigned char letterBuff[256];

unsigned char getChar(){
  return  letterBuff[rand()%totalChance];
}
typedef struct{
  int l;
  char* str;
}wordbuffer;

wordbuffer found[1000];
int foundl = 0;

unsigned char buff[1024*1024*4];
unsigned int buffSize = 1;

unsigned char *c;

int e3[29][29][29] = {0};

unsigned char e2[29][29] = {0};

int b[6][6];
int used[6][6];

int score[] = {0, 0, 1, 2, 4, 7, 10, 15, 25, 40, 60, 80 };

unsigned char tmp[128];
int wordl;
int k;

void rec(int x, int y){
  if(used[x][y]) return;
  used[x][y] = -1;
  tmp[wordl] = b[x][y];
  wordl++;
  if(wordl==2 && !e2[tmp[0]][tmp[1]]){
    goto noword;
  }
  if(wordl==3 && !e3[tmp[0]][tmp[1]][tmp[2]]) goto noword;

  c = buff+e3[tmp[0]][tmp[1]][tmp[2]];
  while(1){
    if(c >= buff+buffSize || c[1] != tmp[0] || c[2] != tmp[1] || c[3] != tmp[2]) goto loopend;
    if(c[0] == wordl){
      for(k=0; k<wordl; k++) if(c[k+1] != tmp[k]) break;

      if(k==wordl){
        found[foundl].l = wordl;
        found[foundl].str = c+1;
        foundl++;
      }
    }
    c += c[0]+1;

  }
  loopend:
  

  rec(x-1, y-1);
  rec(x+1, y-1);
  rec(x+1, y+1);
  rec(x-1, y+1);

  rec(x-1, y);
  rec(x+1, y);

  rec(x, y-1);
  rec(x, y+1);

  noword:
  used[x][y] = 0;
  wordl--;
  return;
}

int compare (const void * a, const void * b){
  int z = ( *(int*)a - *(int*)b );
  if(z)return z;
  wordbuffer *y = (wordbuffer*)a;
  wordbuffer *x = (wordbuffer*)b;
  int len = x->l;
  for(z=0; z<len; z++) if(x->str[z]-y->str[z]) return x->str[z]-y->str[z];
  return 0;
}


int main(){
  int i, j, len;
  int z = 10;
  srand(time(0));
  FILE *f = fopen("t3.txt", "r");
  for(i=0; i<29; i++) places[abc[i]] = i;
    
  totalChance = 0;
  len=0;
  for(i=0; i<29; i++){
     for(j=0; j<chance[i]; j++) letterBuff[len++] = i;
     totalChance += chance[i];
  }
 
    
  unsigned char u[128];

  while(1){
    fgets(tmp, 127, f);
    if(feof(f)) break;
    c = tmp;
    len=0;

    while(*c && *c-10){
      u[len] =  places[*c];
      c++;
      len++;
    }

    
    if(e3[u[0]][u[1]][u[2]] == 0){
      e3[u[0]][u[1]][u[2]] = buffSize;
      e2[u[0]][u[1]] = 1;
    }
    buff[buffSize++] = len;
    for(i=0; i<len; i++) buff[buffSize++] = u[i];
  }
  fclose(f);   
  
  char out[100] = "fout";
  char num[5];
  sprintf(num, "%4d", rand()%10000);
  strcat(out, num);
  strcat(out, ".txt");
  printf("%s", out);
  FILE *fout = fopen(out, "w");
  z=1000;
  while(z){
    
    for(i=0; i<16; i++) tmp[i] = abc[getChar()];
    tmp[16] = '\0';   
    for(i=0; i<6; i++)
      for(j=0; j<6; j++) used[i][j] = b[i][j] = -1;


    c = tmp;
    for(i=1; i<5; i++){
      for(j=1; j<5; j++){
        while(*c == ' ') c++;
        b[i][j] = places[*c];
        used[i][j] = 0;
        c++;
      }
    }

    foundl = 1;
    found[0].l = 1;
    found[0].str = buff+1;
    
    for(i=1; i<5; i++){
      for(j=1; j<5; j++){
        wordl = 0;
        rec(i, j);
      }
    }

    int has7 = 0;
    len=0;
    qsort(found, foundl, sizeof(wordbuffer),  compare);
    for(i=1; i<foundl; i++){
        if(found[i].l == found[i-1].l){
          for(k=found[i].l-1; k>=0; k--) if(found[i].str[k] != found[i-1].str[k]) goto printword2;
          continue;
        }
        printword2:
        if(found[i].l >= 7) has7 = 1;
        len+= score[found[i].l];
        
    }
    if(len>500 && has7){
      z--;
      
    for(i=1; i<5; i++){
      for(j=1; j<5; j++){
        fprintf(fout, "%c", abc[b[i][j]]);
      }
    }
    fprintf(fout, " %d ", len);
    printf("%d\n", len);
      for(i=1; i<foundl; i++){
        if(found[i].l == found[i-1].l){
          for(k=found[i].l-1; k>=0; k--) if(found[i].str[k] != found[i-1].str[k]) goto printword;
          continue;
        }
        printword:
        for(k=0; k<found[i].l; k++) fprintf(fout, "%s", str[found[i].str[k]]);
        j+= score[found[i].l];
        fprintf(fout, ", ");
    }
        fprintf(fout, "\n");
        fflush(fout);
      
    }
  
  
  }
  fclose(fout);
  
  scanf("%d\n", &i); 
  return 0;
}
