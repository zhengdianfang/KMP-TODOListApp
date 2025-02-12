package com.zhengdianfang.todo.ui.task

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.zhengdianfang.todo.domain.ToDoTask
import io.realm.kotlin.ext.copyFromRealm

@OptIn(ExperimentalMaterial3Api::class)
class TaskScreen(val task: ToDoTask? = null) : Screen {

    @Composable
    override fun Content() {
        val taskViewModel = koinScreenModel<TaskViewModel>()
        val navigator = LocalNavigator.currentOrThrow
        var title by remember { mutableStateOf(task?.title ?: "") }
        var description by remember { mutableStateOf(task?.description ?: "") }
        var titleFieldFocus by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        BasicTextField(
                            value = title,
                            onValueChange = { text -> title = text },
                            modifier = Modifier.onFocusChanged { titleFieldFocus = it.isFocused },
                            textStyle = TextStyle(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = MaterialTheme.typography.titleLarge.fontSize,
                            ),
                            decorationBox = { innerTextField ->
                                if (title.isNotEmpty() || titleFieldFocus) {
                                    innerTextField()
                                } else {
                                    Text(
                                        "Enter Task Title",
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = MaterialTheme.typography.titleLarge.fontSize
                                    )
                                }
                            }
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { navigator.pop() }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = "Back Icon"
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                if (title.isNotEmpty() && description.isNotEmpty()) {
                    FloatingActionButton(
                        onClick = {
                            if (task == null) {
                                taskViewModel.addTask(ToDoTask().apply {
                                    this.title = title
                                    this.description = description
                                })
                            } else {
                                taskViewModel.updateTask(task.copyFromRealm().apply {
                                    this.title = title
                                    this.description = description
                                })
                            }
                            navigator.pop()
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Check Icon",
                        )
                    }
                }
            }
        ) { padding ->
            BasicTextField(
                modifier = Modifier.fillMaxSize().padding(all = 24.dp).padding(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding()
                ),
                value = description,
                onValueChange = { text -> description = text },
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = MaterialTheme.typography.titleMedium.fontSize,
                )
            )
        }
    }
}