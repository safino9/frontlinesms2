var SystemNotification = function() {
	var
	_MARK_READ_ACTION = url_root + "systemNotification/markRead/",
	_getId = function(e) {
		return e.attr("id").substring(13);
	},
	add = function(id, text) {
		$("#notifications").append(create(id, text));
	},
	create = function(id, text) {
		var elementId = "notification-" + id;
		return '<div class="system-notification" id="' + elementId + '">'
				+ text
				+ '<a onclick="systemNotification.hide(' + id + ')" class="hider">x</a></div>';
	},
	hide = function(id) {
		$.get(_MARK_READ_ACTION + id);
		$("#notification-" + id).slideUp(500);
	},
	_refresh = function(data) {
		// remove any notifications no longer in the list
		var _key, value, found, notifications;
		data = data.system_notification;
		if(!data) { return; }
		found = [];
		$(".system-notification").each(function(i, e) {
			e = $(e);
			var notificationId = _getId(e);
			if(!data[notificationId]) {
				// remove dead notification
				e.slideUp(200);
			} else {
				// prevent the notification being re-added
				data[notificationId] = null;
			}
		});

		// add any new notifications to the bottom of the list
		notifications = $("#notifications");
		for(_key in data) {
			value = data[_key];
			if(value) {
				notifications.append(create(_key, value));
			}
		}
	},
	init = function() {
		app_info.listen("system_notification", _refresh);
	};
	return {
		add:add,
		hide:hide,
		init:init
	};
};

