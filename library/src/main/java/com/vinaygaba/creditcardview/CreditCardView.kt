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

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.AllCaps
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.vinaygaba.creditcardview.util.AndroidUtils
import java.util.regex.Pattern

@SuppressLint("DefaultLocale")
class CreditCardView @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null) : RelativeLayout(context, attrs) {
    private var mContext: Context? = null
    private var mCardNumber: String? = null
    private var mCardName: String? = null
    private var mExpiryDate: String? = null
    private var mCvv: String? = null
    private var mFontPath: String? = null
    private var mCardNumberTextColor = Color.WHITE
    private var mCardNumberFormat = CardNumberFormat.ALL_DIGITS
    private var mCardNameTextColor = Color.WHITE
    private var mExpiryDateTextColor = Color.WHITE
    private var mCvvTextColor = Color.BLACK
    private var mValidTillTextColor = Color.WHITE
    @CreditCardType
    private var mType: Int = CardType.VISA
    private var mBrandLogo = 0
    private var cardSide = CARD_FRONT
    private var mPutChip = false
    private var mIsEditable = false
    private var mIsCardNumberEditable = false
    private var mIsCardNameEditable = false
    private var mIsExpiryDateEditable = false
    private var mIsCvvEditable = false
    private var mIsFlippable = false
    private var mHintTextColor = Color.WHITE
    private val mCvvHintColor = Color.WHITE
    @DrawableRes
    private var mCardFrontBackground = 0
    @DrawableRes
    private var mCardBackBackground = 0
    private var mCreditCardTypeFace: Typeface? = null
    private var mFlipBtn: ImageButton? = null
    private var mCardNumberView: EditText? = null
    private var mCardNameView: EditText? = null
    private var mExpiryDateView: EditText? = null
    private var mCvvView: EditText? = null
    private var mCardTypeView: ImageView? = null
    private var mBrandLogoView: ImageView? = null
    private var mChipView: ImageView? = null
    private var mValidTill: TextView? = null
    private var mStripe: View? = null
    private var mAuthorizedSig: View? = null
    private var mSignature: View? = null
    /**
     * Initialize various views and variables
     */
    private fun init() {
        val inflater = LayoutInflater.from(mContext)
        inflater.inflate(R.layout.creditcardview, this, true)
        mCardNumberView = findViewById<View>(R.id.card_number) as EditText
        mCardNameView = findViewById<View>(R.id.card_name) as EditText
        mCardTypeView = findViewById<View>(R.id.card_logo) as ImageView
        mBrandLogoView = findViewById<View>(R.id.brand_logo) as ImageView
        mChipView = findViewById<View>(R.id.chip) as ImageView
        mValidTill = findViewById<View>(R.id.valid_till) as TextView
        mExpiryDateView = findViewById<View>(R.id.expiry_date) as EditText
        mFlipBtn = findViewById<View>(R.id.flip_btn) as ImageButton
        mCvvView = findViewById<View>(R.id.cvv_et) as EditText
        mStripe = findViewById(R.id.stripe)
        mAuthorizedSig = findViewById(R.id.authorized_sig_tv)
        mSignature = findViewById(R.id.signature)
    }

