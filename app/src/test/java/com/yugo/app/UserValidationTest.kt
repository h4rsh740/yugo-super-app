package com.yugo.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserValidationTest {

    private fun normalizeIndianPhoneNumber(input: String): String? {
        val digits = input.filter { it.isDigit() }
        return when {
            digits.length == 10 -> "+91$digits"
            digits.length == 12 && digits.startsWith("91") -> "+$digits"
            else -> null
        }
    }

    private fun isValidOtp(otp: String): Boolean {
        val trimmed = otp.trim()
        return trimmed.length == 6 && trimmed.all { it.isDigit() }
    }

    private fun isValidEmail(email: String): Boolean {
        val pattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return pattern.matches(email.trim())
    }

    @Test
    fun testValidPhoneNormalization() {
        assertEquals("+919876500112", normalizeIndianPhoneNumber("9876500112"))
        assertEquals("+919876500112", normalizeIndianPhoneNumber("98765 00112"))
        assertEquals("+919876500112", normalizeIndianPhoneNumber("+91 98765-00112"))
    }

    @Test
    fun testInvalidPhoneNumberRejection() {
        assertEquals(null, normalizeIndianPhoneNumber("12345"))
        assertEquals(null, normalizeIndianPhoneNumber("98765432109876"))
        assertEquals(null, normalizeIndianPhoneNumber(""))
    }

    @Test
    fun testOtpValidation() {
        assertTrue(isValidOtp("123456"))
        assertTrue(isValidOtp(" 000000 "))
        assertFalse(isValidOtp("12345"))
        assertFalse(isValidOtp("1234567"))
        assertFalse(isValidOtp("12A456"))
        assertFalse(isValidOtp(""))
    }

    @Test
    fun testEmailValidation() {
        assertTrue(isValidEmail("user@yugo.app"))
        assertTrue(isValidEmail("harsh.singh@example.co.in"))
        assertFalse(isValidEmail("invalid-email"))
        assertFalse(isValidEmail("@domain.com"))
        assertFalse(isValidEmail("user@domain"))
    }
}
