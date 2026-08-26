package com.example.contactvip

import com.example.contactvip.data.entity.Contact
import com.example.contactvip.utils.AccountUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun contact_getFullName_returnsCorrectName() {
        val contactWithName = Contact().apply {
            name = "Nguyen Van A"
        }
        assertEquals("Nguyen Van A", contactWithName.getFullName())

        val contactWithNullName = Contact().apply {
            name = null
        }
        assertEquals("No Name", contactWithNullName.getFullName())

        val contactWithEmptyName = Contact().apply {
            name = "   "
        }
        assertEquals("No Name", contactWithEmptyName.getFullName())
    }

    @Test
    fun accountUtils_formatAccountDisplay_returnsCorrectLabel() {
        assertEquals("Thiết bị (Chỉ lưu trên máy)", AccountUtils.formatAccountDisplay(null, null))
        assertEquals("Thiết bị (Chỉ lưu trên máy)", AccountUtils.formatAccountDisplay("", ""))
        assertEquals("Google (user@gmail.com)", AccountUtils.formatAccountDisplay("com.google", "user@gmail.com"))
        assertEquals("Thẻ SIM (Viettel)", AccountUtils.formatAccountDisplay("vnd.sec.contact.sim", "Viettel"))
        assertEquals("Samsung Account (samsung_user)", AccountUtils.formatAccountDisplay("com.osp.app.signin", "samsung_user"))
        assertEquals("Xiaomi Account (xiaomi_user)", AccountUtils.formatAccountDisplay("com.xiaomi", "xiaomi_user"))
    }

    @Test
    fun accountUtils_getShortAccountBadge_returnsCorrectBadge() {
        assertEquals("Thiết bị", AccountUtils.getShortAccountBadge(null, null))
        assertEquals("Google: user@gmail.com", AccountUtils.getShortAccountBadge("com.google", "user@gmail.com"))
        assertEquals("SIM: Viettel", AccountUtils.getShortAccountBadge("vnd.sec.contact.sim", "Viettel"))
    }
}
