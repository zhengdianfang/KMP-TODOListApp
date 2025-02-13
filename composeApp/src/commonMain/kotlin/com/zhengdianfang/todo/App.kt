package com.zhengdianfang.todo

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import com.zhengdianfang.todo.data.MongoDB
import com.zhengdianfang.todo.theme.AppTheme
import com.zhengdianfang.todo.ui.home.HomeScreen
import com.zhengdianfang.todo.ui.home.HomeViewModel
import com.zhengdianfang.todo.ui.task.TaskViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.mp.KoinPlatformTools

@Composable
@Preview
fun App() {
    initializeKoin()
    AppTheme {
        Navigator(HomeScreen())
    }
}

val mongoModule = module {
    single { MongoDB() }
    factory { HomeViewModel(get()) }
    factory { TaskViewModel(get()) }
}

fun initializeKoin() {
    val koin = KoinPlatformTools.defaultContext().getOrNull()
    if (koin == null) {
        startKoin {
            modules(mongoModule)
        }
    }
}