    private fun loadAttributes(attrs: AttributeSet?) {
        val a = mContext!!.theme.obtainStyledAttributes(attrs,
                R.styleable.CreditCardView, 0, 0)
        try {
            mCardNumber = a.getString(R.styleable.CreditCardView_cardNumber)
            mCardName = a.getString(R.styleable.CreditCardView_cardName)
            mExpiryDate = a.getString(R.styleable.CreditCardView_expiryDate)
            mCardNumberTextColor = a.getColor(R.styleable.CreditCardView_cardNumberTextColor,
                    Color.WHITE)
            mCardNumberFormat = a.getInt(R.styleable.CreditCardView_cardNumberFormat, 0)
            mCardNameTextColor = a.getColor(R.styleable.CreditCardView_cardNumberTextColor,
                    Color.WHITE)
            mExpiryDateTextColor = a.getColor(R.styleable.CreditCardView_expiryDateTextColor,
                    Color.WHITE)
            mCvvTextColor = a.getColor(R.styleable.CreditCardView_cvvTextColor,
                    Color.BLACK)
            mValidTillTextColor = a.getColor(R.styleable.CreditCardView_validTillTextColor,
                    Color.WHITE)
            mType = a.getInt(R.styleable.CreditCardView_type, CardType.VISA)
            mBrandLogo = a.getResourceId(R.styleable.CreditCardView_brandLogo, 0)
            // mBrandLogoPosition = a.getInt(R.styleable.CreditCardView_brandLogoPosition, 1);
            mPutChip = a.getBoolean(R.styleable.CreditCardView_putChip, false)
            mIsEditable = a.getBoolean(R.styleable.CreditCardView_isEditable, false)
            //For more granular control to the fields. Issue #7
            mIsCardNameEditable = a.getBoolean(R.styleable.CreditCardView_isCardNameEditable,
                    mIsEditable)
            mIsCardNumberEditable = a.getBoolean(R.styleable.CreditCardView_isCardNumberEditable,
                    mIsEditable)
            mIsExpiryDateEditable = a.getBoolean(R.styleable.CreditCardView_isExpiryDateEditable,
                    mIsEditable)
            mIsCvvEditable = a.getBoolean(R.styleable.CreditCardView_isCvvEditable, mIsEditable)
            mHintTextColor = a.getColor(R.styleable.CreditCardView_hintTextColor, Color.WHITE)
            mIsFlippable = a.getBoolean(R.styleable.CreditCardView_isFlippable, mIsFlippable)
            mCvv = a.getString(R.styleable.CreditCardView_cvv)
            mCardFrontBackground = a.getResourceId(R.styleable.CreditCardView_cardFrontBackground,
                    R.drawable.cardbackground_sky)
            mCardBackBackground = a.getResourceId(R.styleable.CreditCardView_cardBackBackground,
                    R.drawable.cardbackground_canvas)
            mFontPath = a.getString(R.styleable.CreditCardView_fontPath)
        } finally {
            a.recycle()
        }
    }

