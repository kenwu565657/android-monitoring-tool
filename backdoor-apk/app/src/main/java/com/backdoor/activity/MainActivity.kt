package com.backdoor.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.backdoor.ui.theme.BackdoorApkTheme
import com.jaredrummler.ktsh.Shell
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BackdoorApkTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
        runHelloWorld()
        runHelloWorld()
    }

    private fun runHelloWorld() {
        val shell = Shell("sh")                         // create a shell
        val result = shell.run("echo 'Hello, World!'")  // execute a command
        if (result.isSuccess) {                         // check if the exit-code was 0
            println(result.stdout())                    // prints "Hello, World!"
        }

        val rootResult = shell.run("su root")
        if (rootResult.isSuccess) {
            println("Switched to root user successfully.")
        } else {
            println("Failed to switch to root user: ${rootResult.stderr()}")
        }

        val lsResult = shell.run("ls")
        if (lsResult.isSuccess) {
            println("Files in current directory:")
            lsResult.stdout().forEach { println(it) }  // prints the files in the current directory
        } else {
            println("Error: ${lsResult.stderr()}")     // prints any error messages
        }
    }

    fun runShellCommand() {
        try {
            //在系統裡，一般應用程式受到UID的限制，僅能查看屬於自己的Log
            //若想蒐集完整的Log必須先使用su指令切換至root身份
            val process: Process = Runtime.getRuntime().exec("su");

            //透過Outputstream將指令輸出至Shell執行
            val dos: DataOutputStream = DataOutputStream(process.outputStream);

            //將欲執行的指令寫入OutPutStream
            //write方法會先將資料暫存於Buffer(緩衝區)裡面
            //"\n"是換行，相當於在Shell按下Enter讓指令執行
            dos.writeBytes("logcat -v time -d |grep 'android.intent.action.'\n");

            //透過getInputstream接收Shell回傳的資料，並放入BufferedReader
            val bufferedReader: BufferedReader = BufferedReader( InputStreamReader(process.inputStream));

            //離開root身份
            dos.writeBytes("exit\n");

            //flush這個方法會強制將Stream的Buffer(緩衝區)裡暫存的資料發送出去
            //在這裡的意思就是開始執行上述寫好的指令
            dos.flush();

            val mylog = StringBuilder()
            var line: String
            while ((bufferedReader.readLine().also { line = it }) != null) {
                mylog.append(line)
                mylog.append("\n")
            }

            val splitlog: Array<String> = mylog.toString().split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        } catch (e: IOException) {
            e.printStackTrace();
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BackdoorApkTheme {
        Greeting("Android")
    }
}