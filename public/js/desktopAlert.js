define(['jquery'], function($) {
	var notifications = {};

	$(document).ready(function($) {
		init();
	})

	function init (){
		notifications.support = window.webkitNotifications;
		console.log(testBrowserSupport());
		setupEventHandlers();
	};

	function setupEventHandlers () {
		var _this = this;
		$('#alert-me-btn').bind('click', function(e) {
			_this.checkPermission("desktopAlert");
		});
	};

	function testBrowserSupport () {
		var $browserMsg = $('#browser-support-msg');
		if(notifications.support) {
			return true;
		}
		else {
			return false;
		}
	};

	function checkPermission (callback) {
		var _this = this;
		if (this.cache.notifications.checkPermission() == 0) {
			_this[callback]();
		}
		else {
			this.cache.notifications.requestPermission(function() {
				if (this.cache.notifications.checkPermission() == 0) _this[callback]();
			});
		}
	};

	return {
		desktopAlert: function() {
			console.log('sending alert...');
			var notification = window.webkitNotifications.createNotification("", $('#da-title').val(), $('#da-message').val());
			notification.show();
		}

	}

	


});