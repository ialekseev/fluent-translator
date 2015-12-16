package com.smartelk.translator

class TranslatorSettings(val clientId: String, val clientSecret: String, val client: RemoteServiceClient = new RemoteServiceClientImpl)
