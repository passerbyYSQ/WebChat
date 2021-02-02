window.addEventListener('refresh', function(e) {
	window.location.reload();//页面更新
});

window.app = {
	
	initMyMui: function() {
		var _this = this;
		return mui.init({
			beforeback: function() {
		　　　　 //获得父页面的webview
				var parent = plus.webview.currentWebview().opener();
		　　　　 //触发父页面的自定义事件(refresh),从而进行刷新
				if (_this.isNotNull(parent)) {
					mui.fire(parent, 'refresh');
					console.log(parent.id);
				}
				//返回true,继续页面关闭逻辑
				return true;
			}
		});
	},
	
	/**
	 * netty服务后端发布的url地址
	 */
	nettyServerUrl: 'ws://101.200.79.231:8888/ws',
	
	/**
	 * 后端服务发布的url地址
	 * 开发时，服务端和测试手机需要在同一局域网。此处的ip是服务端所在电脑的局域网ip
	 * 由于是动态分配，所以ip可能会变
	 */
	serverUrl: "http://192.168.0.109:8080/v1/api/",  
	
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
	
	/**
	 * 封装ajax请求,携带token。
	 * 必须要在plusReady()事件之后调用
	 */
	ajax: function(url, data, successCallback, type = "post", errorCallback = function() {}) {
		plus.nativeUI.showWaiting("Loading...");
		var user = this.getUserGlobalInfo();
		var token = null;
		if (this.isNotNull(user)) {
			token = user.token;
		}
		console.log(token);
		var _this = this;
		mui.ajax(this.serverUrl + url, {
			type: type,
			data: data,
			// dataType: "json",
			timeout: 10000 ,// 超时时间为10秒
			headers: {
				token: token // 携带token
			},
			success: function(res) {
				console.log("success");
				console.log(JSON.stringify(res));
				plus.nativeUI.closeWaiting();
				if (res.code !== 2000) { // 与后端约定：成功的业务状态的请求统一返回2000
					return _this.showToast(res.msg, "error");
				}
				successCallback(res);
			},
			error: function(xhr, textStatus, errorThrown) {
				console.log("error");
				plus.nativeUI.closeWaiting();
				if (textStatus == "timeout") {
					app.showToast("连接超时", "error");
				} else if (textStatus == "error") {
					app.showToast("服务端错误", "error");
				}
				errorCallback(xhr, textStatus, errorThrown);
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
		var webviews = plus.webview.all();
		for(var i=0; i<webviews.length; i++) {
			if (webviews[i].id !== 'login') {
				webviews[i].close();
			}
		}
	},
	
	//保存用户的联系人列表
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
			return [];
		}
		
		return JSON.parse(contactListStr);
	},
	getFriendFromContactList:function(friendId){
		//获取联系人列表的本地缓存
		var contactListStr = plus.storage.getItem("contactList");
		if(this.isNotNull(contactListStr)){
			//不为空，则把用户信息返回
			var contactList = JSON.parse(contactListStr);
			for(var i = 0;i<contactList.length;i++){
				var friend = contactList[i];
				if(friend.friendUserId == friendId){
					return friend;
					break;
				}
			}
		}else{
			return null;
		}
		
	},
	/**
	 * @param {Object} myId
	 * @param {Object} friendId
	 * @param {Object} msg
	 * @param {Object} flag 判断本条消息是我发送的，还是朋友发送的  1:我  2： 朋友
	 */
	saveUserChatHistory:function(myId,friendId,msg,flag){
		var me = this;
		var chatKey = "chat-"+myId+"-" + friendId;
		//从本地缓存获取聊天记录是否存在
		var chatHistoryListStr = plus.storage.getItem(chatKey);
		
		//用于存储本地聊天对象的变量
		var chatHistoryList;
		if(me.isNotNull(chatHistoryListStr)){
			chatHistoryList = JSON.parse(chatHistoryListStr);
		}else{
			//如果唯恐，赋一个空的list
			chatHistoryList = [];
		}
		
		//构建聊天记录对象
		var singleMsg = new me.ChatHistory(myId,friendId,msg,flag);
		//向list 中追加msg对象
		chatHistoryList.push(singleMsg);
		
		//写入本地缓存
		plus.storage.setItem(chatKey,JSON.stringify(chatHistoryList));
		
	},
	
	getUserChatHistory:function(myId,friendId){
		var me = this;
		var chatKey = "chat-"+myId+"-" + friendId;
		//从本地缓存获取聊天记录是否存在
		var chatHistoryListStr = plus.storage.getItem(chatKey);
		
		//用于存储本地聊天对象的变量
		var chatHistoryList;
		if(me.isNotNull(chatHistoryListStr)){
			chatHistoryList = JSON.parse(chatHistoryListStr);
		}else{
			//如果唯恐，赋一个空的list
			chatHistoryList = [];
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
	 * @param {Object} msg
	 * @param {Object} isRead 
	 */
	saveUserChatSnapshot:function(myId,friendId,msg,isRead){
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
				if(chatSnapshotList[i].friendId == friendId){
					//删除已经存在的friendId 所对应的快照对象
					chatSnapshotList.splice(i,1);
					break;
				}
			}
		}else{
			//如果为空，赋一个空的list
			chatSnapshotList = [];
		}
		
		//构建聊天快照对象
		var singleMsg = new me.CHatSnapshot(myId,friendId,msg,isRead);
		//向list 中追加snap对象
		chatSnapshotList.unshift(singleMsg);
		
		//写入本地缓存
		plus.storage.setItem(chatKey,JSON.stringify(chatSnapshotList));
		
	},
	getUserChatSnapshot:function(myId){
		var me = this;
		var chatKey = "chat-snapshot" + myId;
		//从本地缓存中获取聊天快照的list
		var chatSnapshotListStr = plus.storage.getItem(chatKey);
		
		//用于存储本地聊天快照对象的变量
		var chatSnapshotList;
		if(me.isNotNull(chatSnapshotListStr)){
			chatSnapshotList = JSON.parse(chatSnapshotListStr);
		}else{
			chatSnapshotList = [];
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
	},
	//和后段枚举一一对应
	CONNECT: 1,    // "第一次(或重连)初始化连接"
	CHAT: 2,       // "聊天消息"),	
	SIGNED: 3,     // "消息签收"),
	KEEPALIVE: 4,  //"客户端保持心跳"),
	PULL_FRIEND: 5,// "拉取好友");
	/**
	 * 和后端的ChatMsg 聊天模型对象保持一致
	 * @param {Object} senderId
	 * @param {Object} receiverId
	 * @param {Object} msg
	 * @param {Object} msgId
	 */
	ChatMsg: function(senderId,receiverId,msg,msgId){
		this.senderId = senderId;
		this.receiverId = receiverId;
		this.msg = msg;
		this.msgId = msgId;
	},
	/**构建消息DataContent模型对象
	 * @param {Object} action
	 * @param {Object} chatMsg
	 * @param {Object} extand
	 */
	DataContent: function(action,chatMsg,extand){
		this.action = action;
		this.chatMsg = chatMsg;
		this.extand = extand;
	},
	ChatHistory: function(myId,friendId,msg,flag){
		this.myId = myId;
		this.friendId = friendId;
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
	CHatSnapshot:function(myId,friendId,msg,isRead){
		this.myId = myId;
		this.friendId = friendId;
		this.msg = msg;
		this.isRead = isRead;
	}
	
}
