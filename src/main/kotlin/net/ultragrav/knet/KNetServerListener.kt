package net.ultragrav.knet

interface KNetServerListener {
    fun onConnect(connection: ServerConnection)
    fun onDisconnect(connection: ServerConnection)
}