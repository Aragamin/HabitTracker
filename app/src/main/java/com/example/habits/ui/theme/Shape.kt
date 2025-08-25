package com.example.habits.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Скругления в духе M3, но чуть «дружелюбнее»
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small      = RoundedCornerShape(10.dp),
    medium     = RoundedCornerShape(14.dp),  // карточки, чипы
    large      = RoundedCornerShape(20.dp),  // диалоги, большие поверхности
    extraLarge = RoundedCornerShape(28.dp)
)
