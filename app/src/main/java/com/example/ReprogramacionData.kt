package com.example

data class ReprogramacionData(
    val expedienteNro: String = "",
    val nombreImputado: String = "",
    val delito: String = "",
    val perjudicado: String = "",
    val tipoAudienciaReprogramada: String = "",
    val nuevaFecha: String = "",
    val nuevaHora: String = "",
    val nombreJuez: String = "",
    val nombreSecretario: String = "",
    val articuloCpp: String = "",
    val motivoReprogramacion: String = ""
)
