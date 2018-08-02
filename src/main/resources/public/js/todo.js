$.fn.datepicker.setDefaults({
    autoPick: true,
    format: 'm-d-yyyy',
    startDate: new Date()
})
$(document).ready(function () {

    $("#todo-list").on("mouseover", "li", function () {

        $(this).find("button").css("visibility", "visible");
    });

    $("#todo-list").on("mouseleave", "li", function () {

        $(this).find("button").css("visibility", "hidden");
    });

    $("#todo-list").on("focus", "input", function () {
        $(this).addClass("focused");
    });

    $("#todo-list").on("blur", "input", function () {
        $(this).removeClass("focused");
    });

    $("[data-toggle='datepicker']").datepicker();
    $("#todo-list").on("elementAdded.ic", function () {
        $("[data-toggle='datepicker']").datepicker();
    });
});


