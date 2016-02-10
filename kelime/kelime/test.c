#include <stdio.h>
#include <stdlib.h>
#include <string.h>
unsigned char places[256] = {-1};
unsigned char abc[29] =  "abc0defg1hi2jklmno3prs4tu5vyz";


char* str[] = {"a","b","c","ç","d","e","f","g","ğ","h","i","ı","j","k","l","m","n","o","ö","p","r","s","ş","t","u","ü","v","y","z"};


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
    if(c[1] != tmp[0] || c[2] != tmp[1] || c[3] != tmp[2]) goto loopend;
    if(c[0] == wordl){
      for(k=0; k<wordl; k++) if(c[k+1] != tmp[k]) break;

      if(k==wordl){
        found[foundl].l = wordl;
        found[foundl].str = c+1;
        foundl++;
        /*
        for(k=0; k<wordl; k++) printf("%s", str[tmp[k]]);
        printf("\n");*/
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

int compare (const void * a, const void * b)
{
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
  FILE *f = fopen("t3.txt", "r");
  for(i=0; i<29; i++) places[abc[i]] = i;
    
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
  
  while(1){

    unsigned char s[100] = "aghitefak3r545ki";

    k=0;

    for(i=0; s[i]; i++){
  
      if(s[i] >= 'a' && s[i] <= 'z') tmp[k++] = s[i];
      else if(s[i] >= '0' && s[i] <= '9') tmp[k++] = s[i];
      else if(s[i] == 195 && s[i+1] == 182){i++; tmp[k++] ='3';}  
      else if(s[i] == 195 && s[i+1] == 167){i++; tmp[k++] ='0';}  
      else if(s[i] == 197 && s[i+1] == 159){i++; tmp[k++] ='4';}  
      else if(s[i] == 196 && s[i+1] == 177){i++; tmp[k++] ='2';}  
      else if(s[i] == 196 && s[i+1] == 159){i++; tmp[k++] ='1';}  
      else if(s[i] == 195 && s[i+1] == 188){i++; tmp[k++] ='5';}  
       

    }

    for(i=0; i<6; i++)
      for(j=0; j<6; j++) used[i][j] = b[i][j] = -1;

    tmp[k] = 0;
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
    int q = 1;
    FILE *fo = fopen("out.txt", "w");
    
    for(i=1; i<5; i++){
      for(j=1; j<5; j++){
        fprintf(fo, "%s", str[b[i][j]]);
      }fprintf(fo, "\n");
    }fprintf(fo, "\n");
    qsort(found, foundl, sizeof(wordbuffer),  compare);
    for(i=1; i<foundl; i++){
        if(found[i].l == found[i-1].l){
          for(k=found[i].l-1; k>=0; k--) if(found[i].str[k] != found[i-1].str[k]) goto printword;
          continue;
        }
        printword:
        for(k=0; k<found[i].l; k++) fprintf(fo, "%s", str[found[i].str[k]]);
        if(!q)fprintf(fo, "\n");
        else{for(;k<8;k++)fprintf(fo, " ");}
        q++;
        if(q==5) q = 0;
    }
    printf("\n");
    fclose(fo);
    break;
  }
  return 0;
}
