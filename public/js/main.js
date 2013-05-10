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
    $("form").submit(function(eventData){
        $.post(eventData.currentTarget.action, $(eventData.currentTarget).serialize());
        if ($(eventData.currentTarget).attr("name") == "createTask") {
        	addTaskToList(
        		$(eventData.currentTarget).find(':checkbox').prop('checked'),
	        	$(eventData.currentTarget).serializeArray()[0].value,
	        	$(eventData.currentTarget).serializeArray()[1].value,
	        	$(eventData.currentTarget).serializeArray()[2].value
	        );
	        
	        $(eventData.currentTarget).find("input[type=text], textarea, input[type=number]").val("");
	        $(eventData.currentTarget).find('#createTaskTitle').focus();
	        $('.sortable').sortable({
						connectWith: '.connected'
					});
        };
        
        return false;
    });
});


function addTaskToList (checkbox, title, numberOfPoms, description) {
		var $targetList = $('#today ol.taskList');
		if (checkbox) {
			$targetList = $('#inventory ol.taskList');
		};
		$newLi = $('<li draggable="true" />');
		$newLi.append('<div class="task" data-taskId="temp100">');
		$newLi.find('.task').append('<h3>'+title+'</h3>');
		$newLi.find('.task').append('<p>'+description+'</p>');
		$newLi.find('.task').append('<ol class="pomodoros">');
		for (var i = numberOfPoms - 1; i >= 0; i--) {
			$newLi.find('.pomodoros').append('<li class="active">&nbsp;</li>');
		};
		$targetList.append($newLi);
	}