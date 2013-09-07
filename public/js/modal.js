define(['jquery'], function ($) {
    "use strict";
    var modalOpen = false;

    function newModal(title, content, focusObject) {
        if (!modalOpen) {
            if (!title) {
                title = 'Nothing to see here';
            }
            if (!content) {
                content = '<span>Move along folk</span>';
            }
            var $newMod = $('<div class="modal-background"><div class="modal"><header><button title="close modal" class="close-button">x</button><h3>No title</h3></header><div class="content"></div></div></div>');
            $newMod.find('header h3').html(title);
            $newMod.find('.content').html(content);
            blurForm();
            $('body').append($newMod);
            focusFormInModal(focusObject);
            $('.modal-background, .modal-background .close-button').click(function () {
                closeModal();
            });
            $('.modal-background .modal').click(function (e) {
                e.stopPropagation();
            });
            modalOpen = true;
        }
    }

    function closeModal() {
        if (modalOpen) {
            $('.modal-background').remove();
            modalOpen = false;
        }
    }

    function resetForm() {
        $('.active form.createTaskForm').find("input[type=text], textarea, input[type=number]").val("");
    }

    function focusFormInModal(focusObject) {
        if (focusObject) {
            $('.modal form').find('.' + focusObject + ', #' + focusObject).first().focus();
        } else {
            $('.modal form').find('input, textarea').first().focus();
        }
    }

    function focusForm() {
        $('.active form.createTaskForm').find('.title').focus();
    }

    function blurForm() {
        $('input, textarea').blur();
    }

    return {
        create: function (title, content) {
            newModal(title, content);
        },
        destroy: function () {
            closeModal();
        }
    };
});