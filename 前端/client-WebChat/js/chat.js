
window.CHAT = {
	socket: null,
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
		if (CHAT.socket != null && CHAT.socket.readyState == WebSocket.OPEN) {
			CHAT.socket.send(msg);
			console.log("发送的消息：" + msg);
		} else {
			// 尝试重连
			CHAT.init();
			setTimeout(function() {
				// 注意CHAT.send()和CHAT.socket.send()别搞混了
				CHAT.send(msg);  // 递归调用，不断尝试重连，一旦连接成功。就会跳出递归
				console.log("发送的消息：" + msg);
			}, 1000);
		}
	},
	wsopen: function() {
		console.log("WebSocket已连接");
		// 一旦连接建立，立马请求绑定userId
		var me = app.getUserGlobalInfo();
		var msgModel = new app.TextMsgModel(app.action.CONNECT, null, me.token);
		CHAT.send(JSON.stringify(msgModel));
		console.log("已发送绑定请求");
	},
	wsmessage: function(e) {
		console.log("接收到的消息：" + e.data);
		var msgModel = JSON.parse(e.data);
		var action = msgModel.action;
		
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
			var msg = msgModel.msg;
			var friendId = msg.senderId;
			var myId = msg.receiverId;
		
			// 显示和缓存
			// 有可能没创建
			var chattingView = plus.webview.getWebviewById("chatting_with_" + friendId);
			if (app.isNotNull(chattingView)) {
				chattingView.evalJS("receiveMsg('" + msg.content + "')");
			} else {
				// 没创建
			}
			
			// 签收
			// 第三个参数extend防msgId拼接成的字符串
			var signModel = new app.TextMsgModel(app.action.SIGNED, null, msg.msgId); // 单签
			CHAT.send(JSON.stringify(signModel));
			
			// 将聊天记录保存到本地缓存中
			app.saveUserChatHistory(myId, friendId, msg.content, 2); // 2：朋友发送的
			// 更新聊天快照的本地缓存
			var isRead = app.isNotNull(chattingView); // 不为空(true)，表示停留在聊天页面，那么当前收到的消息已读(true)
			app.saveUserChatSnapshot(myId, friendId, msg.content, isRead); // 我发送的消息，对于我来说，肯定是已读的
			
			// 更新快照列表
			var chatListView = plus.webview.getWebviewById("chat_list");
			chatListView.evalJS("updateChatSnapshot('" + friendId + "', '" + msg.content +"')");
		}
	},
	wsclose: function() {
		console.log("连接已关闭");
	},
	wserror: function() {
		console.log("发生错误");
	}
};