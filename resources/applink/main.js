/**
 * Created by jinmin on 2016/4/26.
 */
(function(window){
    function getParameterByName(name) {
        name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
        var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
            results = regex.exec(location.search);
        return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
    }

    var isAndroid = !!navigator.userAgent.match(/android/ig),
        isIos = !!navigator.userAgent.match(/iphone|ipod|ipad/ig),
        isIpad = !!navigator.userAgent.match(/ipad/ig),
        isIos9 = !!navigator.userAgent.match(/OS 9/ig),
        isYnoteApp = !!navigator.userAgent.match(/YnoteiOS/i),
        isAndroidApp = /YnoteAndroid/i.test(navigator.userAgent),
        isWeixin = (/MicroMessenger/ig).test(navigator.userAgent),
        ios_scheme = 'knote://',
        android_scheme = 'knote://',
        isQQ = !(/QQBrowser/ig).test(navigator.userAgent) && (/qq/ig).test(navigator.userAgent),
        isWeibo = (/weibo/ig).test(navigator.userAgent),
        platform;

    // 没有collab 就是需要唤起app，不管能不能定位到笔记
    var collab = getParameterByName('collab') == 'true',
        shareKey = getParameterByName('shareKey'),
        fileId = getParameterByName('fileId'),
        operation = getParameterByName('operation'),
        ownerId = getParameterByName('ownerId'),
        userId = getParameterByName('userId'),
        keyfrom = getParameterByName('keyfrom');
    
    //rlog统计数据
    if (_rlog) {
        if (isAndroid) {
            platform = 'android';
        } else if (isIos) {
            platform = 'ios';
        } else {
            platform = 'other';
        }
        _rlog.push(['_trackEvent' , 'applinks_' + keyfrom + '_' + platform]);
    }

    var ios_url = 'https://itunes.apple.com/cn/app/id450748070?ls=1&mt=8',
        android_url = !collab ? 'http://a.app.qq.com/o/simple.jsp?pkgname=com.zd.tutor&ckey=CK1317929020803' : 'http://codown.youdao.com/note/youdaonote_youdaowap.apk';
    var height = document.documentElement.clientHeight;

    if (shareKey && fileId && operation && ownerId) {
        ios_scheme = 'knote://?shareKey=' + shareKey + '&fileId=' + fileId + '&operation=' + operation + '&ownerId=' + ownerId + '&userId=' + userId;
        android_scheme = 'knote://?shareKey=' + shareKey + '&fileId=' + fileId + '&operation=' + operation + '&ownerId=' + ownerId + '&userId=' + userId;
    }

    if (isIos) {
        document.getElementById('download').setAttribute('href', ios_url);
        document.getElementById('iframe').src = ios_scheme;
    } else {
        document.getElementById('download').setAttribute('href', android_url);
        document.getElementById('iframe').src = android_scheme;
    }
    

    if(!isWeixin && !isQQ && !isWeibo){
        document.querySelector('.body').style.display = 'block';
        document.querySelector('.body').style.height = height + 100 + 'px';
        if(isIos9){
            document.querySelector('.body').classList.add('showtip');
        }
        setTimeout(function(){
            document.body.scrollTop = 0;
        },200);
    } else {
        document.getElementById('guide').style.display = 'block';
        if (isIos) {
            document.getElementById('guide').classList.add('iosguide');
        } else {
            document.getElementById('guide').classList.add('androidguide');
        }
    }

    function checkOpen() {
        // 如果4s时间后,还是无法唤醒app，则直接打开下载页
        // opera 无效
        var start = Date.now(),
            loadTimer = setTimeout(function() {
                if (document.hidden || document.webkitHidden) {
                    return;
                }
                // 如果app启动，浏览器最小化进入后台，则计时器存在推迟或者变慢的问题
                // 那么代码执行到此处时，时间间隔必然大于设置的定时时间
                if (Date.now() - start < 3200) {
                    // 如果浏览器未因为app启动进入后台，则定时器会准时执行，故应该跳转到下载页
                    // alert('若长时间未打开应用，请确认您的设备是否已安装云笔记，尚未安装请点击立即下载。');
                }
            }, 3000);

        // 当本地app被唤起，则页面会隐藏掉，就会触发pagehide与visibilitychange事件
        // 在部分浏览器中可行，网上提供方案，作hack处理
        var visibilitychange = function() {
            var tag = document.hidden || document.webkitHidden;
            tag && clearTimeout(loadTimer);
        };
        document.addEventListener('visibilitychange', visibilitychange, false);
        document.addEventListener('webkitvisibilitychange', visibilitychange, false);
        // pagehide 必须绑定到window
        window.addEventListener('pagehide', function() {
            clearTimeout(loadTimer);
        }, false);
    }
    
    document.getElementById('open').addEventListener('click', function() {
        if (isAndroidApp || isYnoteApp) {
            alert('您已在云笔记客户端里啦~');
            return;
        }
        if (isWeixin || isQQ || isWeibo) {
           return;
        }
        if (isIos) {
            window.location = ios_scheme;
        } else if (isAndroid) {
            if(navigator.userAgent.match(/ucbrowser|yixin|MailMaster/i)){
                document.getElementById('iframe').src = android_scheme;
            } else {
                // 像chrome这类的浏览器不支持iframe src的方式打开
                window.location.href = android_scheme;
            }
        }
        checkOpen();
    }, false);
})(window);