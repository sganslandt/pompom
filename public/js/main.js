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
require(['jquery','timer','taskList','taskCreator','desktopAlert'], function($, timer, taskList, taskCreator, notify) {
  $("form").submit(function(eventData){
    $.post(eventData.currentTarget.action, $(eventData.currentTarget).serialize());

    if ($(eventData.currentTarget).attr("name") == "createTask") {
      if ($(eventData.currentTarget).find(':checkbox').prop('checked')) { var targetList = $('#inventory .taskList');}
      else{ var targetList = $('#today .taskList');}
    	taskList.addTaskToList(
        targetList,
      	$(eventData.currentTarget).serializeArray()[0].value,
      	$(eventData.currentTarget).serializeArray()[1].value,
      	$(eventData.currentTarget).serializeArray()[2].value
      );
      taskCreator.resetForm();
    };

    return false;
  });

});