    private fun initDefaults() {
        setBackgroundResource(mCardFrontBackground)
        if (TextUtils.isEmpty(mFontPath)) { // Default Font path
            mFontPath = mContext!!.getString(R.string.font_path)
        }
        // Added this check to fix the issue of custom view not rendering correctly in the layout
// preview.
        if (!isInEditMode) { // Loading Font Face
            mCreditCardTypeFace = Typeface.createFromAsset(mContext!!.assets, mFontPath)
        }
        if (TextUtils.isEmpty(mFontPath)) { // Default Font path
            mFontPath = mContext!!.getString(R.string.font_path)
        }
        // Added this check to fix the issue of custom view not rendering correctly in the layout
// preview.
        if (!isInEditMode) { // Loading Font Face
            mCreditCardTypeFace = Typeface.createFromAsset(mContext!!.assets, mFontPath)
        }
        if (!mIsEditable) { // If card is not set to be editable, disable the edit texts
            mCardNumberView!!.isEnabled = false
            mCardNameView!!.isEnabled = false
            mExpiryDateView!!.isEnabled = false
            mCvvView!!.isEnabled = false
        } else { // If the card is editable, set the hint text and hint values which will be displayed
// when the edit text is blank
            mCardNumberView!!.setHint(R.string.card_number_hint)
            mCardNumberView!!.setHintTextColor(mHintTextColor)
            mCardNameView!!.setHint(R.string.card_name_hint)
            mCardNameView!!.setHintTextColor(mHintTextColor)
            mExpiryDateView!!.setHint(R.string.expiry_date_hint)
            mExpiryDateView!!.setHintTextColor(mHintTextColor)
            mCvvView!!.setHint(R.string.cvv_hint)
            mCvvView!!.setHintTextColor(mCvvTextColor)
        }
        //For more granular control of the editable fields. Issue #7
        if (mIsCardNameEditable != mIsEditable) { //If the mIsCardNameEditable is different than mIsEditable field, the granular
//precedence comes into picture and the value needs to be checked and modified
//accordingly
            if (mIsCardNameEditable) {
                mCardNameView!!.setHint(R.string.card_name_hint)
                mCardNameView!!.setHintTextColor(mHintTextColor)
            } else {
                mCardNameView!!.hint = ""
            }
            mCardNameView!!.isEnabled = mIsCardNameEditable
        }
        if (mIsCardNumberEditable != mIsEditable) { //If the mIsCardNumberEditable is different than mIsEditable field, the granular
//precedence comes into picture and the value needs to be checked and modified
//accordingly
            if (mIsCardNumberEditable) {
                mCardNumberView!!.setHint(R.string.card_number_hint)
                mCardNumberView!!.setHintTextColor(mHintTextColor)
            } else {
                mCardNumberView!!.hint = ""
            }
            mCardNumberView!!.isEnabled = mIsCardNumberEditable
        }
        if (mIsExpiryDateEditable != mIsEditable) { //If the mIsExpiryDateEditable is different than mIsEditable field, the granular
//precedence comes into picture and the value needs to be checked and modified
//accordingly
            if (mIsExpiryDateEditable) {
                mExpiryDateView!!.setHint(R.string.expiry_date_hint)
                mExpiryDateView!!.setHintTextColor(mHintTextColor)
            } else {
                mExpiryDateView!!.hint = ""
            }
            mExpiryDateView!!.isEnabled = mIsExpiryDateEditable
        }
        // If card number is not null, add space every 4 characters and format it in the appropriate
// format
        if (!TextUtils.isEmpty(mCardNumber)) {
            mCardNumberView!!.setText(getFormattedCardNumber(addSpaceToCardNumber()))
        }
        // Set the user entered card number color to card number field
        mCardNumberView!!.setTextColor(mCardNumberTextColor)
        // Added this check to fix the issue of custom view not rendering correctly in the layout
// preview.
        if (!isInEditMode) {
            mCardNumberView!!.typeface = mCreditCardTypeFace
        }
        // If card name is not null, convert the text to upper case
        if (!TextUtils.isEmpty(mCardName)) {
            mCardNameView!!.setText(mCardName!!.toUpperCase())
        }
        // This filter will ensure the text entered is in uppercase when the user manually enters
// the card name
        mCardNameView!!.filters = arrayOf<InputFilter>(
                AllCaps()
        )
        // Set the user entered card name color to card name field
        mCardNameView!!.setTextColor(mCardNumberTextColor)
        // Added this check to fix the issue of custom view not rendering correctly in the layout
// preview.
        if (!isInEditMode) {
            mCardNameView!!.typeface = mCreditCardTypeFace
        }
        // Set the appropriate logo based on the type of card
        mCardTypeView!!.setBackgroundResource(logo)
        // If background logo attribute is present, set it as the brand logo background resource
        if (mBrandLogo != 0) {
            mBrandLogoView!!.setBackgroundResource(mBrandLogo)
            // brandLogo.setLayoutParams(params);
        }
        // If putChip attribute is present, change the visibility of the putChip view and display it
        if (mPutChip) {
            mChipView!!.visibility = View.VISIBLE
        }
        // If expiry date is not null, set it to the expiryDate TextView
        if (!TextUtils.isEmpty(mExpiryDate)) {
            mExpiryDateView!!.setText(mExpiryDate)
        }
        // Set the user entered expiry date color to expiry date field
        mExpiryDateView!!.setTextColor(mExpiryDateTextColor)
        // Added this check to fix the issue of custom view not rendering correctly in the layout
// preview.
        if (!isInEditMode) {
            mExpiryDateView!!.typeface = mCreditCardTypeFace
        }
        // Set the appropriate text color to the validTill TextView
        mValidTill!!.setTextColor(mValidTillTextColor)
        // If CVV is not null, set it to the expiryDate TextView
        if (!TextUtils.isEmpty(mCvv)) {
            mCvvView!!.setText(mCvv)
        }
        // Set the user entered card number color to card number field
        mCvvView!!.setTextColor(mCvvTextColor)
        // Added this check to fix the issue of custom view not rendering correctly in the layout
// preview.
        if (!isInEditMode) {
            mCvvView!!.typeface = mCreditCardTypeFace
        }
        if (mIsCvvEditable != mIsEditable) {
            if (mIsCvvEditable) {
                mCvvView!!.setHint(R.string.cvv_hint)
                mCvvView!!.setHintTextColor(mCvvHintColor)
            } else {
                mCvvView!!.hint = ""
            }
            mCvvView!!.isEnabled = mIsCvvEditable
        }
        if (mIsFlippable) {
            mFlipBtn!!.visibility = View.VISIBLE
        }
        mFlipBtn!!.isEnabled = mIsFlippable
    }

