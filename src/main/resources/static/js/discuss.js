$(function () {
    $("#topBtn").click(setTop);
    $("#wonderfulBtn").click(setWonderful);
    $("#deleteBtn").click(setDelete);
    $("#unWonderfulBtn").click(unWonderful);
    $("#unTopBtn").click(unTop);
});
function like(btn, entityType, entityId, entityUserId, postId) {
    // 发送AJAX请求前,将CSRF令牌设置到请求消息头中
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");
    $(document).ajaxSend(function (e, xhr, options){
        xhr.setRequestHeader(header, token);
    });
    $.post(
        CONTEXT_PATH + "/like",
        {"entityType":entityType,"entityId":entityId,"entityUserId":entityUserId,"postId":postId},
        function (data) {
            data = $.parseJSON(data);
            if(data.code == 0) {
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus==1?'已赞':'赞');
            } else {
                alert(data.msg);
            }
        }
    );
}
function setDelete(){
    // 发送AJAX请求前,将CSRF令牌设置到请求消息头中
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");
    $(document).ajaxSend(function (e, xhr, options){
        xhr.setRequestHeader(header, token);
    });
    var id = $("#postId").val();
        // 发送异步请求
    $.post(
        CONTEXT_PATH + "/discuss/delete",
        {"id":id},
        function data(data) {
            data = $.parseJSON(data);
            // 在提示框中显示返回消息
            $("#hintBody").text(data.msg);
            // 显示提示框
            $("#hintModal").modal("show");
            // 2秒后自动隐藏
            setTimeout(function(){
                $("#hintModal").modal("hide");
                // 刷新页面
                if (data.code == 0) {
                    window.location.href=CONTEXT_PATH + "/index";
                }
            }, 2000);
        }
    );
    $("#deleteModal").modal("hide");
    $("#hintModal").modal("show");
    setTimeout(function(){
        $("#hintModal").modal("hide");
    }, 2000);
}
function setTop(){
    // 发送AJAX请求前,将CSRF令牌设置到请求消息头中
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");
    $(document).ajaxSend(function (e, xhr, options){
        xhr.setRequestHeader(header, token);
    });
    var id = $("#postId").val();
    // 发送异步请求
    $.post(
        CONTEXT_PATH + "/discuss/top",
        {"id":id},
        function data(data) {
            data = $.parseJSON(data);
            // 在提示框中显示返回消息
            if (data.code == 0) {
                window.location.reload();
            } else {
                alert(data.msg);
            }
        }
    );
}

function setWonderful() {
    // 发送AJAX请求前,将CSRF令牌设置到请求消息头中
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");
    $(document).ajaxSend(function (e, xhr, options) {
        xhr.setRequestHeader(header, token);
    });
    // 获取标题和内容
    var id = $("#postId").val();
    // 发送异步请求
    $.post(
        CONTEXT_PATH + "/discuss/wonderful",
        {"id": id},
        function data(data) {
            data = $.parseJSON(data);

            if (data.code == 0) {
                window.location.reload();
            } else {
                alert(data.msg);
            }
        }
    );
}
function unTop(){
    // 发送AJAX请求前,将CSRF令牌设置到请求消息头中
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");
    $(document).ajaxSend(function (e, xhr, options){
        xhr.setRequestHeader(header, token);
    });
    var id = $("#postId").val();
    // 发送异步请求
    $.post(
        CONTEXT_PATH + "/discuss/unTop",
        {"id":id},
        function data(data) {
            data = $.parseJSON(data);
            // 在提示框中显示返回消息
            if (data.code == 0) {
                window.location.reload();
            } else {
                alert(data.msg);
            }
        });
}

function unWonderful(){
    // 发送AJAX请求前,将CSRF令牌设置到请求消息头中
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");
    $(document).ajaxSend(function (e, xhr, options){
        xhr.setRequestHeader(header, token);
    });
    // 获取标题和内容
    var id = $("#postId").val();
    // 发送异步请求
    $.post(
        CONTEXT_PATH + "/discuss/unWonderful",
        {"id":id},
        function data(data) {
            data = $.parseJSON(data);

            if (data.code == 0) {
                window.location.reload();
            } else {
                alert(data.msg);
            }
        });
}