
package uz.oqituvchi.hisobi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType

data class Student(
    val id: Long,
    val name: String,
    val phone: String,
    val monthlyFee: Long,
    val paid: Long
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("teacher_data", MODE_PRIVATE)

        setContent {
            var students by remember {
                mutableStateOf(
                    prefs.getStringSet("students", emptySet())!!
                        .mapNotNull {
                            val p = it.split("|")
                            if (p.size == 5)
                                Student(
                                    p[0].toLong(), p[1], p[2],
                                    p[3].toLong(), p[4].toLong()
                                )
                            else null
                        }
                )
            }

            var showAdd by remember { mutableStateOf(false) }

            fun save(list: List<Student>) {
                students = list
                prefs.edit().putStringSet(
                    "students",
                    list.map {
                        "${it.id}|${it.name}|${it.phone}|${it.monthlyFee}|${it.paid}"
                    }.toSet()
                ).apply()
            }

            MaterialTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("O‘QITUVCHI HISOBI") }
                        )
                    },
                    floatingActionButton = {
                        FloatingActionButton(onClick = {
                            showAdd = true
                        }) {
                            Text("+")
                        }
                    }
                ) { padding ->
                    Column(
                        Modifier.padding(padding).padding(16.dp)
                    ) {
                        val fee = students.sumOf { it.monthlyFee }
                        val paid = students.sumOf { it.paid }

                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Text("UMUMIY HISOB",
                                    fontWeight = FontWeight.Bold)
                                Text("O‘quvchilar: ${students.size}")
                                Text("Oylik to‘lov: $fee so‘m")
                                Text("To‘langan: $paid so‘m")
                                Text("Qarzdorlik: ${maxOf(0, fee-paid)} so‘m")
                                Text("Oldindan to‘lov: ${maxOf(0, paid-fee)} so‘m")
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        Text("O‘QUVCHILAR",
                            fontWeight = FontWeight.Bold)

                        LazyColumn {
                            items(students, key = { it.id }) { s ->
                                Card(
                                    Modifier.fillMaxWidth().padding(vertical = 5.dp)
                                ) {
                                    Column(Modifier.padding(12.dp)) {
                                        Text(s.name,
                                            fontWeight = FontWeight.Bold)
                                        Text("Telefon: ${s.phone}")
                                        Text("Oylik: ${s.monthlyFee} so‘m")
                                        Text("To‘langan: ${s.paid} so‘m")
                                        Text("Qoldiq: ${s.monthlyFee-s.paid} so‘m")

                                        Button(onClick = {
                                            save(students.filterNot {
                                                it.id == s.id
                                            })
                                        }) {
                                            Text("O‘chirish")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (showAdd) {
                    AddStudentDialog(
                        onDismiss = { showAdd = false },
                        onSave = { name, phone, fee ->
                            save(students + Student(
                                System.currentTimeMillis(),
                                name, phone, fee, 0
                            ))
                            showAdd = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStudentDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var fee by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yangi o‘quvchi") },
        text = {
            Column {
                OutlinedTextField(
                    name, { name = it },
                    label = { Text("Ism-familiya") }
                )
                OutlinedTextField(
                    phone, { phone = it },
                    label = { Text("Telefon") }
                )
                OutlinedTextField(
                    fee, { fee = it.filter(Char::isDigit) },
                    label = { Text("Oylik to‘lov") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )
            }
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank() && fee.toLongOrNull() != null,
                onClick = {
                    onSave(name, phone, fee.toLong())
                }
            ) {
                Text("Saqlash")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Bekor qilish")
            }
        }
    )
}
