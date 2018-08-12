$.fn.datepicker.setDefaults({
    format: 'm-d-yy',
    date: null,
    startDate: new Date()
});


$(document).ready(function () {

    var todolist = $("#todo-list");

    todolist.on("mouseover", "li", function () {

        $(this).find(".actions").css("visibility", "visible");
    });

    todolist.on("mouseleave", "li", function () {

        $(this).find(".actions").css("visibility", "hidden");
    });

    todolist.on("focus", ".edit-task input", function () {
        $(this).addClass("focused");
    });

    todolist.on("blur", ".edit-task input", function () {
        $(this).removeClass("focused");
    });

    todolist.on("elementAdded.ic", function () {
        $("[data-toggle='datepicker']").datepicker();
    });
    $("[data-toggle='datepicker']").datepicker();
});

