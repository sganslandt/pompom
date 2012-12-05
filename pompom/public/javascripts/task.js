var task = {id:'null', title:'', taskLength:'0'};


function createNewTask (id, title, taskLength) {
	$task = $(task);
	$task.prop('id' , id);
	$task.prop('title' , title);
	$task.prop('taskLength' , taskLength);
	return $task;
}