/*
 * Copyright (C) 2015 Vinay Gaba
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vinaygaba.creditcardview

import androidx.annotation.IntDef

object CardType {
    const val VISA = 0
    const val MASTERCARD = 1
    const val AMERICAN_EXPRESS = 2
    const val DISCOVER = 3
    const val AUTO = 4
}

object CardNumberPatterns {
    const val PATTERN_VISA = "^4[0-9]{12}(?:[0-9]{3})?$^5[1-5][0-9]{14}$"
    const val PATTERN_MASTER_CARD = "^5[1-5][0-9]{14}$"
    const val PATTERN_AMERICAN_EXPRESS = "^3[47][0-9]{13}$"
    //@formatter:off
    const val PATTERN_DISCOVER = "^65[4-9][0-9]{13}|64[4-9][0-9]{13}|6011[0-9]{12}|(622(?:12[6-9]|1[3-9][0-9]|[2-8][0-9][0-9]|9[01][0-9]|92[0-5])[0-9]{10})$"
    //@formatter:on
}

object CardNumberFormat {
    const val ALL_DIGITS = 0
    const val MASKED_ALL_BUT_LAST_FOUR = 1
    const val ONLY_LAST_FOUR = 2
    const val MASKED_ALL = 3
}

@IntDef(CardType.VISA, CardType.MASTERCARD, CardType.AMERICAN_EXPRESS, CardType.DISCOVER, CardType.AUTO)
@kotlin.annotation.Retention(AnnotationRetention.SOURCE)
annotation class CreditCardType

@IntDef(CardNumberFormat.ALL_DIGITS, CardNumberFormat.MASKED_ALL_BUT_LAST_FOUR, CardNumberFormat.ONLY_LAST_FOUR, CardNumberFormat.MASKED_ALL)
@kotlin.annotation.Retention(AnnotationRetention.SOURCE)
annotation class CreditCardFormat