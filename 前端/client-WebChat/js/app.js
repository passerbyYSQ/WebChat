window.addEventListener('refresh', function(e) {
	window.location.reload();//页面更新
});

window.app = {
	/**
	 * netty服务后端发布的url地址
	 */
	nettyServerUrl: 'ws://192.168.1.4:8081/ws',
	
	/**
	 * 后端服务发布的url地址
	 * 开发时，服务端和测试手机需要在同一局域网。此处的ip是服务端所在电脑的局域网ip
	 * 由于是动态分配，所以ip可能会变
	 */
	serverUrl: "http://192.168.1.4:8080/v1/api/",  
	
	/**
	 * 图片服务器的url地址
	 */
	imgServerUrl: 'http://101.200.79.231:88/wdzl/',
	
	/**
	 * 判断字符串是否为空
	 * @param {Object} str
	 * true：不为空
	 * false：为空c
	 */
	isNotBlank: function(str) {
		return str != null && str != undefined && str != "";
	},
	
	/**
	 * 判断对象是否为null或者undefined
	 * @param {Object} str
	 * true：不为空
	 * false：为空c
	 */
	isNotNull: function(obj) {
		return obj != null && obj != undefined;
	},
	
	formatDate: function(dateStr) {
		//字符串转换为时间戳。js中可以嵌套定义函数
		function getDateTimeStamp (dateStr) {
		    return Date.parse(dateStr.replace(/-/gi,"/"));
		}
		
	    var publishTime = getDateTimeStamp(dateStr)/1000,
	        d_seconds,
	        d_minutes,
	        d_hours,
	        d_days,
	        timeNow = parseInt(new Date().getTime()/1000),
	        d,
	
	        date = new Date(publishTime*1000),
	        Y = date.getFullYear(),
	        M = date.getMonth() + 1,
	        D = date.getDate(),
	        H = date.getHours(),
	        m = date.getMinutes(),
	        s = date.getSeconds();
	        //小于10的在前面补0
	        if (M < 10) {
	            M = '0' + M;
	        }
	        if (D < 10) {
	            D = '0' + D;
	        }
	        if (H < 10) {
	            H = '0' + H;
	        }
	        if (m < 10) {
	            m = '0' + m;
	        }
	        if (s < 10) {
	            s = '0' + s;
	        }
	
	    d = timeNow - publishTime;
	    d_days = parseInt(d/86400);
	    d_hours = parseInt(d/3600);
	    d_minutes = parseInt(d/60);
	    d_seconds = parseInt(d);
	
	    if(d_days > 0 && d_days < 3){
	        return d_days + '天前';
	    }else if(d_days <= 0 && d_hours > 0){
	        return d_hours + '小时前';
	    }else if(d_hours <= 0 && d_minutes > 0){
	        return d_minutes + '分钟前';
	    }else if (d_seconds < 60) {
	        if (d_seconds <= 0) {
	            return '刚刚';
	        }else {
	            return d_seconds + '秒前';
	        }
	    }else if (d_days >= 3 && d_days < 30){
	        return M + '-' + D + ' ' + H + ':' + m;
	    }else if (d_days >= 30) {
	        return Y + '-' + M + '-' + D + ' ' + H + ':' + m;
	    }
	},
	
	/**
	 * 封装ajax请求,携带token。
	 * 必须要在plusReady()事件之后调用
	 */
	ajax: function(url, data, successCallback, type = "post", 
		errorCallback = function() {}, 
		failedCallback = function(res) { app.showToast(res.msg, "error"); }) {
			
		plus.nativeUI.showWaiting("Loading...");
		var user = this.getUserGlobalInfo();
		var token = null;
		if (this.isNotNull(user)) {
			token = user.token;
		}
		// console.log(token);
		var _this = this;
		mui.ajax(this.serverUrl + url, {
			type: type,
			dataType: "json",
			// traditional:true, // value可以是数组
			data: data,
			timeout: 30000 ,// 超时时间为30秒
			headers: {
				token: token // 携带token
			},
			success: function(res) {
				console.log("success");
				console.log(JSON.stringify(res));
				plus.nativeUI.closeWaiting();
				if (res.code !== 2000) { // 与后端约定：成功的业务状态的请求统一返回2000
					failedCallback(res); // 业务失败
				} else {
					successCallback(res); // 业务成功
				}
			},
			error: function(xhr, textStatus, errorThrown) {
				console.log("error");
				plus.nativeUI.closeWaiting();
				if (textStatus == "timeout") {
					app.showToast("连接超时", "error");
				} else if (textStatus == "error") {
					app.showToast("服务端错误", "error");
				}
				if (errorCallback != null) {
					errorCallback(xhr, textStatus, errorThrown);
				}
			}
		});
	},
	
	/**
	 * 封装消息提示框，默认mui的不支持居中和自定义icon，所以使用h5+
	 * @param {Object} msg
	 * @param {Object} type
	 */
	showToast: function(msg, type) {
		/*
		style: 提示消息框上显示的样式，默认值为"block"
			可取值： "block" - 表示图标与文字分两行显示，上面显示图标，下面显示文字； 
			"inline" - 表示图标与文字在同一行显示，左边显示图标，右边显示文字。 。
		*/
		plus.nativeUI.toast(msg, {	
			icon: "../imgs/" + type + ".png", 
			style: "inline", 
			verticalAlign: "center",
		});
	},
	
	/**
	 * 封装输入对话框
	 */
	showPrompt: function(title, placeHolder, confirmCallback) {
		mui.prompt("", "", title, ["取消", "确认"], function(res) {
			console.log(JSON.stringify(res));
			if (res.index == 1) {
				confirmCallback(res.value);
			}
		}, "div");
		document.querySelector(".mui-popup-input input").value = placeHolder;
	},
	
	/**
	 * 监听网络状态变化
	 */
	listenNetworkChange: function(onDisconnected = function() {}) {
		document.addEventListener("netchange", function() {
			// 当网络变化时，获取网络状态。如果网络断开，就做相应的处理
			var networkType = plus.networkinfo.getCurrentType();
			if (networkType == plus.networkinfo.CONNECTION_UNKNOW 
				|| networkType == plus.networkinfo.CONNECTION_NONE) {
					// 0或1
				console.log("网络断开");
				app.showToast("未连接网络", "error");
				onDisconnected();
			}
		});
	},
	
	/**
	 * android系统下，强制弹出软键盘
	 */
	popUpKeybord: function() {
		var Context = plus.android.importClass("android.content.Context");  
		var InputMethodManager = plus.android.importClass("android.view.inputmethod.InputMethodManager");  
		var main = plus.android.runtimeMainActivity();  
		var imm = main.getSystemService(Context.INPUT_METHOD_SERVICE);  
		imm.toggleSoftInput(0,InputMethodManager.SHOW_FORCED);
	},
	
	/**
	 * 保存用户的全局对象
	 */
	setUserGlobalInfo: function(user){
		var userInfoStr = JSON.stringify(user);
		plus.storage.setItem("userInfo",userInfoStr);
	},
	/**
	 * 获取用户的全局对象
	 */
	getUserGlobalInfo:function(){
		var userInfoStr = plus.storage.getItem("userInfo");
		return JSON.parse(userInfoStr);
	},
	/**
	 * 退出登录
	 */
	userLogout: function(){
		plus.storage.removeItem("userInfo");
		// 还要清除联系人列表的缓存
		var webviews = plus.webview.all();
		for(var i=0; i<webviews.length; i++) {
			if (webviews[i].id !== 'login') {
				webviews[i].close();
			}
		}
	},
	
	/**
	 * 缓存用户的联系人列表
	 */
	setContactList:function(myFriendList){
		var contactListStr = JSON.stringify(myFriendList);
		plus.storage.setItem("contactList",contactListStr);
	},
	/**
	 * 获取本地缓存中的联系人列表
	 */
	getContactList: function() {
		var contactListStr = plus.storage.getItem("contactList");
		if (!this.isNotNull(contactListStr)) {
			return null;
		}
		return JSON.parse(contactListStr);
	},
	
	/**
	 * 从联系人列表的缓存中获取好友的相关信息，用于渲染聊天快照列表
	 * 
	 * @param {Object} friendId
	 */
	getFriendFromContactList:function(friendId){
		//获取联系人列表的本地缓存
		var contactListStr = plus.storage.getItem("contactList");
		if(app.isNotNull(contactListStr)){
			//不为空，则把用户信息返回
			var contactList = JSON.parse(contactListStr);
			for(var i=0; i<contactList.length;i++){
				var friend = contactList[i];
				if(friend.userId == friendId){
					return friend;
				}
			}
		}
		return null;
	},
	
	// **************websocket相关**************
	
	// 消息action的枚举
	action: {
		CONNECT: 1, // 第一次（或重连）初始化连接
		CHAT: 2, // 聊天消息
		SIGNED: 3, // 消息签收
		KEEPALIVE: 4, // 心跳
		PULL_FRIEND: 5, // 拉取好友
		FRIEND_REQUEST: 6  // 请求添加为好友
	},
	// 通信模型，与服务端一一对应
	TextMsg: function(senderId, receiverId, content, msgId) {
		this.senderId = senderId;
		this.receiverId = receiverId;
		this.content = content;
		this.msgId = msgId;
	},
	TextMsgModel: function(action, msg, extend) {
		this.action = action;
		this.msg = msg;
		this.extend = extend;
	},
	
	/**
	 * 聊天记录
	 * @param {Object} myId			当前用户的id
	 * @param {Object} friendId		好友的id
	 * @param {Object} msg			消息的具体内容
	 * @param {Object} flag			区分这条消息是我发的，还是好友发的，约定  1：我；2：朋友
	 */ 
	ChatHistory: function(msg,flag){ // myId,friendId,
		// this.myId = myId;
		// this.friendId = friendId;
		this.msg = msg;
		this.flag = flag;
	},
	
	/**
	 * 创建快照对象的函数
	 * @param {Object} myId
	 * @param {Object} friendId
	 * @param {Object} msg
	 * @param {Object} isRead 用于判断消息是否为已读还是未读消息
	 */
	ChatSnapshot:function(friendId,msg,isRead){ //myId,
		// this.myId = myId;
		this.friendId = friendId;
		this.msg = msg;
		this.isRead = isRead;
	},
	
	/**
	 * 保存消息到本地缓存
	 * 
	 * @param {Object} myId
	 * @param {Object} friendId
	 * @param {Object} msg
	 * @param {Object} flag 判断本条消息是我发送的，还是朋友发送的  1:我  2： 朋友
	 */
	saveUserChatHistory:function(myId, friendId, msg, flag) {
		// 与每一个好友的聊天记录，单独成一个键值对
		var chatKey = "chat-" + myId + "-" + friendId;
		//从本地缓存获取聊天记录是否存在
		var chatHistoryListStr = plus.storage.getItem(chatKey);
		
		//用于存储本地聊天对象的变量
		var chatHistoryList = [];
		if(app.isNotNull(chatHistoryListStr)) { // 本地缓存有聊天记录
			chatHistoryList = JSON.parse(chatHistoryListStr);
		}
		
		//构建聊天记录对象
		var singleMsg = new app.ChatHistory(msg, flag); // myId, friendId, 
		//向list 中追加msg对象
		chatHistoryList.push(singleMsg);
		
		//写入本地缓存
		plus.storage.setItem(chatKey, JSON.stringify(chatHistoryList));
	},
	
	/**
	 * 从本地缓存获取与好友的聊天记录
	 * 暂不考虑，聊天记录非常多的情况。暂时一次性全部取出。优化留到以后再做
	 * 
	 * @param {Object} myId
	 * @param {Object} friendId
	 */
	getUserChatHistory:function(myId, friendId) {
		var chatKey = "chat-"+myId+"-" + friendId;
		//从本地缓存获取聊天记录是否存在
		var chatHistoryListStr = plus.storage.getItem(chatKey);
		// console.log(chatHistoryListStr);
		//用于存储本地聊天对象的变量
		var chatHistoryList = [];
		if(app.isNotNull(chatHistoryListStr)){
			chatHistoryList = JSON.parse(chatHistoryListStr);
		}
		
		return chatHistoryList;
	},
	
	/**
	 * 删除当前登录用户和朋友的聊天记录
	 * @param {Object} myId
	 * @param {Object} friendId
	 */
	deleteUserChatHistory:function(myId,friendId){
		var chatKey = "chat-"+myId+"-" + friendId;
		plus.storage.removeItem(chatKey);
	},
	
	/**
	 * 聊天记录的快照，仅仅保存每次和朋友聊天的最后一条消息
	 * @param {Object} myId
	 * @param {Object} friendId
	 * @param {Object} msg			最后一条消息的内容
	 * @param {Object} isRead 
	 */
	saveUserChatSnapshot:function(myId, friendId, msg, isRead) {
		var chatKey = "chat-snapshot" + myId;
		//从本地缓存获取聊天快照记录是否存在
		var chatSnapshotListStr = plus.storage.getItem(chatKey);
		
		//用于存储本地聊天快照对象的变量
		var chatSnapshotList = [];
		if(app.isNotNull(chatSnapshotListStr)){
			chatSnapshotList = JSON.parse(chatSnapshotListStr);
			//循环快照list，并且判断每个元素是否包含friendId，如果匹配，则删除
			for(var i=0; i<chatSnapshotList.length; i++){
				if(chatSnapshotList[i].friendId == friendId){
					//删除已经存在的friendId 所对应的快照对象
					chatSnapshotList.splice(i,1);
					break;
				}
			}
		}
		
		//构建聊天快照对象
		var singleMsg = new app.ChatSnapshot(friendId, msg, isRead);
		//向list头部中追加snap对象
		chatSnapshotList.unshift(singleMsg);
		
		//写入本地缓存
		plus.storage.setItem(chatKey,JSON.stringify(chatSnapshotList));
		
	},
	
	/**
	 * 获取当前登录用户的聊天快照
	 * @param {Object} myId
	 */
	getUserChatSnapshot:function(myId){
		var chatKey = "chat-snapshot" + myId;
		//从本地缓存中获取聊天快照的list
		var chatSnapshotListStr = plus.storage.getItem(chatKey);
		
		//用于存储本地聊天快照对象的变量
		var chatSnapshotList = [];
		if(app.isNotNull(chatSnapshotListStr)){
			chatSnapshotList = JSON.parse(chatSnapshotListStr);
		}
		return chatSnapshotList;
	},
	
	readUserChatSnapShot:function(myId,friendId){
		var me = this;
		var chatKey = "chat-snapshot" + myId;
		//从本地缓存获取聊天快照记录是否存在
		var chatSnapshotListStr = plus.storage.getItem(chatKey);
		
		//用于存储本地聊天快照对象的变量
		var chatSnapshotList;
		if(me.isNotNull(chatSnapshotListStr)){
			chatSnapshotList = JSON.parse(chatSnapshotListStr);
			//循环快照list，并且判断每个元素是否包含friendId，如果匹配，则删除
			for(var i = 0;i<chatSnapshotList.length;i++){
				var item = chatSnapshotList[i];
				if(item.friendId == friendId){
					item.isRead = true;//标记为已读
					//替换原有快照
					chatSnapshotList.splice(i,1,item);
					break;
				}
			}
			//替换原有的快照列表
			plus.storage.setItem(chatKey,JSON.stringify(chatSnapshotList));
		}else{
			return;
		}
	},
	/**
	 * 删除本地的聊天快照记录
	 * @param {Object} myId
	 * @param {Object} friendId
	 */
	deleteUserChatSnapshot:function(myId,friendId){
		var me = this;
		var chatKey = "chat-snapshot" + myId;
		//从本地缓存获取聊天快照记录是否存在
		var chatSnapshotListStr = plus.storage.getItem(chatKey);
		
		//用于存储本地聊天快照对象的变量
		var chatSnapshotList;
		if(me.isNotNull(chatSnapshotListStr)){
			chatSnapshotList = JSON.parse(chatSnapshotListStr);
			//循环快照list，并且判断每个元素是否包含friendId，如果匹配，则删除
			for(var i = 0;i<chatSnapshotList.length;i++){
				var item = chatSnapshotList[i];
				if(item.friendId == friendId){
					//替换原有快照
					chatSnapshotList.splice(i,1);
					break;
				}
			}
		}else{
			return;
		}
		//替换原有的快照列表
		plus.storage.setItem(chatKey,JSON.stringify(chatSnapshotList));
	}
}



