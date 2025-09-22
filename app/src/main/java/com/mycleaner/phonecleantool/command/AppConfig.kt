package com.mycleaner.phonecleantool.command

import com.mycleaner.phonecleantool.adv.LogAdParam

object AppConfig {

    var NOTICE_FLAG: Long=1
    var ACCESS_FLAG:Long=1

    var RATE_FLAG:Long=1

    var isDebug=true


    var openAdmobMediation = true
    var showAdPlatform = LogAdParam.ad_platform_admob

    const val packageName = "com.mycleaner.phonecleantool"
    const val Singular_Api_Key = "mar2game_f7b9272a"
    const val Singular_Secret = "72b3df2ee5d0a64a6c404ce01937c3d6"

    const val ADVHost = "http://192.168.110.68:10002"
  // const val ADVHost = "https://api.cigars-of-cuba.top"
    const val CheckUrl = "$ADVHost/check"
    const val IPInfoUrl = "$ADVHost/getIpInfo"
    const val PushUrl = "$ADVHost/publish"


}