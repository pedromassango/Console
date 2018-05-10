package com.pedromassango.console.ui.console

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Patterns
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.TextView
import com.pedromassango.console.BuildConfig
import com.pedromassango.console.R
import kotlinx.android.synthetic.main.activity_console.*
import java.io.IOException
import java.io.PrintWriter
import java.net.Socket
import java.util.*

class TerminalActivity : AppCompatActivity(), TextView.OnEditorActionListener {

    // Status das mensagens retornadas pelo servidor
    private val GOOD_INFO = "GREEN"
    private val BAD_INFO = "RED"

    // Porta para se conecta [Deve ser a mesma que a do servidor]
    private val DEFAULT_CONNECTION_PORT = 5123

    lateinit var layout: LinearLayout
    private var writer: PrintWriter? = null
    // ponte entre o servidor e o app, para envio e recepcão de comandos
    private var socket: Socket? = null

    // [START] comandos validos
    private val CONNECT_COMMAND = ".connect/"
    private val GET_PROPERTY_COMMAND = ".getProperty/"
    private val EXEC_COMMAND = ".exec/"
    private val CHECK_IP_PORT_COMMAND = ".check.ipport/"
    private val CD_COMMAND = ".cd/"
    private val LS_COMMAND = ".ls"
    private val CLEAR_COMMAND = ".clear"
    private val ABOUT_COMMAND = ".about"
    private val SHUTDOWN_COMMAND = ".shutdown/"
    private val SHUTDOWN_DEFAULT_COMMAND = ".shutdown/shutdown -s"
    // [END] comandos validos.

    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_console)

        // O layout para adicionar a mensagens recebidas ou mensagens de erro
        layout = findViewById<LinearLayout>(R.id.view_log)

        // Aidiciona um escutador de clicks no EditText
        edt_commands.setOnEditorActionListener(this)

    }

    // retorna o texto digitado
    private fun geTextTyped(): String = edt_commands.text.toString()

    // Gera um TextView para se mostrado na tela
    private fun getTextView(): TextView {

        val textView = TextView(this)
        textView.setTextColor(Color.WHITE)
        return textView
    }

    //[START] Mostram mensagens simples na tela
    private fun showTitle(title: String) {

        layout.post {
            val tvLog = getTextView()
            tvLog.text = String.format("\n\n%s\n", title)
            layout.addView(tvLog)
        }
    }

    private fun showMessage(message: String) {

        layout.post {
            val tvLog = getTextView()
            tvLog.text = String.format("%s", message)
            layout.addView(tvLog)
        }
    }

    private fun showMessage(title: String, color: Int) {

        layout.post {
            val tvLog = getTextView()
            tvLog.setTextColor(color)

            tvLog.text = String.format("%s", title)
            layout.addView(tvLog)
        }
    }
    //[END] MOstram mensagens simples na tela


    // Metodo chamado quando clicado eme enviar comando pelo teclado
    // aqui, verificamos se o texto digitado é um comando valido, e som assim enviamos.
    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_SEND) {

            val typed = geTextTyped()
            edt_commands.setText("")

            if (typed.isEmpty()) {
                showMessage("Digite um comando", Color.RED)
                return true
            }

            showTitle(typed)

            // se o texto nao inicia com um . (ponto), é um comando invalido
            if (!typed.startsWith(".")) {
                showMessage("Comando inválido", Color.RED)
                return true
            }

            // limpa os comandos digitados da tela
            if (typed.startsWith(CLEAR_COMMAND)) {
                layout.removeAllViews()
                return true
            }

            if (typed.startsWith(ABOUT_COMMAND)) {

                showMessage("********************", Color.RED)
                showMessage("Por: Pedro Massango")
                showMessage("Nome: " + getString(R.string.app_name))
                showMessage("Versão: " + BuildConfig.VERSION_NAME)
                showMessage("********************", Color.RED)
                return true
            }

            if (!typed.contains("/")) {
                showMessage("Comando inválido", Color.RED)
                return true
            }

            //Command to connect
            if (typed.startsWith(CONNECT_COMMAND)) {
                val IP = getCommand(typed)

                if (!Patterns.IP_ADDRESS.matcher(IP).matches()) {
                    showMessage("Endereço IP inválido", Color.RED)
                    return true
                }

                connectToIP(IP)
                return true
            }

            //Checking an open port from an IP
            if (typed.startsWith(CHECK_IP_PORT_COMMAND)) {
                val IP = typed.substring(typed.indexOf("/") + 1, typed.indexOf(":"))

                if (!Patterns.IP_ADDRESS.matcher(IP).matches()) {
                    showMessage("Endereço IP inválido", Color.RED)
                    return true
                }

                //Try to convert String port to int.
                try {
                    val portString = typed.substring(typed.indexOf(":") + 1, typed.length)
                    val port = Integer.parseInt(portString)

                    checkOpenPortOnIp(IP, port)

                } catch (e: Exception) {
                    showMessage("Digite uma porta válida", Color.RED)
                }

                return true
            }

            //Check if we are connected, before send an commando to server
            if (!isConnected()) {
                showMessage("Não conectado a algum IP, execute: '" + CONNECT_COMMAND + "ip_address' para se conectar", Color.RED)
                return true
            }

            //Commands to execute when connected to server (to an IP)
            if (typed.startsWith(GET_PROPERTY_COMMAND)
                    || typed.startsWith(EXEC_COMMAND)
                    || typed.startsWith(CD_COMMAND)
                    || typed.startsWith(SHUTDOWN_COMMAND)
                    || typed.startsWith(SHUTDOWN_DEFAULT_COMMAND)
                    || typed.startsWith(LS_COMMAND)) {

                sendCommand(typed)
                return true
            }

        }
        return false
    }

    /**
     * Verifica se o app esta connectado a um servidor alvo.
     * @return true se o app se conectou com sucesso a um servidor alvo
     */
    private fun isConnected(): Boolean {
        return null != socket && socket!!.isConnected
    }

    /**
     * Metodo responsavel a conectar a um alvo pelo IP
     * @param ip o ip do computador alvo.
     */
    private fun connectToIP(ip: String) {
        showMessage("Connectando a: " + ip)

        Thread(Runnable {
            try {
                //TODO: connectado com o alvo
                socket = Socket(ip, DEFAULT_CONNECTION_PORT)
                writer = PrintWriter(socket!!.getOutputStream())
                val reader = Scanner(socket!!.getInputStream())

                //Start the Listener of Server message
                ServerMessageReceiveListener(reader)
                        .execute()

                showMessage("Conexão sucedida com: " + ip, Color.GREEN)
                showMessage("Acesso garantido", Color.GREEN)

            } catch (e: Exception) {
                showMessage("Falha na connexão: \n " + e.message, Color.RED)
            }
        }).start()
    }

    /**
     * Metodo que verifica se uma determinada porta esta aberta em um
     * determinado IP
     * @param ip o ip alvo para verificar determinada porta
     * @param port a porta a ser verificada
     */
    private fun checkOpenPortOnIp(ip: String, port: Int) {
        showMessage("Verificando: $ip na porta: $port")

        Thread(Runnable {
            try {
                if (isConnected()) {
                    socket!!.close()
                }

                socket = Socket(ip, port)

                var result = ""

                if (socket!!.isClosed) {
                    result = String.format("Porta %s FECHADA no IP -> %s", port, ip)
                    showMessage(result, Color.RED)
                    return@Runnable
                }

                result = String.format("Porta %s ABERTA no IP -> %s", port, ip)
                showMessage(result, Color.GREEN)

            } catch (e: IOException) {
                showMessage(e.message!!, Color.RED)
            }
        }).start()

    }

    /**
     * Metodo responsavel a enviar comandos no servidor alvo.
     * @param command o comando a ser enviado.
     */
    private fun sendCommand(command: String) {
        Thread(Runnable {
            try {
                writer!!.println(command)
                writer!!.flush()
            }catch (e: Exception){
             e.printStackTrace()
                showMessage("Sem conexão.", Color.RED)
            }
        }).start()
    }

    /*
        Simplesmente formata o texto recebido e transforma em um comando valido.
     */
    private fun getCommand(command: String): String {
        return command.substring(command.indexOf("/") + 1, command.length)
    }

    /**
     * Esta classe é a resposavel por receber comandos enviados pelo
     * servidor rodando no alvo.
     * @param reader o scanner de onde vem as mensages.
     */
    @SuppressLint("StaticFieldLeak")
    private inner class ServerMessageReceiveListener(private val reader: Scanner) : AsyncTask<Void, Void, String>() {

        override fun doInBackground(vararg params: Void): String? {

            while (reader.hasNextLine()) {
                //val message = reader.nextLine()
                val message = reader.next()
                if (message != null) {
                    return message
                }
            }
            return null
        }

        override fun onPostExecute(result: String?) {
            var message = result ?: ""
            super.onPostExecute(result)

            var colorInt = 0

            if (message.contains(BAD_INFO)) {
                colorInt = Color.RED
                message = message.replace(BAD_INFO, "")

            } else if (message.contains(GOOD_INFO)) {
                colorInt = Color.GREEN
                message = message.replace(GOOD_INFO, "")
            }

            showMessage(message, colorInt)
        }
    }
}
