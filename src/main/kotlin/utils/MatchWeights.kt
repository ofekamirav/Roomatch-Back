package com.utils
import com.models.Attribute
import com.models.CondoPreference
import com.models.RoommateUser


object MatchWeights {
    const val dealbreakerWeight = 50.0
    const val preferenceWeight = 10.0
    const val condoMatchWeight = 5.0
    const val attributeWeight = 1.0

    // Default weights for roommate attributes
    val attributeWeights = mapOf(
        Attribute.SMOKER to 0.2,
        Attribute.STUDENT to 0.3,
        Attribute.PET_LOVER to 0.2,
        Attribute.PET_OWNER to 0.3,
        Attribute.VEGETARIAN to 0.1,
        Attribute.CLEAN to 0.4,
        Attribute.NIGHT_WORKER to 0.4,
        Attribute.IN_RELATIONSHIP to 0.1,
        Attribute.KOSHER to 0.3,
        Attribute.JEWISH to 0.1,
        Attribute.MUSLIM to 0.1,
        Attribute.CHRISTIAN to 0.1,
        Attribute.ATHEIST to 0.1,
        Attribute.REMOTE_WORKER to 0.2,
        Attribute.QUIET to 0.7
    )


    // Default weights for condo preferences
    val condoPreferenceWeights = mapOf(
        CondoPreference.BALCONY to 0.2,
        CondoPreference.ELEVATOR to 0.3,
        CondoPreference.PARKING to 0.4,
        CondoPreference.SHELTER to 0.2,
        CondoPreference.FURNISHED to 0.3,
        CondoPreference.PET_ALLOWED to 0.3
    )
}

