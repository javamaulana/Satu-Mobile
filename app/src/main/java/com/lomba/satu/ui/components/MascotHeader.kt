package com.lomba.satu.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lomba.satu.R
import com.lomba.satu.data.AppRole
import com.lomba.satu.ui.theme.*

@Composable
fun MascotHeader(
    title: String,
    modifier: Modifier = Modifier,
    role: AppRole = AppRole.GURU,
    onBackClick: (() -> Unit)? = null,
    actionSlot: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val headerBg = if (role == AppRole.GURU) PrimaryYellow else PrimaryOrange
    val cardBg = if (role == AppRole.GURU) LightYellowBg else LightOrangeBg

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(headerBg)
    ) {
        // Main Cream Card underneath
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 90.dp),
            color = cardBg,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 40.dp, start = 20.dp, end = 20.dp, bottom = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Screen Title Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = if (actionSlot != null) Arrangement.SpaceBetween else Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                        color = TextPrimary,
                        textAlign = if (actionSlot != null) TextAlign.Start else TextAlign.Center
                    )

                    if (actionSlot != null) {
                        actionSlot()
                    }
                }

                // Body content slot
                content()
            }
        }

        // Floating Mascot sitting right on the curved top edge of the card
        Image(
            painter = painterResource(id = R.drawable.maskot),
            contentDescription = "Mascot",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 22.dp)
                .size(105.dp)
        )

        // Back Arrow Button if onBackClick is provided
        if (onBackClick != null) {
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val scale by animateFloatAsState(targetValue = if (isPressed) 0.88f else 1f, label = "backScale")

            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 16.dp, top = 24.dp)
                    .size(44.dp)
                    .scale(scale)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onBackClick
                    ),
                shape = CircleShape,
                color = SurfaceWhite,
                shadowElevation = 4.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    IconBackArrow(
                        tint = if (role == AppRole.GURU) PrimaryYellow else PrimaryOrange,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
