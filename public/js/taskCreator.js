define('taskCreator', ['jquery', 'taskList'], function ($, taskList) {
    $(document).ready(function ($) {
        $('#taskCreator').find('button.expand').click(function () {
            openPopup();
        });
        focusForm();
    });

    $(".createTaskForm").submit(function (eventData) {
        createTask(eventData);
    });
    function createTask(eventData) {
        var targetList;
        if ($(eventData.currentTarget).find(':checkbox').prop('checked')) targetList = $('#inventory').find('.taskList');
        else targetList = $('#tasks').find('section.active .taskList');
        taskList.addTaskToList(
            targetList,
            $(eventData.currentTarget).serializeArray()[0].value,
            $(eventData.currentTarget).serializeArray()[1].value
        );
        resetForm();
        focusForm();
    }

    function resetForm() {
        $('form#createTask').find("input[type=text], textarea, input[type=number]").val("");
    }

    function focusForm() {
        $('form#createTask').find('#createTaskTitle').focus();
    }

    function blurForm() {
        $('#createTask input, #createTask textarea').blur();
    }

    function openPopup() {
        var $newPop = $('<div class="popup"><div class="content"></div></div>');
        $newPop.find('.content').append('<h2>Create new pomodoros<h2/>');
        $newPop.find('.content').append('<button title="close popup" class="closeButton">x</button>');
        $('body').append($newPop);

        $('.popup, .popup .closeButton').click(function () {
            closePopup();
        });
        $(".popup .content").click(function (e) {
            e.stopPropagation();
        });
        $('.popup .content').append($('#createTask'));
        focusForm();
    }

    function closePopup() {
        $('#taskCreator').append($('#createTask'));
        blurForm();
        $('.popup').remove();
    }

    return {
        closeCreateFormPopup: function () {
            closePopup();
        }
    }
});