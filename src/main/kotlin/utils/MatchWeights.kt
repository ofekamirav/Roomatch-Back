package com.utils
import com.models.Attribute
import com.models.CondoPreference
import com.models.RoommateUser


object MatchWeights {
    const val dealbreakerBonus = 100.0

    // Default weights for roommate attributes
    val attributeWeights = mapOf(
        Attribute.CLEAN to 0.5,
        Attribute.QUIET to 0.5,
        Attribute.STUDENT to 0.5,
        Attribute.SMOKER to 0.5,
        Attribute.PET_LOVER to 0.5,
        Attribute.HAS_PET to 0.5,
        Attribute.REMOTE_JOB to 0.5,
        Attribute.NIGHT_JOB to 0.5,
        Attribute.TAKEN to 0.5,
        Attribute.KOSHER to 0.5,
        Attribute.VEGGIE to 0.5,
        Attribute.JEWISH to 0.5,
        Attribute.MUSLIM to 0.5,
        Attribute.CHRISTIAN to 0.5,
        Attribute.ATHEIST to 0.5
    )



    // Default weights for condo preferences
    val condoPreferenceWeights = mapOf(
        CondoPreference.BALCONY to 0.5,
        CondoPreference.ELEVATOR to 0.5,
        CondoPreference.FURNISHED to 0.5,
        CondoPreference.PARKING to 0.5,
        CondoPreference.PET_VERIFY to 0.5
    )

}

