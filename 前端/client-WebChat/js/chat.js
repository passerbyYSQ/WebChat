
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
		} else {
			// 尝试重连
			CHAT.init();
			setTimeout(function() {
				// 注意CHAT.send()和CHAT.socket.send()别搞混了
				CHAT.send(msg);  // 递归调用，不断尝试重连，一旦连接成功。就会跳出递归
			}, 1000);
		}
	},
	wsopen: function() {
		console.log("WebSocket已连接");
	},
	wsmessage: function(e) {
		console.log("接收到的消息：" + e.data);
	},
	wsclose: function() {
		console.log("连接已关闭");
	},
	wserror: function() {
		console.log("发生错误");
	},
	// 消息action的枚举
	action: {
		CONNECT: 1, // 第一次（或重连）初始化连接
		CHAT: 2, // 聊天消息
		SIGNED: 3, // 消息签收
		KEEPALIVE: 4, // 心跳
		PULL_FRIEND: 5 // 拉取好友
	},
	// 通信模型，与服务端一一对应
	TextMsg: function(senderId, receiverId, content, msgId) {
		this.senderId = senderId;
		this.receiverId = receiverId;
		this.content = content;
		this.msgId = msgId;
	},
	TextMsgModel: function(action, textMsg, extend) {
		this.action = action;
		this.textMsg = textMsg;
		this.extend = extend;
	}
};