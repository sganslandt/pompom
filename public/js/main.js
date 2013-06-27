requirejs.config({
    "baseUrl": "assets/js",
    "paths": {
        "jquery": "vendor/jquery-2.0.2.min",
        "modernizr": "vendor/modernizr-2.6.2.min",
        "sortable": "vendor/jquery-ui-1.10.3.custom.min",
        "touch-punch": "vendor/jquery.ui.touch-punch.min"
    },
    "shim": {
        'sortable': [ 'jquery' ],
        'touch-punch': ['sortable']
    }
});

require(['jquery', 'navigation', 'timer', 'taskList', 'taskCreator', 'task', 'notify', 'responsive'], function ($, navigation, timer, taskList, taskCreator, task, notify, responsive) {

    $('#loader').fadeOut('slow', function()
    {
        $('#loader').remove();
    });
});