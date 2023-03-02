package com.github.minimi.amplifi

class AmplifiClientException(override val message: String? = null, override val cause: Throwable? = null) :
    RuntimeException(message, cause)