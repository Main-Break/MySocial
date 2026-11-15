package com.mysocial.data

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ViaCepService {
    @GET("ws/{cep}/json/")
    suspend fun buscarCep(@Path("cep") cep: String): Response<CepResponse>
}
