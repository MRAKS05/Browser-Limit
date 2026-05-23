package com.example.ui.exceptions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.data.ExceptionsManager

@Composable
fun ExceptionsScreen() {
    val context = LocalContext.current
    val exceptionsManager = remember { ExceptionsManager(context) }
    val exceptions by exceptionsManager.exceptionsFlow.collectAsState()
    
    var newException by remember { mutableStateOf("") }
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Exceptions Manager", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 16.dp))
        
        OutlinedTextField(
            value = newException,
            onValueChange = { newException = it },
            label = { Text("Add Package Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = {
                Button(
                    onClick = { 
                        if (newException.isNotBlank()) {
                            exceptionsManager.addException(newException)
                            newException = ""
                        }
                    },
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Text("Add")
                }
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(exceptions) { packageName ->
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(packageName)
                        if (packageName != "com.aistudio.browserlimit.abxyz" && packageName != "com.example") {
                            IconButton(onClick = { exceptionsManager.removeException(packageName) }) {
                                Icon(Icons.Filled.Close, contentDescription = "Remove")
                            }
                        } else {
                            Text("(Permanent)", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}
