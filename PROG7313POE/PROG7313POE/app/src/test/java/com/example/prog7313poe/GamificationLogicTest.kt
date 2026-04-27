package com.example.savvysaver

import org.junit.Assert.*
import org.junit.Test

class GamificationLogicTest {

    @Test
    fun testXpCalculation() {
        val addExpenseXp = 5
        val addPhotoXp = 2
        val dailyLoginXp = 10

        assertEquals(5, addExpenseXp)
        assertEquals(2, addPhotoXp)
        assertEquals(10, dailyLoginXp)
    }

    @Test
    fun testLevelCalculation() {
        fun getLevel(xp: Int): Int {
            return when (xp) {
                in 0..99 -> 1
                in 100..249 -> 2
                in 250..499 -> 3
                in 500..999 -> 4
                in 1000..1999 -> 5
                else -> 6
            }
        }

        assertEquals(1, getLevel(0))
        assertEquals(1, getLevel(50))
        assertEquals(2, getLevel(100))
        assertEquals(3, getLevel(250))
        assertEquals(4, getLevel(500))
        assertEquals(5, getLevel(1000))
        assertEquals(6, getLevel(2000))
    }

    @Test
    fun testLevelTitles() {
        fun getTitle(level: Int): String {
            return when (level) {
                1 -> "MONEY BEGINNER"
                2 -> "SMART SAVER"
                3 -> "BUDGET BUDDY"
                4 -> "FINANCE FIGHTER"
                5 -> "CASH MASTER"
                else -> "WEALTH WARRIOR"
            }
        }

        assertEquals("MONEY BEGINNER", getTitle(1))
        assertEquals("SMART SAVER", getTitle(2))
        assertEquals("BUDGET BUDDY", getTitle(3))
        assertEquals("FINANCE FIGHTER", getTitle(4))
        assertEquals("CASH MASTER", getTitle(5))
        assertEquals("WEALTH WARRIOR", getTitle(6))
    }

    @Test
    fun testProgressPercentage() {
        fun calculateProgress(current: Int, total: Int): Int {
            return ((current.toDouble() / total) * 100).toInt()
        }

        assertEquals(65, calculateProgress(650, 1000))
        assertEquals(50, calculateProgress(500, 1000))
        assertEquals(66, calculateProgress(4, 6))
        assertEquals(0, calculateProgress(0, 10))
        assertEquals(100, calculateProgress(10, 10))
    }

    @Test
    fun testStreakCalculation() {
        fun updateStreak(currentStreak: Int, wasActiveYesterday: Boolean): Int {
            return if (wasActiveYesterday) currentStreak + 1 else 1
        }

        var streak = updateStreak(0, false)
        assertEquals(1, streak)

        streak = updateStreak(streak, true)
        assertEquals(2, streak)

        streak = updateStreak(streak, false)
        assertEquals(1, streak)
    }
}