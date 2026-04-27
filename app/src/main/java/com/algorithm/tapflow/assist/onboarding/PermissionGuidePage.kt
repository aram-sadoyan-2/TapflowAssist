package com.algorithm.tapflow.assist.onboarding

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.algorithm.tapflow.assist.R
import kotlinx.coroutines.launch

private data class PermissionGuidePage(
    val title: String,
    val description: String,
    @DrawableRes val imageRes: Int,
    val accentStart: Color,
    val accentEnd: Color
)

@Composable
fun AccessibilityPermissionGuideScreen(
    onFinish: () -> Unit
) {
    val pages = remember {
        listOf(
            PermissionGuidePage(
                title = "Open Downloaded Apps",
                description = "In Accessibility settings, tap Downloaded apps.",
                imageRes = R.drawable.prm1,
                accentStart = Color(0xFF42A5FF),
                accentEnd = Color(0xFF7C3DFF)
            ),
            PermissionGuidePage(
                title = "Choose TapFlow Assist",
                description = "Find TapFlow Assist in the list and open it.",
                imageRes = R.drawable.prm2,
                accentStart = Color(0xFF8B5CFF),
                accentEnd = Color(0xFFC45CFF)
            ),
            PermissionGuidePage(
                title = "Enable Use TapFlow Assist",
                description = "Turn on Use TapFlow Assist to allow your own tap, long press, and swipe actions.",
                imageRes = R.drawable.prm3,
                accentStart = Color(0xFF506DFF),
                accentEnd = Color(0xFFB65CFF)
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    val currentPage by remember {
        derivedStateOf { pagerState.currentPage }
    }

    val isLastPage by remember {
        derivedStateOf { currentPage == pages.lastIndex }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF17234A),
                        Color(0xFF090B13),
                        Color(0xFF05060B)
                    ),
                    radius = 1200f
                )
            )
            .padding(WindowInsets.statusBars.asPaddingValues())
            .padding(WindowInsets.navigationBars.asPaddingValues())
    ) {
        SoftPermissionGlow(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 80.dp),
            color = pages[currentPage].accentEnd
        )

        TextButton(
            onClick = onFinish,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 6.dp, end = 18.dp)
        ) {
            Text(
                text = "Skip",
                color = Color.White.copy(alpha = 0.62f),
                fontWeight = FontWeight.Medium
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 38.dp, bottom = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Permission Guide",
                color = Color.White,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Follow these 3 steps in Android settings.",
                color = Color.White.copy(alpha = 0.72f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(6.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { pageIndex ->
                PermissionGuidePageContent(
                    page = pages[pageIndex]
                )
            }

            PermissionPagerDots(
                pageCount = pages.size,
                currentPage = currentPage,
                currentAccent = pages[currentPage].accentStart
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    if (isLastPage) {
                        onFinish()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(currentPage + 1)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .shadow(
                        elevation = 18.dp,
                        shape = RoundedCornerShape(18.dp),
                        ambientColor = Color.White.copy(alpha = 0.18f),
                        spotColor = Color.White.copy(alpha = 0.18f)
                    ),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF080B15)
                )
            ) {
                AnimatedContent(
                    targetState = isLastPage,
                    transitionSpec = {
                        fadeIn(tween(180)) togetherWith fadeOut(tween(180))
                    },
                    label = "permission_button_text"
                ) { last ->
                    Text(
                        text = if (last) "Start App" else "Next  →",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionGuidePageContent(
    page: PermissionGuidePage
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.62f)
                    .aspectRatio(0.54f),
                contentAlignment = Alignment.Center
            ) {
                SoftPermissionGlow(
                    modifier = Modifier.align(Alignment.Center),
                    color = page.accentEnd
                )

                Image(
                    painter = painterResource(id = page.imageRes),
                    contentDescription = page.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(30.dp))
                        .border(
                            border = BorderStroke(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.14f)
                            ),
                            shape = RoundedCornerShape(30.dp)
                        ),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Text(
            text = page.title,
            color = Color.White,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.ExtraBold,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 2,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = page.description,
            color = Color.White.copy(alpha = 0.82f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
            maxLines = 3,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))
    }
}

@Composable
private fun PermissionPagerDots(
    pageCount: Int,
    currentPage: Int,
    currentAccent: Color
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val selected = index == currentPage

            val width by animateDpAsState(
                targetValue = if (selected) 18.dp else 8.dp,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "permission_dot_width"
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = 5.dp)
                    .height(8.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(
                        if (selected) currentAccent
                        else Color.White.copy(alpha = 0.28f)
                    )
            )
        }
    }
}

@Composable
private fun SoftPermissionGlow(
    modifier: Modifier = Modifier,
    color: Color
) {
    Box(
        modifier = modifier
            .size(240.dp)
            .blur(70.dp)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = 0.40f),
                        color.copy(alpha = 0.12f),
                        Color.Transparent
                    )
                ),
                shape = CircleShape
            )
    )
}