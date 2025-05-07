package com.vlog.my.data.api

import com.vlog.my.data.api.subscript.SubScriptResponse
import com.vlog.my.data.api.subscript.SubScriptStatusResponse
import com.vlog.my.data.model.SubScripts
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SubScriptService {
    @GET("subscripts/list")
    suspend fun getSubScriptsList(
        @Query("token") token: String
    ): SubScriptResponse<List<SubScripts>>

    @POST("subscripts-created")
    suspend fun createSubScript(
        @Query("token") token: String,
        @Body subScript: SubScripts
    ): SubScriptStatusResponse
}
