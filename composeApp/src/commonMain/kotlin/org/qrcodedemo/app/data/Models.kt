package org.qrcodedemo.app.data

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

@Serializable
data class QrSignRequest(
    val content: String
)

@Serializable
data class MessageResponse<T>(
    val message: T
)

@Serializable
data class QrSignResponse(
    val qq: Long,
    val response: AutoSignResponse
)

@Serializable
data class AutoSignResponse(
    val type: String,
    val data: AutoSignStatusData
) {
    fun getDisplayMessage(): String {
        val status = data.status
        val content = data.data
        
        return when (type) {
            "radar" -> {
                when (status) {
                    "success" -> "成功雷达签到${content?.course_name}，签到位置为：${content?.student_location}(${content?.latitude}, ${content?.longitude})，距离为：${content?.student_distance}米"
                    "already_signed" -> "雷达签到${content?.course_name}已签到"
                    else -> "雷达签到状态: $status"
                }
            }
            "number" -> {
                when (status) {
                    "success" -> "成功数字签到${content?.course_name}，签到码为${content?.number_code}"
                    "already_signed" -> "数字签到${content?.course_name}已签到"
                    else -> "数字签到状态: $status"
                }
            }
            "qr" -> {
                when (status) {
                    "success" -> {
                        val result = content?.sign_result
                        val detail = when (result) {
                            is QrSignResult.Success -> "签到成功，签到id为: ${result.id}"
                            is QrSignResult.Failed -> when (result.errorCode) {
                                "rollcall_already_closed" -> "签到已结束"
                                "qr_code_expired" -> "二维码已过期"
                                else -> "失败: ${result.message ?: result.errorCode}"
                            }
                            null -> "成功"
                        }
                        "二维码签到成功${content?.course_name}，$detail"
                    }
                    "already_signed" -> "二维码签到${content?.course_name}已签到"
                    "pending" -> "未二维码签到${content?.course_name}，请用/sign查看状态，如果有人发送二维码会自动推送"
                    else -> "二维码签到状态: $status"
                }
            }
            else -> "未知签到类型: $type"
        }
    }
}

@Serializable
data class AutoSignStatusData(
    val status: String,
    val data: AutoSignDetailData? = null
)

@Serializable(with = QrSignResultSerializer::class)
sealed class QrSignResult {
    @Serializable
    data class Success(
        val id: Long,
        val status: String
    ) : QrSignResult()

    @Serializable
    data class Failed(
        @SerialName("error_code") val errorCode: String,
        val message: String? = null
    ) : QrSignResult()
}

object QrSignResultSerializer : JsonContentPolymorphicSerializer<QrSignResult>(QrSignResult::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<QrSignResult> {
        val jsonObject = element.jsonObject
        return when {
            "id" in jsonObject -> QrSignResult.Success.serializer()
            "error_code" in jsonObject -> QrSignResult.Failed.serializer()
            else -> QrSignResult.Failed.serializer() // Default fallback
        }
    }
}

@Serializable
data class AutoSignDetailData(
    @SerialName("course_name") val course_name: String? = null,
    @SerialName("student_location") val student_location: String? = null,
    @SerialName("latitude") val latitude: Double? = null,
    @SerialName("longitude") val longitude: Double? = null,
    @SerialName("student_distance") val student_distance: Double? = null,
    @SerialName("number_code") val number_code: String? = null,
    @SerialName("sign_result") val sign_result: QrSignResult? = null
)
