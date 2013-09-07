define(['jquery', 'notify'], function ($, notify) {
    "use strict";

    $(document).ready(function ($) {
        $(".modal button.authorizeNotification").click(function () {
            notify.authorize();
        });
    });
});