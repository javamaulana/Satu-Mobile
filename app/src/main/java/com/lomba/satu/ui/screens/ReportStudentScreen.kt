package com.lomba.satu.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lomba.satu.data.ApiService
import com.lomba.satu.data.AppRole
import com.lomba.satu.data.StudentItem
import com.lomba.satu.ui.components.*
import com.lomba.satu.ui.theme.*

@Composable
fun CustomProgressBar(
    progressFraction: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(10.dp)
            .background(color.copy(alpha = 0.2f), RoundedCornerShape(5.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progressFraction.coerceIn(0.02f, 1.0f))
                .background(color, RoundedCornerShape(5.dp))
        )
    }
}

@Composable
fun ReportStudentScreen(
    student: StudentItem,
    role: AppRole,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var studentReportData by remember { mutableStateOf(student) }
    var activeToast by remember { mutableStateOf<ToastBannerData?>(null) }

    val themeColor = if (role == AppRole.GURU) PrimaryYellow else PrimaryOrange

    LaunchedEffect(student.id) {
        if (role == AppRole.GURU && ApiService.teacherToken != null) {
            try {
                val rep = ApiService.fetchStudentReport(student.id)
                studentReportData = rep
            } catch (_: Exception) {}
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        MascotHeader(
            title = "Report ${studentReportData.name}",
            role = role,
            onBackClick = onBack,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Top 3 Stat Cards Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatBadge(
                            value = "${studentReportData.practiceDays7}/7",
                            label = "Hari latihan",
                            modifier = Modifier.weight(1f),
                            role = role
                        )
                        StatBadge(
                            value = "⭐ ${studentReportData.totalPoints}",
                            label = "Poin",
                            modifier = Modifier.weight(1f),
                            role = role
                        )
                        StatBadge(
                            value = "🔥 ${studentReportData.streakDays}",
                            label = "Poin",
                            modifier = Modifier.weight(1f),
                            role = role
                        )
                    }

                    // Perkembangan Bicara Line Chart Card (Sumbu X: Hari, Sumbu Y: Akurasi %)
                    CardContainer(role = role) {
                        Column {
                            Text(
                                text = "Grafik Perkembangan Bicara (%)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                            ) {
                                // Sumbu Y (100%, 75%, 50%, 25%, 0%)
                                Column(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .padding(end = 8.dp),
                                    verticalArrangement = Arrangement.SpaceBetween,
                                    horizontalAlignment = Alignment.End
                                ) {
                                    listOf("100%", "75%", "50%", "25%", "0%").forEach { yLabel ->
                                        Text(text = yLabel, fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                // Custom Line Chart Graphic Area
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val w = size.width
                                        val h = size.height

                                        // Horizontal grid lines
                                        drawLine(Color.LightGray.copy(alpha = 0.4f), Offset(0f, 0f), Offset(w, 0f), strokeWidth = 1.5f)
                                        drawLine(Color.LightGray.copy(alpha = 0.4f), Offset(0f, h * 0.25f), Offset(w, h * 0.25f), strokeWidth = 1.5f)
                                        drawLine(Color.LightGray.copy(alpha = 0.4f), Offset(0f, h * 0.50f), Offset(w, h * 0.50f), strokeWidth = 1.5f)
                                        drawLine(Color.LightGray.copy(alpha = 0.4f), Offset(0f, h * 0.75f), Offset(w, h * 0.75f), strokeWidth = 1.5f)
                                        drawLine(Color.LightGray.copy(alpha = 0.4f), Offset(0f, h), Offset(w, h), strokeWidth = 1.5f)

                                        // 7 Days Accuracy rates: 35%, 50%, 65%, 75%, 85%, 90%, 95%
                                        val rates = listOf(0.35f, 0.50f, 0.65f, 0.75f, 0.85f, 0.90f, 0.95f)
                                        val xPos = listOf(0.04f, 0.20f, 0.36f, 0.52f, 0.68f, 0.84f, 0.96f)

                                        val points = rates.mapIndexed { idx, rate ->
                                            Offset(w * xPos[idx], h * (1f - rate))
                                        }

                                        val linePath = Path().apply {
                                            moveTo(points[0].x, points[0].y)
                                            for (i in 0 until points.size - 1) {
                                                val p1 = points[i]
                                                val p2 = points[i + 1]
                                                cubicTo(
                                                    (p1.x + p2.x) / 2, p1.y,
                                                    (p1.x + p2.x) / 2, p2.y,
                                                    p2.x, p2.y
                                                )
                                            }
                                        }

                                        drawPath(linePath, color = themeColor, style = Stroke(width = 6f))

                                        points.forEach { pt ->
                                            drawCircle(color = SurfaceWhite, radius = 10f, center = pt)
                                            drawCircle(color = themeColor, radius = 6f, center = pt)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Sumbu X (Senin - Minggu)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 36.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min").forEach { day ->
                                    Text(text = day, fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Multi-Level Progress Bars: Level 1 Vokal, Level 2 Konsonan, Level 3 1 Suku Kata, Level 4 2 Suku Kata
                    Text(
                        text = "Tahap Penguasaan Bunyi & Kata per Level",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    // 1. Level 1: Vokal (a, i, u, e, o)
                    CardContainer(role = role) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Level 1: Vokal (a - i - u - e - o)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "100%",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColor
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            CustomProgressBar(progressFraction = 1.0f, color = themeColor)
                        }
                    }

                    // 2. Level 2: Konsonan (f, h, m, w, y)
                    CardContainer(role = role) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Level 2: Konsonan (f - h - m - w - y)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "60%",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColor
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            CustomProgressBar(progressFraction = 0.60f, color = themeColor)
                        }
                    }

                    // 3. Level 3: 1 Suku Kata (fa, ha, ma, wa, ya)
                    CardContainer(role = role) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Level 3: 1 Suku Kata (fa - ha - ma)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "20%",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColor
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            CustomProgressBar(progressFraction = 0.20f, color = themeColor)
                        }
                    }

                    // 4. Level 4: 2 Suku Kata (fafa, haha, mama, wawa, yaya)
                    CardContainer(role = role) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Level 4: 2 Suku Kata (fafa - haha - mama)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "0%",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            CustomProgressBar(progressFraction = 0.05f, color = themeColor.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }

        TopToastBanner(
            data = activeToast,
            role = role,
            onDismiss = { activeToast = null }
        )
    }
}

@Composable
private fun StatBadge(
    value: String,
    label: String,
    role: AppRole,
    modifier: Modifier = Modifier
) {
    CardContainer(role = role, modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
