package com.monitoring.app.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import com.monitoring.app.databinding.ActivityShellBinding
import java.io.File

class ShellActivity : BaseActivity() {
    private lateinit var viewBinding: ActivityShellBinding
    private var currentWorkingDirectory: String = "/"
    private var outputCursorHandler: Handler? = null
    private var outputCursorRunnable: Runnable? = null
    private var isOutputCursorVisible = true
    private var isCommandExecuting = false

    override fun initViewBinding() {
        viewBinding = ActivityShellBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initShell()
        setupClickListeners()
        startOutputCursorBlinking()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopOutputCursorBlinking()
    }

    private fun startOutputCursorBlinking() {
        outputCursorHandler = Handler(Looper.getMainLooper())
        outputCursorRunnable = object : Runnable {
            override fun run() {
                if (!isCommandExecuting) {
                    isOutputCursorVisible = !isOutputCursorVisible
                    viewBinding.tvOutputCursor.visibility = if (isOutputCursorVisible) View.VISIBLE else View.INVISIBLE
                }
                outputCursorHandler?.postDelayed(this, 500)
            }
        }
        outputCursorHandler?.post(outputCursorRunnable!!)
    }

    private fun stopOutputCursorBlinking() {
        outputCursorRunnable?.let { outputCursorHandler?.removeCallbacks(it) }
        outputCursorHandler = null
        outputCursorRunnable = null
    }

    private fun hideOutputCursor() {
        isCommandExecuting = true
        viewBinding.tvOutputCursor.visibility = View.INVISIBLE
    }

    private fun showOutputCursor() {
        isCommandExecuting = false
        isOutputCursorVisible = true
        viewBinding.tvOutputCursor.visibility = View.VISIBLE
    }

    private fun initShell() {
        getCurrentWorkingDirectory()
        updatePrompt()

        viewBinding.tvShellOutput.text = buildString {
            append("Android Shell v1.0\n")
            append("Type 'help' for available commands\n")
            append(getPrompt())
        }
    }

    private fun getCurrentWorkingDirectory() {
        try {
            val process = Runtime.getRuntime().exec("pwd")
            val output = process.inputStream.bufferedReader().readText().trim()
            if (output.isNotEmpty()) {
                currentWorkingDirectory = output
            }
            process.waitFor()
        } catch (e: Exception) {
            currentWorkingDirectory = System.getProperty("user.dir") ?: "/"
        }
    }

    private fun getPrompt(): String {
        val shortPath = getShortPath(currentWorkingDirectory)
        return "shell:$shortPath$ "
    }

    private fun getShortPath(fullPath: String): String {
        return when {
            fullPath == "/" -> "/"
            fullPath.startsWith("/storage/emulated/0") -> {
                fullPath.replace("/storage/emulated/0", "~")
            }
            fullPath.length > 30 -> {
                "...${fullPath.takeLast(25)}"
            }
            else -> fullPath
        }
    }

    private fun updatePrompt() {
        viewBinding.tvPrompt.text = getPrompt()
    }

    private fun setupClickListeners() {
        viewBinding.btnSend.setOnClickListener {
            executeCommand()
        }

        viewBinding.btnClear.setOnClickListener {
            clearShellOutput()
        }

        viewBinding.btnLs.setOnClickListener {
            executeShellCommand("ls -la")
        }

        viewBinding.btnPwd.setOnClickListener {
            executeShellCommand("pwd")
        }

        viewBinding.btnPs.setOnClickListener {
            executeShellCommand("ps")
        }

        viewBinding.btnTop.setOnClickListener {
            executeShellCommand("top -n 1")
        }

        viewBinding.btnDf.setOnClickListener {
            executeShellCommand("df -h")
        }

        viewBinding.btnNetstat.setOnClickListener {
            executeShellCommand("netstat -an")
        }

        viewBinding.btnLogcat.setOnClickListener {
            executeShellCommand("logcat -d | tail -20")
        }

        viewBinding.etShellInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND) {
                executeCommand()
                true
            } else {
                false
            }
        }

        viewBinding.etShellInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                viewBinding.tvCursor.visibility = View.INVISIBLE
            } else {
                viewBinding.tvCursor.visibility = View.VISIBLE
            }
        }
    }

    private fun executeCommand() {
        val command = viewBinding.etShellInput.text.toString().trim()
        if (command.isNotEmpty()) {
            executeShellCommand(command)
            viewBinding.etShellInput.text?.clear()
        }
    }

    private fun executeShellCommand(command: String) {
        hideOutputCursor()

        appendToOutput("${getPrompt()}$command\n")

        try {
            if (command.startsWith("cd ")) {
                handleCdCommand(command)
            } else {
                executeNormalCommand(command)
            }
        } catch (e: Exception) {
            appendToOutput("Error: ${e.message}\n")
        }

        if (command.startsWith("cd ")) {
            getCurrentWorkingDirectory()
            updatePrompt()
        }

        appendToOutput(getPrompt())
        scrollToBottom()

        showOutputCursor()
    }

    private fun handleCdCommand(command: String) {
        val path = command.substring(3).trim()

        try {
            val targetPath = when {
                path.isEmpty() || path == "~" -> "/storage/emulated/0"
                path.startsWith("/") -> path
                path == ".." -> File(currentWorkingDirectory).parent ?: "/"
                else -> File(currentWorkingDirectory, path).absolutePath
            }

            val targetDir = File(targetPath)
            if (targetDir.exists() && targetDir.isDirectory) {
                currentWorkingDirectory = targetDir.canonicalPath
                appendToOutput("")
            } else {
                appendToOutput("cd: $path: No such file or directory\n")
            }
        } catch (e: Exception) {
            appendToOutput("cd: $path: ${e.message}\n")
        }
    }

    private fun executeNormalCommand(command: String) {
        val processBuilder = ProcessBuilder()
        processBuilder.command("sh", "-c", command)
        processBuilder.directory(File(currentWorkingDirectory))

        val process = processBuilder.start()

        val output = process.inputStream.bufferedReader().readText()
        val error = process.errorStream.bufferedReader().readText()

        if (output.isNotEmpty()) {
            appendToOutput(output)
        }
        if (error.isNotEmpty()) {
            appendToOutput("Error: $error")
        }

        process.waitFor()
    }

    private fun appendToOutput(text: String) {
        viewBinding.tvShellOutput.append(text)
    }

    private fun clearShellOutput() {
        getCurrentWorkingDirectory()
        updatePrompt()
        viewBinding.tvShellOutput.text = buildString {
            append("Android Shell v1.0\n")
            append("Type 'help' for available commands\n")
            append(getPrompt())
        }
        scrollToBottom()
    }

    private fun scrollToBottom() {
        viewBinding.scrollViewOutput.post {
            viewBinding.scrollViewOutput.fullScroll(View.FOCUS_DOWN)
        }
    }
}
