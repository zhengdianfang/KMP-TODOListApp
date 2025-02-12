package com.zhengdianfang.todo.data

import com.zhengdianfang.todo.domain.RequestState
import com.zhengdianfang.todo.domain.ToDoTask
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.delete
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class MongoDB {
    private var realm: Realm? = null

    init {
        configureRealm()
    }

    private fun configureRealm() {
        if (realm == null || realm!!.isClosed()) {
            val config = RealmConfiguration.Builder(
                schema = setOf(ToDoTask::class)
            ).compactOnLaunch()
                .build()
            realm = Realm.open(config)
        }
    }

    fun readActiveTasks(): Flow<RequestState<List<ToDoTask>>> {
        return realm?.query<ToDoTask>(query = "completed == $0", false)
            ?.asFlow()
            ?.map { result ->
                RequestState.Success(
                    data = result.list.sortedByDescending { task -> task.favorite }
                )
            } ?: flow { RequestState.Error(message = "Realm is not available") }
    }

    fun readCompleteTasks(): Flow<RequestState<List<ToDoTask>>> {
        return realm?.query<ToDoTask>(query = "completed == $0", true)
            ?.asFlow()
            ?.map { result ->
                RequestState.Success(
                    data = result.list.sortedByDescending { task -> task.favorite }
                )
            } ?: flow { RequestState.Error(message = "Realm is not available") }
    }

    suspend fun addTask(task: ToDoTask) {
        realm?.write { copyToRealm(task) }
    }

    suspend fun updateTask(task: ToDoTask) {
        try {
            realm?.query<ToDoTask>(query = "_id == $0", task._id)
                ?.find()
                ?.first()
                ?.also {
                    realm?.write {
                        findLatest(it)?.apply{
                            this.title = task.title
                            this.description = task.description
                        }
                    }
                }
        } catch (e: Exception) {
            println(e)
        }
    }

    suspend fun setFavorite(task: ToDoTask, favorite: Boolean) {
        realm?.query<ToDoTask>(query = "_id == $0", task._id)
            ?.find()
            ?.first()
            ?.also {
                realm?.write {
                    findLatest(it)?.apply {
                        this.favorite = favorite
                    }
                }
            }
    }

    suspend fun setComplete(task: ToDoTask, completed: Boolean) {
        realm?.query<ToDoTask>(query = "_id == $0", task._id)
            ?.find()
            ?.first()
            ?.also {
                realm?.write {
                    findLatest(it)?.apply {
                        this.completed = completed
                    }
                }
            }
    }

    suspend fun deleteTask(task: ToDoTask) {
        try {
            realm?.query<ToDoTask>(query = "_id == $0", task._id)
                ?.find()
                ?.first()
                ?.also {
                    realm?.write {
                        findLatest(it)?.also { deleteOne -> delete(deleteOne) }
                    }
                }
        } catch (e: Exception) {
            println(e)
        }
    }
}