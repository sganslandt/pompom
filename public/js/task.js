define(['jquery', 'modal'], function ($, modal) {

    $("li.task").click(function () {
    	var title = $(this).find('h3').html();
    	var totalPoms = $(this).find('ol').length;
    	var freshPoms = $(this).find('ol .fresh').length;
    	var content =  "<p>Pomodoros left: <span class='pom-left'>" + freshPoms + " of " + totalPoms + "</span></p> \
    				    <form class='extendEstimateForm' name='extendEstimateForm' method='post' action='/tasks/" + $(this).attr('taskid') + "/extendEstimate'> \
    				    	<input class='additionalPomodoros' name='additionalPomodoros' type='number' min='1' max='8' required='' placeholder='1'> \
    				    	<input name='extendEstimateButton' type='submit' id='extendEstimateButton' value='Extend'> \
    				    </form> \
    				    <button method='post' action='/tasks/" + $(this).attr('taskid') + "/completeTask'>Complete this task</button>"
        modal.new(title, content, 'additionalPomodoros');
    });

});