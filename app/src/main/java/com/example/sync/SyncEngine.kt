package com.example.sync

import android.content.Context
import android.net.wifi.WifiManager
import android.text.format.Formatter
import com.example.data.repository.GlanceRepository
import com.example.domain.model.GlanceItem
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket

data class SyncPayload(
    val version: String = "1.0",
    val deviceName: String = "iQOO Neo Device",
    val exportedAt: Long = System.currentTimeMillis(),
    val items: List<GlanceItem>
)

interface SyncEngine {
    val serverStatus: StateFlow<Boolean>
    val serverUrl: StateFlow<String?>
    suspend fun exportJson(items: List<GlanceItem>): String
    suspend fun importJson(json: String): List<GlanceItem>
    fun startLocalServer(repository: GlanceRepository)
    fun stopLocalServer()
}

class DefaultSyncEngine(private val context: Context) : SyncEngine {

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val payloadAdapter = moshi.adapter(SyncPayload::class.java)

    private val _serverStatus = MutableStateFlow(false)
    override val serverStatus: StateFlow<Boolean> = _serverStatus

    private val _serverUrl = MutableStateFlow<String?>(null)
    override val serverUrl: StateFlow<String?> = _serverUrl

    private var serverSocket: ServerSocket? = null
    private var serverJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    override suspend fun exportJson(items: List<GlanceItem>): String {
        return payloadAdapter.indent("  ").toJson(SyncPayload(items = items))
    }

