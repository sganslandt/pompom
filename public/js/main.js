requirejs.config({
    "baseUrl": "assets/js",
    "paths": {
      "jquery": "vendor/jquery-2.0.0.min",
      "modernizr": "vendor/modernizr-2.6.2.min",
      "sortable": "jquery.sortable.custom"
    },
    "shim": {
        'sortable': [ 'jquery' ]
    }
});
require(['jquery','timer','todoList','taskCreator'], function($, timer) {
	console.log('all done');
});
