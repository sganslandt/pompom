define(['jquery', 'notify'], function ($, notify) {

	$(document).ready(function ($)
	{
		$(".modal button.authorizeNotification").click(function () {
            notify.authorize();
        });
	});
});