package com.monitoring.app

import android.Manifest
import android.os.Bundle
import android.os.PowerManager
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.jaredrummler.ktsh.Shell


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        reboot()
        // runHelloWorld()
    }

    @RequiresPermission(Manifest.permission.REBOOT)
    private fun reboot() {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        pm.reboot(null)
    }

    private fun runHelloWorld() {
        val shell = Shell.SU                         // create a shell
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
}