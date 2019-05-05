/**
 * Created by zhoujh on 2018/11/24
 */
$(function() {
    var obj = {
        init : function() {
            this.initEvent();
        },
        initEvent : function() {
            var thisObj = this;
            this.loadFile($("#businessId").val(),$("#agentType").val());
            $("#btnFileUpload").click(function () {
                thisObj.toFileUpload();
            });
            $("#upload").change(function () {
                thisObj.fileUpload();
            });
        },
        loadFile : function(businessId,agentType) {
            $("#file_refresh").load(_root+agentType+"/fileUpload/refreshfileUpload?businessId="+businessId);
        },
        toFileUpload: function () {
            $("#upload").click();
        },
        fileUpload: function () {
            var thisObj = this;
            var businessId = $("#businessId").val();
            var businessCode = $("#businessCode").val();
            var agentType = $("#agentType").val();
            var extensions = $("#extensions").val();
            var uploadFile = document.getElementById("upload").files;
            if(uploadFile.length==0){
                return;
            }
            var flag = true;
            var info = "";
            if(extensions==''){
                extensions="jpg|jpeg|gif|bmp|png|pdf|doc|docx|xls|xlsx|rar|zip";
            }
            var pattern = new RegExp("^("+extensions+")$");
            var fileSizeMax = 10*1024*1024;
            for(var i = 0; i < uploadFile.length; i++){
                var fileExtend = uploadFile[i].name.substring(uploadFile[i].name.lastIndexOf('.')).toLowerCase();
                fileExtend = fileExtend.substring(1,fileExtend.length);
                var isMatch = pattern.test(fileExtend);
                if(!isMatch){
                    flag = false;
                    info = "上传文件格式不匹配!";
                    break;
                }
                var fileSize = uploadFile[i].size;

                if(fileSize>fileSizeMax){
                    flag = false;
                    info = "上传文件大小不能超过10M!";
                    break;
                }
            }

            if(!flag){
                yslCommon.layer.alert( info, {icon: 2});
            }else {
                var formFile = new FormData();
                formFile.append("action", "UploadVMKImagePath");
                for(var i = 0; i < uploadFile.length; i++){
                    formFile.append("file", uploadFile[i]);//加入文件对象
                }
                formFile.append("businessId", businessId);
                formFile.append("businessCode", businessCode);
                formFile.append("extensions", extensions);
                yslCommon.ajax({
                    url: _root + agentType + "/fileUpload/fileUpload",
                    data: formFile,
                    type: "post",
                    dataType: "json",
                    cache: false,//上传文件无需缓存
                    processData: false,//用于对data参数进行序列化处理 这里必须false
                    contentType: false, //必须
                    success: function () {
                        yslCommon.layer.alert( "上传成功！", {icon: 1});
                        thisObj.loadFile(businessId,agentType);
                    }
                });
            }
        }
    }
    obj.init();
})