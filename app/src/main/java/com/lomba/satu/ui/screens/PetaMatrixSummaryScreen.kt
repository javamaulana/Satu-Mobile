package com.lomba.satu.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lomba.satu.data.ApiService
import com.lomba.satu.data.AppRole
import com.lomba.satu.data.SampleData
import com.lomba.satu.data.VowelStatus
import com.lomba.satu.ui.components.*
import com.lomba.satu.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun PetaMatrixSummaryScreen(
    role: AppRole,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var titleName by remember { mutableStateOf(ApiService.currentClassroomName) }
    var studentsList by remember { mutableStateOf(SampleData.classes.first().students) }
    var isLoading by remember { mutableStateOf(true) }
    var activeToast by remember { mutableStateOf<ToastBannerData?>(null) }

    val themeColor = PrimaryYellow

    val level1Sounds = remember { listOf("a", "i", "u", "e", "o") }
    val level2Sounds = remember { listOf("f", "h", "m", "w", "y") }
    val level3Sounds = remember { listOf("fa", "ha", "ma", "wa", "ya") }
    val level4Sounds = remember { listOf("fafa", "haha", "mama", "wawa", "yaya") }

    val hasLevel2Touched = remember(studentsList) {
        studentsList.any { st ->
            level2Sounds.any { s -> (st.vowelsProgress[s] ?: (st.vowelsProgress[s.uppercase()] ?: VowelStatus.NOT_STARTED)) != VowelStatus.NOT_STARTED } ||
            level1Sounds.all { s -> (st.vowelsProgress[s] ?: VowelStatus.NOT_STARTED) == VowelStatus.MASTERED }
        }
    }

    val hasLevel3Touched = remember(studentsList) {
        studentsList.any { st ->
            level3Sounds.any { s -> (st.vowelsProgress[s] ?: VowelStatus.NOT_STARTED) != VowelStatus.NOT_STARTED }
        }
    }

    val hasLevel4Touched = remember(studentsList) {
        studentsList.any { st ->
            level4Sounds.any { s -> (st.vowelsProgress[s] ?: VowelStatus.NOT_STARTED) != VowelStatus.NOT_STARTED }
        }
    }

    val activeLevels = remember(hasLevel2Touched, hasLevel3Touched, hasLevel4Touched) {
        buildList {
            add("Level 1: Vokal" to level1Sounds)
            if (hasLevel2Touched) add("Level 2: Konsonan" to level2Sounds)
            if (hasLevel3Touched) add("Level 3: 1 Suku Kata" to level3Sounds)
            if (hasLevel4Touched) add("Level 4: 2 Suku Kata" to level4Sounds)
        }
    }

    val activeSounds = remember(activeLevels) {
        activeLevels.flatMap { it.second }
    }

    fun reloadMatrix() {
        coroutineScope.launch {
            isLoading = true
            try {
                if (role == AppRole.GURU && ApiService.teacherToken != null) {
                    val (cName, _, sList) = ApiService.fetchClassSoundsMatrix()
                    titleName = cName
                    if (sList.isNotEmpty()) studentsList = sList
                }
            } catch (_: Exception) {
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        reloadMatrix()
    }

    Box(modifier = modifier.fillMaxSize()) {
        MascotHeader(
            title = titleName,
            role = role,
            modifier = Modifier.fillMaxSize()
        ) {
            if (isLoading && studentsList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = themeColor)
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    val horizontalScrollState = rememberScrollState()

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .horizontalScroll(horizontalScrollState)
                    ) {
                        // Level Header Row
                        Row(
                            modifier = Modifier.padding(top = 4.dp, bottom = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(modifier = Modifier.width(110.dp))

                            activeLevels.forEach { (levelTitle, sounds) ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = themeColor.copy(alpha = 0.18f),
                                    border = BorderStroke(1.dp, themeColor),
                                    modifier = Modifier
                                        .width((sounds.size * 52).dp)
                                        .padding(horizontal = 4.dp)
                                ) {
                                    Text(
                                        text = levelTitle,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        // Sound Header Row
                        Row(
                            modifier = Modifier.padding(bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Nama Murid",
                                modifier = Modifier.width(110.dp),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            activeSounds.forEach { sound ->
                                Text(
                                    text = sound,
                                    modifier = Modifier.width(52.dp),
                                    textAlign = TextAlign.Center,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(bottom = 8.dp),
                            color = themeColor.copy(alpha = 0.4f)
                        )

                        // Matrix Rows for each student
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(studentsList) { student ->
                                Row(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = student.name,
                                        modifier = Modifier.width(110.dp),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )

                                    activeSounds.forEach { sound ->
                                        val soundKey = sound.lowercase()
                                        val soundKeyUpper = sound.uppercase()
                                        val currentStatus = student.vowelsProgress[soundKey]
                                            ?: (student.vowelsProgress[soundKeyUpper] ?: VowelStatus.NOT_STARTED)
                                        val isMastered = currentStatus == VowelStatus.MASTERED
                                        val isPracticing = currentStatus == VowelStatus.PRACTICING

                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = when {
                                                isMastered -> themeColor
                                                isPracticing -> themeColor.copy(alpha = 0.35f)
                                                else -> SurfaceWhite
                                            },
                                            border = BorderStroke(1.5.dp, themeColor),
                                            modifier = Modifier
                                                .width(52.dp)
                                                .height(38.dp)
                                                .padding(horizontal = 4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isMastered) {
                                                    Text(
                                                        text = "✓",
                                                        color = SurfaceWhite,
                                                        fontSize = 18.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                } else if (isPracticing) {
                                                    Text(
                                                        text = "?",
                                                        color = TextPrimary,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
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