    private fun addListeners() { // Add text change listener
        mCardNumberView!!.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) { // Change card type to auto to dynamically detect the card type based on the card
// number
                mType = CardType.AUTO //TODO - is this really required?
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) { // Delete any spaces the user might have entered manually. The library automatically
// adds spaces after every 4 characters to the view.
                mCardNumber = s.toString().replace("\\s+".toRegex(), "")
            }
        })
        // Add focus change listener to detect focus being shifted from the cardNumber EditText
        mCardNumberView!!.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            // If the field just lost focus
            if (!hasFocus) { //Fix for NPE. Issue #6
                if (!TextUtils.isEmpty(mCardNumber) && mCardNumber!!.length > 12) { // If card type is "auto",find the appropriate logo
                    if (mType == CardType.AUTO) {
                        mCardTypeView!!.setBackgroundResource(logo)
                    }
                    // If the length of card is >12, add space every 4 characters and format it
// in the appropriate format
                    mCardNumberView!!.setText(getFormattedCardNumber(addSpaceToCardNumber()))
                }
            }
        }
        mCardNameView!!.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) { // Set the mCardName attribute the user entered value in the Card Name field
                mCardName = s.toString().toUpperCase()
            }
        })
        mExpiryDateView!!.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) { // Set the mExpiryDate attribute the user entered value in the Expiry Date field
                mExpiryDate = s.toString()
            }
        })
        mFlipBtn!!.setOnClickListener { flip() }
    }

    var isFlippable: Boolean
        get() = mIsFlippable
        set(flippable) {
            mIsFlippable = flippable
            mFlipBtn!!.visibility = if (mIsFlippable) View.VISIBLE else View.INVISIBLE
            mFlipBtn!!.isEnabled = mIsFlippable
        }

    fun flip() {
        if (mIsFlippable) {
            if (AndroidUtils.icsOrBetter()) {
                if (cardSide == CARD_FRONT) {
                    rotateInToBack()
                } else if (cardSide == CARD_BACK) {
                    rotateInToFront()
                }
            } else {
                if (cardSide == CARD_FRONT) {
                    rotateInToBackBeforeEleven()
                } else if (cardSide == CARD_BACK) {
                    rotateInToFrontBeforeEleven()
                }
            }
        }
    }

    private fun showFrontView() {
        mCardNumberView!!.visibility = View.VISIBLE
        mCardNameView!!.visibility = View.VISIBLE
        mCardTypeView!!.visibility = View.VISIBLE
        mBrandLogoView!!.visibility = View.VISIBLE
        if (mPutChip) {
            mChipView!!.visibility = View.VISIBLE
        }
        mValidTill!!.visibility = View.VISIBLE
        mExpiryDateView!!.visibility = View.VISIBLE
    }

    private fun hideFrontView() {
        mCardNumberView!!.visibility = View.GONE
        mCardNameView!!.visibility = View.GONE
        mCardTypeView!!.visibility = View.GONE
        mBrandLogoView!!.visibility = View.GONE
        mChipView!!.visibility = View.GONE
        mValidTill!!.visibility = View.GONE
        mExpiryDateView!!.visibility = View.GONE
    }

    private fun showBackView() {
        mStripe!!.visibility = View.VISIBLE
        mAuthorizedSig!!.visibility = View.VISIBLE
        mSignature!!.visibility = View.VISIBLE
        mCvvView!!.visibility = View.VISIBLE
    }

    private fun hideBackView() {
        mStripe!!.visibility = View.GONE
        mAuthorizedSig!!.visibility = View.GONE
        mSignature!!.visibility = View.GONE
        mCvvView!!.visibility = View.GONE
    }

    private fun redrawViews() {
        invalidate()
        requestLayout()
    }

    var cardNumber: String?
        get() = mCardNumber
        set(cardNumber) {
            if (cardNumber == null) {
                throw NullPointerException("Card Number cannot be null.")
            }
            mCardNumber = cardNumber.replace("\\s+".toRegex(), "")
            mCardNumberView!!.setText(addSpaceToCardNumber())
            redrawViews()
        }

    var cardName: String?
        get() = mCardName
        set(cardName) {
            if (cardName == null) {
                throw NullPointerException("Card Name cannot be null.")
            }
            mCardName = cardName.toUpperCase()
            mCardNameView!!.setText(mCardName)
            redrawViews()
        }

    @get:ColorInt
    var cardNumberTextColor: Int
        get() = mCardNumberTextColor
        set(cardNumberTextColor) {
            mCardNumberTextColor = cardNumberTextColor
            mCardNumberView!!.setTextColor(mCardNumberTextColor)
            redrawViews()
        }

    @get:CreditCardFormat
    var cardNumberFormat: Int
        get() = mCardNumberFormat
        set(cardNumberFormat) {
            if ((cardNumberFormat < 0) or (cardNumberFormat > 3)) {
                throw UnsupportedOperationException("CardNumberFormat: " + cardNumberFormat + "  " +
                        "is not supported. Use `CardNumberFormat.*` or `CardType.ALL_DIGITS` if " +
                        "unknown")
            }
            mCardNumberFormat = cardNumberFormat
            mCardNumberView!!.setText(getFormattedCardNumber(mCardNumber))
            redrawViews()
        }

    @get:ColorInt
    var cardNameTextColor: Int
        get() = mCardNameTextColor
        set(cardNameTextColor) {
            mCardNameTextColor = cardNameTextColor
            mCardNameView!!.setTextColor(mCardNameTextColor)
            redrawViews()
        }

    var expiryDate: String?
        get() = mExpiryDate
        set(expiryDate) {
            mExpiryDate = expiryDate
            mExpiryDateView!!.setText(mExpiryDate)
            redrawViews()
        }

    @get:ColorInt
    var expiryDateTextColor: Int
        get() = mExpiryDateTextColor
        set(expiryDateTextColor) {
            mExpiryDateTextColor = expiryDateTextColor
            mExpiryDateView!!.setTextColor(mExpiryDateTextColor)
            redrawViews()
        }

    @get:ColorInt
    var validTillTextColor: Int
        get() = mValidTillTextColor
        set(validTillTextColor) {
            mValidTillTextColor = validTillTextColor
            mValidTill!!.setTextColor(mValidTillTextColor)
            redrawViews()
        }

    @get:CreditCardType
    var type: Int
        get() = mType
        set(type) {
            if ((type < 0) or (type > 4)) {
                throw UnsupportedOperationException("CardType: " + type + "  is not supported. " +
                        "Use `CardType.*` or `CardType.AUTO` if unknown")
            }
            mType = type
            mCardTypeView!!.setBackgroundResource(logo)
            redrawViews()
        }

    var isEditable: Boolean
        get() = mIsEditable
        set(isEditable) {
            mIsEditable = isEditable
            redrawViews()
        }

    var isCardNameEditable: Boolean
        get() = mIsCardNameEditable
        set(isCardNameEditable) {
            mIsCardNameEditable = isCardNameEditable
            redrawViews()
        }

    var isCardNumberEditable: Boolean
        get() = mIsCardNumberEditable
        set(isCardNumberEditable) {
            mIsCardNumberEditable = isCardNumberEditable
            redrawViews()
        }

    var isExpiryDateEditable: Boolean
        get() = mIsExpiryDateEditable
        set(isExpiryDateEditable) {
            mIsExpiryDateEditable = isExpiryDateEditable
            redrawViews()
        }

    @get:ColorInt
    var hintTextColor: Int
        get() = mHintTextColor
        set(hintTextColor) {
            mHintTextColor = hintTextColor
            mCardNameView!!.setHintTextColor(mHintTextColor)
            mCardNumberView!!.setHintTextColor(mHintTextColor)
            mExpiryDateView!!.setHintTextColor(mHintTextColor)
            redrawViews()
        }

    @get:DrawableRes
    var brandLogo: Int
        get() = mBrandLogo
        set(brandLogo) {
            mBrandLogo = brandLogo
            mBrandLogoView!!.setBackgroundResource(mBrandLogo)
            redrawViews()
        }

    var brandLogoPosition: Int
        get() = mBrandLogo
        set(brandLogoPosition) {
            redrawViews()
        }

    fun putChip(flag: Boolean) {
        mPutChip = flag
        mChipView!!.visibility = if (mPutChip) View.VISIBLE else View.GONE
        redrawViews()
    }

    var isCvvEditable: Boolean
        get() = mIsCvvEditable
        set(editable) {
            mIsCvvEditable = editable
            redrawViews()
        }

    @get:DrawableRes
    var cardBackBackground: Int
        get() = mCardBackBackground
        set(cardBackBackground) {
            mCardBackBackground = cardBackBackground
            setBackgroundResource(mCardBackBackground)
            redrawViews()
        }

    var cardFrontBackground: Int
        get() = mCardFrontBackground
        set(mCardFrontBackground) {
            this.mCardFrontBackground = mCardFrontBackground
            setBackgroundResource(mCardFrontBackground)
            redrawViews()
        }

    // Loading Font Face
    var fontPath: String?
        get() = mFontPath
        set(mFontPath) {
            this.mFontPath = mFontPath
            if (!isInEditMode) { // Loading Font Face
                mCreditCardTypeFace = Typeface.createFromAsset(mContext!!.assets, mFontPath)
                mCardNumberView!!.typeface = mCreditCardTypeFace
                mCardNameView!!.typeface = mCreditCardTypeFace
                mExpiryDateView!!.typeface = mCreditCardTypeFace
                mCvvView!!.typeface = mCreditCardTypeFace
            }
            redrawViews()
        }

    /**
     * Return the appropriate drawable resource based on the card type
     */
    @get:DrawableRes
    private val logo: Int
        private get() = when (mType) {
            VISA -> R.drawable.visa
            MASTERCARD -> R.drawable.mastercard
            AMERICAN_EXPRESS -> R.drawable.amex
            DISCOVER -> R.drawable.discover
            AUTO -> findCardType()
            else -> throw UnsupportedOperationException("CardType: " + mType + "  is not supported" +
                    ". Use `CardType.*` or `CardType.AUTO` if unknown")
        }

    /**
     * Returns the formatted card number based on the user entered value for card number format
     *
     * @param cardNumber Card Number.
     */
    private fun getFormattedCardNumber(cardNumber: String?): String? {
        var cardNumber = cardNumber
        if (DEBUG) {
            Log.e("Card Number", cardNumber)
        }
        when (cardNumberFormat) {
            MASKED_ALL_BUT_LAST_FOUR -> cardNumber = "**** **** **** " + cardNumber!!.substring(cardNumber.length - 4)
            ONLY_LAST_FOUR -> cardNumber = cardNumber!!.substring(cardNumber.length - 4)
            MASKED_ALL -> cardNumber = "**** **** **** ****"
            ALL_DIGITS -> {
            }
            else -> throw UnsupportedOperationException("CreditCardFormat: " + mCardNumberFormat +
                    " is not supported. Use `CreditCardFormat.*`")
        }
        return cardNumber
    }

    /**
     * Returns the appropriate card type drawable resource based on the regex pattern of the card
     * number
     */
    @DrawableRes
    private fun findCardType(): Int {
        mType = VISA
        if (!TextUtils.isEmpty(mCardNumber)) {
            val cardNumber = mCardNumber!!.replace("\\s+".toRegex(), "")
            if (Pattern.compile(CardNumberPatterns.PATTERN_MASTER_CARD).matcher(cardNumber).matches()) {
                mType = MASTERCARD
            } else if (Pattern.compile(CardNumberPatterns.PATTERN_AMERICAN_EXPRESS).matcher(cardNumber).matches()) {
                mType = AMERICAN_EXPRESS
            } else if (Pattern.compile(CardNumberPatterns.PATTERN_DISCOVER).matcher(cardNumber).matches()) {
                mType = DISCOVER
            }
        }
        return logo
    }

    /**
     * Adds space after every 4 characters to the card number if the card number is divisible by 4
     */
    private fun addSpaceToCardNumber(): String? {
        val splitBy = 4
        val length = mCardNumber!!.length
        return if (length % splitBy != 0 || length <= splitBy) {
            mCardNumber
        } else {
            val result = StringBuilder()
            result.append(mCardNumber!!.substring(0, splitBy))
            for (i in splitBy until length) {
                if (i % splitBy == 0) {
                    result.append(" ")
                }
                result.append(mCardNumber!![i])
            }
            result.toString()
        }
    }

    @TargetApi(11)
    private fun rotateInToBack() {
        val set = AnimatorSet()
        val rotateIn = ObjectAnimator.ofFloat(this, "rotationY", 0f, 90f)
        val hideFrontView = ObjectAnimator.ofFloat(this, "alpha", 1f, 0f)
        rotateIn.interpolator = AccelerateDecelerateInterpolator()
        rotateIn.duration = 300
        hideFrontView.duration = 1
        set.addListener(object : AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                rotateOutToBack()
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        set.play(hideFrontView).after(rotateIn)
        set.start()
    }

    @TargetApi(11)
    private fun rotateInToFront() {
        val set = AnimatorSet()
        val rotateIn = ObjectAnimator.ofFloat(this, "rotationY", 0f, 90f)
        val hideBackView = ObjectAnimator.ofFloat(this, "alpha", 1f, 0f)
        rotateIn.duration = 300
        hideBackView.duration = 1
        set.addListener(object : AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                rotateOutToFront()
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        set.play(hideBackView).after(rotateIn)
        set.start()
    }

    @TargetApi(11)
    private fun rotateOutToBack() {
        hideFrontView()
        showBackView()
        this@CreditCardView.rotationY = -90f
        setBackgroundResource(mCardBackBackground)
        val set = AnimatorSet()
        val flipView = ObjectAnimator.ofInt(this@CreditCardView, "rotationY", 90, -90)
        val rotateOut = ObjectAnimator.ofFloat(this@CreditCardView, "rotationY", -90f, 0f)
        val showBackView = ObjectAnimator.ofFloat(this@CreditCardView, "alpha", 0f, 1f)
        flipView.duration = 0
        showBackView.duration = 1
        rotateOut.duration = 300
        showBackView.startDelay = 150
        rotateOut.interpolator = AccelerateDecelerateInterpolator()
        set.addListener(object : AnimatorListener {
            override fun onAnimationStart(animation: Animator) { //Do nothing
            }

            override fun onAnimationEnd(animation: Animator) {
                cardSide = CARD_BACK
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) { //Do nothing
            }
        })
        set.play(flipView).with(showBackView).before(rotateOut)
        set.start()
    }

    @TargetApi(11)
    private fun rotateOutToFront() {
        showFrontView()
        hideBackView()
        this@CreditCardView.rotationY = -90f
        setBackgroundResource(mCardFrontBackground)
        val set = AnimatorSet()
        val flipView = ObjectAnimator.ofInt(this@CreditCardView, "rotationY", 90, -90)
        val rotateOut = ObjectAnimator.ofFloat(this@CreditCardView, "rotationY", -90f, 0f)
        val showFrontView = ObjectAnimator.ofFloat(this@CreditCardView, "alpha", 0f, 1f)
        showFrontView.duration = 1
        rotateOut.duration = 300
        showFrontView.startDelay = 150
        rotateOut.interpolator = AccelerateDecelerateInterpolator()
        set.addListener(object : AnimatorListener {
            override fun onAnimationStart(animation: Animator) { //Do nothing
            }

            override fun onAnimationEnd(animation: Animator) {
                cardSide = CARD_FRONT
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) { //Do nothing
            }
        })
        set.play(flipView).with(showFrontView).before(rotateOut)
        set.start()
    }

    private fun rotateInToBackBeforeEleven() {
        val set = AnimatorSet()
        val rotateIn = ObjectAnimator.ofFloat(this, "rotationY", 0f, 90f)
        val hideFrontView = ObjectAnimator.ofFloat(this, "alpha", 1f, 0f)
        rotateIn.interpolator = AccelerateDecelerateInterpolator()
        rotateIn.duration = 300
        hideFrontView.duration = 1
        set.addListener(object : AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                rotateOutToBackBeforeEleven()
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        set.play(hideFrontView).after(rotateIn)
        set.start()
    }

    private fun rotateInToFrontBeforeEleven() {
        val set = AnimatorSet()
        val rotateIn = ObjectAnimator.ofFloat(this, "rotationY", 0f, 90f)
        val hideBackView = ObjectAnimator.ofFloat(this, "alpha", 1f, 0f)
        rotateIn.interpolator = AccelerateDecelerateInterpolator()
        rotateIn.duration = 300
        hideBackView.duration = 1
        set.addListener(object : AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                rotateOutToFrontBeforeEleven()
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        set.play(hideBackView).after(rotateIn)
        set.start()
    }

    private fun rotateOutToBackBeforeEleven() {
        hideFrontView()
        showBackView()
        setBackgroundResource(mCardBackBackground)
        val set = AnimatorSet()
        val flip = ObjectAnimator.ofFloat(this@CreditCardView, "rotationY", 90f, -90f)
        val rotateOut = ObjectAnimator.ofFloat(this@CreditCardView, "rotationY", -90f, 0f)
        val showBackView = ObjectAnimator.ofFloat(this@CreditCardView, "alpha", 0f, 1f)
        flip.duration = 0
        showBackView.duration = 1
        rotateOut.duration = 300
        showBackView.startDelay = 150
        rotateOut.interpolator = AccelerateDecelerateInterpolator()
        set.addListener(object : AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                cardSide = CARD_BACK
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        set.play(flip).with(showBackView).before(rotateOut)
        set.start()
    }

    private fun rotateOutToFrontBeforeEleven() {
        showFrontView()
        hideBackView()
        setBackgroundResource(R.drawable.cardbackground_sky)
        val set = AnimatorSet()
        val flip = ObjectAnimator.ofFloat(this@CreditCardView, "rotationY", 90f, -90f)
        val rotateOut = ObjectAnimator.ofFloat(this@CreditCardView, "rotationY", -90f, 0f)
        val showFrontView = ObjectAnimator.ofFloat(this@CreditCardView, "alpha", 0f, 1f)
        showFrontView.duration = 1
        rotateOut.duration = 300
        rotateOut.interpolator = AccelerateDecelerateInterpolator()
        showFrontView.startDelay = 150
        set.addListener(object : AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                cardSide = CARD_FRONT
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        set.play(flip).with(showFrontView).with(rotateOut)
        set.start()
    }

    companion object {
        private const val CARD_FRONT = 0
        private const val CARD_BACK = 1
        private const val DEBUG = false
    }

    init {
        if (context != null) {
            mContext = context
        } else {
            mContext = getContext()
        }
        init()
        loadAttributes(attrs)
        initDefaults()
        addListeners()
    }
}