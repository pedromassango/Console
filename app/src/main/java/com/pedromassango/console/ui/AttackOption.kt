package com.pedromassango.console.ui

/**
 * Created by Pedro Massango on 2/23/18.
 */

/**
 * Adicione aqui mais tipos de ataques.
 * TODO: adicionar mais tipos de ataques na lista.
 */
enum class AttackOptions(type:String){ TERMINAL("Terminal"), DDOS("DDOS"), DOS("DOS")}

class AttackOption(val type: AttackOptions){

    var attackName: String = type.name
}
