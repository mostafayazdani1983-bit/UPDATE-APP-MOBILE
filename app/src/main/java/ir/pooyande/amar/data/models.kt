package ir.pooyande.amar.data

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null
)

data class LoginData(
    val token: String,
    @SerializedName("user_id") val userId: Int,
    val username: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("is_admin") val isAdmin: Int,
    val roles: List<Role>
)

data class Role(
    val id: Int,
    val name: String,
    @SerializedName("step_order") val stepOrder: Int,
    @SerializedName("requires_page_count") val requiresPageCount: Int,
    @SerializedName("wage_per_page") val wagePerPage: Double? = null
) {
    val needsPages: Boolean get() = requiresPageCount == 1
    override fun toString(): String = "$name (گام $stepOrder)"
}

data class CaseItem(
    val id: Int,
    @SerializedName("case_number") val caseNumber: String,
    @SerializedName("contract_nums") val contractNums: String? = null,
    @SerializedName("excel_pages") val excelPages: Int? = null,
    @SerializedName("verified_pages") val verifiedPages: Int? = null,
    @SerializedName("current_step") val currentStep: Int? = null,
    @SerializedName("has_conflict") val hasConflict: Int? = null,
    @SerializedName("latest_submitted_pages") val latestSubmittedPages: Int? = null,
    @SerializedName("requires_page_count") val requiresPageCount: Int? = null,
    @SerializedName("role_name") val roleName: String? = null
) {
    val displayPages: Int
        get() = verifiedPages ?: latestSubmittedPages ?: excelPages ?: 0

    val needsPageInput: Boolean
        get() = requiresPageCount == 1
}

data class ClaimRequest(
    @SerializedName("case_id") val caseId: Int,
    @SerializedName("role_id") val roleId: Int,
    @SerializedName("submitted_pages") val submittedPages: Int? = null
)

data class ClaimResult(
    @SerializedName("case_id") val caseId: Int,
    @SerializedName("next_step") val nextStep: Int,
    @SerializedName("has_conflict") val hasConflict: Int
)

data class UserProfile(
    @SerializedName("user_id") val userId: Int,
    val username: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("is_admin") val isAdmin: Int,
    val roles: List<Role>
)