    override suspend fun importJson(json: String): List<GlanceItem> {
        return try {
            val payload = payloadAdapter.fromJson(json)
            payload?.items ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun startLocalServer(repository: GlanceRepository) {
        if (_serverStatus.value) return

        serverJob = scope.launch {
            try {
                val port = 8080
                serverSocket = ServerSocket(port)
                val ip = getIpAddress()
                _serverUrl.value = "http://$ip:$port"
                _serverStatus.value = true

                while (_serverStatus.value && !serverSocket!!.isClosed) {
                    val clientSocket = serverSocket!!.accept()
                    handleClient(clientSocket, repository)
                }
            } catch (e: Exception) {
                _serverStatus.value = false
                _serverUrl.value = null
            }
        }
    }

    override fun stopLocalServer() {
        try {
            _serverStatus.value = false
            _serverUrl.value = null
            serverSocket?.close()
            serverJob?.cancel()
        } catch (e: Exception) {
            // Ignored
        }
    }

    @Suppress("DEPRECATION")
    private fun getIpAddress(): String {
        return try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            val ipInt = wifiManager?.connectionInfo?.ipAddress ?: 0
            if (ipInt != 0) {
                Formatter.formatIpAddress(ipInt)
            } else {
                "localhost"
            }
        } catch (e: Exception) {
            "localhost"
        }
    }

    private fun handleClient(socket: Socket, repository: GlanceRepository) {
        scope.launch {
            try {
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                val out = PrintWriter(socket.getOutputStream(), true)

                val requestLine = reader.readLine() ?: ""
                val isApiCall = requestLine.contains("/api/glances")

                val items = repository.getAllGlancesSnapshot()
                val json = exportJson(items)

                if (isApiCall) {
                    out.print("HTTP/1.1 200 OK\r\n")
                    out.print("Content-Type: application/json; charset=utf-8\r\n")
                    out.print("Access-Control-Allow-Origin: *\r\n")
                    out.print("Content-Length: ${json.toByteArray().size}\r\n\r\n")
                    out.print(json)
                    out.flush()
                } else {
                    // Serve Laptop Companion Web App
                    val html = buildLaptopCompanionHtml(items)
                    val htmlBytes = html.toByteArray()
                    out.print("HTTP/1.1 200 OK\r\n")
                    out.print("Content-Type: text/html; charset=utf-8\r\n")
                    out.print("Content-Length: ${htmlBytes.size}\r\n\r\n")
                    out.print(html)
                    out.flush()
                }
                socket.close()
            } catch (e: Exception) {
                try { socket.close() } catch (ignored: Exception) {}
            }
        }
    }

    private fun buildLaptopCompanionHtml(items: List<GlanceItem>): String {
        val cardsHtml = if (items.isEmpty()) {
            """<div class="empty">No Glance cards synced yet. Scan notes on your iQOO device to populate!</div>"""
        } else {
            items.joinToString("\n") { item ->
                val priorityClass = when (item.priority.name) {
                    "HIGH" -> "p-high"
                    "MEDIUM" -> "p-med"
                    else -> "p-low"
                }
                val taskListHtml = item.tasks.joinToString("") { t ->
                    """<li><input type="checkbox" ${if (t.isCompleted) "checked" else ""}> <span>${t.title}</span></li>"""
                }
                val tagsHtml = item.tags.joinToString(" ") { """<span class="tag">$it</span>""" }

                """
                <div class="card $priorityClass">
                    <div class="card-header">
                        <span class="badge ${item.category.name}">${item.category.label}</span>
                        <span class="priority-pill">${item.priority.label}</span>
                    </div>
                    <h3>${item.title}</h3>
                    ${if (!item.deadline.isNullOrBlank()) """<div class="deadline">📅 Due: ${item.deadline}</div>""" else ""}
                    <ul class="tasks">$taskListHtml</ul>
                    <div class="tags-container">$tagsHtml</div>
                </div>
                """
            }
        }

        return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>GlanceFlow • Laptop Companion</title>
            <style>
                * { box-sizing: border-box; margin: 0; padding: 0; }
                body { background: #070A11; color: #F1F5F9; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; padding: 28px; }
                header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #1E293B; padding-bottom: 20px; margin-bottom: 28px; }
                .logo { font-size: 26px; font-weight: 800; background: linear-gradient(90deg, #00F2FE, #FF6D00); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
                .sync-badge { background: rgba(0,242,254,0.15); color: #00F2FE; border: 1px solid #00F2FE; padding: 6px 14px; border-radius: 20px; font-size: 13px; font-weight: 600; }
                .grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 20px; }
                .card { background: #0F1728; border: 1px solid #233252; border-radius: 14px; padding: 20px; transition: transform 0.2s; }
                .card:hover { transform: translateY(-3px); border-color: #00F2FE; }
                .card.p-high { border-left: 4px solid #FF6D00; }
                .card.p-med { border-left: 4px solid #FFD600; }
                .card.p-low { border-left: 4px solid #00F2FE; }
                .card-header { display: flex; justify-content: space-between; margin-bottom: 12px; }
                .badge { font-size: 11px; font-weight: 700; text-transform: uppercase; padding: 4px 8px; border-radius: 6px; background: #172036; color: #94A3B8; }
                .priority-pill { font-size: 11px; font-weight: 800; color: #FF6D00; }
                h3 { font-size: 18px; margin-bottom: 8px; }
                .deadline { color: #00F2FE; font-size: 13px; margin-bottom: 12px; font-weight: 500; }
                .tasks { list-style: none; margin-bottom: 14px; }
                .tasks li { display: flex; align-items: center; gap: 8px; font-size: 14px; color: #CBD5E1; margin-bottom: 6px; }
                .tag { font-size: 11px; background: rgba(255,255,255,0.06); padding: 3px 8px; border-radius: 4px; color: #94A3B8; margin-right: 4px; }
                .empty { text-align: center; grid-column: 1/-1; padding: 60px; color: #64748B; font-size: 18px; }
            </style>
        </head>
        <body>
            <header>
                <div>
                    <div class="logo">⚡ GlanceFlow Desktop Companion</div>
                    <p style="color: #64748B; font-size: 14px; margin-top: 4px;">Local Wireless Synchronization • iQOO Ambient Intelligence</p>
                </div>
                <div class="sync-badge">● LIVE LOCAL SYNC (${items.size} Cards)</div>
            </header>
            <main class="grid">
                $cardsHtml
            </main>
        </body>
        </html>
        """.trimIndent()
    }
}
