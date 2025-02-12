package com.zhengdianfang.todo.ui.home

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.zhengdianfang.todo.data.MongoDB
import com.zhengdianfang.todo.domain.RequestState
import com.zhengdianfang.todo.domain.ToDoTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

typealias MutableTasks = MutableState<RequestState<List<ToDoTask>>>
typealias Tasks = State<RequestState<List<ToDoTask>>>

class HomeViewModel(private val mongoDB: MongoDB) : ScreenModel {
    private var _activeTasks: MutableTasks = mutableStateOf(RequestState.Idle)
    val activeTasks: Tasks = _activeTasks

    private var _completeTasks: MutableTasks = mutableStateOf(RequestState.Idle)
    val completeTasks: Tasks = _completeTasks

    init {
        _activeTasks.value = RequestState.Loading
        _completeTasks.value = RequestState.Loading

        screenModelScope.launch(Dispatchers.Main) {
            delay(500)
            mongoDB.readActiveTasks().collectLatest {
                _activeTasks.value = it
            }
        }

        screenModelScope.launch(Dispatchers.Main) {
            delay(500)
            mongoDB.readCompleteTasks().collectLatest {
                _completeTasks.value = it
            }
        }
    }

    fun setTaskFavorite(task: ToDoTask, isFavorite: Boolean) {
        screenModelScope.launch(Dispatchers.IO) {
            mongoDB.setFavorite(task, isFavorite)
        }
    }

    fun setTaskCompleted(task: ToDoTask, isCompleted: Boolean) {
        screenModelScope.launch(Dispatchers.IO) {
            mongoDB.setComplete(task, isCompleted)
        }
    }

    fun deleteTask(task: ToDoTask) {
        screenModelScope.launch(Dispatchers.IO) {
            mongoDB.deleteTask(task)
        }
    }
}