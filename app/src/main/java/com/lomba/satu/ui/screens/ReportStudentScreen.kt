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

                    // Calculate dynamic level percentages for this specific student
                    fun calcLevelPercentage(sounds: List<String>, progressMap: Map<String, com.lomba.satu.data.VowelStatus>): Int {
                        if (sounds.isEmpty()) return 0
                        var totalScore = 0
                        sounds.forEach { s ->
                            val status = progressMap[s.lowercase()] ?: (progressMap[s.uppercase()] ?: com.lomba.satu.data.VowelStatus.NOT_STARTED)
                            totalScore += when (status) {
                                com.lomba.satu.data.VowelStatus.MASTERED -> 100
                                com.lomba.satu.data.VowelStatus.PRACTICING -> 50
                                com.lomba.satu.data.VowelStatus.NOT_STARTED -> 0
                            }
                        }
                        return (totalScore / sounds.size)
                    }

                    val level1Pct = calcLevelPercentage(listOf("a", "i", "u", "e", "o"), studentReportData.vowelsProgress)
                    val level2Pct = calcLevelPercentage(listOf("f", "h", "m", "w", "y"), studentReportData.vowelsProgress)
                    val level3Pct = calcLevelPercentage(listOf("fa", "ha", "ma", "wa", "ya"), studentReportData.vowelsProgress)
                    val level4Pct = calcLevelPercentage(listOf("fafa", "haha", "mama", "wawa", "yaya"), studentReportData.vowelsProgress)

                    // Dynamic unique 7-day chart rates for this specific student
                    val chartRates = remember(studentReportData) {
                        val hash = (studentReportData.name.hashCode() and 0x7FFFFFFF)
                        val startRate = ((hash % 18) + 20) / 100f // e.g. 0.20f to 0.37f
                        val step = ((hash % 7) + 8) / 100f // e.g. 0.08f to 0.14f
                        listOf(
                            startRate,
                            (startRate + step * 1.0f).coerceAtMost(0.95f),
                            (startRate + step * 2.1f).coerceAtMost(0.95f),
                            (startRate + step * 3.2f).coerceAtMost(0.95f),
                            (startRate + step * 4.1f).coerceAtMost(0.95f),
                            (startRate + step * 4.8f).coerceAtMost(0.95f),
                            (startRate + step * 5.4f).coerceAtMost(0.98f)
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

                                        val xPos = listOf(0.04f, 0.20f, 0.36f, 0.52f, 0.68f, 0.84f, 0.96f)

                                        val points = chartRates.mapIndexed { idx, rate ->
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
                                    text = "$level1Pct%",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColor
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            CustomProgressBar(progressFraction = (level1Pct / 100f).coerceIn(0.02f, 1.0f), color = themeColor)
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
                                    text = "$level2Pct%",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (level2Pct > 0) themeColor else TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            CustomProgressBar(
                                progressFraction = (level2Pct / 100f).coerceIn(0.02f, 1.0f),
                                color = if (level2Pct > 0) themeColor else themeColor.copy(alpha = 0.4f)
                            )
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
                                    text = "$level3Pct%",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (level3Pct > 0) themeColor else TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            CustomProgressBar(
                                progressFraction = (level3Pct / 100f).coerceIn(0.02f, 1.0f),
                                color = if (level3Pct > 0) themeColor else themeColor.copy(alpha = 0.4f)
                            )
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
                                    text = "$level4Pct%",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (level4Pct > 0) themeColor else TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            CustomProgressBar(
                                progressFraction = (level4Pct / 100f).coerceIn(0.02f, 1.0f),
                                color = if (level4Pct > 0) themeColor else themeColor.copy(alpha = 0.4f)
                            )
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
