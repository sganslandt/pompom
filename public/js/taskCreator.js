define('taskCreator', ['jquery', 'taskList'], function ($, taskList) {
    var ListTypes = {
        'today' : 'TodoToday',
        'inventory': 'ActivityInventory'
    };

    $(document).ready(function ($) {
        focusForm();
    });

    $(".createTaskForm").submit(function (eventData) {

        createTask(eventData);
        return false;
    });

    function createTask(eventData) {
        var targetList = $(eventData.currentTarget).closest('section').find('ol.taskList');
        // Add a temporary task in list with JS
        taskList.addTaskToList(
            targetList,
            $(eventData.currentTarget).serializeArray()[0].value,
            $(eventData.currentTarget).serializeArray()[1].value
        );
        // Add task in list on server and then remove the the temporary task
        $.post(eventData.currentTarget.action, $(eventData.currentTarget).serialize(), function(data){
            $newTask = $('<div class="newone" />');
            $newTask.html(data);
            setTimeout(function()
            {
                $(".new-task").replaceWith($newTask.find('li.task'));
                taskList.refreshList(targetList);
            }, 250);
        });
        // Reset and focus on the form
        resetForm();
        focusForm();
    }

    function resetForm() {
        $('.active form.createTaskForm').find("input[type=text], textarea, input[type=number]").val("");
    }

    function focusForm() {
        $('.active form.createTaskForm').find('.title').focus();
    }

    function blurForm() {
        $('.active .createTaskForm input, .active .createTaskForm textarea').blur();
    }
});