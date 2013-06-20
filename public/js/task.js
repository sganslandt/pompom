define(['jquery', 'taskList', 'modal', 'notify'], function ($, taskList, modal, notify) {
    $(document).ready(function($) {
        bindAllTasksToModal();
    });

    function bindAllTasksToModal ()
    {
        $("li.task").click(function () {
            bindTaskModal($(this));
        });
    }
    function bindTaskModal(task)
    {
        var title = $(task).find('h3').html();
        var totalPoms = $(task).find('ol li').length;
        var freshPoms = $(task).find('ol .fresh').length;
        var content =  "<p>Pomodoros left: <span class='pom-left'>" + freshPoms + " of " + totalPoms + "</span></p> \
                        <form class='extendEstimateForm' name='extendEstimateForm' method='post' action='/tasks/" + $(task).data('taskid') + "/extendEstimate'> \
                            <input class='additionalPomodoros' name='additionalPomodoros' type='number' min='1' max='8' required='' placeholder='1'> \
                            <input name='extendEstimateButton' type='submit' id='extendEstimateButton' value='Extend'> \
                        </form> \
                        <form class='completeTaskForm' name='completeTaskForm' method='post' action='/tasks/" + $(task).data('taskid') + "/completeTask'> \
                            <input name='completeTaskButton' type='submit' id='completeTaskButton' value='Complete this task'> \
                        </form>"
        modal.new(title, content, 'additionalPomodoros');
        $(".modal form").submit(function (eventData) {
            $.post(eventData.currentTarget.action, $(eventData.currentTarget).serialize(), function(data){
                if (eventData.currentTarget == completeTaskForm)
                {
                    taskList.removeTask($(task));
                    notify.bar(title + ' was completed');
                };
                console.log(data);
                modal.destroy();
            });
            return false;
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