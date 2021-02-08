
window.CHAT = {
	socket: null,
	heartBeatTimer: null,  // 用于发送心跳包的定时器
	init: function() {
		if (!window.WebSocket) { // 判断浏览器是否支持WebSocket协议
			return console.log("您的手机版本过低，不支持WebSocket协议");
		}
		if (CHAT.socket != null && CHAT.socket.readyState == WebSocket.OPEN) {
			return;
		}
		CHAT.socket = new WebSocket(app.nettyServerUrl);
		CHAT.socket.onopen = CHAT.wsopen;
		CHAT.socket.close = CHAT.wsclose;
		CHAT.socket.onerror = CHAT.wserror;
		CHAT.socket.onmessage = CHAT.wsmessage;
	},
	send: function(msg) {
		// console.log(CHAT.socket.readyState, WebSocket.OPEN);
		if (CHAT.socket != null && CHAT.socket.readyState == WebSocket.OPEN) {
			CHAT.socket.send(msg);
			// console.log("发送的消息：" + msg);
		} else {
			// 尝试重连
			CHAT.init();
			setTimeout(function() {
				// 注意CHAT.send()和CHAT.socket.send()别搞混了
				CHAT.socket.send(msg); 
			}, 1000);
		}
	},
	wsopen: function() {
		console.log("WebSocket已连接");
		// 一旦连接建立，立马请求绑定userId
		var me = app.getUserGlobalInfo();
		var msgModel = new app.MsgModel(app.action.BIND, me.token);
		CHAT.send(JSON.stringify(msgModel));
		console.log("已发送绑定请求");
		
		// 开启心跳
		clearInterval(CHAT.heartBeatTimer);
		CHAT.heartBeatTimer = CHAT.keepAlive();
	},
	wsmessage: function(e) {
		console.log("接收到的消息：" + e.data);
		var msgModel = JSON.parse(e.data);
		var action = msgModel.action;
		var me = app.getUserGlobalInfo();
		
		if (action == app.action.PULL_FRIEND) { // 拉取好友
			var contactView = plus.webview.getWebviewById("contact");
			// 更新好友列表
			// 1、更新本地缓存，使缓存最新。下次打开，缓存不为空，直接从缓存中。由于保证了缓存最新，所以数据是正确的
			contactView.evalJS("fetchContactList()"); // 从后台拿到最新数据，并重新[全部]覆盖缓存
			// 2、界面更新。由于局部更新太麻烦了，这里重新构建并刷新html
			contactView.evalJS("showContactList()");
			// 3、更新好友申请列表
			// 关于好友请求结合Websocket的优化，放在聊天之后再做
			
		} else if (action == app.action.CHAT) { // 聊天类型的消息
			var msgList = msgModel.data;
			console.log("未签收的消息数量：" + msgList.length);
			var myId = me.id;
			
			// 更新快照列表
			var chatListView = plus.webview.getWebviewById("chat_list");
			var msgIdsStr = "";
			for(var i=0; i<msgList.length; i++) {
				var msg = msgList[i];
				var friendId = msg.senderId;
				msgIdsStr += (',' + msg.msgId);
				
				// 如果聊天窗口已经打开，则更新聊天消息
				var chattingView = plus.webview.getWebviewById("chatting_with_" + friendId);
				if (app.isNotNull(chattingView)) { // 有可能没创建
					chattingView.evalJS("receiveMsg('" + msg.content + "')");
				}
				
				// 将聊天记录保存到本地缓存中
				app.saveUserChatHistory(myId, friendId, msg.content, 2); // 2：朋友发送的
			}
			
			// 签收
			var signModel = new app.MsgModel(app.action.SIGNED, msgIdsStr.substr(1)); 
			CHAT.send(JSON.stringify(signModel));
			
			// 更新聊天快照（最后一条收到的消息的快照）的本地缓存
			if (app.isNotBlank(msgList)) {
				var isRead = app.isNotNull(chattingView); // 不为空(true)，表示停留在聊天页面，那么当前收到的消息已读(true)
				var lastMsg = msgList[msgList.length - 1]; 
				// lastMsg.time是一个字符串
				var timestamp = app.getDateTimeStamp(lastMsg.time);
				app.saveUserChatSnapshot(myId, lastMsg.senderId, lastMsg.content, isRead, timestamp); // 我发送的消息，对于我来说，肯定是已读的
				chatListView.evalJS("updateChatSnapshot('" + lastMsg.senderId + "', '" 
				+ lastMsg.content +"', " + isRead + ", '" + timestamp + "')");
			}
		}
	},
	wsclose: function() {
		console.log("连接已关闭");
	},
	wserror: function() {
		// 连接失败，会回调到这里
		console.log("发生错误");
		CHAT.socket.close();
		clearInterval(CHAT.heartBeatTimer);
		console.log("心跳定时器已关闭");
	},
	keepAlive: function() {
		var msgModel = new app.MsgModel(app.action.KEEP_ALIVE, null);
		// 发送心跳包
		return setInterval(function() {
			CHAT.send(JSON.stringify(msgModel));
			// console.log("已发送心跳包：" + new Date());
		}, 10000); // 每隔10秒（必须小与后端定义的超时时间）发送一个心跳包
		
	}
};