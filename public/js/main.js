requirejs.config({
    "baseUrl": "assets/js",
    "paths": {
      "jquery": "vendor/jquery-2.0.0.min",
      "modernizr": "vendor/modernizr-2.6.2.min",
      "sortable": "vendor/jquery-ui-1.10.3.custom.min"
    },
    "shim": {
        'sortable': [ 'jquery' ]
    }
});
require(['jquery','timer','taskList','taskCreator','desktopAlert', 'responsive'], function($, timer, taskList, taskCreator, notify, responsive) {
    $("form").submit(function(eventData){
    $.post(eventData.currentTarget.action, $(eventData.currentTarget).serialize());

    if ($(eventData.currentTarget).attr("name") == "createTask") {
      taskCreator.createTask(eventData);
    };

    return false;
    });

    $("button").click(function(eventData){
        $.post(eventData.currentTarget.formAction);
        return false;
    });

});


