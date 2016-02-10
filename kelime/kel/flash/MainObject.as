package  {
	
	import flash.display.MovieClip;
	import flash.events.*;
	import flash.net.*;
	import flash.utils.ByteArray;    
	import flash.system.Security;
	
	import flash.text.TextFormat;
    import flash.text.TextFieldAutoSize;

	
	
		
	import flash.utils.*;
	
	public class MainObject extends MovieClip {
			
		var host:String = "localhost";
		var port:int = 80;
		
		var forceStart:Boolean = false;
		
		var buffer:ByteArray;
		var letters:Array = new Array("A", "B", "C", "Ç", "D", "E", "F", "G", "G", "H", "I", "İ", "J", "K", "L", "M", "N", "O", "Ö", "P", "R", "S", "S", "T", "U", "Ü", "V", "Y", "Z");
        var abc:String = "abc0defg1h2ijklmno3prs4tu5vyz";
		
		var lettersBuff:Array = new Array(256);
		
		var byteArr:ByteArray = new ByteArray();
	
		var board:Array = new Array();
		var boardContainer:MovieClip;
		var counter:Counter;
		var getReady:GetReady;
		var leftMenu:LeftMenu;
		var waitingPopUp:WaitingPopUp;
		var scoreList:ScoreList;
		var login:Login;
		var wordBox:WordBox;
		
		var boardInit:Boolean=false;
		var gotBoard:Boolean=false;
		
		var messageLength:int = -1;
		
		var wordListItems:Array = new Array();
		
		var nextGame:Number;
		var timeDiff:Number;
		
		var id:int;
		
		var score:int;
			
		var getReadyTime:int = 3;
		var gameTime:int = 25;
		var scoreRecieveWaitTime:int = 3;
		var scoreSendWaitTime:int = 3;
		var scoreShowTime:int = 5;
		
		var words:Array;
		
		var points:Array = new Array(0, 0, 0, 1, 2, 4, 7, 10, 15, 25, 40, 60, 80 );

		var overheadFin:Boolean = false;
		var byteNeed:int = -1;
		
		public function getInt1byte():int{
		
			var z:int = buffer.readByte();
			
			if(z>=48 && z<=57) z-=48 ;
			else if(z>=97 && z<=102) z-=97;
					
			return z;
		}
		
		
		public function getInt2byte():int{
			var n:int = 0;
			
			for(var i:int=0; i<2; i++){
				var z:int = buffer.readByte();
				n *= 16;
				
				if(z>=48 && z<=57) n += (z-48);
				else if(z>=97 && z<=102) n += (z-97)+10;
					
			}
			return n;
		}
		
		
		public function getInt4byte():int{
			var n:int = 0;
			
			for(var i:int=0; i<4; i++){
				var z:int = buffer.readByte();
				n *= 16;
				
				if(z>=48 && z<=57) n += (z-48);
				else if(z>=97 && z<=102) n += (z-97)+10;
					
			}
			return n;
		}
		
		public function getInt8byte():Number{
			var n:Number = 0;
			
			for(var i:int=0; i<8; i++){
				var z:int = buffer.readByte();
				n *= 16;
				
				if(z>=48 && z<=57) n += (z-48);
				else if(z>=97 && z<=102) n += (z-97)+10;
					
			}
			return n;
		}
		
		public function getInt16byte():Number{
			var n:Number = 0;
			
			for(var i:int=0; i<16; i++){
				var z:int = buffer.readByte();
				
				n *= 16;
				
				if(z>=48 && z<=57) n += (z-48);
				else if(z>=97 && z<=102) n += (z-97)+10;
					
			}
			return n;
		}
		
		
		function getInit():void {
			var scriptRequest:URLRequest = new URLRequest("http://"+host+":"+port+"/init.php?"+Math.random());
			var scriptLoader:URLLoader = new URLLoader();
			
			scriptRequest.method = URLRequestMethod.POST;
			scriptLoader.addEventListener(Event.COMPLETE, initMessageReceive);
			scriptLoader.dataFormat = URLLoaderDataFormat.BINARY;
				
			scriptLoader.load(scriptRequest);			
		}
		
		public function initMessageReceive(e:Event):void {
			var loader:URLLoader = URLLoader(e.target);
			
			
			buffer = loader.data;
			
			var current:Number = getInt16byte();
			
			var d:Date = new Date();
				
				
			timeDiff = current-d.getTime();
			
			nextGame = getInt16byte();
			
			secondsLeft = ((nextGame-current)/1000)+1;
			counter.second.text = secondsLeft.toString(10);
			
			counter.visible = true;
			var myTimer:Timer = new Timer(1000, secondsLeft); 
			myTimer.addEventListener(TimerEvent.TIMER, decCounter);
			myTimer.start();
				
			
			if(secondsLeft <= scoreRecieveWaitTime + scoreSendWaitTime + scoreShowTime + 10){
				counter.playNow.visible = false;
			}
			
			byteNeed = -1;
			
			initializeBoard();
			
		}
		function getBoard():void {
			
			
			var scriptRequest:URLRequest = new URLRequest("http://"+host+":"+port+"/getBoard.php?"+Math.random());
			var scriptLoader:URLLoader = new URLLoader();
			
			scriptRequest.method = URLRequestMethod.POST;
			scriptLoader.addEventListener(Event.COMPLETE, boardMessageReceive);
			scriptLoader.dataFormat = URLLoaderDataFormat.BINARY;
				
			scriptLoader.load(scriptRequest);			
		}
		
		
		public function initializeBoard(){
			
			var bo:String = "";
			for(var i:int=0; i<16; i++){
				var letterBox:LetterBox = board[i];
				var b:int = buffer.readByte();
				letterBox.letter.text = lettersBuff[b];
				bo += lettersBuff[b];
				
			}
			
			
			var scoreTotal:int = getInt4byte();
			var wordNum:int = getInt4byte();
			
			
			words = new Array(wordNum);
			for(var i:int=0; i<wordNum; i++){
				var l:int = getInt1byte();
				var w:String = "";
				for(var j:int=0; j<l; j++){
					w += lettersBuff[buffer.readByte()];
				}
				
				words[i] = w;
			}
				
			for(var i:int=0; i<wordListItems.length; i++){
				scoreList.wordListContainer.removeChild(wordListItems[i]);
			}
			
			wordListItems = new Array();
			var ycurrent:int = 5;
			var lastLen:int = -1;
			
			for(var i:int=wordNum-1; i>=0; i--){
				//trace(words[i]);
				if(words[i].length != lastLen){
					
					if(lastLen != -1){
						ycurrent += 30;
					}
					lastLen = words[i].length;
					var nseperater:WordListItem = new WordListItem();
					nseperater.gotoAndStop(2);
					nseperater.word.text = lastLen + " harf";
					nseperater.x = 0;
					nseperater.y = ycurrent;
					ycurrent += 30;
					wordListItems.push(nseperater);
					scoreList.wordListContainer.addChild(nseperater);
				}
				var nitem:WordListItem = new WordListItem();
				nitem.word.text = words[i];
				nitem.x = 0;
				nitem.y = ycurrent;
				ycurrent += 30;
				wordListItems.push(nitem);
				scoreList.wordListContainer.addChild(nitem);
			}
			
			
					
			score = 0;
			
		}
		public function boardMessageReceive(e:Event):void {
			trace("<<<<<<<<<<");
			var loader:URLLoader = URLLoader(e.target);
			
			
			buffer = loader.data;
			
			initializeBoard()
			
			if(boardInit == true){
				boardInit = false;
				gotBoard = false;
				getReady.visible = false;
					
				boardContainer.visible = true;
				leftMenu.visible = true;
				wordBox.visible = true;
				score = 0;
				leftMenu.score.text = "0 puan";
				leftMenu.words.text = "";
				
				secondsLeft = gameTime;
				leftMenu.timeLeft.text = gameTime.toString(10);
			
				var myTimer:Timer = new Timer(1000, secondsLeft);
				myTimer.addEventListener(TimerEvent.TIMER, decSecondsLeft);
				myTimer.start();
			}else{
				gotBoard = true;
			}
		}
		
		function getScores():void {
			
			var scriptRequest:URLRequest = new URLRequest("http://"+host+":"+port+"/getScores.php?"+Math.random());
			var scriptLoader:URLLoader = new URLLoader();
			
			var variables : URLVariables = new URLVariables();  
			variables.n = login.nick.text;
			variables.s = score;
			
			scriptRequest.data = variables;  

			scriptRequest.method = URLRequestMethod.POST;
			
			scriptLoader.addEventListener(Event.COMPLETE, scoresMessageReceive);
			scriptLoader.dataFormat = URLLoaderDataFormat.BINARY;
				
			scriptLoader.load(scriptRequest);			
		}
		
	
	
		public function scoresMessageReceive(e:Event):void {
			
			var loader:URLLoader = URLLoader(e.target);
			
			
			buffer = loader.data;
			
			con.text += buffer.toString();
			
			
			trace("rec.");/*
			var s ="";
			while(socket.bytesAvailable>0){
				s += String.fromCharCode(socket.readByte());
			}
			trace(s);
			return;*/
			
			var len:int = getInt4byte();
			
			scoreList.names.text = "";
			scoreList.scores.text = "";
			for(var i:int=0; i<len; i++){
					
					
				var len2:int = getInt2byte();
				var pname:String = buffer.readUTFBytes(len2);
				var pscore:int = getInt4byte();
				
				scoreList.names.text+=(pname + "\n");
				scoreList.scores.text+=(pscore + "\n");
				trace(pname, pscore);
			}
			
		}
		
		
		
		
		
		
		
		var secondsLeft:int;
		
		function decScoreCloseWait(event:TimerEvent):void {
			
			secondsLeft--;
			if(secondsLeft==0){
				
				scoreList.visible = false;
			
					
				getReady.second.text = "3";
				getReady.visible = true;
				secondsLeft = 3;
				
				var myTimer:Timer = new Timer(1000, secondsLeft);
				myTimer.addEventListener(TimerEvent.TIMER, decGetReady);
				myTimer.start();
				getBoard();
			}
			scoreList.timeLeft.text = secondsLeft.toString(10);
			
		}
		
		function decScoreOpenWait(event:TimerEvent):void {
			
			secondsLeft--;
			if(secondsLeft==0){
				
				waitingPopUp.visible = false;			
				
	
				nextGame += (getReadyTime+gameTime+scoreRecieveWaitTime+scoreSendWaitTime+scoreShowTime)*1000;
				
				trace(getReadyTime+gameTime+scoreRecieveWaitTime+scoreSendWaitTime+scoreShowTime);
				con.text += nextGame+"\n";
				var d:Date = new Date();
				
								
				
				secondsLeft = (int)((nextGame-d.getTime()-timeDiff)/1000);
				if(secondsLeft <= 0) secondsLeft = 0;
				
				scoreList.timeLeft.text = secondsLeft.toString(10);
				var myTimer:Timer = new Timer(1000, secondsLeft);
				myTimer.addEventListener(TimerEvent.TIMER, decScoreCloseWait);
				myTimer.start();
				
			}
			waitingPopUp.timeLeft.text = secondsLeft.toString(10);
			
		}
		function decSecondsLeft(event:TimerEvent):void {
			
			secondsLeft--;
			if(secondsLeft==0){
				getReady.visible = false;
					
				boardContainer.visible = false;
				leftMenu.visible = false;
				wordBox.visible = false;
				
				
				scoreList.myScore.text = "Your score : " + score;
				scoreList.timeLeft.text = "";
				
				secondsLeft = scoreRecieveWaitTime+scoreSendWaitTime;
				waitingPopUp.timeLeft.text = secondsLeft.toString(10);
				waitingPopUp.visible = true;
				scoreList.visible = true;
				
				
				var myTimer:Timer = new Timer(1000, secondsLeft);
				myTimer.addEventListener(TimerEvent.TIMER, decScoreOpenWait);
				myTimer.start();
				
				getScores();
				
				
			}
			leftMenu.timeLeft.text = secondsLeft.toString(10);
		}
		
		
		
		function decGetReady(event:TimerEvent):void {
			
			secondsLeft--;
			if(secondsLeft==0 ){
				if(gotBoard == false){
					boardInit = true;
					getReady.second.text = "0 ...";
					return;
				}
				gotBoard = false;
				getReady.visible = false;
					
				boardContainer.visible = true;
				leftMenu.visible = true;
				wordBox.visible = true;
				
				score = 0;
				leftMenu.score.text = "0 puan";
				leftMenu.words.text = "";
				
				secondsLeft = gameTime;
				leftMenu.timeLeft.text = gameTime.toString(10);
			
				var myTimer:Timer = new Timer(1000, secondsLeft);
				myTimer.addEventListener(TimerEvent.TIMER, decSecondsLeft);
				myTimer.start();
				
			}
			getReady.second.text = secondsLeft.toString(10);
			return;
		}
		
		function decCounter(event:TimerEvent):void {
			if(forceStart) return;
			secondsLeft--;
			if(secondsLeft == scoreRecieveWaitTime + scoreSendWaitTime + scoreShowTime + 10 ){
				
				counter.playNow.visible = false;
				
			}
			if(secondsLeft == 0){
				counter.visible = false;
				getReady.visible = true;
					
					
				secondsLeft = getReadyTime;
				getReady.second.text = secondsLeft.toString(10);
				
				var myTimer:Timer = new Timer(1000, secondsLeft);
				myTimer.addEventListener(TimerEvent.TIMER, decGetReady);
				myTimer.start();
				getBoard();
			}
			
			counter.second.text = secondsLeft.toString(10);
		}
		
		

		function loginButtonClicked($e:MouseEvent):void{
			login.visible = false;
			
			getInit();
		}
		
		function playNowClicked($e:MouseEvent):void{

			counter.visible = false;
			forceStart = true;
			
			nextGame -= (getReadyTime+gameTime+scoreRecieveWaitTime+scoreSendWaitTime+scoreShowTime)*1000;
				
			boardContainer.visible = true;
			leftMenu.visible = true;
			wordBox.visible = true;
			score = 0;
			leftMenu.score.text = "0 puan";
			leftMenu.words.text = "";
			
			secondsLeft = secondsLeft-(scoreRecieveWaitTime + scoreSendWaitTime + scoreShowTime);
			leftMenu.timeLeft.text = gameTime.toString(10);
		
			var myTimer:Timer = new Timer(1000, secondsLeft);
			myTimer.addEventListener(TimerEvent.TIMER, decSecondsLeft);
			myTimer.start();
			
			
		}
		
		
		public function MainObject() {
			trace("hi");
			
			
			var xp = 230;
			var yp = 10;
			
			boardContainer = new MovieClip();
			boardContainer.x = 0;
			boardContainer.y = 0;
			
			for(var i:int=0; i<16; i++){
				var mc:LetterBox = new LetterBox();
				mc.x = xp;
				mc.y = yp;
				mc.xp = (int)(i%4);
				mc.yp = (int)(i/4);
				
				mc.letter.text = "";
				xp += 100;
				if(i%4 == 3){
					xp = 230;
					yp += 100;
				}
				boardContainer.addChild(mc);
				board.push(mc);
				
				
				mc.hit.addEventListener(MouseEvent.MOUSE_DOWN, mdown);
				mc.hit.addEventListener(MouseEvent.MOUSE_OVER, mover);
				mc.hit.mouseChildren = false;
			}
			stage.addEventListener(MouseEvent.MOUSE_UP, mup);
			
			boardContainer.visible = false;
			addChild(boardContainer);
			
			counter = new Counter();
			counter.second.text = "";
			counter.x = 320;
			counter.y = 240;
			counter.visible = false;
			addChild(counter);
			
			
			getReady = new GetReady();
			getReady.second.text = "";
			getReady.x = 320;
			getReady.y = 240;
			getReady.visible = false;
			addChild(getReady);
			
			leftMenu = new LeftMenu();
			leftMenu.timeLeft.text = "";
			leftMenu.x = 0;
			leftMenu.y = 0;
			leftMenu.visible = false;
			addChild(leftMenu);
			
			
			
			scoreList = new ScoreList();
			scoreList.x = 0;
			scoreList.y = 0;
			scoreList.visible = false;
			addChild(scoreList);
			
						
			login = new Login();
			login.x = 320;
			login.y = 240;
			login.visible = true;
			stage.focus  = login.nick;
			addChild(login);
			
			
			waitingPopUp = new WaitingPopUp();
			waitingPopUp.x = 230;
			waitingPopUp.y = 10;
			waitingPopUp.visible = false;
			addChild(waitingPopUp);
						
			wordBox = new WordBox();
			wordBox.x = 230;
			wordBox.y = 410;
			wordBox.visible = false;
			addChild(wordBox);
			
			login.btn.addEventListener(MouseEvent.CLICK, loginButtonClicked); 

	
			counter.playNow.addEventListener(MouseEvent.CLICK, playNowClicked); 
				
			scoreList.wordsBG.addEventListener(MouseEvent.MOUSE_DOWN, wordsdown);
			
			
			stage.addEventListener(Event.ENTER_FRAME,enterFrameE);

			Security.allowDomain("*");
			
			
			var scriptRequest:URLRequest = new URLRequest("http://"+host+":"+port+"/test.txt");
			var scriptLoader:URLLoader = new URLLoader();
			
			   
			scriptRequest.method = URLRequestMethod.POST;
			scriptLoader.addEventListener(Event.COMPLETE, handleLoadSuccessful);
			
			scriptLoader.addEventListener(Event.COMPLETE, completeHandler);
						
			function completeHandler(event:Event):void {
						  
            con.text+=("completeHandler: " + scriptLoader.data)+"\n";
        }
		
        con.text+="http://"+host+":"+port+"/test.txt\n";

			 
			function handleLoadSuccessful($evt:Event):void
			{
				trace("Message sent.");
			}
			 
			scriptLoader.addEventListener(IOErrorEvent.IO_ERROR, ioErrorHandler);
        	scriptLoader.addEventListener(SecurityErrorEvent.SECURITY_ERROR, securityErrorHandler);
		
        	con.text+="connecting"+"\n";
		
		    function ioErrorHandler(event:IOErrorEvent):void {
				con.text+=("ioErrorHandler: " + event)+"\n";
			}
		
			 function securityErrorHandler(event:SecurityErrorEvent):void {
				con.text+=("securityErrorHandler: " + event)+"\n";
			}
				
				
			scriptLoader.load(scriptRequest);



			for(var i:int=0; i<256; i++){
				lettersBuff[i] = "";
			}
			
			for(var i:int=0; i<abc.length; i++){
				lettersBuff[abc.charCodeAt(i)] = letters[i];
			}
			//connectToSocket();
		}
		
		var pressed:Boolean = false;
		var pressType:int = 0;
		
		var mpressX:int;
		var mpressY:int;
		
		
		private function wordsdown(event:MouseEvent):void {
			pressed = true;
			pressType = 2;
			mpressX = stage.mouseX;
			mpressY = stage.mouseY;
		}


		
		function enterFrameE(event:Event) {
			if(pressed){
				if(pressType == 2){
					var sp:Number = (stage.mouseY-mpressY)/8;
					if(sp<-40) sp = -40;
					else if(sp>40) sp = 40;
					
					scoreList.wordListContainer.y += sp;
					if(scoreList.wordListContainer.y > 20){
						scoreList.wordListContainer.y = 20;
					}else if(scoreList.wordListContainer.y < 460-scoreList.wordListContainer.height){
						scoreList.wordListContainer.y = 460-scoreList.wordListContainer.height;
					}
					
				}
			}
		}
		var word:String = "";
		
		var usedmcs:Array = new Array();
		
		private function mdown(event:MouseEvent):void {
			pressed = true;
			var mc:LetterBox = (LetterBox)(event.target.parent);
			word = mc.letter.text;
			for(var j:int=0; j<usedmcs.length; j++){
				usedmcs[j].gotoAndStop(1);
			}
			usedmcs = new Array();
			usedmcs.push(mc);
			wordBox.word.text = word;
			mc.gotoAndStop(2);
			pressType = 1;
		}

		private function mover(event:MouseEvent):void {
			if(!pressed) return;
			var mc:LetterBox = (LetterBox)(event.target.parent);
			
			var deleted:Boolean = false;
		
			for(var i:int=0; i<usedmcs.length; i++){
				if(usedmcs[i] == mc){
					word = word.slice(0, i+1);			
					for(var j:int=i+1; j<usedmcs.length; j++){
						usedmcs[j].gotoAndStop(1);
					}
					usedmcs = usedmcs.slice(0, i+1);
					deleted = true;
					break;
				}
			}
			if(!deleted){
				
				if(Math.abs(usedmcs[usedmcs.length-1].xp-mc.xp) <= 1 && Math.abs(usedmcs[usedmcs.length-1].yp-mc.yp) <= 1 ){
						
					word += mc.letter.text;
					mc.gotoAndStop(2);
					usedmcs.push(mc);
				}
				
			}
			wordBox.word.text = word;
		}
		
		
		
		private function mup(event:MouseEvent):void {
			if(!pressed) return;
			
			if(pressType == 1){
				pressed = false;
				wordBox.word.text = "";
				var fr:int = 6;
				if(word.length >= 3){
					for(var i:int=0; i<words.length; i++){
						if(word == words[i]){
							fr = 16;
							score += points[word.length];
							leftMenu.score.text = score + " puan";
							leftMenu.words.text = word + "\n" + leftMenu.words.text;
							
							//scoreList.words.htmlText  = scoreList.words.htmlText.replace(" " + word+" ", "<font color='#3F658B'> "+word+" <font>");
							words[i] = "";
							for(var j:int=0; j<wordListItems.length; j++){
								if(wordListItems[j].word.text == word){
									wordListItems[j].word.textColor = "0x3F658B";
								}
							}
							
						}
					}
				}
				for(var j:int=0; j<usedmcs.length; j++){
					usedmcs[j].gotoAndPlay(fr);
				}
				
			}else if(pressType == 2){
				pressed = false;
			}
			
		}
	}
	
}
