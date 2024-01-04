package net.ultragrav.knet.proxy

class NoSuchProxyException(name: String) : IllegalStateException("No proxy registered for $name")