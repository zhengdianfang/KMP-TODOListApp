package com.zhengdianfang.todo.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.HorizontalAlignmentLine
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.zhengdianfang.todo.domain.RequestState
import com.zhengdianfang.todo.domain.ToDoTask
import com.zhengdianfang.todo.ui.components.ErrorScreen
import com.zhengdianfang.todo.ui.components.LoadingScreen
import com.zhengdianfang.todo.ui.components.TaskView
import com.zhengdianfang.todo.ui.task.TaskScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

class HomeScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val homeViewModel = koinScreenModel<HomeViewModel>()
        val activeTasks by homeViewModel.activeTasks
        val completeTasks by homeViewModel.completeTasks
        val navigator = LocalNavigator.currentOrThrow
        var showDialog by remember { mutableStateOf(false) }
        var deleteTask: ToDoTask? by remember { mutableStateOf(null) }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(title = { Text(text = "Home") })
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navigator.push(TaskScreen()) },
                    shape = RoundedCornerShape(size = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Icon",
                    )
                }
            }
        ) { padding ->
            if (showDialog) {
                BasicAlertDialog(
                    onDismissRequest = {
                        showDialog = false
                        deleteTask = null
                    }
                ) {
                    Surface(
                        modifier = Modifier.wrapContentWidth().wrapContentHeight(),
                        shape = MaterialTheme.shapes.large,
                        tonalElevation = 10.dp,
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text =
                                "This area typically contains the supportive text " +
                                        "which presents the details regarding the Dialog's purpose.",
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                            ) {
                                TextButton(
                                    onClick = {
                                        showDialog = false
                                        deleteTask?.let { willDelete ->
                                            homeViewModel.deleteTask(willDelete)
                                        }
                                    }
                                ) {
                                    Text("Confirm")
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                TextButton(onClick = { showDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        }
                    }
                }
            }
            Column(
                modifier = Modifier.fillMaxSize().padding(top = 24.dp).padding(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding()
                )
            ) {
                DisplayTasks(
                    modifier = Modifier.weight(1f),
                    tasks = activeTasks,
                    onSelect = { task -> navigator.push(TaskScreen(task)) },
                    onComplete = { task, complete ->
                        homeViewModel.setTaskCompleted(
                            task,
                            complete
                        )
                    },
                    onFavorite = { task, favorite ->
                        homeViewModel.setTaskFavorite(
                            task,
                            favorite
                        )
                    },
                )
                Spacer(modifier = Modifier.height(24.dp))
                DisplayTasks(
                    modifier = Modifier.weight(1f),
                    tasks = completeTasks,
                    onSelect = { task -> navigator.push(TaskScreen(task)) },
                    onComplete = { task, complete ->
                        homeViewModel.setTaskCompleted(
                            task,
                            complete
                        )
                    },
                    onFavorite = { task, favorite ->
                        homeViewModel.setTaskFavorite(
                            task,
                            favorite
                        )
                    },
                    onDelete = { task ->
                        showDialog = true
                        deleteTask = task
                    },
                    showActive = false,
                )
            }
        }
    }
}

@Composable
fun DisplayTasks(
    modifier: Modifier = Modifier,
    tasks: RequestState<List<ToDoTask>>,
    showActive: Boolean = true,
    onSelect: ((ToDoTask) -> Unit)? = null,
    onFavorite: ((ToDoTask, Boolean) -> Unit)? = null,
    onComplete: ((ToDoTask, Boolean) -> Unit)? = null,
    onDelete: ((ToDoTask) -> Unit)? = null,
) {


    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier.padding(horizontal = 12.dp),
            text = if (showActive) "Active Tasks" else "Completed Tasks",
            fontSize = MaterialTheme.typography.titleMedium.fontSize,
            fontWeight = FontWeight.Medium,
        )
        Spacer(modifier = Modifier.height(12.dp))
        tasks.DisplayResult(
            onLoading = { LoadingScreen() },
            onIdle = {},
            onError = { ErrorScreen(message = it) },
            onSuccess = {
                if (it.isNotEmpty()) {
                    LazyColumn(modifier = Modifier.padding(horizontal = 24.dp)) {
                        items(
                            items = it,
                            key = { task -> task._id.toHexString() }
                        ) { task ->
                            TaskView(
                                task = task,
                                showActive = showActive,
                                onSelect = { selectTask -> onSelect?.invoke(selectTask) },
                                onFavorite = { selectTask, favorite ->
                                    onFavorite?.invoke(
                                        selectTask,
                                        favorite
                                    )
                                },
                                onComplete = { selectTask, complete ->
                                    onComplete?.invoke(
                                        selectTask,
                                        complete
                                    )
                                },
                                onDelete = { selectTask ->
                                    onDelete?.invoke(selectTask)
                                }
                            )
                        }
                    }
                } else {
                    ErrorScreen()
                }
            }
        )
    }
}
