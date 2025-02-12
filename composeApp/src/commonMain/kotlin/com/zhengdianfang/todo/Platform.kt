package com.zhengdianfang.todo

interface Platform {
   val name: String
}

expect fun  getPlatform(): Platform