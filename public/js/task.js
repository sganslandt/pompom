define(['jquery', 'modal'], function ($, modal) {
    $(document).ready(function($) {
        $.each($("li.task"), function() {
            bindAllTasksToModal();
        });
    });

    function bindAllTasksToModal ()
    {
        $.each($("li.task"), function() {
            bindTaskModal($(this));
        });
    }
    function bindTaskModal(task)
    {
        $(task).click(function () {
            bindTaskModal(task)
            var title = $(task).find('h3').html();
            var totalPoms = $(task).find('ol li').length;
            var freshPoms = $(task).find('ol .fresh').length;
            var content =  "<p>Pomodoros left: <span class='pom-left'>" + freshPoms + " of " + totalPoms + "</span></p> \
                            <form class='extendEstimateForm' name='extendEstimateForm' method='post' action='/tasks/" + $(task).attr('taskid') + "/extendEstimate'> \
                                <input class='additionalPomodoros' name='additionalPomodoros' type='number' min='1' max='8' required='' placeholder='1'> \
                                <input name='extendEstimateButton' type='submit' id='extendEstimateButton' value='Extend'> \
                            </form> \
                            <button method='post' action='/tasks/" + $(task).attr('taskid') + "/completeTask'>Complete this task</button>"
            modal.new(title, content, 'additionalPomodoros');
        });
    }

    return {
        bindModal: function (task) {
            bindTaskModal(task);
        },
        bindRefresh: function (task) {
            bindAllTasksToModal();
        }
    }
});