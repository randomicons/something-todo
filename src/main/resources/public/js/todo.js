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

    function save() {
        $.ajax({
            type: 'POST',
            url: 'save/' + userId,
            // Always include an `X-Requested-With` header in every AJAX request,
            // to protect against CSRF attacks.
            headers: {
                'X-Requested-With': 'XMLHttpRequest'
            },
            contentType: 'application/json',
            dataType: "json",
            success: function (result) {
                console.log("save sucesss" + result)
            },
            data: createSaveBody()
        });
    }
    $(window).on('unload', function () {
        save();
    });

    $("#save_button").click(function () {
        save();
    })

});

function createSaveBody() {
    var out = ""
    $(".edit-task").each(function (index, e) {
        out += (index + 1) + ". " + $(e).find(".input-name").val();
        out += " " + $(e).find(".input-date input").val() + "\n"
    })
    return out;
}
