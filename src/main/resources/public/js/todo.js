$.fn.datepicker.setDefaults({
    format: 'm-d-yy',
    date: null,
    startDate: new Date()
});


$(document).ready(function () {

    $("#todo-list").on("mouseover", "li", function () {

        $(this).find(".actions").css("visibility", "visible");
    });

    $("#todo-list").on("mouseleave", "li", function () {

        $(this).find(".actions").css("visibility", "hidden");
    });

    $("#todo-list").on("focus", ".edit-task input", function () {
        $(this).addClass("focused");
    });

    $("#todo-list").on("blur", ".edit-task input", function () {
        $(this).removeClass("focused");
    });

    $("[data-toggle='datepicker']").datepicker();
    $("#todo-list").on("elementAdded.ic", function () {
        $("[data-toggle='datepicker']").datepicker();
    });
});

