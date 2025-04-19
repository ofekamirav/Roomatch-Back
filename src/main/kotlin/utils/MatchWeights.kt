package com.utils
import com.models.Attribute
import com.models.CondoPreference
import com.models.RoommateUser


object MatchWeights {
//    const val dealbreakerWeight = 50.0
//    const val preferenceWeight = 20.0
//    const val condoMatchWeight = 10.0
//    const val attributeWeight = 1.0

    const val dealbreakerBonus = 25.0

    // Default weights for roommate attributes
    val attributeWeights = mapOf(
        Attribute.CLEAN to 0.25,
        Attribute.QUIET to 0.25,
        Attribute.STUDENT to 0.25,
        Attribute.SMOKER to 0.25,
        Attribute.PET_LOVER to 0.25,
        Attribute.HAS_PET to 0.25,
        Attribute.REMOTE_JOB to 0.25,
        Attribute.NIGHT_JOB to 0.25,
        Attribute.TAKEN to 0.25,
        Attribute.KOSHER to 0.25,
        Attribute.VEGGIE to 0.25,
        Attribute.JEWISH to 0.25,
        Attribute.MUSLIM to 0.25,
        Attribute.CHRISTIAN to 0.25,
        Attribute.ATHEIST to 0.25
    )



    // Default weights for condo preferences
    val condoPreferenceWeights = mapOf(
        CondoPreference.BALCONY to 0.25,
        CondoPreference.ELEVATOR to 0.25,
        CondoPreference.FURNISHED to 0.25,
        CondoPreference.PARKING to 0.25,
        CondoPreference.PET_VERIFY to 0.25
    )

}

