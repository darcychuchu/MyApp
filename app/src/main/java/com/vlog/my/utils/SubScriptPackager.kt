package com.vlog.my.utils

import android.util.Base64
import com.vlog.my.data.model.SubScriptPackage
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * 小程序包装和加密工具
 * 用于加密和解密小程序包
 */
object SubScriptPackager {
    private val json = Json { 
        prettyPrint = true 
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    /**
     * 打包并加密小程序包
     * @param subScriptPackage 小程序包
     * @param recipientId 接收者ID
     * @return 加密后的字符串
     */
    fun packAndEncrypt(
        subScriptPackage: SubScriptPackage, 
        recipientId: String
    ): String {
        // 1. 序列化为JSON
        val jsonData = json.encodeToString(subScriptPackage)
        
        // 2. 使用接收者ID作为密钥的一部分进行加密
        val encryptionKey = generateEncryptionKey(recipientId)
        return encrypt(jsonData, encryptionKey)
    }
    
    /**
     * 解密并解包小程序包
     * @param encryptedData 加密的数据
     * @param userId 用户ID
     * @return 解密后的小程序包
     */
    fun decryptAndUnpack(
        encryptedData: String, 
        userId: String
    ): Result<SubScriptPackage> {
        return try {
            // 1. 使用用户ID生成解密密钥
            val decryptionKey = generateEncryptionKey(userId)
            
            // 2. 解密
            val jsonData = decrypt(encryptedData, decryptionKey)
            
            // 3. 反序列化
            val subScriptPackage = json.decodeFromString<SubScriptPackage>(jsonData)
            Result.success(subScriptPackage)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 生成加密密钥
     * @param userId 用户ID
     * @return 密钥
     */
    private fun generateEncryptionKey(userId: String): SecretKey {
        // 使用SHA-256哈希用户ID作为密钥
        val digest = MessageDigest.getInstance("SHA-256")
        val keyBytes = digest.digest(userId.toByteArray())
        return SecretKeySpec(keyBytes, "AES")
    }
    
    /**
     * 加密数据
     * @param data 要加密的数据
     * @param key 密钥
     * @return 加密后的Base64字符串
     */
    private fun encrypt(data: String, key: SecretKey): String {
        try {
            // AES/GCM/NoPadding是一种安全的加密模式
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(data.toByteArray())
            
            // 将IV和加密数据合并并Base64编码
            val combined = ByteArray(iv.size + encryptedBytes.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)
            return Base64.encodeToString(combined, Base64.DEFAULT)
        } catch (e: Exception) {
            throw RuntimeException("加密失败: ${e.message}", e)
        }
    }
    
    /**
     * 解密数据
     * @param encryptedData 加密的Base64字符串
     * @param key 密钥
     * @return 解密后的字符串
     */
    private fun decrypt(encryptedData: String, key: SecretKey): String {
        try {
            // Base64解码
            val combined = Base64.decode(encryptedData, Base64.DEFAULT)
            
            // 提取IV和加密数据
            val iv = combined.copyOfRange(0, 12)  // GCM模式IV长度为12字节
            val encryptedBytes = combined.copyOfRange(12, combined.size)
            
            // AES解密
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, spec)
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            return String(decryptedBytes)
        } catch (e: Exception) {
            throw RuntimeException("解密失败: ${e.message}", e)
        }
    }
}
