package com.vlog.my.data.api

import com.vlog.my.data.model.Messages
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 消息服务接口
 */
interface MessageService {
    /**
     * 获取消息列表
     * @param name 用户名
     * @param token 用户令牌
     * @return 消息列表
     */
    @GET("{name}/messages")
    suspend fun getMessages(
        @Path("name") name: String,
        @Query("token") token: String
    ): ApiResponse<List<Messages>>

    /**
     * 发送消息
     * @param name 用户名
     * @param token 用户令牌
     * @param recipientBy 接收者
     * @param subject 主题
     * @param description 内容
     * @return 响应结果
     */
    @POST("{name}/messages-composed")
    suspend fun sendMessage(
        @Path("name") name: String,
        @Query("token") token: String,
        @Query("recipientBy") recipientBy: String,
        @Query("subject") subject: String,
        @Query("description") description: String
    ): ApiResponse<Any>

    /**
     * 获取消息详情
     * @param name 用户名
     * @param id 消息ID
     * @param token 用户令牌
     * @return 消息详情
     */
    @GET("{name}/messages/{id}")
    suspend fun getMessageDetail(
        @Path("name") name: String,
        @Path("id") id: String,
        @Query("token") token: String
    ): ApiResponse<Messages>

    /**
     * 回复消息
     * @param name 用户名
     * @param id 消息ID
     * @param token 用户令牌
     * @param message 回复内容
     * @return 响应结果
     */
    @POST("{name}/messages/reply/{id}")
    suspend fun replyMessage(
        @Path("name") name: String,
        @Path("id") id: String,
        @Query("token") token: String,
        @Body message: Messages
    ): ApiResponse<Any>
}
