#include<stdio.h>
#include<stdlib.h>
#include<time.h>

#define bufferSize 450806
#define MAXN 16
int scores[] = {0, 0, 0, 1, 5, 9, 15, 22, 30, 39, 49, 60, 72, 84, 98, 112, 127, 143, 160, 178};

int chance[29] ={6, 3, 2, 2, 4, 6, 1, 2, 1, 2, 4, 5, 1, 4, 5, 6, 6, 2, 1, 2, 3, 2, 2, 3, 2, 2,2,  4, 2};
int totalChance;
unsigned char letterBuff[256];

unsigned char getChar(){
  return letterBuff[rand()%totalChance];
}

struct Result{
  char board[MAXN][MAXN];
  int score;
  int longest;
  struct Result *next;
  struct Result *prev;
};

int found[10000];
int foundl = 0;

int n = 5;
int longest;
unsigned char abc[30] =  "abc0defg1hi2jklmno3prs4tu5vyz";
int arr[256] = {-1};

int exist2[29][29] = {-1};
int exist3[29][29][29] = {-1};

char buffer[bufferSize+1];

char board[MAXN][MAXN];
char used[MAXN][MAXN];

char word[128];
int wordl = 0;

int m = 0;

void rec(int x, int y, int start){
  if(x<0||y<0||x>=n||y>=n) return;
  if(used[x][y]) return;
  
  used[x][y] = 1;
  word[wordl++] = board[x][y];
  int hasHope = 0;
  
  if(wordl == 1) hasHope = 1;
  else if(wordl == 2){
    if(exist2[word[0]][word[1]] != -1) hasHope = 1;    
  }else{
    
    int c = exist3[word[0]][word[1]][word[2]];
    if(wordl == 3) start = c;
    while(c<bufferSize){
      if(buffer[c+1] != word[0] || buffer[c+2] != word[1] || buffer[c+3] != word[2]) break;
      
      
      if(buffer[c] >= wordl){
        int i;
        for(i=0; i<wordl; i++){
          if(buffer[c+i+1] != word[i]){
            if(buffer[c+i+1] < word[i]) start = c;
            break;
          }
        }
        int valid = (i==wordl);
        hasHope |= valid;
        if(valid && buffer[c] == wordl){
          found[foundl++] = c;
          if(longest<buffer[c]) longest = buffer[c];
        }
      }
      c+= buffer[c]+1;
    }   
  }
  
  if(hasHope){
    rec(x-1, y, start);
    rec(x+1, y, start);
    rec(x, y-1, start);
    rec(x, y+1, start);
    
    rec(x-1, y-1, start);
    rec(x+1, y-1, start);
    rec(x-1, y+1, start);
    rec(x+1, y+1, start);
  }
    
    
  used[x][y] = 0;
  wordl--;
}


int compare (const void * a, const void * b){
  int x = *((int*)a);
  int y = *((int*)b);
  int l = buffer[x]+1;
  for(int i=0; i<l+1; i++){
    int diff = buffer[x+i]-buffer[y+i];
    if(diff) return diff;
  }
  
  return 0;
}


#define BESTSIZE 500
int main(){
  int seed = time(0);
  srand(seed);
  
  struct Result best[BESTSIZE];
  struct Result *first, *last;
  
  best[0].score = 10000000;
  best[0].next = &best[1];
  best[0].prev = 0;
    
  for(int i=1; i<BESTSIZE-1; i++){
    best[i].score = 0;
    best[i].next = &best[i+1];
    best[i].prev = &best[i-1];
  }
  best[BESTSIZE-1].score = 0;
  best[BESTSIZE-1].next = 0;
  best[BESTSIZE-1].prev = &best[BESTSIZE-2];
  
  first = &best[0];
  last = &best[BESTSIZE-1];
  
  for(int i=0; i<30; i++) arr[abc[i]] = i;
  
  for(int i=0; i<30; i++){
    for(int j=0; j<30; j++){
      exist2[i][j] = -1;
      for(int k=0; k<30; k++){
        exist3[i][j][k] = -1;
      }
    }
  }
  
  
  totalChance = 0;
  int len=0;
  for(int i=0; i<29; i++){
     for(int j=0; j<chance[i]; j++) letterBuff[len++] = i;
     totalChance += chance[i];
  }
 
 
  FILE *f = fopen("words.bin", "rb");
  fread(buffer, 1, bufferSize, f);
  fclose(f);  
  buffer[bufferSize] = 0;
  
  int c = 0;
  
  while(c<bufferSize){
    int l = buffer[c];
    exist2[buffer[c+1]][buffer[c+2]] = 1; 
    if(exist3[buffer[c+1]][buffer[c+2]][buffer[c+3]]<0){      
      exist3[buffer[c+1]][buffer[c+2]][buffer[c+3]] = c; 
    }
    c += l+1;
  }
  n = 4;
  scanf("%d", &n);
  /**/
  int count = 0;
  char buff[64];
  while(1){
    if(++count%100 == 0){
      sprintf(buff, "out_%d_%d.txt", n, seed, count);
      FILE *out = fopen(buff, "w");
      
      struct Result *current = first->next;
      while(current){
        for(int i=0; i<n; i++){
          for(int j=0; j<n; j++){
            fprintf(out, "%c", abc[current->board[i][j]]);
          }
        }
        fprintf(out, " %d\n", current->score);
        current = current->next; 
      }
      fclose(out);
    }
    for(int i=0; i<n; i++){
      for(int j=0; j<n; j++){
        board[i][j] = getChar(); 
        used[i][j] = 0;
      }
    }
    found[0] = bufferSize;
    foundl = 1;
    longest = 0;
    /*
    char s[] = "neyyrel0emkrsknh";
    for(int i=0; i<16; i++) board[i/4][i%4] = arr[s[i]];
    */
    wordl = 0;
    for(int i=0; i<n; i++){
      for(int j=0; j<n; j++){
        rec(i, j, -1);
      }
    }
    qsort(found, foundl, sizeof(int),  compare);
    int score = 0;
    for(int i=1; i<foundl; i++){
      if(found[i-1] == found[i]) continue;
      int c = found[i];
      score += scores[buffer[c]];
      /*for(int i=0; i<buffer[c]; i++){
        printf("%c", abc[buffer[c+i+1]]);
      }  
      printf("\n");*/
    }  
    if(score > last->score){
      last->score = score;
      last->longest = longest;
      for(int i=0; i<n; i++){
        for(int j=0; j<n; j++){
          last->board[i][j] = board[i][j];
        }
      }
      
      if(last->score > last->prev->score){
        
        struct Result *temp = last;
        last = last->prev;
        last->next = 0;
        
        struct Result *current = last;
        
        while(current->score < temp->score) current=current->prev;
        
        temp->next = current->next;
        temp->next->prev = temp;
        temp->prev = current;
        current->next = temp;
      }
      printf("%d %d %d %d %d fin %d %d\n", score, longest, first->next->score, first->next->longest, last->score, n, seed);
      
    }
  }
  int asd;
  scanf("%d\n", asd);
}
