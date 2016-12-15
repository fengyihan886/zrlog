$(function(){
    function checkVersion(){
        $.get("api/admin/upgrade/checkNewVersion",function(e){
           var message = "";
            if(e.upgrade){
                message = "新版本 v" + e.version.version + "-" + e.version.buildId + " ("+e.version.type+")";
            }else{
                message = "暂时没有新版本";
            }
            new PNotify({
              title: message,
              type: 'info',
              delay:3000,
              hide: true,
              styling: 'bootstrap3'
            });
        })
        return false;
    }

    $("#checkUpgrade").click(function(e){
        checkVersion();
    });

})
