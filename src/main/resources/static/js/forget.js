
$(function(){
    $("#emailBtn").click(get_code);

});

function get_code() {

    // 发送AJAX请求前,将CSRF令牌设置到请求消息头中
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");
    $(document).ajaxSend(function (e, xhr, options){
        xhr.setRequestHeader(header, token);
    });
    var email = $("#your-email").val();
    if (email != "") {
        $("#emailBtn").click(time(this));
    }
    $.post(
        CONTEXT_PATH + "/user/getCode",
        {"email":email},
        function data(data) {
            data = $.parseJSON(data);
            $("#hintBody").text(data.msg);
            $("#hintModal").modal("show");
            setTimeout(function(){
                $("#hintModal").modal("hide");
            }, 2000);
        }
    );

    $("#hintModal").modal("show");
    setTimeout(function(){
        $("#hintModal").modal("hide");
    }, 2000);
}
var wait=60;
function time(obj) {
    if (wait == 0) {
        obj.removeAttribute("disabled");
        obj.innerHTML="获取验证码";
        wait = 60;
    } else {
        obj.setAttribute("disabled", true);
        obj.innerHTML=wait+"秒后重新发送";
        wait--;
        setTimeout(function() {
                time(obj)
            },
            1000)
    }
}
